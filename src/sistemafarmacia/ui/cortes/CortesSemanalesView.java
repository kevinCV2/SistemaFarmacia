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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class CortesSemanalesView {

    private BorderPane root;
    private TableView<CorteRenglon> tablaCortes;
    private Runnable actionVolver;
    private DatePicker dateDesde, dateHasta;
    private Label lblContadorCortes;
    private int cortesRealizados = 0;

    public CortesSemanalesView(Runnable actionVolver) {
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

        // --- STATS PANEL (Se eliminó Ventas Semanales) ---
        HBox statsPanel = new HBox(20);
        VBox cardCortes = (VBox) UIComponents.statCard("Cortes Generados", "0", "/sistemafarmacia/assets/icons/Cortes semanales.png");
        lblContadorCortes = (Label) cardCortes.lookup(".label");

        // Hacemos que la tarjeta de cortes se expanda para llenar el ancho si es necesario
        HBox.setHgrow(cardCortes, Priority.ALWAYS);
        statsPanel.getChildren().addAll(cardCortes);

        // --- TOOLBAR ---
        HBox toolbar = createToolbar();

        // --- TABLE ---
        tablaCortes = createTable();
        VBox.setVgrow(tablaCortes, Priority.ALWAYS);

        content.getChildren().addAll(topBar, statsPanel, toolbar, tablaCortes);
        root.setCenter(content);

        cargarDatosDesdeBD();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #111827; -fx-padding: 15; -fx-background-radius: 10;");

        Label l1 = new Label("Desde:"); l1.setTextFill(Color.WHITE);
        dateDesde = new DatePicker(LocalDate.now().minusDays(7));
        dateDesde.setOnAction(e -> cargarDatosDesdeBD());

        Label l2 = new Label("Hasta:"); l2.setTextFill(Color.WHITE);
        dateHasta = new DatePicker(LocalDate.now());
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

        String sql = """
            SELECT 
                m.categoria, 
                m.nombre, 
                m.existencia AS inicial,
                COALESCE(SUM(CASE WHEN mov.tipo = 'ENTRADA' THEN mov.cantidad ELSE 0 END), 0) AS entradas,
                COALESCE(SUM(CASE WHEN mov.tipo = 'SALIDA' THEN mov.cantidad ELSE 0 END), 0) AS salidas
            FROM medicamentos m
            LEFT JOIN movimientos_inventario mov 
                ON m.id_medicamento = mov.id_medicamento 
                AND mov.fecha::date >= ? 
                AND mov.fecha::date <= ?
            GROUP BY m.categoria, m.nombre, m.existencia, m.id_categoria
            ORDER BY m.id_categoria ASC, m.nombre ASC
        """;

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, dateDesde.getValue());
            ps.setObject(2, dateHasta.getValue());
            
            ResultSet rs = ps.executeQuery();

            String categoriaActual = "";
            int contadorSeccion = 1;

            while (rs.next()) {
                String catRaw = rs.getString("categoria");
                if (catRaw == null) catRaw = "SIN CATEGORÍA";
                
                if (!catRaw.equals(categoriaActual)) {
                    categoriaActual = catRaw;
                    tablaCortes.getItems().add(new CorteRenglon(categoriaActual.toUpperCase()));
                    contadorSeccion = 1;
                }

                int inicial = rs.getInt("inicial");
                int entradas = rs.getInt("entradas");
                int salidas = rs.getInt("salidas");
                int total = inicial + entradas - salidas;

                tablaCortes.getItems().add(
                    new CorteRenglon(
                        String.valueOf(contadorSeccion++),
                        "PZA",
                        rs.getString("nombre"),
                        String.valueOf(inicial),
                        String.valueOf(entradas),
                        String.valueOf(salidas),
                        String.valueOf(total)
                    )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private TableView<CorteRenglon> createTable() {
        TableView<CorteRenglon> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827;");

        TableColumn<CorteRenglon, String> c1 = new TableColumn<>("No.");
        c1.setCellValueFactory(new PropertyValueFactory<>("numero"));
        c1.setPrefWidth(50);
        c1.setMaxWidth(50);

        TableColumn<CorteRenglon, String> c2 = new TableColumn<>("Descripción / Insumo");
        c2.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        TableColumn<CorteRenglon, String> c3 = new TableColumn<>("Stock");
        c3.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        
        TableColumn<CorteRenglon, String> c4 = new TableColumn<>("Ent.");
        c4.setCellValueFactory(new PropertyValueFactory<>("entrada"));
        
        TableColumn<CorteRenglon, String> c5 = new TableColumn<>("Sal.");
        c5.setCellValueFactory(new PropertyValueFactory<>("salida"));
        
        TableColumn<CorteRenglon, String> c6 = new TableColumn<>("Total");
        c6.setCellValueFactory(new PropertyValueFactory<>("inventarioFinal"));

        table.getColumns().addAll(c1, c2, c3, c4, c5, c6);

        table.setRowFactory(tv -> new TableRow<CorteRenglon>() {
            @Override protected void updateItem(CorteRenglon item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isEsSeccion()) {
                    setStyle("-fx-background-color: #1e40af; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        return table;
    }

    private void imprimirSinPerdida() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return;
        Stage stage = (Stage) root.getScene().getWindow();
        if (!job.showPrintDialog(stage)) return;

        PageLayout layout = job.getJobSettings().getPageLayout();
        double printableWidth = layout.getPrintableWidth();
        double printableHeight = layout.getPrintableHeight();
        
        TableView<CorteRenglon> tablaFull = createTable();
        tablaFull.getItems().addAll(tablaCortes.getItems());
        
        double rowHeight = 32.0;
        double contentAreaHeight = printableHeight - 180;
        int rowsPerPage = (int) (contentAreaHeight / rowHeight);

        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();
        String fechaRango = String.format("%d-%d %s %d", desde.getDayOfMonth(), hasta.getDayOfMonth(), desde.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")), desde.getYear());

        int pageNum = 1;
        boolean exito = false;
        for (int start = 0; start < tablaFull.getItems().size(); start += rowsPerPage) {
            int end = Math.min(start + rowsPerPage, tablaFull.getItems().size());
            TableView<CorteRenglon> pageTable = createTable();
            pageTable.getItems().addAll(tablaFull.getItems().subList(start, end));
            pageTable.setPrefWidth(printableWidth);
            pageTable.setPrefHeight(contentAreaHeight);
            
            VBox page = new VBox(10);
            page.setPadding(new Insets(20));
            page.setStyle("-fx-background-color: white;");
            
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            ImageView logo = new ImageView("/sistemafarmacia/assets/icons/logo.png");
            logo.setFitWidth(80); logo.setFitHeight(40);
            
            VBox headerText = new VBox(2);
            Label title = new Label("CORTE DE INVENTARIO");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            Label info = new Label("Semana: " + fechaRango + " | Hora: " + LocalTime.now().withNano(0));
            info.setFont(Font.font("Arial", 12));
            headerText.getChildren().addAll(title, info);
            
            header.getChildren().addAll(logo, headerText);
            page.getChildren().addAll(header, new Separator(), pageTable);
            
            if (!job.printPage(page)) break;
            exito = true;
            pageNum++;
        }
        if (exito) {
            job.endJob();
            cortesRealizados++;
            if (lblContadorCortes != null) lblContadorCortes.setText(String.valueOf(cortesRealizados));
            mostrarAlertaConfirmacion();
        }
    }

    private void mostrarAlertaConfirmacion() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operación Exitosa");
        alert.setContentText("El reporte de inventario se ha generado correctamente.");
        alert.showAndWait();
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

        public CorteRenglon(String nombreSeccion) { 
            this.numero = ""; this.descripcion = nombreSeccion; 
            this.presentacion = ""; this.existencia = ""; this.entrada = ""; 
            this.salida = ""; this.inventarioFinal = "";
            this.esSeccion = true; 
        }

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