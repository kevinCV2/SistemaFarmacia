package sistemafarmacia.ui.ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import sistemafarmacia.utils.ConexionDB;

public class GenerarTicketAdicionalView {

    private BorderPane root;
    private VBox ticketPaper;
    private Runnable actionVolver;
    private Label lblTicketDireccion;
    private Label lblTicketNumero;
    private Label lblTicketPacienteValor;
    private Label lblTicketIdValor;
    private Label lblTicketFechaValor; 
    private Label lblTicketMetodoPago;
    private VBox contenedorTicketProductos;
    private Label lblTicketTotalNum;
    private TextField txtIdTicket;
    private TextField txtDireccion;
    private TextField txtNumero;
    private TextField txtPaciente;
    private VBox contenedorProductosFormulario;
    private VBox contenedorSesionesFormulario;
    private static int contadorTicket = 1;

    // --- DATOS FIJOS ---
    private final String DIRECCION_FIJA = "VALLE EMBRUJADO NO.131, ESQ. SANTA ANA\nFRACCIONAMIENTO VALLE DE SAN JAVIER C.P.42086\nPACHUCA DE SOTO, HIDALGO.";
    private final String TELEFONO_FIJO = "771 377 81 07 / 771 102 7324";

    public GenerarTicketAdicionalView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");
        
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Button btnVolver = new Button("⬅ Regresar");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #374151; -fx-border-radius: 5; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> {
            if (this.actionVolver != null) this.actionVolver.run();
        });

        Label title = new Label("Generador de Tickets");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: white;");
        topBar.getChildren().addAll(btnVolver, title);
        root.setTop(topBar);

        HBox splitLayout = new HBox(50);
        splitLayout.setPadding(new Insets(20, 20, 20, 20));
        splitLayout.setAlignment(Pos.TOP_CENTER);

        VBox panelDerecho = crearPanelTicket();
        panelDerecho.setMaxWidth(450);

        VBox panelIzquierdo = crearPanelFormulario();
        panelIzquierdo.setMaxWidth(600);

        splitLayout.getChildren().addAll(panelIzquierdo, panelDerecho);
        root.setCenter(splitLayout);

        obtenerUltimoFolioYDatosCompletos();
    }

    private VBox crearPanelFormulario() {
        VBox panelPrincipal = new VBox(20);
        VBox contenidoScroll = new VBox(20);
        contenidoScroll.setPadding(new Insets(0, 10, 0, 0));

        VBox cardDatos = new VBox(15);
        cardDatos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");
        
        txtIdTicket = crearTextField("Folio");
        txtIdTicket.setEditable(false);
        
        txtDireccion = crearTextField("");
        txtDireccion.setText(DIRECCION_FIJA);
        txtDireccion.textProperty().addListener((obs, old, nv) -> lblTicketDireccion.setText(nv));
        
        txtNumero = crearTextField("");
        txtNumero.setText(TELEFONO_FIJO);
        txtNumero.textProperty().addListener((obs, old, nv) -> lblTicketNumero.setText(nv.isEmpty() ? "" : "TEL: " + nv));
        
        txtPaciente = crearTextField("Nombre del Paciente");
        txtPaciente.textProperty().addListener((obs, old, nv) -> lblTicketPacienteValor.setText(nv.isEmpty() ? "MOSTRADOR" : nv.toUpperCase()));

        cardDatos.getChildren().addAll(
            new Label("Datos de la Venta") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;"); }},
            new Label("Folio") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtIdTicket,
            new Label("Dirección") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtDireccion,
            new Label("Teléfono") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtNumero,
            new Label("Paciente") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtPaciente
        );

        VBox cardProductos = new VBox(15);
        cardProductos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");
        contenedorProductosFormulario = new VBox(10);
        Button btnAddProd = new Button("+ Agregar Producto/Insumo");
        btnAddProd.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-cursor: hand;");
        btnAddProd.setOnAction(e -> agregarFilaProductoDinamica("", 1, 0.0));
        cardProductos.getChildren().addAll(new HBox(new Label("Artículos e Insumos") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnAddProd), contenedorProductosFormulario);

        VBox cardSesiones = new VBox(15);
        cardSesiones.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");
        contenedorSesionesFormulario = new VBox(10);
        Button btnAddSesion = new Button("+ Agregar Servicio");
        btnAddSesion.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-cursor: hand;");
        btnAddSesion.setOnAction(e -> {
            String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            agregarFilaSesionDinamica("", 0.0, fechaHoy);
        });
        cardSesiones.getChildren().addAll(new HBox(new Label("Servicios y Sesiones") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnAddSesion), contenedorSesionesFormulario);

        contenidoScroll.getChildren().addAll(cardDatos, cardProductos, cardSesiones);
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

    private void construirDisenoTicket() {
        ticketPaper.getChildren().clear();
        ticketPaper.setAlignment(Pos.TOP_CENTER);
        
        // --- AJUSTE POS-58 ---
        ticketPaper.setMinWidth(380);
        ticketPaper.setMaxWidth(380);

        ImageView logo = new ImageView();
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/sistemafarmacia/assets/icons/ticket.jpeg")));
            logo.setFitWidth(240);
            logo.setPreserveRatio(true);
        } catch (Exception e) {}

        Label lblNombre = new Label("UNIDAD DE HEMODIÁLISIS\nINTEGRAL SAN RAFAEL");
        lblNombre.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        lblNombre.setTextAlignment(TextAlignment.CENTER);
        lblNombre.setWrapText(true);
        lblNombre.setMaxWidth(360);
        lblNombre.setStyle("-fx-text-fill: black;");

        lblTicketDireccion = new Label(DIRECCION_FIJA);
        lblTicketDireccion.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        lblTicketDireccion.setTextAlignment(TextAlignment.CENTER);
        lblTicketDireccion.setWrapText(true);
        lblTicketDireccion.setMaxWidth(360);
        lblTicketDireccion.setStyle("-fx-text-fill: black;");

        lblTicketNumero = new Label("TEL: " + TELEFONO_FIJO);
        lblTicketNumero.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        lblTicketNumero.setTextAlignment(TextAlignment.CENTER);
        lblTicketNumero.setStyle("-fx-text-fill: black;");

        VBox bTicket = crearBloqueInfo("No. TICKET:", "000000", 14, 20);
        lblTicketIdValor = (Label) bTicket.getChildren().get(1);

        VBox bFecha = crearBloqueInfo("FECHA DE EMISIÓN:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 14, 20);
        lblTicketFechaValor = (Label) bFecha.getChildren().get(1);

        VBox bPaciente = crearBloqueInfo("PACIENTE / CLIENTE:", "MOSTRADOR", 14, 20);
        lblTicketPacienteValor = (Label) bPaciente.getChildren().get(1);

        VBox bMetodoPago = new VBox(2);
        bMetodoPago.setAlignment(Pos.CENTER);
        Label lblMetodoTitulo = new Label("MÉTODO DE PAGO:");
        lblMetodoTitulo.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        lblMetodoTitulo.setStyle("-fx-text-fill: black;");
        lblTicketMetodoPago = new Label("EFECTIVO");
        lblTicketMetodoPago.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
        lblTicketMetodoPago.setStyle("-fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2; -fx-padding: 3 15;");
        bMetodoPago.getChildren().addAll(lblMetodoTitulo, lblTicketMetodoPago);

        contenedorTicketProductos = new VBox(8);
        lblTicketTotalNum = new Label("$0.00");
        lblTicketTotalNum.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        lblTicketTotalNum.setStyle("-fx-text-fill: black;");

        ticketPaper.getChildren().addAll(
            logo, lblNombre, new Label(""), lblTicketDireccion, lblTicketNumero, separador(),
            bTicket, new Label(""), bFecha, new Label(""), bPaciente, new Label(""), 
            bMetodoPago, separador(), 
            new HBox() {{
                getChildren().addAll(
                    new Label("DESC.") {{ setPrefWidth(180); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 13)); }},
                    new Label("CT") {{ setPrefWidth(40); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 13)); }},
                    new Label("COSTO") {{ setPrefWidth(100); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 13)); }}
                );
            }},
            separador(), contenedorTicketProductos, separador(),
            new HBox(new Label("TOTAL:") {{ setFont(Font.font("Courier New", FontWeight.BOLD, 22)); setStyle("-fx-text-fill: black;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, lblTicketTotalNum),
            new Label("\n¡Gracias por su confianza!") {{ setFont(Font.font("Courier New", FontWeight.BOLD, 14)); setStyle("-fx-text-fill: black;"); }}
        );
    }

    private void obtenerUltimoFolioYDatosCompletos() {
        if (contenedorProductosFormulario != null) contenedorProductosFormulario.getChildren().clear();
        if (contenedorSesionesFormulario != null) contenedorSesionesFormulario.getChildren().clear();

        try (Connection conn = ConexionDB.getInstance(); Statement stmt = conn.createStatement()) {
            ResultSet rsF = stmt.executeQuery("SELECT MAX(id_ticket) FROM public.tickets");
            if (rsF.next()) {
                contadorTicket = rsF.getInt(1) + 1;
            }
            String f = String.format("%06d", contadorTicket);
            txtIdTicket.setText(f);
            lblTicketIdValor.setText(f);

            ResultSet rsS = stmt.executeQuery("SELECT * FROM public.sesiones ORDER BY id_sesion DESC LIMIT 1");
            if (rsS.next()) {
                txtPaciente.setText(rsS.getString("paciente"));
                java.sql.Date fechaDB = rsS.getDate("fecha");
                String fechaStr = (fechaDB != null) ? fechaDB.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                if (!fechaStr.isEmpty()) { lblTicketFechaValor.setText(fechaStr); }

                String met = rsS.getString("estado_pago");
                lblTicketMetodoPago.setText(met != null ? met.toUpperCase() : "EFECTIVO");

                agregarFilaSesionDinamica(rsS.getString("consulta").toUpperCase(), rsS.getDouble("total"), fechaStr);
                
                String medsStr = rsS.getString("medicamentos");
                if (medsStr != null && !medsStr.isEmpty()) {
                    for (String m : medsStr.split(",")) {
                        String nLimpio = m.trim();
                        if (!nLimpio.isEmpty()) {
                            double precioEncontrado = buscarPrecioCualquierTabla(nLimpio);
                            agregarFilaProductoDinamica(nLimpio.toUpperCase(), 1, precioEncontrado);
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void imprimirTicket() {
        if (contenedorTicketProductos.getChildren().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No hay productos o servicios.").show();
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
                new Alert(Alert.AlertType.INFORMATION, "Ticket guardado.").show();
            }
        }
    }

    private void agregarFilaProductoDinamica(String nombre, int cant, double precio) {
        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);
        TextField tNom = crearTextField("Producto");
        tNom.setText(nombre);
        HBox.setHgrow(tNom, Priority.ALWAYS);
        TextField tCan = crearTextField("Cant");
        tCan.setPrefWidth(50);
        tCan.setText(String.valueOf(cant));
        TextField tPre = crearTextField("Precio");
        tPre.setPrefWidth(80);
        tPre.setText(String.valueOf(precio));

        ContextMenu suggestionsMenu = new ContextMenu();
        tNom.textProperty().addListener((obs, old, nv) -> {
            if (nv.isEmpty()) { suggestionsMenu.hide(); return; }
            suggestionsMenu.getItems().clear();
            String sql = "SELECT nombre, precio FROM (SELECT nombre, 0.0 AS precio FROM public.medicamentos UNION ALL SELECT nombre, precio FROM public.insumos) AS todo WHERE nombre ILIKE ? LIMIT 10";
            try (Connection conn = ConexionDB.getInstance(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + nv + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String n = rs.getString("nombre");
                    double p = rs.getDouble("precio");
                    MenuItem item = new MenuItem(n + " ($" + p + ")");
                    item.setOnAction(e -> { tNom.setText(n); tPre.setText(String.valueOf(p)); actualizarTablaTicket(); suggestionsMenu.hide(); });
                    suggestionsMenu.getItems().add(item);
                }
                if (!suggestionsMenu.getItems().isEmpty()) suggestionsMenu.show(tNom, Side.BOTTOM, 0, 0);
                else suggestionsMenu.hide();
            } catch (Exception ex) {}
        });

        Button btnDel = new Button("×");
        btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDel.setOnAction(e -> { contenedorProductosFormulario.getChildren().remove(fila); actualizarTablaTicket(); });

        tNom.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        tCan.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        tPre.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());

        fila.getChildren().addAll(tNom, tCan, tPre, btnDel);
        contenedorProductosFormulario.getChildren().add(fila);
        actualizarTablaTicket();
    }

    private void actualizarTablaTicket() {
        if (contenedorTicketProductos == null || lblTicketTotalNum == null) return;
        contenedorTicketProductos.getChildren().clear();
        double granTotal = 0.0;
        for (Node n : contenedorProductosFormulario.getChildren()) {
            if (!(n instanceof HBox)) continue;
            HBox f = (HBox) n;
            String nom = ((TextField) f.getChildren().get(0)).getText().trim();
            if (nom.isEmpty()) continue;
            int can = 1; double pre = 0.0;
            try { can = Integer.parseInt(((TextField) f.getChildren().get(1)).getText()); } catch (Exception e) {}
            try { pre = Double.parseDouble(((TextField) f.getChildren().get(2)).getText()); } catch (Exception e) {}
            double subtotal = can * pre;
            granTotal += subtotal;
            contenedorTicketProductos.getChildren().add(crearFilaTicketUI(nom, can, subtotal));
        }
        for (Node n : contenedorSesionesFormulario.getChildren()) {
            if (!(n instanceof HBox)) continue;
            HBox f = (HBox) n;
            String tipo = ((TextField) f.getChildren().get(0)).getText().trim();
            if (tipo.isEmpty()) continue;
            double costo = 0.0;
            try { costo = Double.parseDouble(((TextField) f.getChildren().get(1)).getText()); } catch (Exception e) {}
            granTotal += costo;
            contenedorTicketProductos.getChildren().add(crearFilaTicketUI(tipo, 1, costo));
        }
        lblTicketTotalNum.setText(String.format("$%.2f", granTotal));
    }

    private boolean guardarTicketEnBD() {
        String insertTicket = "INSERT INTO public.tickets (folio, fecha, paciente, direccion, telefono, total) VALUES (?, NOW(), ?, ?, ?, ?) RETURNING id_ticket";
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
                ResultSet rs = psTicket.executeQuery();
                if (rs.next()) {
                    int idGen = rs.getInt(1);
                    try (PreparedStatement psDet = conn.prepareStatement(insertDetalle)) {
                        for (Node n : contenedorProductosFormulario.getChildren()) {
                            if (!(n instanceof HBox)) continue;
                            HBox f = (HBox) n;
                            psDet.setInt(1, idGen);
                            psDet.setString(2, ((TextField)f.getChildren().get(0)).getText());
                            psDet.setInt(3, Integer.parseInt(((TextField)f.getChildren().get(1)).getText()));
                            double p = Double.parseDouble(((TextField)f.getChildren().get(2)).getText());
                            psDet.setDouble(4, p);
                            psDet.setDouble(5, p * Integer.parseInt(((TextField)f.getChildren().get(1)).getText()));
                            psDet.addBatch();
                        }
                        for (Node n : contenedorSesionesFormulario.getChildren()) {
                            if (!(n instanceof HBox)) continue;
                            HBox f = (HBox) n;
                            psDet.setInt(1, idGen);
                            psDet.setString(2, ((TextField)f.getChildren().get(0)).getText());
                            psDet.setInt(3, 1);
                            double c = Double.parseDouble(((TextField)f.getChildren().get(1)).getText());
                            psDet.setDouble(4, c);
                            psDet.setDouble(5, c);
                            psDet.addBatch();
                        }
                        psDet.executeBatch();
                    }
                }
                conn.commit(); return true;
            } catch (Exception e) { conn.rollback(); return false; }
        } catch (Exception e) { return false; }
    }

    private double buscarPrecioCualquierTabla(String nombre) {
        String sql = "SELECT precio FROM public.insumos WHERE nombre ILIKE ? LIMIT 1";
        try (Connection conn = ConexionDB.getInstance(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre.trim() + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("precio");
        } catch (Exception e) {}
        return 0.0;
    }

    private void agregarFilaSesionDinamica(String desc, double costo, String fechaSesion) {
        HBox fila = new HBox(10);
        String descFinal = desc + (fechaSesion != null && !fechaSesion.isEmpty() ? " (" + fechaSesion + ")" : "");
        TextField tTipo = crearTextField("Servicio");
        tTipo.setText(descFinal);
        HBox.setHgrow(tTipo, Priority.ALWAYS);
        TextField tCosto = crearTextField("Costo");
        tCosto.setText(String.valueOf(costo));
        tCosto.setPrefWidth(100);
        Button btnDel = new Button("×");
        btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDel.setOnAction(e -> { contenedorSesionesFormulario.getChildren().remove(fila); actualizarTablaTicket(); });
        tTipo.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        tCosto.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        fila.getChildren().addAll(tTipo, tCosto, btnDel);
        contenedorSesionesFormulario.getChildren().add(fila);
        actualizarTablaTicket();
    }

    private HBox crearFilaTicketUI(String nombre, int cant, double total) {
        HBox row = new HBox();
        row.setAlignment(Pos.TOP_LEFT);
        row.setSpacing(5);
        Label lN = new Label(nombre.toUpperCase());
        lN.setPrefWidth(180); lN.setMaxWidth(180); lN.setWrapText(true);
        lN.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        lN.setStyle("-fx-text-fill: black;");
        Label lC = new Label(String.valueOf(cant));
        lC.setPrefWidth(40); lC.setAlignment(Pos.TOP_CENTER);
        lC.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        lC.setStyle("-fx-text-fill: black;");
        Label lP = new Label(String.format("$%.2f", total));
        lP.setPrefWidth(100); lP.setAlignment(Pos.TOP_RIGHT);
        lP.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        lP.setStyle("-fx-text-fill: black;");
        row.getChildren().addAll(lN, lC, lP);
        return row;
    }

    private VBox crearPanelTicket() {
        VBox contenedor = new VBox();
        contenedor.setAlignment(Pos.TOP_CENTER);
        ticketPaper = new VBox(8);
        ticketPaper.setPadding(new Insets(30, 20, 30, 20));
        ticketPaper.setStyle("-fx-background-color: white;");
        ticketPaper.setEffect(new DropShadow(15, Color.color(0, 0, 0, 0.5)));
        construirDisenoTicket();
        ScrollPane scroll = new ScrollPane(ticketPaper);
        scroll.setStyle("-fx-background: #1f2933; -fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setFitToWidth(true);
        contenedor.getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return contenedor;
    }

    private TextField crearTextField(String p) {
        TextField t = new TextField();
        t.setPromptText(p);
        t.setStyle("-fx-background-color: #1f2933; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; -fx-border-color: #374151; -fx-border-radius: 5; -fx-padding: 8;");
        return t;
    }

    private VBox crearBloqueInfo(String e, String v, int sizeE, int sizeV) {
        VBox b = new VBox(0);
        b.setAlignment(Pos.CENTER);
        Label lE = new Label(e);
        lE.setFont(Font.font("Courier New", FontWeight.BOLD, sizeE));
        lE.setStyle("-fx-text-fill: black;");
        Label lV = new Label(v);
        lV.setFont(Font.font("Courier New", FontWeight.BOLD, sizeV));
        lV.setStyle("-fx-text-fill: black;");
        b.getChildren().addAll(lE, lV);
        return b;
    }

    private Label separador() {
        return new Label("--------------------------------") {{
            setFont(Font.font("Courier New", FontWeight.BOLD, 14));
            setStyle("-fx-text-fill: black;");
        }};
    }

    public BorderPane getRoot() { return root; }
}