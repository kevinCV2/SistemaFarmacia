package sistemafarmacia.ui.filtros;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sistemafarmacia.utils.ConexionDB;

import java.sql.*;

public class FiltrosView {

    private BorderPane root;
    private TableView<ObservableList<Object>> table;
    private Runnable actionVolver;

    public FiltrosView(Runnable actionVolver) {
        this.actionVolver = actionVolver;

        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-border-color: #374151; -fx-border-radius: 5; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        VBox headerText = new VBox(5);
        Label title = new Label("Módulo de Filtros");
        title.setFont(Font.font(26));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Control semanal exclusivo de categoría: FILTROS");
        subtitle.setTextFill(Color.web("#9ca3af"));
        headerText.getChildren().addAll(title, subtitle);

        topBar.getChildren().addAll(btnVolver, headerText);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827; -fx-background-color: #111827;");

        // Columnas alineadas con Almacén Digital
        table.getColumns().add(createColumn("ID", 0, Pos.CENTER, false));
        table.getColumns().add(createColumn("No.", 1, Pos.CENTER, true));
        table.getColumns().add(createColumn("Nombre", 2, Pos.CENTER_LEFT, true));
        table.getColumns().add(createColumn("Presentación", 3, Pos.CENTER_LEFT, true));
        table.getColumns().add(createColumn("Inicial", 4, Pos.CENTER, true));
        table.getColumns().add(createColumn("Entrada", 5, Pos.CENTER, true));
        table.getColumns().add(createColumn("Salidas", 6, Pos.CENTER, true));
        table.getColumns().add(createColumn("Stock Final", 7, Pos.CENTER, true));

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
                    confirmarEliminacion(Integer.parseInt(fila.get(0).toString()), fila.get(2).toString());
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        table.getColumns().add(accionesCol);

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(topBar, table);
        root.setCenter(content);

        actualizarTabla();
    }

    private void actualizarTabla() {
        table.setItems(getFiltrosFromDB());
        table.refresh();
    }

    private void abrirVentanaMovimiento(ObservableList<Object> row) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Movimiento: " + row.get(2));

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color:#1f2933;");

        Label lbl = new Label("Registrar Movimiento");
        lbl.setTextFill(Color.WHITE);

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("ENTRADA", "SALIDA"));
        cbTipo.setValue("ENTRADA");

        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad");

        Button btnGuardar = new Button("Confirmar");
        btnGuardar.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white;");
        btnGuardar.setOnAction(e -> {
            try {
                registrarMovimientoInDB(
                        Integer.parseInt(row.get(0).toString()),
                        cbTipo.getValue(),
                        Integer.parseInt(txtCantidad.getText())
                );
                modal.close();
                actualizarTabla();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Dato inválido").show();
            }
        });

        layout.getChildren().addAll(lbl, cbTipo, txtCantidad, btnGuardar);
        modal.setScene(new Scene(layout, 250, 220));
        modal.showAndWait();
    }

    private void registrarMovimientoInDB(int idInsumo, String tipo, int cantidad) {
        String sql = "INSERT INTO movimientos_inventario (id_medicamento, tipo, cantidad, fecha) VALUES (?, ?, ?, NOW())";
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idInsumo);
            ps.setString(2, tipo);
            ps.setInt(3, cantidad);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private ObservableList<ObservableList<Object>> getFiltrosFromDB() {
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

        // SQL CORREGIDO: Filtra por id_categoria = 6 (FILTROS) en la tabla medicamentos
        String sql = """
            SELECT 
                m.id_medicamento,
                m.nombre,
                m.presentacion,
                m.existencia,
                COALESCE(SUM(CASE WHEN mov.tipo='ENTRADA' THEN mov.cantidad ELSE 0 END), 0) AS entradas,
                COALESCE(SUM(CASE WHEN mov.tipo='SALIDA' THEN mov.cantidad ELSE 0 END), 0) AS salidas
            FROM medicamentos m
            JOIN categorias c ON m.id_categoria = c.id_categoria
            LEFT JOIN movimientos_inventario mov 
                ON m.id_medicamento = mov.id_medicamento
                AND mov.fecha >= date_trunc('week', CURRENT_DATE)
            WHERE c.nombre = 'FILTROS'
            GROUP BY m.id_medicamento, m.nombre, m.presentacion, m.existencia
            ORDER BY m.nombre
        """;

        try (Connection conn = ConexionDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int i = 1;
            while (rs.next()) {
                int inicial = rs.getInt("existencia");
                int ent = rs.getInt("entradas");
                int sal = rs.getInt("salidas");
                int stockFinal = inicial + ent - sal;

                data.add(FXCollections.observableArrayList(
                        rs.getInt("id_medicamento"),
                        i++,
                        rs.getString("nombre"),
                        rs.getString("presentacion"),
                        inicial,
                        ent,
                        sal,
                        stockFinal
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    private void confirmarEliminacion(int id, String nombre) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el filtro " + nombre + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = ConexionDB.getInstance();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM medicamentos WHERE id_medicamento=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    actualizarTabla();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "No se puede eliminar: tiene movimientos asociados.").show();
                }
            }
        });
    }

    private TableColumn<ObservableList<Object>, Object> createColumn(String title, int index, Pos alignment, boolean visible) {
        TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().get(index)));
        col.setStyle("-fx-alignment: " + alignment.name().replace("_", "-") + "; -fx-text-fill: white;");
        col.setVisible(visible);
        return col;
    }

    public BorderPane getRoot() { return root; }
}