package sistemafarmacia.ui.filtros;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import sistemafarmacia.utils.ConexionDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

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

        // --- TOP BAR ---
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #9ca3af;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #374151;" +
                "-fx-border-radius: 5;" +
                "-fx-border-width: 1;"
        );

        btnVolver.setOnAction(e -> {
            if (actionVolver != null) actionVolver.run();
        });

        VBox headerText = new VBox(5);
        Label title = new Label("Filtros");
        title.setFont(Font.font("System Bold", 26));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Listado y control de inventario en filtros reusables y no reusables");
        subtitle.setTextFill(Color.web("#9ca3af"));
        subtitle.setFont(Font.font(14));

        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        // --- TABLA ---
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
                -fx-base: #1f2933;
                -fx-control-inner-background: #111827;
                -fx-background-color: #111827;
                -fx-table-cell-border-color: #1f2933;
                -fx-padding: 5;
                """);

        // -------- COLUMNAS (MISMAS QUE CATALOGO) --------
        table.getColumns().add(createColumn("No.", 0, 800, Pos.CENTER));
        table.getColumns().add(createColumn("Presentación", 1, 2000, Pos.CENTER_LEFT));
        table.getColumns().add(createColumn("Descripción", 2, 6000, Pos.CENTER_LEFT));

        TableColumn<ObservableList<Object>, Object> existenciaCol =
                createColumn("Existencia", 3, 1500, Pos.CENTER);

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
                    if (stock <= 10) {
                        setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #4ade80;");
                    }
                }
            }
        });

        table.getColumns().add(existenciaCol);
        table.getColumns().add(createColumn("Entrada", 4, 1500, Pos.CENTER));
        table.getColumns().add(createColumn("Salidas", 5, 1500, Pos.CENTER));
        table.getColumns().add(createColumn("Inventario Final", 6, 1800, Pos.CENTER));

        // -------- ACCIONES (IGUAL QUE SIEMPRE) --------
        TableColumn<ObservableList<Object>, Void> accionesCol = new TableColumn<>("Acciones");
        accionesCol.setMaxWidth(1800);
        accionesCol.setStyle("-fx-alignment: CENTER;");

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

        table.setItems(getMockData());
    }

    private TableColumn<ObservableList<Object>, Object> createColumn(
            String title, int index, double width, Pos alignment) {

        TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(title);
        col.setMaxWidth(width);
        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().get(index))
        );
        col.setStyle("-fx-alignment: " + alignment.name().replace("_", "-") + ";");
        return col;
    }

    private ObservableList<ObservableList<Object>> getMockData() {

        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

        try {
            Connection conn = ConexionDB.getInstance();

            String sql = """
                SELECT 
                    m.nombre,
                    m.nombre AS descripcion,
                    m.stock
                FROM medicamentos m
                ORDER BY m.nombre;
            """;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int contador = 1;

            while (rs.next()) {
                int stock = rs.getInt("stock");

                ObservableList<Object> row = FXCollections.observableArrayList(
                        contador++,                 // No.
                        rs.getString("nombre"),     // Presentación
                        rs.getString("descripcion"),// Descripción
                        stock,                      // Existencia
                        0,                          // Entrada
                        0,                          // Salidas
                        stock                       // Inventario Final
                );

                data.add(row);
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