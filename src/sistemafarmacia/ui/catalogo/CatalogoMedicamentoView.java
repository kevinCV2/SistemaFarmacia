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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CatalogoMedicamentoView {
    
    private BorderPane root;
    private TableView<ObservableList<Object>> table;
    private Runnable actionVolver;

    public CatalogoMedicamentoView(Runnable actionVolver) {
        this.actionVolver = actionVolver;

        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #9ca3af;
            -fx-border-color: #374151;
            -fx-border-radius: 5;
            -fx-cursor: hand;
        """);

        btnVolver.setOnAction(e -> {
            if (actionVolver != null) actionVolver.run();
        });

        VBox headerText = new VBox(5);
        Label title = new Label("Catálogo de Medicamentos");
        title.setFont(Font.font(26));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Listado y control de inventario");
        subtitle.setTextFill(Color.web("#9ca3af"));

        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
            -fx-base: #1f2933;
            -fx-control-inner-background: #111827;
            -fx-background-color: #111827;
        """);

        TableColumn<ObservableList<Object>, Object> idCol =
                createColumn("ID", 0, Pos.CENTER);
        idCol.setVisible(false);

        table.getColumns().add(idCol);
        table.getColumns().add(createColumn("No.", 1, Pos.CENTER));
        table.getColumns().add(createColumn("Presentación", 2, Pos.CENTER_LEFT));
        table.getColumns().add(createColumn("Descripción", 3, Pos.CENTER_LEFT));
        table.getColumns().add(createColumn("Existencia", 4, Pos.CENTER));
        table.getColumns().add(createColumn("Entrada", 5, Pos.CENTER));
        table.getColumns().add(createColumn("Salidas", 6, Pos.CENTER));
        table.getColumns().add(createColumn("Inventario Final", 7, Pos.CENTER));

        // ACCIONES
        TableColumn<ObservableList<Object>, Void> accionesCol =
                new TableColumn<>("Acciones");

        accionesCol.setCellFactory(col -> new TableCell<>() {

            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(8, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER);

                btnEdit.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white;");
                btnDelete.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white;");

                btnEdit.setOnAction(e -> {
                    ObservableList<Object> row =
                            getTableView().getItems().get(getIndex());
                    abrirVentanaEditar(row);
                });

                btnDelete.setOnAction(e -> {
                    ObservableList<Object> row =
                            getTableView().getItems().get(getIndex());

                    int idMedicamento = Integer.parseInt(row.get(0).toString());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmar eliminación");
                    confirm.setHeaderText("¿Eliminar medicamento?");
                    confirm.setContentText("Esta acción no se puede deshacer.");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            eliminarMedicamento(idMedicamento);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().add(accionesCol);

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(topBar, table);
        root.setCenter(content);

        table.setItems(getDataFromDB());
    }

    // ===== METODO EDITAR =====
    private void abrirVentanaEditar(ObservableList<Object> row) {

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Editar Medicamento");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color:#111827;");

        TextField txtNombre = new TextField(row.get(2).toString());
        TextField txtDescripcion = new TextField(row.get(3).toString());
        TextField txtStock = new TextField(row.get(4).toString());

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white;");

        btnGuardar.setOnAction(e -> {
            try {
                int id = Integer.parseInt(row.get(0).toString());

                Connection conn = ConexionDB.getInstance();
                String sql = """
                    UPDATE medicamentos
                    SET nombre=?, descripcion=?, stock=?
                    WHERE id_medicamento=?
                """;

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtDescripcion.getText());
                ps.setInt(3, Integer.parseInt(txtStock.getText()));
                ps.setInt(4, id);

                ps.executeUpdate();

                modal.close();
                table.setItems(getDataFromDB());
                table.refresh();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        layout.getChildren().addAll(
                new Label("Nombre"), txtNombre,
                new Label("Descripción"), txtDescripcion,
                new Label("Stock"), txtStock,
                btnGuardar
        );

        modal.setScene(new Scene(layout, 350, 350));
        modal.showAndWait();
    }

    private void eliminarMedicamento(int idMedicamento) {
        try {
            Connection conn = ConexionDB.getInstance();
            PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM medicamentos WHERE id_medicamento=?");
            ps.setInt(1, idMedicamento);
            ps.executeUpdate();

            table.setItems(getDataFromDB());
            table.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TableColumn<ObservableList<Object>, Object> createColumn(
            String title, int index, Pos alignment) {

        TableColumn<ObservableList<Object>, Object> col =
                new TableColumn<>(title);

        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().get(index))
        );

        col.setStyle("-fx-alignment: " +
                alignment.name().replace("_", "-") + ";");

        return col;
    }

    private ObservableList<ObservableList<Object>> getDataFromDB() {

        ObservableList<ObservableList<Object>> data =
                FXCollections.observableArrayList();

        try {
            Connection conn = ConexionDB.getInstance();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("""
                SELECT id_medicamento, nombre, descripcion, stock
                FROM medicamentos
                ORDER BY nombre
            """);

            int i = 1;

            while (rs.next()) {
                int stock = rs.getInt("stock");

                data.add(FXCollections.observableArrayList(
                        rs.getInt("id_medicamento"),
                        i++,
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        stock,
                        0,
                        0,
                        stock
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public BorderPane getRoot() {
        return root;
    }
}