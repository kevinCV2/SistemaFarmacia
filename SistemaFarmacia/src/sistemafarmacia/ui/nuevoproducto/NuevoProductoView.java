package sistemafarmacia.ui.nuevoproducto;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class NuevoProductoView {

    private BorderPane root;
    private Runnable actionVolver;

    public NuevoProductoView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #111827;");
        construirVista();
    }

    private void construirVista() {

        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setMaxWidth(900);

        // ----- TÍTULO -----
        Label title = new Label("Nuevo Producto");
        title.setFont(Font.font("System Bold", 26));
        title.setTextFill(Color.WHITE);

        // ----- FORMULARIO -----
        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(18);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        form.getColumnConstraints().addAll(col1, col2);

        // ----- CAMPOS -----
        TextField txtNombre = input("Nombre del medicamento");
        TextArea txtDescripcion = textArea("Descripción del medicamento");

        ComboBox<String> cbCategoria = combo();
        ComboBox<String> cbProveedor = combo();

        TextField txtPrecio = input("0.00");
        TextField txtStock = input("0");

        DatePicker dpVencimiento = datePicker();

        // ----- UBICACIÓN -----
        form.add(label("Nombre del Medicamento"), 0, 0, 2, 1);
        form.add(txtNombre, 0, 1, 2, 1);

        form.add(label("Descripción"), 0, 2, 2, 1);
        form.add(txtDescripcion, 0, 3, 2, 1);

        form.add(label("Categoría"), 0, 4);
        form.add(label("Proveedor"), 1, 4);
        form.add(cbCategoria, 0, 5);
        form.add(cbProveedor, 1, 5);

        form.add(label("Precio"), 0, 6);
        form.add(label("Cantidad en Stock"), 1, 6);
        form.add(txtPrecio, 0, 7);
        form.add(txtStock, 1, 7);

        form.add(label("Fecha de Vencimiento"), 0, 8);
        form.add(dpVencimiento, 0, 9);

        // ----- BOTONES -----
        Button btnGuardar = new Button("💾 Guardar Producto");
        Button btnCancelar = new Button("Cancelar");

        btnGuardar.setStyle("""
                -fx-background-color: #4b5563;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 10 20;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                """);

        btnCancelar.setStyle("""
                -fx-background-color: #ef4444;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 10 20;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                """);

        btnCancelar.setOnAction(e -> {
            if (actionVolver != null) actionVolver.run();
        });

        HBox actions = new HBox(15, btnGuardar, btnCancelar);
        actions.setAlignment(Pos.CENTER_RIGHT);

        container.getChildren().addAll(title, form, actions);
        root.setCenter(container);
    }

    // ====== COMPONENTES ======

    private Label label(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#9ca3af"));
        l.setFont(Font.font(13));
        return l;
    }

    private TextField input(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("""
                -fx-background-color: #1f2933;
                -fx-text-fill: white;
                -fx-prompt-text-fill: #6b7280;
                -fx-background-radius: 8;
                -fx-border-color: #374151;
                -fx-border-radius: 8;
                -fx-padding: 10;
                """);
        return tf;
    }

    private TextArea textArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefRowCount(4);
        ta.setWrapText(true);
        ta.setStyle("""
                -fx-background-color: #1f2933;
                -fx-text-fill: white;
                -fx-prompt-text-fill: #6b7280;
                -fx-background-radius: 8;
                -fx-border-color: #374151;
                -fx-border-radius: 8;
                """);
        return ta;
    }

    private ComboBox<String> combo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText("Seleccionar...");
        cb.setStyle("""
                -fx-background-color: #1f2933;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-border-color: #374151;
                -fx-border-radius: 8;
                """);
        return cb;
    }

    private DatePicker datePicker() {
        DatePicker dp = new DatePicker();
        dp.setStyle("""
                -fx-background-color: #1f2933;
                -fx-text-fill: white;
                -fx-background-radius: 8;
                -fx-border-color: #374151;
                -fx-border-radius: 8;
                """);
        return dp;
    }

    public BorderPane getRoot() {
        return root;
    }
}