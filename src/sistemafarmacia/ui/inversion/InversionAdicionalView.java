package sistemafarmacia.ui.inversion;

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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Vista para el registro de inversiones adicionales en la farmacia.
 * Corregido según esquema de BD: inversiones_adicionales
 */
public class InversionAdicionalView {

    private BorderPane root;
    private Runnable actionVolver;

    private TextField txtNombre, txtMonto;
    private TextArea txtDescripcion;

    public InversionAdicionalView(Runnable actionVolver) {
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

        Label titulo = new Label("Registrar Inversión Adicional");
        titulo.setFont(Font.font("System Bold", 32));
        titulo.setTextFill(Color.WHITE);

        // --- Configuración de Controles ---
        txtNombre = crearTextField("Ej. Compra de Estantería, Nuevo Equipo, etc.", 400, 35);
        txtMonto = crearTextField("0.00", 400, 35);
        
        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Detalles de la inversión realizada...");
        txtDescripcion.setPrefHeight(100);
        txtDescripcion.setMaxWidth(400);
        txtDescripcion.setWrapText(true);
        txtDescripcion.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 5;");

        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        int row = 0;
        agregarAlGrid(grid, "Concepto de Inversión:", txtNombre, row++);
        agregarAlGrid(grid, "Monto Invertido ($):", txtMonto, row++);
        agregarAlGrid(grid, "Descripción de la Inversión:", txtDescripcion, row++);

        // --- Botones ---
        Button btnGuardar = new Button("Confirmar Inversión");
        btnGuardar.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-padding: 12 25; -fx-cursor: hand;");
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
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Inversión registrada correctamente.");
                if (actionVolver != null) actionVolver.run();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo conectar con la base de datos.");
            }
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Asegúrate de que el nombre no esté vacío y el monto sea mayor a 0.");
        }
    }

    private boolean guardarEnBD() {
        // SQL corregido: tabla 'inversiones_adicionales', columna de fecha es 'dia'
        String sql = "INSERT INTO inversiones_adicionales (dia, nombre, monto, descripcion) VALUES (CURRENT_DATE, ?, ?, ?)";
        
        // El bloque try-with-resources cierra la conexión automáticamente
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, txtNombre.getText().trim());
            
            // Usamos BigDecimal para coincidir con el tipo numeric(10,2) de PostgreSQL/MySQL
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim());
            ps.setBigDecimal(2, monto);
            
            ps.setString(3, txtDescripcion.getText().trim());
            
            int resultado = ps.executeUpdate();
            return resultado > 0;

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error en BD: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validarCampos() {
        try {
            String nombre = txtNombre.getText().trim();
            String montoStr = txtMonto.getText().trim();
            
            if (nombre.isEmpty()) return false;
            
            BigDecimal monto = new BigDecimal(montoStr);
            return monto.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
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