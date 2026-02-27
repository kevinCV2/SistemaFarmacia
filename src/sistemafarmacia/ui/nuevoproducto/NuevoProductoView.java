package sistemafarmacia.ui.nuevoproducto;

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
    private ComboBox<String> comboTipo;

    public NuevoProductoView(Runnable actionVolver) {
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

        Label titulo = new Label("Registrar Nuevo Producto");
        titulo.setFont(Font.font("System Bold", 32));
        titulo.setTextFill(Color.WHITE);

        // --- Configuración de Controles ---
        comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll("Medicamento", "Insumo", "Filtro");
        comboTipo.setPromptText("Seleccione Tipo");
        comboTipo.setPrefWidth(400);
        estilizarControl(comboTipo);

        txtNombre = crearTextField("Nombre del Producto", 400, 35);
        
        txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción (Para Insumos)...");
        txtDescripcion.setPrefHeight(80);
        txtDescripcion.setMaxWidth(400);
        txtDescripcion.setWrapText(true);
        txtDescripcion.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 5;");

        comboCategoria = new ComboBox<>();
        comboCategoria.setPromptText("Seleccione Categoría");
        comboCategoria.setPrefWidth(400);
        estilizarControl(comboCategoria);

        // --- LÓGICA DINÁMICA DE CATEGORÍAS ---
        comboTipo.setOnAction(e -> {
            String seleccion = comboTipo.getValue();
            comboCategoria.getItems().clear();
            
            if ("Medicamento".equals(seleccion)) {
                // Según tu BD: Medicamentos están en tabla 'insumos'
                cargarCategoriasDesde("insumos");
                comboCategoria.setDisable(false);
                txtDescripcion.setDisable(true); // Usualmente tabla insumos no tiene descripción larga
            } else if ("Insumo".equals(seleccion)) {
                // Según tu BD: Insumos están en tabla 'medicamentos'
                cargarCategoriasDesde("medicamentos");
                comboCategoria.setDisable(false);
                txtDescripcion.setDisable(false);
            } else if ("Filtro".equals(seleccion)) {
                comboCategoria.setDisable(true);
                txtDescripcion.setDisable(true);
            }
        });

        txtPrecio = crearTextField("Precio (0.00)", 400, 35);
        txtStock = crearTextField("Stock Inicial", 400, 35);

        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        int row = 0;
        agregarAlGrid(grid, "Tipo de Producto:", comboTipo, row++);
        agregarAlGrid(grid, "Nombre:", txtNombre, row++);
        agregarAlGrid(grid, "Descripción (Solo Insumos):", txtDescripcion, row++);
        agregarAlGrid(grid, "Categoría:", comboCategoria, row++);
        agregarAlGrid(grid, "Precio:", txtPrecio, row++);
        agregarAlGrid(grid, "Stock inicial:", txtStock, row++);

        Button btnGuardar = new Button("Confirmar Registro");
        btnGuardar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-padding: 12 25; -fx-cursor: hand;");
        btnGuardar.setOnAction(e -> ejecutarGuardado());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 16; -fx-padding: 12 25; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        HBox botones = new HBox(20, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER);
        
        content.getChildren().addAll(titulo, grid, botones);
        scrollPane.setContent(content);
        root.setCenter(scrollPane);
    }

    private void cargarCategoriasDesde(String tablaDestino) {
        // Obtenemos categorías únicas de la tabla correspondiente
        String sql = "SELECT DISTINCT categoria FROM " + tablaDestino + " ORDER BY categoria ASC";
        // Si tienes una tabla maestra 'categorias', podrías filtrar por un campo 'tipo' si existiera.
        try (Connection conn = ConexionDB.getInstance();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                String cat = rs.getString(1);
                if (cat != null && !cat.isEmpty()) comboCategoria.getItems().add(cat);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean guardarEnBD() {
        String tipo = comboTipo.getValue();
        try (Connection conn = ConexionDB.getInstance()) {
            conn.setAutoCommit(false);
            try {
                if ("Medicamento".equals(tipo)) {
                    // DESTINO: Tabla insumos
                    String sql = "INSERT INTO insumos (nombre, precio, stock, categoria) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtNombre.getText());
                    ps.setDouble(2, Double.parseDouble(txtPrecio.getText()));
                    ps.setInt(3, Integer.parseInt(txtStock.getText()));
                    ps.setString(4, comboCategoria.getValue());
                    ps.executeUpdate();
                } else if ("Insumo".equals(tipo)) {
                    // DESTINO: Tabla medicamentos
                    String sql = "INSERT INTO medicamentos (nombre, descripcion, precio, stock, categoria) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtNombre.getText());
                    ps.setString(2, txtDescripcion.getText());
                    ps.setDouble(3, Double.parseDouble(txtPrecio.getText()));
                    ps.setInt(4, Integer.parseInt(txtStock.getText()));
                    ps.setString(5, comboCategoria.getValue());
                    ps.executeUpdate();
                } else if ("Filtro".equals(tipo)) {
                    // DESTINO: Tabla insumos
                    String sql = "INSERT INTO insumos (nombre, precio, stock, categoria) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, txtNombre.getText());
                    ps.setDouble(2, Double.parseDouble(txtPrecio.getText()));
                    ps.setInt(3, Integer.parseInt(txtStock.getText()));
                    ps.setString(4, "Filtros");
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void ejecutarGuardado() {
        if (validarCampos()) {
            if (guardarEnBD()) {
                new Alert(Alert.AlertType.INFORMATION, "Producto guardado correctamente").showAndWait();
                if (actionVolver != null) actionVolver.run();
            }
        }
    }

    private boolean validarCampos() {
        if (comboTipo.getValue() == null || txtNombre.getText().trim().isEmpty()) return false;
        if (!"Filtro".equals(comboTipo.getValue()) && comboCategoria.getValue() == null) return false;
        return true;
    }

    private void agregarAlGrid(GridPane grid, String etiqueta, javafx.scene.Node control, int row) {
        grid.add(crearLabel(etiqueta), 0, row * 2);
        grid.add(control, 0, (row * 2) + 1);
    }

    private void estilizarControl(Control c) {
        c.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-size: 14; -fx-border-color: #334155; -fx-border-radius: 5;");
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