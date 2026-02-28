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
        Label title = new Label("Control de Sesiones e Ingresos");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Reporte Semanal de Movimientos");
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

        Label lblSelect = new Label("Semana del Lunes:");
        lblSelect.setTextFill(Color.WHITE);

        datePickerSemana = new DatePicker(LocalDate.now());
        datePickerSemana.setOnAction(e -> cargarDatos());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnPrint = new Button("🖨 Imprimir Reporte Completo");
        btnPrint.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");
        btnPrint.setOnAction(e -> imprimirReporte());

        toolbar.getChildren().addAll(lblSelect, datePickerSemana, spacer, btnPrint);

        // ─── TABLE PRINCIPAL (VISTA APP) ───
        tablaSesiones = new TableView<>();
        tablaSesiones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaSesiones.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827; -fx-table-cell-border-color: #374151;");

        configurarColumnasSesiones(tablaSesiones, false);

        VBox.setVgrow(tablaSesiones, Priority.ALWAYS);
        content.getChildren().addAll(topBar, statsPanel, toolbar, tablaSesiones);
        root.setCenter(content);

        cargarDatos();
    }

    // ─── LÓGICA DE IMPRESIÓN MEJORADA (3 TABLAS) ───
    private void imprimirReporte() {
        VBox hoja = new VBox(20);
        hoja.setPadding(new Insets(30));
        hoja.setStyle("-fx-background-color: white;");
        hoja.setPrefWidth(800); 

        // 1. Header con Logo
        HBox header = crearHeaderImpresion();
        hoja.getChildren().addAll(header, new Separator());

        // 2. Sección Sesiones
        hoja.getChildren().add(crearSeccion("1. RESUMEN DE SESIONES Y VENTAS"));
        TableView<CorteDiario> tablaPrintSesiones = new TableView<>(tablaSesiones.getItems());
        configurarColumnasSesiones(tablaPrintSesiones, true);
        formatearTablaParaImpresion(tablaPrintSesiones);
        hoja.getChildren().add(tablaPrintSesiones);

        // 3. Sección Gastos
        hoja.getChildren().add(crearSeccion("2. DETALLE DE GASTOS SEMANALES"));
        TableView<MovimientoSimple> tablaGastos = crearTablaSimple("CONCEPTO GASTO");
        cargarDatosAdicionales(tablaGastos, "gastos");
        formatearTablaParaImpresion(tablaGastos);
        hoja.getChildren().add(tablaGastos);

        // 4. Sección Inversiones
        hoja.getChildren().add(crearSeccion("3. INVERSIONES ADICIONALES"));
        TableView<MovimientoSimple> tablaInversiones = crearTablaSimple("CONCEPTO INVERSIÓN");
        cargarDatosAdicionales(tablaInversiones, "inversiones");
        formatearTablaParaImpresion(tablaInversiones);
        hoja.getChildren().add(tablaInversiones);

        // 5. Footer
        Label total = new Label("TOTAL NETO VENTAS: " + lblTotalSemanal.getText());
        total.setStyle("-fx-font-size: 14pt; -fx-font-weight: bold; -fx-text-fill: black;");
        hoja.getChildren().add(new HBox(total));

        // --- Ejecutar Trabajo de Impresión ---
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
            double pWidth = job.getJobSettings().getPageLayout().getPrintableWidth();
            double scale = pWidth / hoja.getPrefWidth();
            hoja.getTransforms().setAll(new Scale(scale, scale));
            if (job.printPage(hoja)) job.endJob();
        }
    }

    private VBox crearSeccion(String titulo) {
        VBox box = new VBox();
        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11pt; -fx-text-fill: #1f2937; -fx-padding: 5 0;");
        box.getChildren().add(lbl);
        return box;
    }

    private void formatearTablaParaImpresion(TableView<?> tabla) {
        tabla.setStyle("-fx-background-color: white; -fx-control-inner-background: white; -fx-table-cell-border-color: #ddd; -fx-font-size: 8pt;");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setFixedCellSize(25);
        int rows = tabla.getItems().size();
        tabla.setPrefHeight(rows * 25 + 30); // Ajuste dinámico de altura
    }

    private TableView<MovimientoSimple> crearTablaSimple(String colNombre) {
        TableView<MovimientoSimple> tabla = new TableView<>();
        TableColumn<MovimientoSimple, String> colF = new TableColumn<>("FECHA");
        colF.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colF.setPrefWidth(80);

        TableColumn<MovimientoSimple, String> colN = new TableColumn<>(colNombre);
        colN.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<MovimientoSimple, String> colD = new TableColumn<>("DESCRIPCIÓN");
        colD.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        TableColumn<MovimientoSimple, Double> colM = new TableColumn<>("MONTO");
        colM.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colM.setStyle("-fx-alignment: CENTER-RIGHT;");

        tabla.getColumns().addAll(colF, colN, colD, colM);
        return tabla;
    }

    private void cargarDatosAdicionales(TableView<MovimientoSimple> tabla, String nombreTabla) {
        ObservableList<MovimientoSimple> lista = FXCollections.observableArrayList();
        LocalDate lunes = datePickerSemana.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sabado = lunes.plusDays(5);

        String sql = "SELECT fecha, nombre, descripcion, monto FROM " + nombreTabla + 
                     " WHERE fecha BETWEEN ? AND ? ORDER BY fecha ASC";

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(lunes));
            ps.setDate(2, Date.valueOf(sabado));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new MovimientoSimple(
                    rs.getDate("fecha").toString(),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getDouble("monto")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        tabla.setItems(lista);
    }

    // ─── CONFIGURACIÓN DE COLUMNAS SESIONES (EXISTENTE) ───
    private void configurarColumnasSesiones(TableView<CorteDiario> tabla, boolean esImpresion) {
        TableColumn<CorteDiario, String> colFecha = new TableColumn<>("DÍA");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaLabel"));
        
        TableColumn<CorteDiario, String> colSes = crearColumnaAjustable("SESIONES", "sesiones", esImpresion, Color.WHITE);
        TableColumn<CorteDiario, String> colAdic = crearColumnaAjustable("ADICIONALES", "adicionales", esImpresion, Color.WHITE);
        TableColumn<CorteDiario, String> colPend = crearColumnaAjustable("PENDIENTES", "pendientes", esImpresion, Color.web("#fb923c"));
        TableColumn<CorteDiario, Double> colSumSes = crearColumnaNumerica("INGRESOS", "sumaSesiones", esImpresion, Color.web("#4ade80"), false);
        TableColumn<CorteDiario, Double> colSumMed = crearColumnaNumerica("ADIC. $", "sumaMedicamentos", esImpresion, Color.web("#60a5fa"), false);
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
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    text.setText(item);
                    text.setFill(esImpresion ? Color.BLACK : colorApp);
                    text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));
                    setGraphic(text);
                }
            }
        });
        return col;
    }

    private HBox crearHeaderImpresion() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        ImageView logoView = new ImageView();
        try {
            var url = getClass().getResource("/sistemafarmacia/assets/logo.jpeg");
            if (url != null) {
                logoView.setImage(new Image(url.toExternalForm()));
                logoView.setFitHeight(60);
                logoView.setPreserveRatio(true);
            }
        } catch (Exception e) {}

        VBox textHeader = new VBox(2);
        Label t1 = new Label("REPORTE SEMANAL INTEGRAL");
        t1.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: black;");
        LocalDate lunes = datePickerSemana.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Label t2 = new Label("Semana: " + lunes + " al " + lunes.plusDays(5));
        t2.setStyle("-fx-font-size: 10; -fx-text-fill: #555;");
        textHeader.getChildren().addAll(t1, t2);
        
        if (logoView.getImage() != null) header.getChildren().add(logoView);
        header.getChildren().add(textHeader);
        return header;
    }

    // ─── CONSULTAS BD SESIONES ───
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
        String sql = "SELECT " +
                     "STRING_AGG(s.paciente || ' ($' || (s.total - COALESCE(i.precio, 0)) || ')', '\n') AS sesiones, " +
                     "STRING_AGG(CASE WHEN s.medicamentos IS NOT NULL AND UPPER(s.medicamentos) <> 'NINGUNO' THEN s.paciente || ' (' || s.medicamentos || ') - $' || COALESCE(i.precio, 0) END, '\n') AS adicionales, " +
                     "STRING_AGG(CASE WHEN s.estado_pago = 'CREDITO' THEN s.paciente || ' ($' || s.total || ')' END, '\n') AS detalle_pendientes, " +
                     "COALESCE(SUM(s.total - COALESCE(i.precio, 0)), 0) AS suma_sesiones, " +
                     "COALESCE(SUM(COALESCE(i.precio, 0)), 0) AS suma_medicamentos, " +
                     "COALESCE(SUM(s.total), 0) AS ingresos " +
                     "FROM sesiones s LEFT JOIN insumos i ON s.medicamentos = i.nombre " +
                     "WHERE CAST(s.fecha AS DATE) = ? AND s.estado_pago <> 'SALDADO'";
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

    // ─── CLASES INTERNAS (POJOS) ───
    public static class CorteDiario {
        private String fechaLabel, sesiones, adicionales, pendientes;
        private double sumaSesiones, sumaMedicamentos, neto;
        public CorteDiario(LocalDate f, String s, String a, String p, double ss, double sm, double n) {
            this.fechaLabel = f.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase() + " " + f.getDayOfMonth();
            this.sesiones = s; this.adicionales = a; this.pendientes = p;
            this.sumaSesiones = ss; this.sumaMedicamentos = sm; this.neto = n;
        }
        public String getFechaLabel() { return fechaLabel; }
        public String getSesiones() { return sesiones; }
        public String getAdicionales() { return adicionales; }
        public String getPendientes() { return pendientes; }
        public double getSumaSesiones() { return sumaSesiones; }
        public double getSumaMedicamentos() { return sumaMedicamentos; }
        public double getNeto() { return neto; }
    }

    public static class MovimientoSimple {
        private String fecha, nombre, descripcion;
        private double monto;
        public MovimientoSimple(String f, String n, String d, double m) {
            this.fecha = f; this.nombre = n; this.descripcion = d; this.monto = m;
        }
        public String getFecha() { return fecha; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public double getMonto() { return monto; }
    }
}