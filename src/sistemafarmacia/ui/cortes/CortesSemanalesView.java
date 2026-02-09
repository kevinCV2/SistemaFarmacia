package sistemafarmacia.ui.cortes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import sistemafarmacia.utils.UIComponents;

import java.time.LocalDate;

public class CortesSemanalesView {

    private BorderPane root;
    private TableView<CorteRenglon> tablaCortes;
    private Runnable actionVolver;

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
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #374151; -fx-border-radius: 5; -fx-border-width: 1;");
        btnVolver.setOnMouseEntered(e -> btnVolver.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #374151; -fx-border-radius: 5; -fx-border-width: 1;"));
        btnVolver.setOnMouseExited(e -> btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #374151; -fx-border-radius: 5; -fx-border-width: 1;"));

        btnVolver.setOnAction(e -> {
            if (this.actionVolver != null) this.actionVolver.run();
        });

        VBox headerText = new VBox(5);
        Label title = new Label("Almacén Digital");
        title.setFont(Font.font("System Bold", 28));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Control de Insumos y Movimientos");
        subtitle.setTextFill(Color.web("#9ca3af"));
        subtitle.setFont(Font.font(14));
        headerText.getChildren().addAll(title, subtitle);

        topBar.getChildren().addAll(btnVolver, headerText);

        // --- STATS PANEL ---
        HBox statsPanel = new HBox(20);
        Region cardVentas = UIComponents.statCard("Ventas Semanales", "$0.00", "/sistemafarmacia/assets/icons/Ventas2.png");
        Region cardCortes = UIComponents.statCard("Cortes Generados", "0", "/sistemafarmacia/assets/icons/Cortes semanales.png");
        HBox.setHgrow(cardVentas, Priority.ALWAYS);
        HBox.setHgrow(cardCortes, Priority.ALWAYS);
        statsPanel.getChildren().addAll(cardVentas, cardCortes);

        // --- TOOLBAR & TABLE ---
        HBox toolbar = createToolbar();
        tablaCortes = createTable();

        VBox.setVgrow(tablaCortes, Priority.ALWAYS);

        content.getChildren().addAll(topBar, statsPanel, toolbar, tablaCortes);
        root.setCenter(content);
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #111827; -fx-padding: 15; -fx-background-radius: 10;");

        Label lblDesde = new Label("Desde:"); lblDesde.setTextFill(Color.WHITE);
        DatePicker dateDesde = new DatePicker(LocalDate.now().minusDays(7));
        Label lblHasta = new Label("Hasta:"); lblHasta.setTextFill(Color.WHITE);
        DatePicker dateHasta = new DatePicker(LocalDate.now());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnGenerar = new Button("Generar Corte Nuevo");
        btnGenerar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");

        toolbar.getChildren().addAll(lblDesde, dateDesde, lblHasta, dateHasta, spacer, btnGenerar);
        return toolbar;
    }

    private TableView<CorteRenglon> createTable() {
        TableView<CorteRenglon> table = new TableView<>();

        // 1. ACTIVAMOS ESTO PARA QUE NO HAYA COLUMNAS VACÍAS A LA DERECHA
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setStyle("-fx-base: #1f2933; -fx-control-inner-background: #111827; -fx-background-color: #111827;");

        // --- LÓGICA DE FILAS (PINTAR SECCIONES) ---
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(CorteRenglon item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isEsSeccion()) {
                    setStyle("-fx-background-color: #1e3a8a; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        // --- DEFINICIÓN DE COLUMNAS CON PESOS ---
        // El truco con CONSTRAINED_RESIZE_POLICY es que "setMaxWidth" funciona como un porcentaje/peso.

        TableColumn<CorteRenglon, String> colNumero = new TableColumn<>("No.");
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colNumero.setMaxWidth(800); // Peso pequeño (aprox 5%)
        colNumero.setStyle("-fx-alignment: CENTER;");

        TableColumn<CorteRenglon, String> colPresentacion = new TableColumn<>("Presentación");
        colPresentacion.setCellValueFactory(new PropertyValueFactory<>("presentacion"));
        colPresentacion.setMaxWidth(2000); // Peso mediano (aprox 15%)
        colPresentacion.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<CorteRenglon, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDescripcion.setMaxWidth(10000); // ¡PESO GIGANTE! (aprox 50%) - Se come el espacio sobrante

        // Renderizado especial para centrar si es título
        colDescripcion.setCellFactory(new Callback<>() {
            @Override
            public TableCell<CorteRenglon, String> call(TableColumn<CorteRenglon, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            setText(item);
                            TableRow<?> row = getTableRow();
                            if (row != null && row.getItem() != null) {
                                CorteRenglon datos = (CorteRenglon) row.getItem();
                                if (datos.isEsSeccion()) {
                                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #93c5fd;");
                                } else {
                                    setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");
                                }
                            }
                        } else {
                            setText(null); setStyle("");
                        }
                    }
                };
            }
        });

        TableColumn<CorteRenglon, String> colExistencia = new TableColumn<>("Existencia");
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colExistencia.setMaxWidth(1000); // Peso pequeño
        colExistencia.setStyle("-fx-alignment: CENTER;");

        TableColumn<CorteRenglon, String> colEntrada = new TableColumn<>("Entrada");
        colEntrada.setCellValueFactory(new PropertyValueFactory<>("entrada"));
        colEntrada.setMaxWidth(1000);
        colEntrada.setStyle("-fx-alignment: CENTER; -fx-text-fill: #4ade80;");

        TableColumn<CorteRenglon, String> colSalida = new TableColumn<>("Salida");
        colSalida.setCellValueFactory(new PropertyValueFactory<>("salida"));
        colSalida.setMaxWidth(1000);
        colSalida.setStyle("-fx-alignment: CENTER; -fx-text-fill: #f87171;");

        TableColumn<CorteRenglon, String> colInvFinal = new TableColumn<>("Inv. Final");
        colInvFinal.setCellValueFactory(new PropertyValueFactory<>("inventarioFinal"));
        colInvFinal.setMaxWidth(1200);
        colInvFinal.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // Agregamos las columnas
        table.getColumns().addAll(colNumero, colPresentacion, colDescripcion,
                colExistencia, colEntrada, colSalida, colInvFinal);

        // --- DATOS DE PRUEBA ---
        table.getItems().add(new CorteRenglon("S  E  S  I  O  N  E  S"));
        table.getItems().add(new CorteRenglon("1", "EJEMPLO", "SOLUCIONES EJEMPLO", "44", "0", "0", "44"));
        table.getItems().add(new CorteRenglon("R E U S O"));
        table.getItems().add(new CorteRenglon("1", "EJEMPLO", "SOLUCIONES EJEMPLO", "44", "0", "0", "44"));
        table.getItems().add(new CorteRenglon("KITS CONEXIÓN / DESCONEXION"));
        table.getItems().add(new CorteRenglon("1", "EJEMPLO", "SOLUCIONES EJEMPLO", "44", "0", "0", "44"));
        table.getItems().add(new CorteRenglon("V E S T I D O R E S"));
        table.getItems().add(new CorteRenglon("1", "EJEMPLO", "SOLUCIONES EJEMPLO", "44", "0", "0", "44"));
        table.getItems().add(new CorteRenglon("CARRO ROJO"));
        table.getItems().add(new CorteRenglon("1", "EJEMPLO", "SOLUCIONES EJEMPLO", "44", "0", "0", "44"));
        table.getItems().add(new CorteRenglon("F I L T R O S"));
        table.getItems().add(new CorteRenglon("1", "EJEMPLO", "SOLUCIONES EJEMPLO", "44", "0", "0", "44"));
        table.getItems().add(new CorteRenglon("SERVICIOS GENERALES"));
        table.getItems().add(new CorteRenglon("1", "EJEMPLO", "SOLUCIONES EJEMPLO", "44", "0", "0", "44"));


        return table;
    }

    public BorderPane getRoot() { return root; }

    // Clase DTO
    public static class CorteRenglon {
        public String numero;
        public String presentacion;
        public String descripcion;
        public String existencia;
        public String entrada;
        public String salida;
        public String inventarioFinal;
        public boolean esSeccion;

        public CorteRenglon(String n, String p, String d, String e, String en, String s, String f) {
            this.numero = n; this.presentacion = p; this.descripcion = d;
            this.existencia = e; this.entrada = en; this.salida = s; this.inventarioFinal = f;
            this.esSeccion = false;
        }

        public CorteRenglon(String nombreSeccion) {
            this.descripcion = nombreSeccion;
            this.numero = ""; this.presentacion = ""; this.existencia = "";
            this.entrada = ""; this.salida = ""; this.inventarioFinal = "";
            this.esSeccion = true;
        }

        // Getters para PropertyValueFactory
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