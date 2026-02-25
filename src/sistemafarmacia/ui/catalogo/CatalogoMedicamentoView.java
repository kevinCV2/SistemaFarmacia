package sistemafarmacia.ui.catalogo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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

        // ─── CABECERA ─────────────────────────────────────────────
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #9ca3af;" +
                "-fx-border-color: #374151;" +
                "-fx-border-radius: 5;" +
                "-fx-cursor: hand;"
        );
        btnVolver.setOnAction(e -> {
            if (actionVolver != null) actionVolver.run();
        });

        VBox headerText = new VBox(5);
        Label title = new Label("Catálogo de Medicamentos");
        title.setFont(Font.font(26));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Listado general de medicamentos y precios");
        subtitle.setTextFill(Color.web("#9ca3af"));

        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        // ─── TABLA ───────────────────────────────────────────────
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
                "-fx-base: #1f2933;" +
                "-fx-control-inner-background: #111827;" +
                "-fx-background-color: #111827;"
        );

        table.getColumns().add(createColumn("No.", 0, Pos.CENTER));
        table.getColumns().add(createColumn("Listado de Medicamentos", 1, Pos.CENTER_LEFT));
        table.getColumns().add(createColumn("Precio", 2, Pos.CENTER));

        // 🔹 Datos de ejemplo (puedes quitarlos después)
        table.setItems(datosDemo());

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(topBar, table);

        root.setCenter(content);
    }

    // ─── COLUMNAS ───────────────────────────────────────────────
    private TableColumn<ObservableList<Object>, Object> createColumn(
            String title, int index, Pos alignment) {

        TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().get(index))
        );
        col.setStyle("-fx-alignment: " + alignment.name().replace("_", "-") + ";");
        return col;
    }

    // ─── DATOS DE PRUEBA ────────────────────────────────────────
    private ObservableList<ObservableList<Object>> datosDemo() {
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

        data.add(FXCollections.observableArrayList(1, "Paracetamol 500mg", "$25.00"));
        data.add(FXCollections.observableArrayList(2, "Ibuprofeno 400mg", "$32.50"));
        data.add(FXCollections.observableArrayList(3, "Amoxicilina 500mg", "$85.00"));

        return data;
    }

    public BorderPane getRoot() {
        return root;
    }
}