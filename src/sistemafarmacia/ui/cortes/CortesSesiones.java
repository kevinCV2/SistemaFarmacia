package sistemafarmacia.ui.cortes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import sistemafarmacia.utils.UIComponents;
import sistemafarmacia.utils.ConexionDB;

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

        // --- TOP BAR ---
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #374151; -fx-border-radius: 5;");
        btnVolver.setOnAction(e -> { if (this.actionVolver != null) this.actionVolver.run(); });

        VBox headerText = new VBox(5);
        Label title = new Label("Control de Sesiones");
        title.setFont(Font.font("System Bold", 28));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Reporte Semanal de Ingresos, Gastos e Inversiones");
        subtitle.setTextFill(Color.web("#9ca3af"));
        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        // --- STATS PANEL ---
        HBox statsPanel = new HBox(20);
        VBox cardVentas = (VBox) UIComponents.statCard("Ingreso Neto Semanal", "$0.00", "/sistemafarmacia/assets/icons/Ventas2.png");
        lblTotalSemanal = (Label) cardVentas.lookup(".label");
        
        VBox cardSesiones = (VBox) UIComponents.statCard("Total Sesiones", "0", "/sistemafarmacia/assets/icons/Cortes semanales.png");
        lblSesionesTotales = (Label) cardSesiones.lookup(".label");

        HBox.setHgrow(cardVentas, Priority.ALWAYS);
        HBox.setHgrow(cardSesiones, Priority.ALWAYS);
        statsPanel.getChildren().addAll(cardVentas, cardSesiones);

        // --- TOOLBAR ---
        HBox toolbar = createToolbar();

        // --- TABLE ---
        tablaSesiones = createTableSesiones();
        VBox.setVgrow(tablaSesiones, Priority.ALWAYS);

        content.getChildren().addAll(topBar, statsPanel, toolbar, tablaSesiones);
        root.setCenter(content);

        cargarDatos();
    }

    private HBox createToolbar() {
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
        btnPrint.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnPrint.setOnAction(e -> imprimirReporte());

        toolbar.getChildren().addAll(lblSelect, datePickerSemana, spacer, btnPrint);
        return toolbar;
    }

    private TableView<CorteDiario> createTableSesiones() {
        TableView<CorteDiario> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827;");

        TableColumn<CorteDiario, String> colFecha = new TableColumn<>("DÍA / FECHA");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaLabel"));

        TableColumn<CorteDiario, Double> colSes = new TableColumn<>("SESIONES");
        colSes.setCellValueFactory(new PropertyValueFactory<>("sesiones"));

        TableColumn<CorteDiario, Double> colAdic = new TableColumn<>("ADICIONALES");
        colAdic.setCellValueFactory(new PropertyValueFactory<>("adicionales"));

        TableColumn<CorteDiario, Double> colPend = new TableColumn<>("PENDIENTES");
        colPend.setCellValueFactory(new PropertyValueFactory<>("pendientes"));

        TableColumn<CorteDiario, Double> colGastos = new TableColumn<>("GASTOS");
        colGastos.setCellValueFactory(new PropertyValueFactory<>("gastos"));

        TableColumn<CorteDiario, Double> colInv = new TableColumn<>("INV. ADICIONAL");
        colInv.setCellValueFactory(new PropertyValueFactory<>("inversion"));

        TableColumn<CorteDiario, Double> colNeto = new TableColumn<>("TOTAL NETO");
        colNeto.setCellValueFactory(new PropertyValueFactory<>("neto"));
        colNeto.setStyle("-fx-font-weight: bold; -fx-text-fill: #34d399;");

        table.getColumns().addAll(colFecha, colSes, colAdic, colPend, colGastos, colInv, colNeto);
        return table;
    }

    private void cargarDatos() {
        ObservableList<CorteDiario> lista = FXCollections.observableArrayList();
        LocalDate lunes = datePickerSemana.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        double sumaSemana = 0;
        int totalSesiones = 0;

        for (int i = 0; i < 6; i++) { // Lunes a Sábado
            LocalDate dia = lunes.plusDays(i);
            CorteDiario cd = consultarBaseDatos(dia);
            lista.add(cd);
            sumaSemana += cd.getNeto();
            totalSesiones += (int) cd.getSesiones();
        }

        tablaSesiones.setItems(lista);
        lblTotalSemanal.setText(NumberFormat.getCurrencyInstance(Locale.US).format(sumaSemana));
        lblSesionesTotales.setText(String.valueOf(totalSesiones));
    }

    private CorteDiario consultarBaseDatos(LocalDate fecha) {
        // Hemos eliminado las columnas inexistentes y dejado solo 'total'
        // que es la columna estándar para ingresos.
        String sql = """
        SELECT 
            COALESCE(SUM(total), 0) as ingresos, 
            COALESCE(COUNT(*), 0) as num_sesiones 
        FROM tickets 
        WHERE CAST(fecha AS DATE) = ?
    """;

        try (Connection conn = ConexionDB.getInstance(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double ingresos = rs.getDouble("ingresos");
                double numSes = rs.getDouble("num_sesiones");

                // Como las columnas no existen en la DB, enviamos 0.0 a los campos adicionales
                // para que la tabla de la interfaz no se rompa pero el programa no de error.
                return new CorteDiario(
                        fecha,
                        numSes, // Sesiones
                        0.0, // Adicionales (No existen en DB)
                        0.0, // Pendientes (No existen en DB)
                        0.0, // Gastos (No existen en DB)
                        0.0, // Inversion (No existen en DB)
                        ingresos // Neto (Usamos el total de la DB)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CorteDiario(fecha, 0, 0, 0, 0, 0, 0);
    }

    private void imprimirReporte() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
            VBox printable = new VBox(15);
            printable.setPadding(new Insets(30));
            printable.setStyle("-fx-background-color: white;");

            Text header = new Text("REPORTE SEMANAL DE SESIONES - " + 
                datePickerSemana.getValue().getMonth().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase());
            header.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            
            GridPane grid = new GridPane();
            grid.setGridLinesVisible(true);
            
            // Encabezados de columna
            String[] headers = {"CONCEPTO", "LUN", "MAR", "MIE", "JUE", "VIE", "SAB"};
            for (int i = 0; i < headers.length; i++) {
                Label l = new Label(headers[i]);
                l.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-font-size: 10px;");
                l.setPrefWidth(70);
                grid.add(l, i, 0);
            }

            // Filas de conceptos según tu serie solicitada
            String[] conceptos = {"SESIONES", "ADICIONALES", "PENDIENTES", "GASTOS", "INV. ADIC.", "NETO"};
            for (int r = 0; r < conceptos.length; r++) {
                Label lblConcepto = new Label(conceptos[r]);
                lblConcepto.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-font-size: 9px;");
                grid.add(lblConcepto, 0, r + 1);
                
                for (int c = 0; c < 6; c++) {
                    CorteDiario d = tablaSesiones.getItems().get(c);
                    double valor = 0;
                    switch(r) {
                        case 0: valor = d.getSesiones(); break;
                        case 1: valor = d.getAdicionales(); break;
                        case 2: valor = d.getPendientes(); break;
                        case 3: valor = d.getGastos(); break;
                        case 4: valor = d.getInversion(); break;
                        case 5: valor = d.getNeto(); break;
                    }
                    Label v = new Label(String.valueOf(valor));
                    v.setStyle("-fx-padding: 5; -fx-font-size: 9px;");
                    grid.add(v, c + 1, r + 1);
                }
            }

            printable.getChildren().addAll(header, grid);
            
            if (job.printPage(printable)) {
                job.endJob();
            }
        }
    }

    public BorderPane getRoot() { return root; }

    // --- MODELO ACTUALIZADO ---
    public static class CorteDiario {
        private String fechaLabel;
        private double sesiones, adicionales, pendientes, gastos, inversion, neto;

        public CorteDiario(LocalDate fecha, double s, double a, double p, double g, double inv, double n) {
            this.fechaLabel = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es")).toUpperCase() + " " + fecha.getDayOfMonth();
            this.sesiones = s;
            this.adicionales = a;
            this.pendientes = p;
            this.gastos = g;
            this.inversion = inv;
            this.neto = n;
        }

        public String getFechaLabel() { return fechaLabel; }
        public double getSesiones() { return sesiones; }
        public double getAdicionales() { return adicionales; }
        public double getPendientes() { return pendientes; }
        public double getGastos() { return gastos; }
        public double getInversion() { return inversion; }
        public double getNeto() { return neto; }
    }
}