package sistemafarmacia.ui.catalogo;

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

        btnVolver.setOnMouseEntered(e ->
                btnVolver.setStyle(
                        "-fx-background-color: #374151;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #374151;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-width: 1;"
                )
        );

        btnVolver.setOnMouseExited(e ->
                btnVolver.setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: #9ca3af;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #374151;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-width: 1;"
                )
        );

        btnVolver.setOnAction(e -> {
            if (actionVolver != null) actionVolver.run();
        });

        VBox headerText = new VBox(5);
        Label title = new Label("Catálogo de Medicamentos");
        title.setFont(Font.font("System Bold", 26));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Listado y control de productos");
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

        // -------- COLUMNAS (MISMO FORMATO, SIN DTO) --------

        table.getColumns().add(createColumn("Nombre", 0, 2500, Pos.CENTER_LEFT));
        table.getColumns().add(createColumn("Descripción", 1, 6000, Pos.CENTER_LEFT));
        table.getColumns().add(createColumn("Categoría", 2, 2000, Pos.CENTER));
        table.getColumns().add(createColumn("Precio", 3, 1500, Pos.CENTER));

        // Stock con color dinámico
        TableColumn<ObservableList<Object>, Object> stockCol =
                createColumn("Stock", 4, 1200, Pos.CENTER);

        stockCol.setCellFactory(col -> new TableCell<>() {
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

        table.getColumns().add(stockCol);
        table.getColumns().add(createColumn("Vencimiento", 5, 1800, Pos.CENTER));

        // Acciones (sin cambios)
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

        // --- DATOS DE PRUEBA (SIMULANDO BD) ---
        table.setItems(getMockData());
    }

    // 🔧 Método genérico para columnas (BD-friendly)
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

    // 🔌 Simulación de ResultSet
    private ObservableList<ObservableList<Object>> getMockData() {
        return FXCollections.observableArrayList(
                FXCollections.observableArrayList(
                        "Paracetamol 500mg",
                        "Analgésico y antipirético",
                        "Analgésicos",
                        "$45.50",
                        150,
                        "2026-12-31"
                ),
                FXCollections.observableArrayList(
                        "Ibuprofeno 400mg",
                        "Antiinflamatorio no esteroideo",
                        "Antiinflamatorios",
                        "$65.00",
                        8,
                        "2026-08-15"
                ),
                FXCollections.observableArrayList(
                        "Amoxicilina 500mg",
                        "Antibiótico de amplio espectro",
                        "Antibióticos",
                        "$120.00",
                        45,
                        "2025-11-20"
                )
        );
    }

    public BorderPane getRoot() {
        return root;
    }
}
