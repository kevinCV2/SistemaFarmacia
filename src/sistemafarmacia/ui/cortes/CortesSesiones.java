package sistemafarmacia.ui.cortes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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

        // ─── TOP BAR ──────────────────────────────────────────────
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-border-color: #374151; -fx-border-radius: 5;");
        btnVolver.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        VBox headerText = new VBox(5);
        Label title = new Label("Control de Sesiones");
        title.setFont(Font.font("System Bold", 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Reporte Semanal de Ingresos");
        subtitle.setTextFill(Color.web("#9ca3af"));

        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        // ─── STATS ────────────────────────────────────────────────
        HBox statsPanel = new HBox(20);

        VBox cardVentas = (VBox) UIComponents.statCard(
                "Ingreso Neto Semanal", "$0.00",
                "/sistemafarmacia/assets/icons/Ventas2.png"
        );
        lblTotalSemanal = (Label) cardVentas.lookup(".label");

        VBox cardSesiones = (VBox) UIComponents.statCard(
                "Total Sesiones", "0",
                "/sistemafarmacia/assets/icons/Cortes semanales.png"
        );
        lblSesionesTotales = (Label) cardSesiones.lookup(".label");

        statsPanel.getChildren().addAll(cardVentas, cardSesiones);

        // ─── TOOLBAR ──────────────────────────────────────────────
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
        btnPrint.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        btnPrint.setOnAction(e -> imprimirReporte());

        toolbar.getChildren().addAll(lblSelect, datePickerSemana, spacer, btnPrint);

        // ─── TABLE ────────────────────────────────────────────────
        tablaSesiones = new TableView<>();
        tablaSesiones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaSesiones.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827;");

        TableColumn<CorteDiario, String> colFecha = new TableColumn<>("DÍA");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaLabel"));

        TableColumn<CorteDiario, String> colSes = new TableColumn<>("SESIONES");
        colSes.setCellValueFactory(new PropertyValueFactory<>("sesiones"));

        TableColumn<CorteDiario, Double> colAdic = new TableColumn<>("ADICIONALES");
        colAdic.setCellValueFactory(new PropertyValueFactory<>("adicionales"));

        TableColumn<CorteDiario, Double> colPend = new TableColumn<>("PENDIENTES");
        colPend.setCellValueFactory(new PropertyValueFactory<>("pendientes"));

        TableColumn<CorteDiario, Double> colGastos = new TableColumn<>("GASTOS");
        colGastos.setCellValueFactory(new PropertyValueFactory<>("gastos"));

        TableColumn<CorteDiario, Double> colInversion = new TableColumn<>("INV. ADICIONAL");
        colInversion.setCellValueFactory(new PropertyValueFactory<>("inversionAdicional"));

        TableColumn<CorteDiario, Double> colNeto = new TableColumn<>("NETO");
        colNeto.setCellValueFactory(new PropertyValueFactory<>("neto"));
        colNeto.setStyle("-fx-font-weight: bold; -fx-text-fill: #34d399;");

        tablaSesiones.getColumns().addAll(
                colFecha, colSes, colAdic, colPend, colGastos, colInversion, colNeto
        );

        VBox.setVgrow(tablaSesiones, Priority.ALWAYS);

        content.getChildren().addAll(topBar, statsPanel, toolbar, tablaSesiones);
        root.setCenter(content);

        cargarDatos();
    }

    // ─── CARGA DE DATOS ──────────────────────────────────────────
    private void cargarDatos() {
        ObservableList<CorteDiario> lista = FXCollections.observableArrayList();
        LocalDate lunes = datePickerSemana.getValue()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        double sumaSemana = 0;
        int totalSesiones = 0;

        for (int i = 0; i < 6; i++) {
            LocalDate dia = lunes.plusDays(i);
            CorteDiario cd = consultarBaseDatos(dia);
            lista.add(cd);
            sumaSemana += cd.getNeto();
            if (!cd.getSesiones().equals("—")) totalSesiones++;
        }

        tablaSesiones.setItems(lista);
        lblTotalSemanal.setText(NumberFormat.getCurrencyInstance(Locale.US).format(sumaSemana));
        lblSesionesTotales.setText(String.valueOf(totalSesiones));
    }

    // ─── CONSULTA BD ─────────────────────────────────────────────
    private CorteDiario consultarBaseDatos(LocalDate fecha) {

        String sql = """
            SELECT
                STRING_AGG(paciente || ' - ' || consulta, '\n') AS sesiones,
                COALESCE(SUM(total), 0) AS ingresos,
                COALESCE(SUM(CASE WHEN estado_pago = 'CREDITO' THEN total ELSE 0 END), 0) AS pendientes
            FROM sesiones
            WHERE CAST(fecha AS DATE) = ?
        """;

        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new CorteDiario(
                        fecha,
                        rs.getString("sesiones") != null ? rs.getString("sesiones") : "—",
                        0.0, // adicionales
                        rs.getDouble("pendientes"),
                        0.0, // gastos
                        0.0, // inversion adicional
                        rs.getDouble("ingresos")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new CorteDiario(fecha, "—", 0, 0, 0, 0, 0);
    }

    // ─── IMPRESIÓN ───────────────────────────────────────────────
    private void imprimirReporte() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
            job.endJob();
        }
    }

    public BorderPane getRoot() { return root; }

    // ─── MODELO ─────────────────────────────────────────────────
    public static class CorteDiario {

        private String fechaLabel;
        private String sesiones;
        private double adicionales;
        private double pendientes;
        private double gastos;
        private double inversionAdicional;
        private double neto;

        public CorteDiario(LocalDate fecha, String sesiones, double adicionales,
                           double pendientes, double gastos, double inversionAdicional, double neto) {

            this.fechaLabel = fecha.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, new Locale("es"))
                    .toUpperCase() + " " + fecha.getDayOfMonth();

            this.sesiones = sesiones;
            this.adicionales = adicionales;
            this.pendientes = pendientes;
            this.gastos = gastos;
            this.inversionAdicional = inversionAdicional;
            this.neto = neto;
        }

        public String getFechaLabel() { return fechaLabel; }
        public String getSesiones() { return sesiones; }
        public double getAdicionales() { return adicionales; }
        public double getPendientes() { return pendientes; }
        public double getGastos() { return gastos; }
        public double getInversionAdicional() { return inversionAdicional; }
        public double getNeto() { return neto; }
    }
}