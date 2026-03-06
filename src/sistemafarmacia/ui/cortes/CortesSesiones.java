package sistemafarmacia.ui.cortes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
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
import javafx.stage.Stage;
import sistemafarmacia.utils.UIComponents;
import sistemafarmacia.utils.ConexionDB;

import java.sql.*;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public class CortesSesiones {

    private BorderPane root;
    private TableView<CorteDiario> tablaSesiones;
    private DatePicker datePickerSemana;
    private Label lblIngresosTotales; // Card Izquierda
    private Label lblCortesGenerados; // Card Derecha
    private Runnable actionVolver;

    public CortesSesiones(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // --- BARRA SUPERIOR ---
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-border-color: #374151; -fx-border-radius: 5; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        VBox headerText = new VBox(5);
        Label title = new Label("Control de Sesiones e Ingresos");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        headerText.getChildren().addAll(
                title,
                new Label("Reporte Semanal de Movimientos") {{ setTextFill(Color.web("#9ca3af")); }}
        );
        topBar.getChildren().addAll(btnVolver, headerText);

        // --- PANEL DE ESTADÍSTICAS (IZQUIERDA: Ingresos | DERECHA: Cortes) ---
        HBox statsPanel = new HBox(20);
        
        VBox cardIngresos = (VBox) UIComponents.statCard("Ingresos", "$0.00", "/sistemafarmacia/assets/icons/Ventas2.png");
        VBox cardCortes = (VBox) UIComponents.statCard("Cortes Generados", "0", "/sistemafarmacia/assets/icons/Cortes semanales.png");
        
        HBox.setHgrow(cardIngresos, Priority.ALWAYS);
        HBox.setHgrow(cardCortes, Priority.ALWAYS);
        
        // Extraer labels para actualización dinámica
        lblIngresosTotales = obtenerLabelValor(cardIngresos);
        lblCortesGenerados = obtenerLabelValor(cardCortes);
        
        statsPanel.getChildren().addAll(cardIngresos, cardCortes);

        // --- HERRAMIENTAS (FILTRO Y BOTONES) ---
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #111827; -fx-padding: 15; -fx-background-radius: 10;");
        
        datePickerSemana = new DatePicker(LocalDate.now());
        datePickerSemana.setOnAction(e -> cargarDatos());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnImprimir = new Button("🖨 Imprimir Reporte");
        btnImprimir.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnImprimir.setOnAction(e -> imprimirReporteSesiones());

        toolbar.getChildren().addAll(
                new Label("Semana del Lunes:") {{ setTextFill(Color.WHITE); }}, 
                datePickerSemana, 
                spacer, 
                btnImprimir
        );

        // --- TABLA DE DATOS ---
        tablaSesiones = new TableView<>();
        tablaSesiones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaSesiones.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827;");
        configurarColumnasSesiones(tablaSesiones);

        VBox.setVgrow(tablaSesiones, Priority.ALWAYS);
        content.getChildren().addAll(topBar, statsPanel, toolbar, tablaSesiones);
        root.setCenter(content);
        
        cargarDatos();
    }

    private void configurarColumnasSesiones(TableView<CorteDiario> tabla) {
        TableColumn<CorteDiario, String> colFecha = new TableColumn<>("DÍA");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaLabel"));

        TableColumn<CorteDiario, String> colSes = crearColumnaAjustable("SESIONES", "sesiones", Color.WHITE);
        TableColumn<CorteDiario, String> colAdic = crearColumnaAjustable("ADICIONALES", "adicionales", Color.WHITE);
        TableColumn<CorteDiario, String> colPend = crearColumnaAjustable("PENDIENTES", "pendientes", Color.web("#fb923c"));

        // Columna de Ingresos (Verde)
        TableColumn<CorteDiario, Double> colIngresos = crearColumnaNumerica("INGRESOS $", "sumaSesiones", Color.web("#4ade80"), true);
        TableColumn<CorteDiario, Double> colAdicMonto = crearColumnaNumerica("ADICIONALES $", "sumaMedicamentos", Color.web("#60a5fa"), false);

        tabla.getColumns().setAll(
                colFecha, colSes, colAdic, colPend,
                colIngresos, colAdicMonto
        );
    }

    private void cargarDatos() {
        ObservableList<CorteDiario> lista = FXCollections.observableArrayList();
        LocalDate lunes = datePickerSemana.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        double totalIngresosVerde = 0;
        int conteoCortes = 0;

        for (int i = 0; i < 6; i++) {
            CorteDiario cd = consultarBaseDatos(lunes.plusDays(i));
            lista.add(cd);
            
            // Sumamos el valor de la columna "INGRESOS $"
            totalIngresosVerde += cd.getSumaSesiones();
            
            // Si el día tiene algún registro (sesiones o adicionales), se cuenta como corte
            if (!cd.getSesiones().equals("—") || !cd.getAdicionales().equals("—")) {
                conteoCortes++;
            }
        }

        tablaSesiones.setItems(lista);
        
        // Actualizar Cards
        lblIngresosTotales.setText(NumberFormat.getCurrencyInstance(Locale.US).format(totalIngresosVerde));
        lblCortesGenerados.setText(String.valueOf(conteoCortes));
    }

    private CorteDiario consultarBaseDatos(LocalDate fecha) {
        String sql =
                "SELECT " +
                " STRING_AGG(s.paciente || ' ($' || (s.total - COALESCE(i.precio,0)) || ')','\n') AS sesiones," +
                " STRING_AGG(CASE WHEN s.medicamentos IS NOT NULL AND UPPER(s.medicamentos) <> 'NINGUNO' " +
                " THEN s.paciente || ' (' || s.medicamentos || ') - $' || COALESCE(i.precio,0) END,'\n') AS adicionales," +
                " STRING_AGG(CASE WHEN s.estado_pago='CREDITO' THEN s.paciente || ' ($' || s.total || ')' END,'\n') AS detalle_pendientes," +
                " (SELECT STRING_AGG(nombre || ' (' || descripcion || ') - $' || monto,'\n') FROM gastos WHERE CAST(dia AS DATE)=?) AS detalle_gastos," +
                " (SELECT STRING_AGG(nombre || ' (' || descripcion || ') - $' || monto,'\n') FROM inversiones_adicionales WHERE CAST(dia AS DATE)=?) AS detalle_inversion," +
                " COALESCE(SUM(s.total - COALESCE(i.precio,0)),0) AS suma_sesiones," +
                " COALESCE(SUM(COALESCE(i.precio,0)),0) AS suma_medicamentos," +
                " (SELECT COALESCE(SUM(monto),0) FROM gastos WHERE CAST(dia AS DATE)=?) AS total_gastos," +
                " (SELECT COALESCE(SUM(monto),0) FROM inversiones_adicionales WHERE CAST(dia AS DATE)=?) AS total_inv " +
                "FROM sesiones s LEFT JOIN insumos i ON s.medicamentos=i.nombre " +
                "WHERE CAST(s.fecha AS DATE)=? AND s.estado_pago<>'SALDADO'";

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));
            ps.setDate(2, Date.valueOf(fecha));
            ps.setDate(3, Date.valueOf(fecha));
            ps.setDate(4, Date.valueOf(fecha));
            ps.setDate(5, Date.valueOf(fecha));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new CorteDiario(
                        fecha,
                        rs.getString("sesiones") != null ? rs.getString("sesiones") : "—",
                        rs.getString("adicionales") != null ? rs.getString("adicionales") : "—",
                        rs.getString("detalle_pendientes") != null ? rs.getString("detalle_pendientes") : "—",
                        rs.getString("detalle_gastos") != null ? rs.getString("detalle_gastos") : "—",
                        rs.getString("detalle_inversion") != null ? rs.getString("detalle_inversion") : "—",
                        rs.getDouble("suma_sesiones"),
                        rs.getDouble("suma_medicamentos"),
                        rs.getDouble("total_gastos"),
                        rs.getDouble("total_inv")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CorteDiario(fecha, "—", "—", "—", "—", "—", 0, 0, 0, 0);
    }

   private void imprimirReporteSesiones() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return;
        
        Printer printer = job.getPrinter();
        // Configuración Vertical (Portrait) con márgenes mínimos
        PageLayout layout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
        job.getJobSettings().setPageLayout(layout);

        Stage stage = (Stage) root.getScene().getWindow();
        if (!job.showPrintDialog(stage)) return;

        double pWidth = layout.getPrintableWidth();
        VBox page = new VBox(12);
        page.setPadding(new Insets(10));
        page.setStyle("-fx-background-color: white;");
        page.setPrefWidth(pWidth); // Forzar ancho al área imprimible

        // --- CABECERA ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        try {
            ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/sistemafarmacia/assets/icons/logo.png")));
            logo.setFitWidth(50); 
            logo.setPreserveRatio(true);
            header.getChildren().add(logo);
        } catch (Exception e) {}

        LocalDate lunes = datePickerSemana.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sabado = lunes.plusDays(5);
        String rango = lunes.getDayOfMonth() + " al " + sabado.getDayOfMonth() + " de " + 
                       lunes.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase();

        VBox headerText = new VBox(2);
        Label title = new Label("CORTE SEMANAL DE CAJA Y SESIONES");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label info = new Label("Semana: " + rango + " | " + lunes.getYear() + " | Generado: " + LocalTime.now().withNano(0));
        info.setFont(Font.font("Arial", 8.5));
        headerText.getChildren().addAll(title, info);
        header.getChildren().add(headerText);

        // --- TABLA (GRIDPANE) AJUSTADA ---
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setPrefWidth(pWidth);

        // Columnas seleccionadas y Pesos calculados para formato Vertical
        String[] headers = {"DÍA", "SESIONES", "ADICIONALES", "PENDIENTES", "INGRESOS", "ADIC. $"};
        double[] pesos = {14, 26, 26, 14, 10, 10}; // Total 100%

        for (int i = 0; i < headers.length; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(pesos[i]);
            grid.getColumnConstraints().add(col);

            Label h = new Label(headers[i]);
            h.setFont(Font.font("Arial", FontWeight.BOLD, 8));
            h.setPadding(new Insets(6, 2, 6, 2));
            h.setAlignment(Pos.CENTER);
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: #f2f2f2;");
            grid.add(h, i, 0);
        }

        int rowIdx = 1;
        for (CorteDiario item : tablaSesiones.getItems()) {
            grid.add(crearCeldaImp(item.getFechaLabel(), Pos.CENTER_LEFT, Color.BLACK), 0, rowIdx);
            grid.add(crearCeldaImp(item.getSesiones(), Pos.TOP_LEFT, Color.BLACK), 1, rowIdx);
            grid.add(crearCeldaImp(item.getAdicionales(), Pos.TOP_LEFT, Color.BLACK), 2, rowIdx);
            grid.add(crearCeldaImp(item.getPendientes(), Pos.TOP_LEFT, Color.web("#fb923c")), 3, rowIdx);
            grid.add(crearCeldaImp(String.format("$%.2f", item.getSumaSesiones()), Pos.CENTER_RIGHT, Color.web("#22c55e")), 4, rowIdx);
            grid.add(crearCeldaImp(String.format("$%.2f", item.getSumaMedicamentos()), Pos.CENTER_RIGHT, Color.web("#3b82f6")), 5, rowIdx);
            rowIdx++;
        }

        // --- PIE DE PÁGINA ---
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 0, 0, 0));
        Label totalTxt = new Label("INGRESOS TOTALES SEMANALES: " + lblIngresosTotales.getText());
        totalTxt.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        footer.getChildren().add(totalTxt);

        page.getChildren().addAll(header, new Separator(), grid, footer);

        // Escalado final para asegurar que entre en el ancho
        double scale = pWidth / page.getBoundsInParent().getWidth();
        if (scale < 1.0) {
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setTranslateX(- (page.getBoundsInParent().getWidth() - pWidth) / 2);
        }

        if (job.printPage(page)) {
            job.endJob();
        }
    }

    private Node crearCeldaImp(String texto, Pos alineacion, Color color) {
        String contenido = (texto == null || texto.equals("—") || texto.isEmpty()) ? " " : texto;
        Text t = new Text(contenido);
        t.setFont(Font.font("Arial", 7.5));
        t.setFill(color);
        StackPane cell = new StackPane(t);
        cell.setPadding(new Insets(2));
        cell.setAlignment(alineacion);
        t.wrappingWidthProperty().bind(cell.widthProperty().subtract(4));
        return cell;
    }

    private TableColumn<CorteDiario, Double> crearColumnaNumerica(String titulo, String propiedad, Color color, boolean negrita) {
        TableColumn<CorteDiario, Double> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(String.format("$%.2f", item));
                    setTextFill(color);
                    setStyle("-fx-alignment: CENTER-RIGHT;" + (negrita ? "-fx-font-weight: bold;" : ""));
                }
            }
        });
        return col;
    }

    private TableColumn<CorteDiario, String> crearColumnaAjustable(String titulo, String propiedad, Color color) {
        TableColumn<CorteDiario, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        col.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    text.setText(item);
                    text.setFill(color);
                    text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));
                    setGraphic(text);
                }
            }
        });
        return col;
    }

    private Label obtenerLabelValor(VBox card) {
        for (Node n : card.getChildren()) {
            if (n instanceof Label) {
                Label l = (Label) n;
                // Detectamos el label que contiene el dato numérico/monto
                if (l.getFont().getSize() > 18 || l.getText().contains("$") || l.getText().matches("\\d+")) {
                    return l;
                }
            }
        }
        return new Label("0");
    }

    public BorderPane getRoot() { return root; }

    public static class CorteDiario {
        private String fechaLabel, sesiones, adicionales, pendientes;
        private double sumaSesiones, sumaMedicamentos, netoOperativo;

        public CorteDiario(LocalDate f, String s, String a, String p, String dg, String di,
                           double ss, double sm, double g, double inv) {
            this.fechaLabel = f.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase() + " " + f.getDayOfMonth();
            this.sesiones = s;
            this.adicionales = a;
            this.pendientes = p;
            this.sumaSesiones = ss;
            this.sumaMedicamentos = sm;
            this.netoOperativo = ss - g;
        }

        public String getFechaLabel() { return fechaLabel; }
        public String getSesiones() { return sesiones; }
        public String getAdicionales() { return adicionales; }
        public String getPendientes() { return pendientes; }
        public double getSumaSesiones() { return sumaSesiones; }
        public double getSumaMedicamentos() { return sumaMedicamentos; }
        public double getNetoOperativo() { return netoOperativo; }
    }
}