package sistemafarmacia.ui.nuevoproducto;

import javafx.geometry.HPos;
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
import java.sql.ResultSet;

public class NuevoProductoView {

    private BorderPane root;
    private Runnable actionVolver;

    private TextField txtNombre, txtPrecio, txtStock;
    private TextArea txtDescripcion;
    private ComboBox<String> comboCategoria;
    private DatePicker dateVencimiento;

    public NuevoProductoView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        initUI();
    }

    private void initUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        VBox content = new VBox(30);
        content.setPadding(new Insets(40));

        // Título
        Label titulo = new Label("Registrar Nuevo Producto");
        titulo.setFont(Font.font("System Bold", 32));
        titulo.setTextFill(Color.WHITE);

        // Inicializar campos con tamaño mayor
        txtNombre = crearTextField("Nombre del Medicamento", 400, 30);

        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción detallada...");
        txtDescripcion.setPrefHeight(150);
        txtDescripcion.setMaxWidth(400);
        txtDescripcion.setStyle(
            "-fx-control-inner-background: #1e293b; " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: #94a3b8; " +
            "-fx-border-color: #334155; " +
            "-fx-border-radius: 5; " +
            "-fx-font-size: 14;"
        );

        comboCategoria = new ComboBox<>();
        comboCategoria.setPromptText("Seleccione Categoría");
        comboCategoria.setMaxWidth(400);
        comboCategoria.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-size: 14;");
        cargarCategorias();

        txtPrecio = crearTextField("Precio (ej: 10.50)", 400, 30);
        txtStock = crearTextField("Cantidad inicial", 400, 30);

        dateVencimiento = new DatePicker();
        dateVencimiento.setMaxWidth(400);
        dateVencimiento.setStyle("-fx-control-inner-background: #1e293b; -fx-font-size: 14;");

        // Formulario en Grid
        GridPane grid = new GridPane();
        grid.setVgap(20);
        grid.setHgap(20);
        grid.setAlignment(Pos.CENTER);

        int row = 0;

        grid.add(crearLabel("Nombre:"), 0, row++);
        grid.add(txtNombre, 0, row++);
        GridPane.setHalignment(txtNombre, HPos.CENTER);

        grid.add(crearLabel("Descripción:"), 0, row++);
        grid.add(txtDescripcion, 0, row++);
        GridPane.setHalignment(txtDescripcion, HPos.CENTER);

        grid.add(crearLabel("Categoría:"), 0, row++);
        grid.add(comboCategoria, 0, row++);
        GridPane.setHalignment(comboCategoria, HPos.CENTER);

        grid.add(crearLabel("Precio:"), 0, row++);
        grid.add(txtPrecio, 0, row++);
        GridPane.setHalignment(txtPrecio, HPos.CENTER);

        grid.add(crearLabel("Stock:"), 0, row++);
        grid.add(txtStock, 0, row++);
        GridPane.setHalignment(txtStock, HPos.CENTER);

        grid.add(crearLabel("Fecha de Vencimiento:"), 0, row++);
        grid.add(dateVencimiento, 0, row++);
        GridPane.setHalignment(dateVencimiento, HPos.CENTER);

        // Botones
        Button btnGuardar = new Button("Confirmar Registro");
        btnGuardar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-padding: 12 25; -fx-cursor: hand;");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 16; -fx-padding: 12 25; -fx-cursor: hand;");

        btnGuardar.setOnAction(e -> ejecutarGuardado());
        btnCancelar.setOnAction(e -> {
            if (actionVolver != null) actionVolver.run();
        });

        HBox botones = new HBox(20, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(titulo, grid, botones);
        root.setCenter(content);
    }

    public BorderPane getRoot() {
        return root;
    }

    private void ejecutarGuardado() {
        if (validarCampos()) {
            if (guardarEnBD()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Producto guardado correctamente");
                alert.showAndWait();
                if (actionVolver != null) actionVolver.run();
            }
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || comboCategoria.getValue() == null ||
            txtPrecio.getText().isEmpty() || txtStock.getText().isEmpty() || dateVencimiento.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Por favor complete todos los campos").show();
            return false;
        }
        try {
            Double.parseDouble(txtPrecio.getText());
            Integer.parseInt(txtStock.getText());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Precio y Stock deben ser números válidos").show();
            return false;
        }
        return true;
    }

    private boolean guardarEnBD() {
        try (Connection conn = ConexionDB.getInstance()) {
            String sqlCat = "SELECT id_categoria FROM categorias WHERE nombre = ?";
            PreparedStatement psCat = conn.prepareStatement(sqlCat);
            psCat.setString(1, comboCategoria.getValue());
            ResultSet rs = psCat.executeQuery();

            int idCat = rs.next() ? rs.getInt("id_categoria") : 0;

            String sql = "INSERT INTO medicamentos (nombre, descripcion, id_categoria, precio, stock, fecha_vencimiento) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNombre.getText());
            ps.setString(2, txtDescripcion.getText());
            ps.setInt(3, idCat);
            ps.setDouble(4, Double.parseDouble(txtPrecio.getText()));
            ps.setInt(5, Integer.parseInt(txtStock.getText()));
            ps.setDate(6, java.sql.Date.valueOf(dateVencimiento.getValue()));

            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al guardar: " + e.getMessage()).show();
            return false;
        }
    }

    private void cargarCategorias() {
        try (Connection conn = ConexionDB.getInstance()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT nombre FROM categorias");
            while (rs.next()) {
                comboCategoria.getItems().add(rs.getString("nombre"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TextField crearTextField(String prompt, double maxWidth, double height) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(maxWidth);
        tf.setPrefHeight(height);
        tf.setStyle(
            "-fx-control-inner-background: #1e293b; " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: #94a3b8; " +
            "-fx-border-color: #334155; " +
            "-fx-border-radius: 5; " +
            "-fx-font-size: 14;"
        );
        return tf;
    }

    private Label crearLabel(String texto) {
        Label l = new Label(texto);
        l.setTextFill(Color.web("#cbd5e1"));
        l.setFont(Font.font(16));
        return l;
    }
}