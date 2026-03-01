package sistemafarmacia.ui.cortes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import sistemafarmacia.utils.UIComponents;
import sistemafarmacia.utils.ConexionDB;

import java.sql.*;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public class CortesSesiones {

    private BorderPane root;
    private TableView<CorteDiario> tablaSesiones;
    private DatePicker datePickerSemana;
    private Label lblTotalSemanal;
    private Label lblSesionesTotales;
    private Runnable actionVolver;

    public CortesSesiones(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // ─── TOP BAR ───
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-border-color: #374151; -fx-border-radius: 5; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        VBox headerText = new VBox(5);
        Label title = new Label("Control de Sesiones");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Reporte Semanal de Ingresos");
        subtitle.setTextFill(Color.web("#9ca3af"));

        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        // ─── STATS ───
        HBox statsPanel = new HBox(20);
        statsPanel.setAlignment(Pos.CENTER_LEFT);

        VBox cardVentas = (VBox) UIComponents.statCard("Ventas Semanales", "$0.00", "/sistemafarmacia/assets/icons/Ventas2.png");
        VBox cardSesiones = (VBox) UIComponents.statCard("Cortes Generados", "0", "/sistemafarmacia/assets/icons/Cortes semanales.png");
        HBox.setHgrow(cardVentas, Priority.ALWAYS);
        HBox.setHgrow(cardSesiones, Priority.ALWAYS);

        lblTotalSemanal = obtenerLabelValor(cardVentas);
        lblSesionesTotales = obtenerLabelValor(cardSesiones);

        statsPanel.getChildren().addAll(cardVentas, cardSesiones);

        // ─── TOOLBAR ───
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #111827; -fx-padding: 15; -fx-background-radius: 10;");

        Label lblSelect = new Label("Seleccionar Semana:");
        lblSelect.setTextFill(Color.WHITE);

        datePickerSemana = new DatePicker(LocalDate.now());
        datePickerSemana.setOnAction(e -> cargarDatos());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnPrint = new Button("🖨 Imprimir Formato");
        btnPrint.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");
        btnPrint.setOnAction(e -> imprimirReporte());

        toolbar.getChildren().addAll(lblSelect, datePickerSemana, spacer, btnPrint);

        // ─── TABLE ───
        tablaSesiones = new TableView<>();
        tablaSesiones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaSesiones.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827; -fx-table-cell-border-color: #374151;");

        configurarColumnas(tablaSesiones, false);

        VBox.setVgrow(tablaSesiones, Priority.ALWAYS);
        content.getChildren().addAll(topBar, statsPanel, toolbar, tablaSesiones);
        root.setCenter(content);

        cargarDatos();
    }

    private void configurarColumnas(TableView<CorteDiario> tabla, boolean esImpresion) {
        // Día
        TableColumn<CorteDiario, String> colFecha = new TableColumn<>("DÍA");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaLabel"));
        colFecha.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item);
                    setTextFill(esImpresion ? Color.BLACK : Color.WHITE);
                }
            }
        });

        // Sesiones y Adicionales (Texto)
        TableColumn<CorteDiario, String> colSes = crearColumnaAjustable("SESIONES", "sesiones", esImpresion, Color.WHITE);
        TableColumn<CorteDiario, String> colAdic = crearColumnaAjustable("ADICIONALES", "adicionales", esImpresion, Color.WHITE);
        
        // PENDIENTES (NARANJA)
        TableColumn<CorteDiario, String> colPend = crearColumnaAjustable("PENDIENTES", "pendientes", esImpresion, Color.web("#fb923c"));
        
        // INGRESOS (VERDE)
        TableColumn<CorteDiario, Double> colSumSes = crearColumnaNumerica("INGRESOS", "sumaSesiones", esImpresion, Color.web("#4ade80"), false);
        
        // ADIC. $ (AZUL)
        TableColumn<CorteDiario, Double> colSumMed = crearColumnaNumerica("ADICIONALES", "sumaMedicamentos", esImpresion, Color.web("#60a5fa"), false);
        
        // Neto (Eliminado Gastos, pasamos directo al Neto)
        TableColumn<CorteDiario, Double> colNeto = crearColumnaNumerica("NETO", "neto", esImpresion, Color.web("#34d399"), true);

        tabla.getColumns().setAll(colFecha, colSes, colAdic, colPend, colSumSes, colSumMed, colNeto);
    }

    private TableColumn<CorteDiario, Double> crearColumnaNumerica(String titulo, String propiedad, boolean esImpresion, Color colorApp, boolean esNegrita) {
        TableColumn<CorteDiario, Double> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f", item));
                    setTextFill(esImpresion ? Color.BLACK : colorApp);
                    String estilo = "-fx-alignment: CENTER-RIGHT;";
                    if (esNegrita) estilo += "-fx-font-weight: bold;";
                    setStyle(estilo);
                }
            }
        });
        return col;
    }

    private TableColumn<CorteDiario, String> crearColumnaAjustable(String titulo, String propiedad, boolean esImpresion, Color colorApp) {
        TableColumn<CorteDiario, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        col.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.setFill(esImpresion ? Color.BLACK : colorApp);
                    text.wrappingWidthProperty().bind(col.widthProperty().subtract(15));
                    setGraphic(text);
                }
            }
        });
        return col;
    }

    private void imprimirReporte() {
        VBox hoja = new VBox(15);
        hoja.setPadding(new Insets(25));
        hoja.setStyle("-fx-background-color: white;");
        hoja.setPrefWidth(850); 

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        ImageView logoView = new ImageView();
        try {
            String ruta = "/sistemafarmacia/assets/logo.jpeg";
            var url = getClass().getResource(ruta);
            if (url != null) {
                logoView.setImage(new Image(url.toExternalForm()));
                logoView.setFitHeight(80);
                logoView.setPreserveRatio(true);
            }
        } catch (Exception e) {}

        VBox textHeader = new VBox(2);
        Label t1 = new Label("REPORTE SEMANAL DE SESIONES");
        t1.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: black;");
        LocalDate lunes = datePickerSemana.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Label t2 = new Label("Periodo: " + lunes + " al " + lunes.plusDays(5));
        t2.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");
        textHeader.getChildren().addAll(t1, t2);
        
        if (logoView.getImage() != null) header.getChildren().add(logoView);
        header.getChildren().add(textHeader);

        TableView<CorteDiario> tablaPrint = new TableView<>();
        tablaPrint.setItems(tablaSesiones.getItems());
        tablaPrint.setStyle("-fx-background-color: white; -fx-control-inner-background: white; -fx-table-cell-border-color: #ccc; -fx-font-size: 9pt;");
        configurarColumnas(tablaPrint, true);
        tablaPrint.setPrefHeight(650);
        tablaPrint.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label total = new Label("TOTAL NETO SEMANAL: " + lblTotalSemanal.getText());
        total.setStyle("-fx-font-size: 14pt; -fx-font-weight: bold; -fx-text-fill: black;");
        HBox footer = new HBox(total);
        footer.setAlignment(Pos.CENTER_RIGHT);

        hoja.getChildren().addAll(header, new Separator(), tablaPrint, footer);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
            double pWidth = job.getJobSettings().getPageLayout().getPrintableWidth();
            double scale = pWidth / hoja.getPrefWidth();
            Scale scaling = new Scale(scale, scale);
            scaling.setPivotX(0); scaling.setPivotY(0);
            hoja.getTransforms().setAll(scaling);
            if (job.printPage(hoja)) job.endJob();
        }
    }

    private void cargarDatos() {
        ObservableList<CorteDiario> lista = FXCollections.observableArrayList();
        LocalDate lunes = datePickerSemana.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        double sumaSemana = 0;
        int count = 0;
        for (int i = 0; i < 6; i++) {
            CorteDiario cd = consultarBaseDatos(lunes.plusDays(i));
            lista.add(cd);
            sumaSemana += cd.getNeto();
            if(!cd.getSesiones().equals("—")) count++;
        }
        tablaSesiones.setItems(lista);
        lblTotalSemanal.setText(NumberFormat.getCurrencyInstance(Locale.US).format(sumaSemana));
        lblSesionesTotales.setText(String.valueOf(count));
    }

    private CorteDiario consultarBaseDatos(LocalDate fecha) {
        String sql = """
SELECT 
    STRING_AGG(s.paciente || ' ($' || 
        (s.total - COALESCE(i.precio, 0)) || ')', '\n') AS sesiones,

    STRING_AGG(
        CASE 
            WHEN s.medicamentos IS NOT NULL 
             AND s.medicamentos <> '' 
             AND UPPER(s.medicamentos) <> 'NINGUNO'
        THEN s.paciente || ' (' || s.medicamentos || ') - $' 
             || COALESCE(i.precio, 0)
        END, '\n') AS adicionales,

    STRING_AGG(
        CASE 
            WHEN s.estado_pago = 'CREDITO' 
        THEN s.paciente || ' ($' || s.total || ')'
        END, '\n') AS detalle_pendientes,

    COALESCE(SUM(s.total - COALESCE(i.precio, 0)), 0) AS suma_sesiones,
    COALESCE(SUM(COALESCE(i.precio, 0)), 0) AS suma_medicamentos,
    COALESCE(SUM(s.total), 0) AS ingresos

FROM sesiones s
LEFT JOIN insumos i ON s.medicamentos = i.nombre

WHERE CAST(s.fecha AS DATE) = ?
AND s.estado_pago <> 'SALDADO'
""";
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new CorteDiario(fecha, 
                    rs.getString("sesiones") != null ? rs.getString("sesiones") : "—",
                    rs.getString("adicionales") != null ? rs.getString("adicionales") : "—",
                    rs.getString("detalle_pendientes") != null ? rs.getString("detalle_pendientes") : "—",
                    rs.getDouble("suma_sesiones"), rs.getDouble("suma_medicamentos"), rs.getDouble("ingresos")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new CorteDiario(fecha, "—", "—", "—", 0, 0, 0);
    }

    private Label obtenerLabelValor(VBox card) {
        for (Node n : card.getChildren()) {
            if (n instanceof Label) {
                String txt = ((Label) n).getText();
                if (txt.contains("$") || txt.matches("\\d+")) return (Label) n;
            }
        }
        return new Label("$0.00");
    }

    public BorderPane getRoot() { return root; }

    public static class CorteDiario {
        private String fechaLabel, sesiones, adicionales, pendientes;
        private double sumaSesiones, sumaMedicamentos, neto;
        public CorteDiario(LocalDate f, String s, String a, String p, double ss, double sm, double n) {
            this.fechaLabel = f.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase() + " " + f.getDayOfMonth();
            this.sesiones = s; this.adicionales = a; this.pendientes = p;
            this.sumaSesiones = ss; this.sumaMedicamentos = sm;
            this.neto = n;
        }
        public String getFechaLabel() { return fechaLabel; }
        public String getSesiones() { return sesiones; }
        public String getAdicionales() { return adicionales; }
        public String getPendientes() { return pendientes; }
        public double getSumaSesiones() { return sumaSesiones; }
        public double getSumaMedicamentos() { return sumaMedicamentos; }
        public double getNeto() { return neto; }
    }
}