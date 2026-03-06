package sistemafarmacia.ui.ticket;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.print.*;
import javafx.scene.transform.Scale;
import sistemafarmacia.utils.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class GenerarTicketView {

    private BorderPane root;
    private VBox ticketPaper;
    private Runnable actionVolver;

    private Label lblTicketDireccion;
    private Label lblTicketNumero;
    private Label lblTicketPacienteValor;
    private Label lblTicketIdValor;
    private Label lblTicketMetodoPago; 
    private VBox contenedorTicketProductos;
    private Label lblTicketTotalNum;
    private Label lblTicketFechaValor;

    private TextField txtIdTicket;
    private TextField txtDireccion;
    private TextField txtNumero;
    private TextField txtPaciente;
    private ComboBox<String> comboMetodoPago; // Nuevo selector
    private VBox contenedorSesionesFormulario;

    private static int contadorTicket = 1;

    private final String DIRECCION_FIJA = "VALLE EMBRUJADO NO.131, ESQ. SANTA ANA\nFRACCIONAMIENTO VALLE DE SAN JAVIER C.P.42086\nPACHUCA DE SOTO, HIDALGO.";
    private final String TELEFONO_FIJO = "771 377 81 07 / 771 102 7324";

    public GenerarTicketView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button btnVolver = new Button("⬅ Regresar");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #374151; -fx-border-radius: 5; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> { if (this.actionVolver != null) this.actionVolver.run(); });
        
        Label title = new Label("Generador de Tickets");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: white;");
        topBar.getChildren().addAll(btnVolver, title);
        root.setTop(topBar);

        HBox splitLayout = new HBox(50);
        splitLayout.setPadding(new Insets(20, 20, 20, 20));
        splitLayout.setAlignment(Pos.TOP_CENTER);

        VBox panelDerecho = crearPanelTicket();
        panelDerecho.setMaxWidth(600);
        VBox panelIzquierdo = crearPanelFormulario();
        panelIzquierdo.setMaxWidth(600);

        splitLayout.getChildren().addAll(panelIzquierdo, panelDerecho);
        root.setCenter(splitLayout);

        obtenerUltimoFolioYDatosCompletos();
    }

    private void obtenerUltimoFolioYDatosCompletos() {
        if (contenedorSesionesFormulario != null) {
            contenedorSesionesFormulario.getChildren().clear();
        }

        try (Connection conn = ConexionDB.getInstance(); Statement stmt = conn.createStatement()) {
            ResultSet rsF = stmt.executeQuery("SELECT MAX(id_ticket) FROM public.tickets");
            if (rsF.next()) contadorTicket = rsF.getInt(1) + 1;
            
            String f = String.format("%06d", contadorTicket);
            txtIdTicket.setText(f);
            lblTicketIdValor.setText(f);

            ResultSet rsS = stmt.executeQuery("SELECT paciente, estado_pago FROM public.sesiones ORDER BY id_sesion DESC LIMIT 1");

            if (rsS.next()) {
                String nombrePaciente = rsS.getString("paciente");
                String metodoCargado = rsS.getString("estado_pago").toUpperCase();
                
                txtPaciente.setText(nombrePaciente);
                comboMetodoPago.setValue(metodoCargado); // Sincroniza el combo con la DB
                lblTicketMetodoPago.setText(metodoCargado);

                String sqlHistorial = "SELECT consulta, total, fecha, estado_pago FROM public.sesiones " +
                                      "WHERE paciente = ? AND estado_pago <> 'SALDADO' " +
                                      "ORDER BY fecha ASC";
                
                try (PreparedStatement psH = conn.prepareStatement(sqlHistorial)) {
                    psH.setString(1, nombrePaciente);
                    ResultSet rsH = psH.executeQuery();
                    while (rsH.next()) {
                        String descBase = rsH.getString("consulta").toUpperCase();
                        double monto = rsH.getDouble("total");
                        java.sql.Date fechaDB = rsH.getDate("fecha");
                        String fechaStr = (fechaDB != null) ? fechaDB.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "S/F";
                        
                        String prefijo = rsH.getString("estado_pago").equalsIgnoreCase("CREDITO") ? "ADEUDO: " : "";
                        agregarFilaSesionDinamica(prefijo + descBase + " (" + fechaStr + ")", monto);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox crearPanelFormulario() {
        VBox panelPrincipal = new VBox(20);
        VBox contenidoScroll = new VBox(20);
        contenidoScroll.setPadding(new Insets(0, 10, 0, 0));

        VBox cardDatos = new VBox(15);
        cardDatos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");
        
        txtIdTicket = crearTextField("Folio"); txtIdTicket.setEditable(false);
        txtDireccion = crearTextField(""); txtDireccion.setText(DIRECCION_FIJA);
        txtDireccion.textProperty().addListener((obs, old, nv) -> lblTicketDireccion.setText(nv));
        
        txtNumero = crearTextField(""); txtNumero.setText(TELEFONO_FIJO);
        txtNumero.textProperty().addListener((obs, old, nv) -> lblTicketNumero.setText(nv.isEmpty() ? "" : "TEL. " + nv));
        
        txtPaciente = crearTextField("Nombre del Paciente");
        txtPaciente.textProperty().addListener((obs, old, nv) -> lblTicketPacienteValor.setText(nv.isEmpty() ? "MOSTRADOR" : nv.toUpperCase()));

        // --- SELECTOR DE MÉTODO DE PAGO ---
        comboMetodoPago = new ComboBox<>();
        comboMetodoPago.getItems().addAll("EFECTIVO", "TRANSFERENCIA", "FACTURA", "CREDITO");
        comboMetodoPago.setValue("EFECTIVO");
        comboMetodoPago.setMaxWidth(Double.MAX_VALUE);
        comboMetodoPago.setStyle("-fx-background-color: #1f2933; -fx-text-fill: white; -fx-border-color: #374151; -fx-padding: 5;");
        
        // Listener para actualizar el ticket visualmente
        comboMetodoPago.valueProperty().addListener((obs, old, nv) -> {
            if(nv != null) lblTicketMetodoPago.setText(nv);
        });

        cardDatos.getChildren().addAll(
            new Label("Datos de la Venta") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;"); }},
            new Label("Folio") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtIdTicket,
            new Label("Paciente") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtPaciente,
            new Label("Forma de Pago") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, comboMetodoPago,
            new Label("Dirección") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtDireccion,
            new Label("Teléfono") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtNumero
        );

        VBox cardSesiones = new VBox(15);
        cardSesiones.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");
        contenedorSesionesFormulario = new VBox(10);
        
        Button btnAddAbono = new Button("💰 + Añadir Abono");
        btnAddAbono.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAddAbono.setOnAction(e -> abrirModalAbonoManual());

        HBox headerSesiones = new HBox(new Label("Servicios y Deudas") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); }}, new Region(){{HBox.setHgrow(this, Priority.ALWAYS);}}, btnAddAbono);

        cardSesiones.getChildren().addAll(headerSesiones, contenedorSesionesFormulario);

        contenidoScroll.getChildren().addAll(cardDatos, cardSesiones);
        ScrollPane scroll = new ScrollPane(contenidoScroll);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        
        Button btnImprimir = new Button("🖨 Guardar e Imprimir ticket");
        btnImprimir.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-background-radius: 8; -fx-cursor: hand;");
        btnImprimir.setMaxWidth(Double.MAX_VALUE);
        btnImprimir.setOnAction(e -> imprimirTicket());

        panelPrincipal.getChildren().addAll(scroll, btnImprimir);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return panelPrincipal;
    }

    private void agregarFilaSesionDinamica(String desc, double costo) {
        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);

        TextField tTipo = crearTextField("Descripción");
        tTipo.setText(desc);
        tTipo.setEditable(false); 
        HBox.setHgrow(tTipo, Priority.ALWAYS);

        TextField tCosto = crearTextField("Costo");
        tCosto.setText(String.format("%.2f", costo));
        tCosto.setEditable(false); 
        tCosto.setPrefWidth(100);

        Button btnQuitar = new Button("✕");
        btnQuitar.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-cursor: hand;");
        btnQuitar.setOnAction(e -> {
            contenedorSesionesFormulario.getChildren().remove(fila);
            actualizarTablaTicket();
        });

        fila.getChildren().addAll(tTipo, tCosto, btnQuitar);
        contenedorSesionesFormulario.getChildren().add(fila);
        actualizarTablaTicket();
    }

    private void actualizarTablaTicket() {
        if (contenedorTicketProductos == null || lblTicketTotalNum == null) return;
        contenedorTicketProductos.getChildren().clear();
        double granTotal = 0.0;

        for (Node n : contenedorSesionesFormulario.getChildren()) {
            if (!(n instanceof HBox)) continue;
            HBox f = (HBox) n;
            String desc = ((TextField) f.getChildren().get(0)).getText().trim();
            double costo = 0.0;
            try { costo = Double.parseDouble(((TextField) f.getChildren().get(1)).getText()); } catch (Exception e) {}
            granTotal += costo;
            contenedorTicketProductos.getChildren().add(crearFilaTicketUI(desc, 1, costo));
        }
        lblTicketTotalNum.setText(String.format("$%.2f", Math.max(0, granTotal)));
    }

    private void construirDisenoTicket() {
        ticketPaper.getChildren().clear();
        ticketPaper.setAlignment(Pos.TOP_CENTER);
        ticketPaper.setMinWidth(360);
        ticketPaper.setMaxWidth(360);
        
        ImageView logo = new ImageView();
        try { 
            logo.setImage(new Image(getClass().getResourceAsStream("/sistemafarmacia/assets/icons/ticket.jpeg"))); 
            logo.setFitWidth(180); logo.setPreserveRatio(true); 
        } catch(Exception e){}

        Label lblNombre = new Label("UNIDAD DE HEMODIÁLISIS\nINTEGRAL SAN RAFAEL");
        lblNombre.setFont(Font.font("Courier New", FontWeight.BOLD, 17));
        lblNombre.setTextAlignment(TextAlignment.CENTER);
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-text-fill: black;");

        lblTicketDireccion = new Label(DIRECCION_FIJA); 
        lblTicketDireccion.setFont(Font.font("Courier New", FontWeight.BOLD, 11)); 
        lblTicketDireccion.setTextAlignment(TextAlignment.CENTER);
        lblTicketDireccion.setWrapText(true);
        lblTicketDireccion.setStyle("-fx-text-fill: black;");

        lblTicketNumero = new Label("TEL. " + TELEFONO_FIJO);
        lblTicketNumero.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        lblTicketNumero.setStyle("-fx-text-fill: black;");

        VBox bTicket = crearBloqueInfo("No. TICKET:", "000000", 14, 20);
        lblTicketIdValor = (Label) bTicket.getChildren().get(1);
        VBox bFecha = crearBloqueInfo("FECHA DE EMISIÓN:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 14, 20);
        lblTicketFechaValor = (Label) bFecha.getChildren().get(1);
        VBox bPaciente = crearBloqueInfo("PACIENTE / CLIENTE:", "MOSTRADOR", 14, 20);
        lblTicketPacienteValor = (Label) bPaciente.getChildren().get(1);

        VBox bMetodoPago = new VBox(2); bMetodoPago.setAlignment(Pos.CENTER);
        Label lblMetodoTitulo = new Label("MÉTODO DE PAGO:"); lblMetodoTitulo.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        lblTicketMetodoPago = new Label("EFECTIVO");
        lblTicketMetodoPago.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
        lblTicketMetodoPago.setStyle("-fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2; -fx-padding: 3 15;"); 
        bMetodoPago.getChildren().addAll(lblMetodoTitulo, lblTicketMetodoPago);

        contenedorTicketProductos = new VBox(5);
        lblTicketTotalNum = new Label("$0.00");
        lblTicketTotalNum.setFont(Font.font("Courier New", FontWeight.BOLD, 22)); 
        lblTicketTotalNum.setStyle("-fx-text-fill: black;");

        ticketPaper.getChildren().addAll(
            logo, lblNombre, new Label(""), lblTicketDireccion, lblTicketNumero, separador(),
            bTicket, new Label(""), bFecha, new Label(""), bPaciente, new Label(""), bMetodoPago, 
            separador(),
            new HBox() {{ getChildren().addAll(
                new Label("DESCRIPCIÓN"){{setPrefWidth(220); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 12));}}, 
                new Label("COSTO"){{setPrefWidth(100); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 12));}}); 
            }},
            separador(), 
            contenedorTicketProductos, 
            separador(),
            new HBox(new Label("TOTAL A PAGAR:"){{setFont(Font.font("Courier New", FontWeight.BOLD, 18)); setStyle("-fx-text-fill: black;");}}, new Region(){{HBox.setHgrow(this, Priority.ALWAYS);}}, lblTicketTotalNum),
            new Label("\n¡Gracias por su confianza!"){{setFont(Font.font("Courier New", FontWeight.BOLD, 13)); setStyle("-fx-text-fill: black;");}}
        );
    }

    private void abrirModalAbonoManual() {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Registrar Abono");
        dialog.setHeaderText("Abono al Saldo Actual");
        dialog.setContentText("Monto a abonar ($):");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(montoStr -> {
            try {
                double monto = Double.parseDouble(montoStr);
                if (monto > 0) {
                    agregarFilaSesionDinamica("ABONO RECIBIDO (" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")", -monto);
                }
            } catch (Exception e) {}
        });
    }

    private boolean guardarTicketEnBD() {
        String insertTicket = "INSERT INTO public.tickets (folio, fecha, paciente, direccion, telefono, total, metodo_pago) VALUES (?, NOW(), ?, ?, ?, ?, ?) RETURNING id_ticket";
        String insertDetalle = "INSERT INTO public.ticket_detalles (id_ticket, producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getInstance()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psTicket = conn.prepareStatement(insertTicket)) {
                psTicket.setString(1, txtIdTicket.getText());
                psTicket.setString(2, txtPaciente.getText().toUpperCase());
                psTicket.setString(3, txtDireccion.getText());
                psTicket.setString(4, txtNumero.getText());
                double totalVal = Double.parseDouble(lblTicketTotalNum.getText().replace("$", "").trim());
                psTicket.setDouble(5, totalVal);
                psTicket.setString(6, comboMetodoPago.getValue()); // Guardar método seleccionado

                ResultSet rs = psTicket.executeQuery();
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    try (PreparedStatement psDetalle = conn.prepareStatement(insertDetalle)) {
                        for (Node n : contenedorSesionesFormulario.getChildren()) {
                            if (!(n instanceof HBox)) continue;
                            HBox fila = (HBox) n;
                            String desc = ((TextField) fila.getChildren().get(0)).getText().trim();
                            double costo = Double.parseDouble(((TextField) fila.getChildren().get(1)).getText().trim());
                            psDetalle.setInt(1, idGenerado);
                            psDetalle.setString(2, desc);
                            psDetalle.setInt(3, 1);
                            psDetalle.setDouble(4, costo);
                            psDetalle.setDouble(5, costo);
                            psDetalle.addBatch();
                        }
                        psDetalle.executeBatch();
                    }
                }
                conn.commit(); return true;
            } catch (Exception e) { conn.rollback(); e.printStackTrace(); return false; }
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private void imprimirTicket() {
        if (contenedorSesionesFormulario.getChildren().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No hay servicios registrados.").show();
            return;
        }
        if (guardarTicketEnBD()) {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
                PageLayout pl = job.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
                ticketPaper.setEffect(null);
                double scale = pl.getPrintableWidth() / ticketPaper.getWidth();
                Scale s = new Scale(scale, scale);
                ticketPaper.getTransforms().add(s);
                if (job.printPage(pl, ticketPaper)) { job.endJob(); }
                ticketPaper.getTransforms().remove(s);
                ticketPaper.setEffect(new DropShadow(15, Color.color(0, 0, 0, 0.5)));
                obtenerUltimoFolioYDatosCompletos();
                new Alert(Alert.AlertType.INFORMATION, "Ticket generado correctamente.").show();
            }
        }
    }

    private HBox crearFilaTicketUI(String nombre, int cant, double total) {
        HBox row = new HBox(); row.setAlignment(Pos.TOP_LEFT); row.setPadding(new Insets(2, 0, 2, 0));
        Label lN = new Label(nombre.toUpperCase()); lN.setPrefWidth(220); lN.setWrapText(true);
        lN.setFont(Font.font("Courier New", FontWeight.BOLD, 11)); lN.setStyle("-fx-text-fill: black;");
        Label lP = new Label(String.format("$%.2f", total)); lP.setPrefWidth(100); lP.setAlignment(Pos.TOP_RIGHT); 
        lP.setFont(Font.font("Courier New", FontWeight.BOLD, 12)); lP.setStyle("-fx-text-fill: black;");
        row.getChildren().addAll(lN, lP);
        return row;
    }

    private VBox crearPanelTicket() {
        VBox contenedor = new VBox(); contenedor.setAlignment(Pos.TOP_CENTER);
        ticketPaper = new VBox(8); ticketPaper.setPadding(new Insets(40, 40, 40, 40));
        ticketPaper.setStyle("-fx-background-color: white;");
        ticketPaper.setMaxWidth(420);
        ticketPaper.setEffect(new DropShadow(15, Color.color(0,0,0,0.5)));
        construirDisenoTicket();
        ScrollPane scroll = new ScrollPane(ticketPaper);
        scroll.setStyle("-fx-background: #1f2933; -fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setFitToWidth(true);
        contenedor.getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return contenedor;
    }

    private TextField crearTextField(String p) {
        TextField t = new TextField(); t.setPromptText(p);
        t.setStyle("-fx-background-color: #1f2933; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; -fx-border-color: #374151; -fx-border-radius: 5; -fx-padding: 8;");
        return t;
    }

    private VBox crearBloqueInfo(String e, String v, int sizeE, int sizeV) {
        VBox b = new VBox(0); b.setAlignment(Pos.CENTER);
        Label lE = new Label(e); lE.setFont(Font.font("Courier New", FontWeight.BOLD, sizeE)); lE.setStyle("-fx-text-fill: black;");
        Label lV = new Label(v); lV.setFont(Font.font("Courier New", FontWeight.BOLD, sizeV)); lV.setStyle("-fx-text-fill: black;");
        b.getChildren().addAll(lE, lV); return b;
    }

    private Label separador() {
        return new Label("----------------------------------------") {{ 
            setFont(Font.font("Courier New", FontWeight.BOLD, 12)); setStyle("-fx-text-fill: black;"); 
        }};
    }

    public BorderPane getRoot() { return root; }
}