package sistemafarmacia.ui.gastos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import sistemafarmacia.utils.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Vista para el registro de gastos de la farmacia.
 * @author cvkca
 */
public class GastosView {

    private BorderPane root;
    private Runnable actionVolver;

    private TextField txtNombre, txtMonto;
    private TextArea txtDescripcion;

    public GastosView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        initUI();
    }

    private void initUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0f172a; -fx-background-color: transparent; -fx-border-color: transparent;");
        
        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Registrar Gasto");
        titulo.setFont(Font.font("System Bold", 32));
        titulo.setTextFill(Color.WHITE);

        // --- Configuración de Controles ---
        txtNombre = crearTextField("¿En qué se gastó? (Ej. Pago de Alquiler)", 400, 35);
        txtMonto = crearTextField("0.00", 400, 35);
        
        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Detalles o descripción del pago...");
        txtDescripcion.setPrefHeight(100);
        txtDescripcion.setMaxWidth(400);
        txtDescripcion.setWrapText(true);
        txtDescripcion.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 5;");

        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        int row = 0;
        agregarAlGrid(grid, "Nombre del Gasto:", txtNombre, row++);
        agregarAlGrid(grid, "Monto en Dinero ($):", txtMonto, row++);
        agregarAlGrid(grid, "Descripción de Pago:", txtDescripcion, row++);

        // --- Botones ---
        Button btnGuardar = new Button("Confirmar Gasto");
        btnGuardar.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-padding: 12 25; -fx-cursor: hand;");
        btnGuardar.setOnAction(e -> ejecutarGuardado());

        Button btnCancelar = new Button("Volver");
        btnCancelar.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 16; -fx-padding: 12 25; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        HBox botones = new HBox(20, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER);
        
        content.getChildren().addAll(titulo, grid, botones);
        scrollPane.setContent(content);
        root.setCenter(scrollPane);
    }

    private void ejecutarGuardado() {
        if (validarCampos()) {
            if (guardarEnBD()) {
                new Alert(Alert.AlertType.INFORMATION, "Gasto registrado correctamente").showAndWait();
                if (actionVolver != null) actionVolver.run();
            } else {
                new Alert(Alert.AlertType.ERROR, "Error al conectar con la base de datos").showAndWait();
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Por favor revisa que el nombre no esté vacío y el monto sea un número válido").showAndWait();
        }
    }

    private boolean guardarEnBD() {
        // SQL asumiendo que tu tabla tiene estos campos
        String sql = "INSERT INTO gastos (nombre, monto, descripcion, fecha) VALUES (?, ?, ?, CURRENT_DATE)";
        
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, txtNombre.getText().trim());
            ps.setDouble(2, Double.parseDouble(txtMonto.getText().trim()));
            ps.setString(3, txtDescripcion.getText().trim());
            
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validarCampos() {
        try {
            if (txtNombre.getText().trim().isEmpty()) return false;
            Double.parseDouble(txtMonto.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void agregarAlGrid(GridPane grid, String etiqueta, javafx.scene.Node control, int row) {
        grid.add(crearLabel(etiqueta), 0, row * 2);
        grid.add(control, 0, (row * 2) + 1);
    }

    private TextField crearTextField(String prompt, double maxWidth, double height) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(maxWidth);
        tf.setPrefHeight(height);
        tf.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white; -fx-prompt-text-fill: #94a3b8; -fx-border-color: #334155; -fx-border-radius: 5;");
        return tf;
    }

    private Label crearLabel(String texto) {
        Label l = new Label(texto);
        l.setTextFill(Color.web("#94a3b8"));
        l.setFont(Font.font("System", 14));
        return l;
    }

    public BorderPane getRoot() { return root; }
}