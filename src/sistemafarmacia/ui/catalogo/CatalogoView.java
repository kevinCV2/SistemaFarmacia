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
import javafx.stage.Modality;
import javafx.stage.Stage;
import sistemafarmacia.utils.ConexionDB;

import java.sql.*;
import java.time.LocalDate;

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
        title.setFont(Font.font(26));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Control Semanal de Inventario");
        subtitle.setTextFill(Color.web("#9ca3af"));
        headerText.getChildren().addAll(title, subtitle);

        topBar.getChildren().addAll(btnVolver, headerText);

        // --- TABLA ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827; -fx-background-color: #111827;");

        // Columnas
        table.getColumns().add(createColumn("ID", 0, Pos.CENTER, false)); // Invisible
        table.getColumns().add(createColumn("No.", 1, Pos.CENTER, true));
        table.getColumns().add(createColumn("Nombre", 2, Pos.CENTER_LEFT, true));
        table.getColumns().add(createColumn("Descripción", 3, Pos.CENTER_LEFT, true));
        table.getColumns().add(createColumn("Inicial", 4, Pos.CENTER, true));
        table.getColumns().add(createColumn("Entradas", 5, Pos.CENTER, true));
        table.getColumns().add(createColumn("Salidas", 6, Pos.CENTER, true));
        table.getColumns().add(createColumn("Stock Final", 7, Pos.CENTER, true));

        // Columna de Acciones
        TableColumn<ObservableList<Object>, Void> accionesCol = new TableColumn<>("Acciones");
        accionesCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnMov = new Button("📦"); // Botón para registrar entrada/salida
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(8, btnMov, btnDelete);
            {
                box.setAlignment(Pos.CENTER);
                btnMov.setStyle("-fx-background-color:#10b981; -fx-text-fill:white;");
                btnDelete.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white;");

                btnMov.setOnAction(e -> abrirVentanaMovimiento(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> eliminarMedicamento(Integer.parseInt(getTableView().getItems().get(getIndex()).get(0).toString())));
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

        // Cargar Datos
        actualizarTabla();
    }

    private void actualizarTabla() {
        table.setItems(getDataFromDB());
        table.refresh();
    }

    // Ventana para registrar Entradas o Salidas
    private void abrirVentanaMovimiento(ObservableList<Object> row) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Registrar Movimiento - " + row.get(2));

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("ENTRADA", "SALIDA"));
        cbTipo.setValue("ENTRADA");
        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad");

        Button btnGuardar = new Button("Registrar");
        btnGuardar.setOnAction(e -> {
            registrarMovimiento(Integer.parseInt(row.get(0).toString()), cbTipo.getValue(), Integer.parseInt(txtCantidad.getText()));
            modal.close();
            actualizarTabla();
        });

        layout.getChildren().addAll(new Label("Tipo de movimiento:"), cbTipo, new Label("Cantidad:"), txtCantidad, btnGuardar);
        modal.setScene(new Scene(layout, 300, 250));
        modal.showAndWait();
    }

    private void registrarMovimiento(int idMed, String tipo, int cantidad) {
        try (Connection conn = ConexionDB.getInstance()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO movimientos (id_medicamento, tipo, cantidad) VALUES (?, ?, ?)");
            ps.setInt(1, idMed);
            ps.setString(2, tipo);
            ps.setInt(3, cantidad);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private ObservableList<ObservableList<Object>> getDataFromDB() {
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        
        // SQL que calcula todo basándose en la semana actual (Lunes a Sábado)
        String sql = """
            SELECT 
                m.id_medicamento, m.nombre, m.descripcion, m.stock_inicial,
                COALESCE(SUM(CASE WHEN mov.tipo = 'ENTRADA' THEN mov.cantidad ELSE 0 END), 0) AS entradas,
                COALESCE(SUM(CASE WHEN mov.tipo = 'SALIDA' THEN mov.cantidad ELSE 0 END), 0) AS salidas
            FROM medicamentos m
            LEFT JOIN movimientos mov ON m.id_medicamento = mov.id_medicamento 
                 AND mov.fecha >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)
            GROUP BY m.id_medicamento
            ORDER BY m.nombre
        """;

        try (Connection conn = ConexionDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int i = 1;
            while (rs.next()) {
                int inicial = rs.getInt("stock_inicial");
                int ent = rs.getInt("entradas");
                int sal = rs.getInt("salidas");
                int stockFinal = inicial + ent - sal;

                data.add(FXCollections.observableArrayList(
                        rs.getInt("id_medicamento"), i++, rs.getString("nombre"),
                        rs.getString("descripcion"), inicial, ent, sal, stockFinal
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    private void eliminarMedicamento(int id) {
        try (Connection conn = ConexionDB.getInstance()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM medicamentos WHERE id_medicamento=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            actualizarTabla();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<Object>, Object> createColumn(String title, int index, Pos alignment, boolean visible) {
        TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().get(index)));
        col.setStyle("-fx-alignment: " + alignment.name().replace("_", "-") + ";");
        col.setVisible(visible);
        return col;
    }

    public BorderPane getRoot() { return root; }
}