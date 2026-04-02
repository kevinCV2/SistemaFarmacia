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
import java.sql.ResultSet;
import java.sql.Statement;

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

        /* ================= TOP BAR ================= */

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

        /* ================= TABLA ================= */

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
            -fx-base: #1f2933;
            -fx-control-inner-background: #111827;
            -fx-background-color: #111827;
        """);

        table.getColumns().add(createColumn("No.", 0, Pos.CENTER));
        table.getColumns().add(createColumn("Presentación", 1, Pos.CENTER_LEFT));
        table.getColumns().add(createColumn("Descripción", 2, Pos.CENTER_LEFT));

        TableColumn<ObservableList<Object>, Object> existenciaCol =
                createColumn("Existencia", 3, Pos.CENTER);

        existenciaCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int stock = Integer.parseInt(item.toString());
                    setText(item.toString());
                    setStyle(stock <= 10
                            ? "-fx-text-fill: #f87171; -fx-font-weight: bold;"
                            : "-fx-text-fill: #4ade80;");
                }
            }
        });

        table.getColumns().add(existenciaCol);
        table.getColumns().add(createColumn("Entrada", 4, Pos.CENTER));
        table.getColumns().add(createColumn("Salidas", 5, Pos.CENTER));
        table.getColumns().add(createColumn("Inventario Final", 6, Pos.CENTER));

        /* ================= ACCIONES ================= */

        TableColumn<ObservableList<Object>, Void> accionesCol = new TableColumn<>("Acciones");

        accionesCol.setCellFactory(col -> new TableCell<>() {

            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox box = new HBox(8, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER);

                btnEdit.setStyle("""
                    -fx-background-color: #2563eb;
                    -fx-text-fill: white;
                    -fx-cursor: hand;
                """);

                btnDelete.setStyle("""
                    -fx-background-color: #dc2626;
                    -fx-text-fill: white;
                    -fx-cursor: hand;
                """);

                btnEdit.setOnAction(e -> {
                    ObservableList<Object> row =
                            getTableView().getItems().get(getIndex());
                    abrirVentanaEditar(row);
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

    /* ================= VENTANA EDITAR ================= */

    private void abrirVentanaEditar(ObservableList<Object> row) {

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Editar Medicamento");

        VBox rootModal = new VBox(15);
        rootModal.setPadding(new Insets(20));
        rootModal.setStyle("-fx-background-color: #111827;");

        Label title = new Label("Modificar Medicamento");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(18));

        TextField txtNombre = crearInput(row.get(1).toString());
        TextField txtDescripcion = crearInput(row.get(2).toString());

        int stockActual = Integer.parseInt(row.get(3).toString());

        TextField txtStockActual = crearInput(String.valueOf(stockActual));
        txtStockActual.setDisable(true);

        TextField txtEntrada = crearInput("0");

        Button btnGuardar = new Button("Guardar Cambios");
        btnGuardar.setStyle("""
            -fx-background-color: #2563eb;
            -fx-text-fill: white;
            -fx-font-weight: bold;
        """);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("""
            -fx-background-color: #374151;
            -fx-text-fill: white;
        """);

        btnCancelar.setOnAction(e -> modal.close());

        btnGuardar.setOnAction(e -> {
            try {
                int entrada = Integer.parseInt(txtEntrada.getText());
                if (entrada < 0) throw new NumberFormatException();

                int nuevoStock = stockActual + entrada;

                Connection conn = ConexionDB.getInstance();
                Statement stmt = conn.createStatement();

                stmt.executeUpdate("""
                    UPDATE medicamentos
                    SET nombre = '%s',
                        stock = %d
                    WHERE nombre = '%s'
                """.formatted(
                        txtNombre.getText(),
                        nuevoStock,
                        row.get(1).toString()
                ));

                row.set(1, txtNombre.getText());
                row.set(2, txtDescripcion.getText());
                row.set(3, nuevoStock);
                row.set(4, entrada);
                row.set(6, nuevoStock);

                modal.close();

            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR,
                        "La entrada debe ser un número válido").show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        rootModal.getChildren().addAll(
                title,
                crearLabel("Nombre"), txtNombre,
                crearLabel("Descripción"), txtDescripcion,
                crearLabel("Stock actual"), txtStockActual,
                crearLabel("Entrada (+)"), txtEntrada,
                botones
        );

        modal.setScene(new Scene(rootModal, 400, 420));
        modal.showAndWait();
    }

    /* ================= UTILIDADES ================= */

    private TableColumn<ObservableList<Object>, Object> createColumn(
            String title, int index, Pos alignment) {

        TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().get(index))
        );
        col.setStyle("-fx-alignment: " +
                alignment.name().replace("_", "-") + ";");
        return col;
    }

    private TextField crearInput(String value) {
        TextField tf = new TextField(value);
        tf.setStyle("""
            -fx-background-color: #0f172a;
            -fx-text-fill: white;
            -fx-border-color: #374151;
            -fx-background-radius: 6;
        """);
        return tf;
    }

    private Label crearLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#9ca3af"));
        return l;
    }

    private ObservableList<ObservableList<Object>> getDataFromDB() {

        ObservableList<ObservableList<Object>> data =
                FXCollections.observableArrayList();

        try {
            Connection conn = ConexionDB.getInstance();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("""
                SELECT nombre, nombre AS descripcion, stock
                FROM medicamentos
                ORDER BY nombre
            """);

            int i = 1;
            while (rs.next()) {
                int stock = rs.getInt("stock");
                data.add(FXCollections.observableArrayList(
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