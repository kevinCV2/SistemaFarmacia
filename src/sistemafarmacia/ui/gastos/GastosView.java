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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Vista para el registro de gastos de la farmacia.
 * Sincronizado con la tabla 'gastos' (id_gasto, dia, nombre, monto, descripcion)
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
        // Color verde esmeralda para gastos
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
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Gasto registrado correctamente.");
                if (actionVolver != null) actionVolver.run();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el gasto en la base de datos.");
            }
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Por favor revisa que el nombre no esté vacío y el monto sea un número válido mayor a 0.");
        }
    }

    private boolean guardarEnBD() {
        // SQL ajustado a la imagen: columna 'dia' en lugar de 'fecha'
        String sql = "INSERT INTO gastos (dia, nombre, monto, descripcion) VALUES (CURRENT_DATE, ?, ?, ?)";
        
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, txtNombre.getText().trim());
            
            // Usamos BigDecimal para la precisión de la moneda (numeric 10,2)
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim());
            ps.setBigDecimal(2, monto);
            
            ps.setString(3, txtDescripcion.getText().trim());
            
            int filas = ps.executeUpdate();
            return filas > 0;
            
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error al registrar gasto: " + e.getMessage());
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