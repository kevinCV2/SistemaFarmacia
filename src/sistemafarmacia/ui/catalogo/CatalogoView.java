package sistemafarmacia.ui.catalogo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sistemafarmacia.utils.ConexionDB;

import java.sql.*;
import java.util.Optional;

public class CatalogoView {

    private BorderPane root;
    private TableView<ObservableList<Object>> table;
    private Runnable actionVolver;

    public CatalogoView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // --- CABECERA ---
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-border-color: #374151; -fx-border-radius: 5; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        VBox headerText = new VBox(5);
        Label title = new Label("Catálogo de Insumos");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Control Semanal de Inventario");
        subtitle.setTextFill(Color.web("#9ca3af"));
        headerText.getChildren().addAll(title, subtitle);

        topBar.getChildren().addAll(btnVolver, headerText);

        // --- TABLA ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827; -fx-background-color: #111827;");

        // Lógica de colores para las filas (Cabeceras azules)
        table.setRowFactory(tv -> new TableRow<ObservableList<Object>>() {
            @Override
            protected void updateItem(ObservableList<Object> item, boolean empty) {
                super.updateItem(item, empty);
                // El índice 8 es nuestro indicador de "esCabecera"
                if (item != null && item.size() > 8 && (boolean) item.get(8)) {
                    setStyle("-fx-background-color: #1e40af; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        // Definición de Columnas
        table.getColumns().add(createColumn("ID", 0, Pos.CENTER, false)); 
        table.getColumns().add(createColumn("No.", 1, Pos.CENTER, true));
        table.getColumns().add(createColumn("Nombre", 2, Pos.CENTER_LEFT, true));
        table.getColumns().add(createColumn("Categoría", 3, Pos.CENTER_LEFT, true));
        table.getColumns().add(createColumn("Inicial", 4, Pos.CENTER, true));
        table.getColumns().add(createColumn("Entradas", 5, Pos.CENTER, true));
        table.getColumns().add(createColumn("Salidas", 6, Pos.CENTER, true));
        table.getColumns().add(createColumn("Stock Final", 7, Pos.CENTER, true));

        // Columna de Acciones
        TableColumn<ObservableList<Object>, Void> accionesCol = new TableColumn<>("Acciones");
        accionesCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnMov = new Button("📦"); 
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(8, btnMov, btnDelete);
            {
                box.setAlignment(Pos.CENTER);
                btnMov.setStyle("-fx-background-color:#10b981; -fx-text-fill:white; -fx-cursor:hand;");
                btnDelete.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-cursor:hand;");

                btnMov.setOnAction(e -> abrirVentanaMovimiento(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> {
                    ObservableList<Object> fila = getTableView().getItems().get(getIndex());
                    confirmarEliminacion(
                        Integer.parseInt(fila.get(0).toString()), 
                        fila.get(2).toString()
                    );
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // No mostrar botones en las filas que son cabeceras de categoría
                if (empty || (getTableRow().getItem() != null && (boolean)getTableRow().getItem().get(8))) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
        table.getColumns().add(accionesCol);

        actualizarTabla();

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(topBar, table);
        root.setCenter(content);
    }

    private void actualizarTabla() {
        table.setItems(getDataFromDB());
        table.refresh();
    }

    private ObservableList<ObservableList<Object>> getDataFromDB() {
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

        String sql = """
            SELECT 
                m.id_medicamento, m.nombre, m.categoria, m.existencia,
                COALESCE(SUM(CASE WHEN mov.tipo = 'ENTRADA' THEN mov.cantidad ELSE 0 END), 0) AS entradas,
                COALESCE(SUM(CASE WHEN mov.tipo = 'SALIDA' THEN mov.cantidad ELSE 0 END), 0) AS salidas
            FROM medicamentos m
            LEFT JOIN movimientos_inventario mov 
                ON m.id_medicamento = mov.id_medicamento
                AND mov.fecha >= date_trunc('week', CURRENT_DATE)
            GROUP BY m.id_medicamento, m.nombre, m.categoria, m.existencia, m.id_categoria
            ORDER BY m.id_categoria ASC, m.nombre ASC
        """;

        try (Connection conn = ConexionDB.getInstance(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {

            String categoriaActual = "";
            int contadorSeccion = 1;

            while (rs.next()) {
                String catRaw = rs.getString("categoria");
                if (catRaw == null) catRaw = "SIN CATEGORÍA";

                // Insertar fila de DIVISIÓN AZUL si cambia la categoría
                if (!catRaw.equals(categoriaActual)) {
                    categoriaActual = catRaw;
                    ObservableList<Object> cabecera = FXCollections.observableArrayList();
                    cabecera.addAll(0, "", categoriaActual.toUpperCase(), "", "", "", "", "", true); 
                    // El índice 8 es el boolean 'esCabecera'
                    data.add(cabecera);
                    contadorSeccion = 1;
                }

                int existencia = rs.getInt("existencia");
                int entradas = rs.getInt("entradas");
                int salidas = rs.getInt("salidas");
                int inventarioFinal = existencia + entradas - salidas;

                ObservableList<Object> fila = FXCollections.observableArrayList(
                        rs.getInt("id_medicamento"),
                        contadorSeccion++,
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        existencia,
                        entradas,
                        salidas,
                        inventarioFinal,
                        false // esCabecera = false
                );
                data.add(fila);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    // --- MÉTODOS DE APOYO (MOVIMIENTOS Y ELIMINACIÓN) ---

    private void abrirVentanaMovimiento(ObservableList<Object> row) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Registrar Movimiento");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("ENTRADA", "SALIDA"));
        cbTipo.setValue("ENTRADA");
        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad");

        Button btnGuardar = new Button("Registrar");
        btnGuardar.setOnAction(e -> {
            try {
                registrarMovimiento(Integer.parseInt(row.get(0).toString()), cbTipo.getValue(), Integer.parseInt(txtCantidad.getText()));
                modal.close();
                actualizarTabla();
            } catch (Exception ex) { 
                new Alert(Alert.AlertType.ERROR, "Datos inválidos").show(); 
            }
        });

        layout.getChildren().addAll(new Label("Movimiento para: " + row.get(2)), cbTipo, txtCantidad, btnGuardar);
        modal.setScene(new Scene(layout, 350, 250));
        modal.showAndWait();
    }

    private void registrarMovimiento(int idMed, String tipo, int cantidad) {
        String sql = "INSERT INTO movimientos_inventario (id_medicamento, tipo, cantidad, fecha) VALUES (?, ?, ?, NOW())";
        try (Connection conn = ConexionDB.getInstance(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMed); ps.setString(2, tipo); ps.setInt(3, cantidad);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void confirmarEliminacion(int id, String nombre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar " + nombre + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) eliminarMedicamento(id);
        });
    }

    private void eliminarMedicamento(int id) {
        try (Connection conn = ConexionDB.getInstance(); 
             PreparedStatement ps = conn.prepareStatement("DELETE FROM medicamentos WHERE id_medicamento=?")) {
            ps.setInt(1, id); ps.executeUpdate(); actualizarTabla();
        } catch (Exception e) { 
            new Alert(Alert.AlertType.ERROR, "No se puede eliminar: tiene historial de movimientos.").show();
        }
    }

    private TableColumn<ObservableList<Object>, Object> createColumn(String title, int index, Pos alignment, boolean visible) {
        TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> {
            if (index < data.getValue().size()) {
                return new javafx.beans.property.SimpleObjectProperty<>(data.getValue().get(index));
            }
            return null;
        });
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setAlignment(alignment);
                    // Si es cabecera, poner texto en blanco
                    if (getTableRow().getItem() != null && (boolean)getTableRow().getItem().get(8)) {
                        setTextFill(Color.WHITE);
                    } else {
                        setTextFill(Color.web("#e5e7eb"));
                    }
                }
            }
        });
        col.setVisible(visible);
        return col;
    }

    public BorderPane getRoot() { return root; }
}