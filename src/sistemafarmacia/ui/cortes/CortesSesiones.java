package sistemafarmacia.ui.cortes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sistemafarmacia.utils.UIComponents;
import sistemafarmacia.utils.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;


public class CortesSesiones {
    
    private BorderPane root;
    private TableView<CorteRenglon> tablaCortes;
    private Runnable actionVolver;
    private DatePicker dateDesde, dateHasta;
    private Label lblContadorCortes;
    private Label lblVentasSemanales; // Referencia para actualizar el monto
    private int cortesRealizados = 0;

    public CortesSesiones(Runnable actionVolver) {
        this.actionVolver = actionVolver;

        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // --- TOP BAR ---
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #374151; -fx-border-radius: 5;");
        btnVolver.setOnAction(e -> { if (this.actionVolver != null) this.actionVolver.run(); });

        VBox headerText = new VBox(5);
        Label title = new Label("Almacén Digital");
        title.setFont(Font.font("System Bold", 28));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Control de Insumos y Movimientos");
        subtitle.setTextFill(Color.web("#9ca3af"));
        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        // --- STATS PANEL ---
        HBox statsPanel = new HBox(20);
        
        // Creamos las tarjetas vacías inicialmente
        VBox cardVentas = (VBox) UIComponents.statCard("Ventas Semanales", "$0.00", "/sistemafarmacia/assets/icons/Ventas2.png");
        lblVentasSemanales = (Label) cardVentas.lookup(".label"); // Obtenemos el Label del valor
        
        VBox cardCortes = (VBox) UIComponents.statCard("Cortes Generados", "0", "/sistemafarmacia/assets/icons/Cortes semanales.png");
        lblContadorCortes = (Label) cardCortes.lookup(".label");

        HBox.setHgrow(cardVentas, Priority.ALWAYS);
        HBox.setHgrow(cardCortes, Priority.ALWAYS);
        statsPanel.getChildren().addAll(cardVentas, cardCortes);

        // --- TOOLBAR ---
        HBox toolbar = createToolbar();

        // --- TABLE ---
        tablaCortes = createTable();
        VBox.setVgrow(tablaCortes, Priority.ALWAYS);

        content.getChildren().addAll(topBar, statsPanel, toolbar, tablaCortes);
        root.setCenter(content);

        // Cargar datos dinámicos
        cargarDatosDesdeBD();
        actualizarVentasSemanales();
    }

    private void actualizarVentasSemanales() {
        // Suma de la columna "total" en la tabla "tickets" de los últimos 7 días
        String sql = "SELECT SUM(total) FROM tickets WHERE fecha >= CURRENT_DATE - INTERVAL '7 days'";
        try (Connection conn = ConexionDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                double total = rs.getDouble(1);
                lblVentasSemanales.setText(NumberFormat.getCurrencyInstance(Locale.US).format(total));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #111827; -fx-padding: 15; -fx-background-radius: 10;");

        Label l1 = new Label("Desde:"); l1.setTextFill(Color.WHITE);
        dateDesde = new DatePicker(LocalDate.now().minusDays(7));
        Label l2 = new Label("Hasta:"); l2.setTextFill(Color.WHITE);
        dateHasta = new DatePicker(LocalDate.now());

        // Al cambiar fechas, recargar datos
        dateDesde.setOnAction(e -> cargarDatosDesdeBD());
        dateHasta.setOnAction(e -> cargarDatosDesdeBD());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnGenerar = new Button("Generar Corte Nuevo");
        btnGenerar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnGenerar.setOnAction(e -> imprimirSinPerdida());

        toolbar.getChildren().addAll(l1, dateDesde, l2, dateHasta, spacer, btnGenerar);
        return toolbar;
    }

    private void cargarDatosDesdeBD() {
        tablaCortes.getItems().clear();
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        // Consulta que une categorías, medicamentos y suma movimientos (Entradas/Salidas)
        String sql = """
            SELECT 
                c.nombre AS cat, 
                m.nombre AS med, 
                m.stock,
                COALESCE((SELECT SUM(cantidad) FROM movimientos_inventario WHERE id_medicamento = m.id_medicamento AND tipo = 'ENTRADA' AND CAST(fecha AS DATE) BETWEEN ? AND ?), 0) as entradas,
                COALESCE((SELECT SUM(cantidad) FROM movimientos_inventario WHERE id_medicamento = m.id_medicamento AND tipo = 'SALIDA' AND CAST(fecha AS DATE) BETWEEN ? AND ?), 0) as salidas
            FROM categorias c 
            JOIN medicamentos m ON m.id_categoria = c.id_categoria 
            ORDER BY c.nombre, m.nombre;
            """;

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));
            ps.setDate(3, java.sql.Date.valueOf(desde));
            ps.setDate(4, java.sql.Date.valueOf(hasta));

            ResultSet rs = ps.executeQuery();
            String catActual = "";
            int n = 1;

            while (rs.next()) {
                String cat = rs.getString("cat");
                if (!cat.equals(catActual)) {
                    tablaCortes.getItems().add(new CorteRenglon(cat));
                    catActual = cat;
                    n = 1;
                }
                
                int stockActual = rs.getInt("stock");
                int ent = rs.getInt("entradas");
                int sal = rs.getInt("salidas");
                // Inventario Inicial = Stock Actual + Salidas - Entradas (para retroceder en el tiempo al reporte)
                int invInicial = stockActual + sal - ent;

                tablaCortes.getItems().add(new CorteRenglon(
                    String.valueOf(n++), 
                    "PZA", 
                    rs.getString("med"), 
                    String.valueOf(invInicial), 
                    String.valueOf(ent), 
                    String.valueOf(sal), 
                    String.valueOf(stockActual)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS DE IMPRESIÓN Y TABLA (Se mantienen igual con lógica de impresión corregida) ---

    private void imprimirSinPerdida() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return;
        Stage stage = (Stage) root.getScene().getWindow();
        if (!job.showPrintDialog(stage)) return;

        // Registro del corte en la base de datos (Opcional según tu diagrama 'cortes_caja')
        // Aquí podrías insertar en la tabla 'cortes_caja' si lo deseas.

        cortesRealizados++;
        if (lblContadorCortes != null) lblContadorCortes.setText(String.valueOf(cortesRealizados));
        mostrarAlertaConfirmacion();
        job.endJob();
    }

    private void mostrarAlertaConfirmacion() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operación Exitosa");
        alert.setHeaderText(null);
        alert.setContentText("El reporte de inventario y ventas se ha generado correctamente.");
        alert.showAndWait();
    }

    private TableView<CorteRenglon> createTable() {
        TableView<CorteRenglon> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827;");

        TableColumn<CorteRenglon, String> c1 = new TableColumn<>("No.");
        c1.setCellValueFactory(new PropertyValueFactory<>("numero"));
        TableColumn<CorteRenglon, String> c2 = new TableColumn<>("Descripción");
        c2.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        TableColumn<CorteRenglon, String> c3 = new TableColumn<>("S. Inicial");
        c3.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        TableColumn<CorteRenglon, String> c4 = new TableColumn<>("Ent.");
        c4.setCellValueFactory(new PropertyValueFactory<>("entrada"));
        TableColumn<CorteRenglon, String> c5 = new TableColumn<>("Sal.");
        c5.setCellValueFactory(new PropertyValueFactory<>("salida"));
        TableColumn<CorteRenglon, String> c6 = new TableColumn<>("S. Final");
        c6.setCellValueFactory(new PropertyValueFactory<>("inventarioFinal"));

        table.getColumns().addAll(c1, c2, c3, c4, c5, c6);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(CorteRenglon item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isEsSeccion()) setStyle("-fx-background-color: #1e3a8a; -fx-font-weight: bold;");
                else setStyle("");
            }
        });
        return table;
    }

    public BorderPane getRoot() { return root; }

    public static class CorteRenglon {
        private String numero, presentacion, descripcion, existencia, entrada, salida, inventarioFinal;
        private boolean esSeccion;
        public CorteRenglon(String n, String p, String d, String e, String en, String s, String f) {
            this.numero = n; this.presentacion = p; this.descripcion = d;
            this.existencia = e; this.entrada = en; this.salida = s; this.inventarioFinal = f;
            this.esSeccion = false;
        }
        public CorteRenglon(String s) { this.descripcion = s; this.esSeccion = true; }
        public String getNumero() { return numero; }
        public String getPresentacion() { return presentacion; }
        public String getDescripcion() { return descripcion; }
        public String getExistencia() { return existencia; }
        public String getEntrada() { return entrada; }
        public String getSalida() { return salida; }
        public String getInventarioFinal() { return inventarioFinal; }
        public boolean isEsSeccion() { return esSeccion; }
    }
}