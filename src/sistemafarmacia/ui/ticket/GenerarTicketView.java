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

    private TextField txtIdTicket;
    private TextField txtDireccion;
    private TextField txtNumero;
    private TextField txtPaciente;
    private VBox contenedorProductosFormulario;
    private VBox contenedorSesionesFormulario;

    private static int contadorTicket = 1;

    public GenerarTicketView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        // Barra superior
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
        panelDerecho.setMaxWidth(550); 
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
        txtDireccion.textProperty().addListener((obs, old, nv) -> lblTicketDireccion.setText(nv));
        txtNumero = crearTextField("");
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
        Button btnAddProd = new Button("+ Agregar Producto");
        btnAddProd.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-cursor: hand;");
        btnAddProd.setOnAction(e -> agregarFilaProductoDinamica("", 1, 0.0));
        
        cardProductos.getChildren().addAll(
            new HBox(new Label("Medicamentos") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnAddProd),
            contenedorProductosFormulario
        );

        VBox cardSesiones = new VBox(15);
        cardSesiones.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");
        contenedorSesionesFormulario = new VBox(10);
        cardSesiones.getChildren().addAll(
            new Label("Sesión Registrada (No editable)") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); }},
            contenedorSesionesFormulario
        );

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
        
        ImageView logo = new ImageView();
        try { 
            logo.setImage(new Image(getClass().getResourceAsStream("/sistemafarmacia/assets/icons/ticket.jpeg"))); 
            logo.setFitWidth(240); 
            logo.setPreserveRatio(true); 
        } catch(Exception e){}

        Label lblNombre = new Label("UNIDAD DE HEMODIÁLISIS\nINTEGRAL SAN RAFAEL");
        lblNombre.setFont(Font.font("Courier New", FontWeight.BOLD, 18)); 
        lblNombre.setTextAlignment(TextAlignment.CENTER); 
        lblNombre.setStyle("-fx-text-fill: black;");

        lblTicketDireccion = new Label(""); 
        lblTicketDireccion.setFont(Font.font("Courier New", FontWeight.BOLD, 12)); 
        lblTicketDireccion.setStyle("-fx-text-fill: black;");

        lblTicketNumero = new Label("");
        lblTicketNumero.setFont(Font.font("Courier New", FontWeight.BOLD, 12)); 
        lblTicketNumero.setStyle("-fx-text-fill: black;");

        VBox bTicket = crearBloqueInfo("No. TICKET:", "000000", 14, 20);
        lblTicketIdValor = (Label) bTicket.getChildren().get(1);

        VBox bFecha = crearBloqueInfo("FECHA DE EMISIÓN:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 14, 20);

        VBox bPaciente = crearBloqueInfo("PACIENTE / CLIENTE:", "MOSTRADOR", 14, 20);
        lblTicketPacienteValor = (Label) bPaciente.getChildren().get(1);

        VBox bMetodoPago = new VBox(2);
        bMetodoPago.setAlignment(Pos.CENTER);
        Label lblMetodoTitulo = new Label("MÉTODO DE PAGO:");
        lblMetodoTitulo.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        lblMetodoTitulo.setStyle("-fx-text-fill: black;");
        
        lblTicketMetodoPago = new Label("EFECTIVO");
        lblTicketMetodoPago.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
        lblTicketMetodoPago.setStyle("-fx-text-fill: black; -fx-border-color: black; -fx-padding: 5 20;"); 
        bMetodoPago.getChildren().addAll(lblMetodoTitulo, lblTicketMetodoPago);

        contenedorTicketProductos = new VBox(8);
        lblTicketTotalNum = new Label("$0.00");
        lblTicketTotalNum.setFont(Font.font("Courier New", FontWeight.BOLD, 24)); 
        lblTicketTotalNum.setStyle("-fx-text-fill: black;");

        ticketPaper.getChildren().addAll(
            logo, lblNombre, lblTicketDireccion, lblTicketNumero, separador(),
            bTicket, new Label(""), bFecha, new Label(""), bPaciente, new Label(""), bMetodoPago, 
            separador(),
            new HBox() {{ getChildren().addAll(
                new Label("DESCRIPCIÓN"){{setPrefWidth(180); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 13));}}, 
                new Label("CANT"){{setPrefWidth(40); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 13));}}, 
                new Label("COSTO"){{setPrefWidth(100); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 13));}}); 
            }},
            separador(), 
            contenedorTicketProductos, 
            separador(),
            new HBox(new Label("TOTAL:"){{setFont(Font.font("Courier New", FontWeight.BOLD, 20)); setStyle("-fx-text-fill: black;");}}, new Region(){{HBox.setHgrow(this, Priority.ALWAYS);}}, lblTicketTotalNum),
            new Label("\n¡Gracias por su confianza!"){{setFont(Font.font("Courier New", FontWeight.BOLD, 14)); setStyle("-fx-text-fill: black;");}}
        );
    }

    private void obtenerUltimoFolioYDatosCompletos() {
        // CORRECCIÓN: Limpiar contenedores para evitar duplicación visual
        if (contenedorProductosFormulario != null) contenedorProductosFormulario.getChildren().clear();
        if (contenedorSesionesFormulario != null) contenedorSesionesFormulario.getChildren().clear();

        try (Connection conn = ConexionDB.getInstance(); Statement stmt = conn.createStatement()) {
            ResultSet rsF = stmt.executeQuery("SELECT MAX(id_ticket) FROM public.tickets");
            if (rsF.next()) contadorTicket = rsF.getInt(1) + 1;
            String f = String.format("%06d", contadorTicket);
            txtIdTicket.setText(f); lblTicketIdValor.setText(f);
            
            ResultSet rsS = stmt.executeQuery("SELECT * FROM public.sesiones ORDER BY id_sesion DESC LIMIT 1");
            if (rsS.next()) {
                txtPaciente.setText(rsS.getString("paciente"));
                lblTicketMetodoPago.setText(rsS.getString("metodo_pago") != null ? rsS.getString("metodo_pago").toUpperCase() : "EFECTIVO");
                
                double costoS = rsS.getDouble("total");
                agregarFilaSesionEstatica(rsS.getString("consulta").toUpperCase(), costoS);

                String meds = rsS.getString("medicamentos");
                if (meds != null && !meds.isEmpty()) {
                    String[] medsArray = meds.split(",");
                    for (String m : medsArray) {
                        String nombre = m.trim();
                        if(nombre.isEmpty()) continue;
                        double precio = buscarPrecioMedicamento(nombre);
                        agregarFilaProductoDinamica(nombre, 1, precio);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean guardarTicketEnBD() {
        String insertTicket = "INSERT INTO public.tickets (folio, fecha, paciente, direccion, telefono, total) VALUES (?, NOW(), ?, ?, ?, ?) RETURNING id_ticket";
        String insertDetalle = "INSERT INTO public.ticket_detalles (id_ticket, producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        String updateStock = "UPDATE public.medicamentos SET stock = stock - ? WHERE nombre = ?";

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
                    int idGenerado = rs.getInt(1); 

                    try (PreparedStatement psDetalle = conn.prepareStatement(insertDetalle); 
                         PreparedStatement psStock = conn.prepareStatement(updateStock)) {

                        for (Node n : contenedorProductosFormulario.getChildren()) {
                            if (!(n instanceof HBox)) continue;
                            HBox fila = (HBox) n;
                            String nombre = ((TextField) fila.getChildren().get(0)).getText().trim();
                            if (nombre.isEmpty()) continue;

                            int cant = Integer.parseInt(((TextField) fila.getChildren().get(1)).getText().trim());
                            double precio = Double.parseDouble(((TextField) fila.getChildren().get(2)).getText().trim());

                            psDetalle.setInt(1, idGenerado);
                            psDetalle.setString(2, nombre);
                            psDetalle.setInt(3, cant);
                            psDetalle.setDouble(4, precio);
                            psDetalle.setDouble(5, cant * precio);
                            psDetalle.addBatch();

                            psStock.setInt(1, cant);
                            psStock.setString(2, nombre);
                            psStock.addBatch();
                        }

                        for (Node n : contenedorSesionesFormulario.getChildren()) {
                            if (!(n instanceof HBox)) continue;
                            HBox fila = (HBox) n;
                            String descServicio = ((TextField) fila.getChildren().get(0)).getText().trim();
                            double costoServicio = Double.parseDouble(((TextField) fila.getChildren().get(1)).getText().trim());

                            psDetalle.setInt(1, idGenerado);
                            psDetalle.setString(2, descServicio);
                            psDetalle.setInt(3, 1); 
                            psDetalle.setDouble(4, costoServicio);
                            psDetalle.setDouble(5, costoServicio);
                            psDetalle.addBatch();
                        }

                        psDetalle.executeBatch();
                        psStock.executeBatch();
                    }
                }
                conn.commit(); 
                return true;
            } catch (Exception e) {
                conn.rollback(); 
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void imprimirTicket() {
        if (contenedorTicketProductos.getChildren().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No hay productos o servicios para generar un ticket.").show();
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

                if (job.printPage(pl, ticketPaper)) {
                    job.endJob();
                }

                ticketPaper.getTransforms().remove(s);
                ticketPaper.setEffect(new DropShadow(15, Color.color(0, 0, 0, 0.5)));

                obtenerUltimoFolioYDatosCompletos();
                new Alert(Alert.AlertType.INFORMATION, "Ticket guardado con éxito.").show();
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "Error: No se pudo guardar en la base de datos.").show();
        }
    }

    // --- Métodos de apoyo UI ---

    private void agregarFilaProductoDinamica(String nombre, int cant, double precio) {
        HBox fila = new HBox(10);
        TextField tNom = crearTextField("Producto"); tNom.setText(nombre); HBox.setHgrow(tNom, Priority.ALWAYS);
        TextField tCan = crearTextField("Cant"); tCan.setPrefWidth(50); tCan.setText(String.valueOf(cant));
        TextField tPre = crearTextField("Precio"); tPre.setPrefWidth(80); tPre.setText(String.valueOf(precio));
        Button btnDel = new Button("×"); btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
        btnDel.setOnAction(e -> { contenedorProductosFormulario.getChildren().remove(fila); actualizarTablaTicket(); });
        
        tNom.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        tCan.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        tPre.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        
        fila.getChildren().addAll(tNom, tCan, tPre, btnDel);
        contenedorProductosFormulario.getChildren().add(fila);
        actualizarTablaTicket();
    }

    private void agregarFilaSesionEstatica(String desc, double costo) {
        HBox fila = new HBox(10);
        TextField tTipo = crearTextField(""); tTipo.setText(desc); tTipo.setEditable(false); HBox.setHgrow(tTipo, Priority.ALWAYS);
        TextField tCosto = crearTextField(""); tCosto.setText(String.valueOf(costo)); tCosto.setEditable(false); tCosto.setPrefWidth(100);
        fila.getChildren().addAll(tTipo, tCosto);
        contenedorSesionesFormulario.getChildren().add(fila);
        actualizarTablaTicket();
    }

    private void actualizarTablaTicket() {
        if (contenedorTicketProductos == null || lblTicketTotalNum == null) return;
        contenedorTicketProductos.getChildren().clear();
        double granTotal = 0.0;
        
        boolean tieneProductos = false;
        for (Node n : contenedorProductosFormulario.getChildren()) {
            HBox f = (HBox) n;
            String nom = ((TextField) f.getChildren().get(0)).getText();
            if (nom.isEmpty()) continue;
            if (!tieneProductos) { agregarEncabezadoSeccion("PRODUCTOS / MEDICAMENTOS"); tieneProductos = true; }
            int can = 1; double pre = 0.0;
            try { can = Integer.parseInt(((TextField) f.getChildren().get(1)).getText()); } catch (Exception e) {}
            try { pre = Double.parseDouble(((TextField) f.getChildren().get(2)).getText()); } catch (Exception e) {}
            double subtotal = can * pre; granTotal += subtotal;
            contenedorTicketProductos.getChildren().add(crearFilaTicketUI(nom, can, subtotal));
        }

        boolean tieneSesiones = false;
        for (Node n : contenedorSesionesFormulario.getChildren()) {
            HBox f = (HBox) n;
            String tipo = ((TextField) f.getChildren().get(0)).getText();
            if (tipo.isEmpty()) continue;
            if (!tieneSesiones) { 
                if (tieneProductos) contenedorTicketProductos.getChildren().add(new Label(" "));
                agregarEncabezadoSeccion("SERVICIOS / SESIONES"); tieneSesiones = true; 
            }
            double costo = 0.0;
            try { costo = Double.parseDouble(((TextField) f.getChildren().get(1)).getText()); } catch (Exception e) {}
            granTotal += costo;
            contenedorTicketProductos.getChildren().add(crearFilaTicketUI(tipo, 1, costo));
        }
        lblTicketTotalNum.setText(String.format("$%.2f", granTotal));
    }

    private void agregarEncabezadoSeccion(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        lbl.setStyle("-fx-text-fill: black; -fx-border-color: black; -fx-border-width: 0 0 1 0;");
        lbl.setPrefWidth(320);
        contenedorTicketProductos.getChildren().add(lbl);
    }

    private HBox crearFilaTicketUI(String nombre, int cant, double total) {
        HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT);
        Label lN = new Label(nombre.toUpperCase()); lN.setPrefWidth(180); lN.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); lN.setStyle("-fx-text-fill: black;");
        Label lC = new Label(String.valueOf(cant)); lC.setPrefWidth(40); lC.setAlignment(Pos.CENTER); lC.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); lC.setStyle("-fx-text-fill: black;");
        Label lP = new Label(String.format("$%.2f", total)); lP.setPrefWidth(100); lP.setAlignment(Pos.CENTER_RIGHT); lP.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); lP.setStyle("-fx-text-fill: black;");
        row.getChildren().addAll(lN, lC, lP);
        return row;
    }

    private double buscarPrecioMedicamento(String nombre) {
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement("SELECT precio FROM public.medicamentos WHERE nombre ILIKE ? LIMIT 1")) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("precio");
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    private VBox crearPanelTicket() {
        VBox contenedor = new VBox();
        contenedor.setAlignment(Pos.TOP_CENTER);
        ticketPaper = new VBox(8);
        ticketPaper.setPadding(new Insets(40, 40, 40, 40));
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
        return new Label("------------------------------------------") {{ setFont(Font.font("Courier New", FontWeight.BOLD, 14)); setStyle("-fx-text-fill: black;"); }};
    }

    public BorderPane getRoot() { return root; }
}