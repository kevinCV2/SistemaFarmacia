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

import java.io.InputStream;
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

    // Elementos de la vista previa del Ticket (Derecha)
    private Label lblTicketDireccion;
    private Label lblTicketNumero;
    private Label lblTicketPacienteValor;
    private Label lblTicketIdValor;
    private VBox contenedorTicketProductos;
    private Label lblTicketTotalNum;

    // Campos del Formulario (Izquierda)
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

        // --- BARRA SUPERIOR ---
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

        // --- CUERPO PRINCIPAL ---
        HBox splitLayout = new HBox(50);
        splitLayout.setPadding(new Insets(20));
        splitLayout.setAlignment(Pos.TOP_CENTER);

        VBox panelDerecho = crearPanelTicket();
        HBox.setHgrow(panelDerecho, Priority.ALWAYS);
        panelDerecho.setMaxWidth(550); 

        VBox panelIzquierdo = crearPanelFormulario();
        HBox.setHgrow(panelIzquierdo, Priority.ALWAYS);
        panelIzquierdo.setMaxWidth(600);

        splitLayout.getChildren().addAll(panelIzquierdo, panelDerecho);
        root.setCenter(splitLayout);

        obtenerUltimoFolioYDatosSesion();
    }

    private VBox crearPanelFormulario() {
        VBox panelPrincipal = new VBox(20);
        VBox contenidoScroll = new VBox(20);
        contenidoScroll.setPadding(new Insets(0, 10, 0, 0));

        // CARD: DATOS GENERALES
        VBox cardDatos = new VBox(15);
        cardDatos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");

        txtIdTicket = crearTextField("Folio");
        txtIdTicket.setEditable(false);
        txtDireccion = crearTextField("Dirección...");
        txtDireccion.textProperty().addListener((obs, old, nv) -> lblTicketDireccion.setText(nv.replace(", ", ",\n")));
        txtNumero = crearTextField("Teléfono");
        txtNumero.textProperty().addListener((obs, old, nv) -> lblTicketNumero.setText("TEL: " + nv));
        txtPaciente = crearTextField("Nombre del Paciente");
        txtPaciente.textProperty().addListener((obs, old, nv) -> lblTicketPacienteValor.setText(nv.isEmpty() ? "MOSTRADOR" : nv.toUpperCase()));

        cardDatos.getChildren().addAll(
            new Label("Datos de la Venta") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;"); }},
            new Label("Folio") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtIdTicket,
            new Label("Dirección") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtDireccion,
            new Label("Teléfono") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtNumero,
            new Label("Paciente") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtPaciente
        );

        // CARD: PRODUCTOS
        VBox cardProductos = new VBox(15);
        cardProductos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");
        contenedorProductosFormulario = new VBox(10);
        Button btnAddProd = new Button("+ Agregar Producto");
        btnAddProd.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-cursor: hand;");
        btnAddProd.setOnAction(e -> agregarFilaProductoManual());
        
        cardProductos.getChildren().addAll(
            new HBox(new Label("Productos") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnAddProd),
            contenedorProductosFormulario
        );

        // CARD: SESIONES
        VBox cardSesiones = new VBox(15);
        cardSesiones.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");
        contenedorSesionesFormulario = new VBox(10);
        Button btnAddSesion = new Button("+ Agregar Sesión");
        btnAddSesion.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-cursor: hand;");
        btnAddSesion.setOnAction(e -> agregarFilaSesionManual());

        cardSesiones.getChildren().addAll(
            new HBox(new Label("Sesiones") {{ setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnAddSesion),
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

    private void agregarFilaProductoManual() {
        HBox fila = new HBox(10);
        TextField tNom = crearTextField("Producto"); HBox.setHgrow(tNom, Priority.ALWAYS);
        TextField tCan = crearTextField("Cant"); tCan.setPrefWidth(50); tCan.setText("1");
        TextField tPre = crearTextField("Precio"); tPre.setPrefWidth(80);
        Button btnDel = new Button("×"); btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
        btnDel.setOnAction(e -> { contenedorProductosFormulario.getChildren().remove(fila); actualizarTablaTicket(); });
        
        tNom.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        tCan.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        tPre.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        
        fila.getChildren().addAll(tNom, tCan, tPre, btnDel);
        contenedorProductosFormulario.getChildren().add(fila);
    }

    private void agregarFilaSesionManual() {
        HBox fila = new HBox(10);
        TextField tTipo = crearTextField("Tipo de Sesión"); HBox.setHgrow(tTipo, Priority.ALWAYS);
        TextField tCosto = crearTextField("Costo"); tCosto.setPrefWidth(100); tCosto.setText("0.00");
        Button btnDel = new Button("×"); btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
        btnDel.setOnAction(e -> { contenedorSesionesFormulario.getChildren().remove(fila); actualizarTablaTicket(); });

        tTipo.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());
        tCosto.textProperty().addListener((o, ol, nv) -> actualizarTablaTicket());

        fila.getChildren().addAll(tTipo, tCosto, btnDel);
        contenedorSesionesFormulario.getChildren().add(fila);
    }

    private void actualizarTablaTicket() {
        if (contenedorTicketProductos == null || lblTicketTotalNum == null) return;
        contenedorTicketProductos.getChildren().clear();
        double granTotal = 0.0;

        for (Node n : contenedorProductosFormulario.getChildren()) {
            HBox f = (HBox) n;
            String nom = ((TextField)f.getChildren().get(0)).getText();
            if (nom.isEmpty()) continue;
            int can = 1; double pre = 0.0;
            try { can = Integer.parseInt(((TextField)f.getChildren().get(1)).getText()); } catch(Exception e){}
            try { pre = Double.parseDouble(((TextField)f.getChildren().get(2)).getText()); } catch(Exception e){}
            granTotal += (can * pre);
            contenedorTicketProductos.getChildren().add(crearFilaTicketUI(nom, can, can * pre));
        }

        for (Node n : contenedorSesionesFormulario.getChildren()) {
            HBox f = (HBox) n;
            String tipo = ((TextField)f.getChildren().get(0)).getText();
            if (tipo.isEmpty()) continue;
            double costo = 0.0;
            try { costo = Double.parseDouble(((TextField)f.getChildren().get(1)).getText()); } catch(Exception e){}
            granTotal += costo;
            contenedorTicketProductos.getChildren().add(crearFilaTicketUI(tipo, 1, costo));
        }
        lblTicketTotalNum.setText(String.format("$%.2f", granTotal));
    }

    private HBox crearFilaTicketUI(String nombre, int cant, double total) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label lN = new Label(nombre.toUpperCase()); lN.setPrefWidth(180); lN.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); lN.setStyle("-fx-text-fill: black;");
        Label lC = new Label(String.valueOf(cant)); lC.setPrefWidth(40); lC.setAlignment(Pos.CENTER); lC.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); lC.setStyle("-fx-text-fill: black;");
        Label lP = new Label(String.format("$%.2f", total)); lP.setPrefWidth(100); lP.setAlignment(Pos.CENTER_RIGHT); lP.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); lP.setStyle("-fx-text-fill: black;");
        row.getChildren().addAll(lN, lC, lP);
        return row;
    }

    private VBox crearPanelTicket() {
        VBox contenedor = new VBox();
        contenedor.setAlignment(Pos.TOP_CENTER);
        ticketPaper = new VBox(8);
        ticketPaper.setPadding(new Insets(40));
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

    private void construirDisenoTicket() {
        ticketPaper.getChildren().clear();
        ticketPaper.setAlignment(Pos.TOP_CENTER);
        
        ImageView logo = new ImageView();
        try { logo.setImage(new Image(getClass().getResourceAsStream("/sistemafarmacia/assets/icons/ticket.jpeg"))); logo.setFitWidth(240); logo.setPreserveRatio(true); } catch(Exception e){}

        Label lblNombre = new Label("UNIDAD DE HEMODIÁLISIS\nINTEGRAL SAN RAFAEL");
        lblNombre.setFont(Font.font("Courier New", FontWeight.BOLD, 22)); lblNombre.setTextAlignment(TextAlignment.CENTER); lblNombre.setStyle("-fx-text-fill: black;");

        lblTicketDireccion = new Label("C. valle embrujado 131,\nPachuca, Hidalgo");
        lblTicketDireccion.setFont(Font.font("Courier New", FontWeight.BOLD, 15)); lblTicketDireccion.setStyle("-fx-text-fill: black;");

        lblTicketNumero = new Label("TEL: 7713778107, 7711027324");
        lblTicketNumero.setFont(Font.font("Courier New", FontWeight.BOLD, 15)); lblTicketNumero.setStyle("-fx-text-fill: black;");

        VBox bTicket = crearBloqueCentrado("No. TICKET:", "000000");
        lblTicketIdValor = (Label) bTicket.getChildren().get(1);
        VBox bFecha = crearBloqueCentrado("FECHA DE EMISIÓN:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        VBox bPaciente = crearBloqueCentrado("PACIENTE / CLIENTE:", "MOSTRADOR");
        lblTicketPacienteValor = (Label) bPaciente.getChildren().get(1);

        contenedorTicketProductos = new VBox(10);
        lblTicketTotalNum = new Label("$0.00");
        lblTicketTotalNum.setFont(Font.font("Courier New", FontWeight.BOLD, 26)); lblTicketTotalNum.setStyle("-fx-text-fill: black;");

        ticketPaper.getChildren().addAll(
            logo, lblNombre, lblTicketDireccion, lblTicketNumero, separador(),
            bTicket, bFecha, bPaciente, separador(),
            new HBox() {{ getChildren().addAll(new Label("DESCRIPCIÓN"){{setPrefWidth(180); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 14));}}, new Label("CANT"){{setPrefWidth(40); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 14));}}, new Label("COSTO"){{setPrefWidth(100); setStyle("-fx-text-fill: black;"); setFont(Font.font("Courier New", FontWeight.BOLD, 14));}}); }},
            separador(), contenedorTicketProductos, separador(),
            new HBox(new Label("TOTAL:"){{setFont(Font.font("Courier New", FontWeight.BOLD, 20)); setStyle("-fx-text-fill: black;");}}, new Region(){{HBox.setHgrow(this, Priority.ALWAYS);}}, lblTicketTotalNum),
            new Label("\n¡Gracias por su confianza!"){{setFont(Font.font("Courier New", FontWeight.BOLD, 15)); setStyle("-fx-text-fill: black;");}}
        );
    }

    private VBox crearBloqueCentrado(String e, String v) {
        VBox b = new VBox(2); b.setAlignment(Pos.CENTER);
        Label lE = new Label(e); lE.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); lE.setStyle("-fx-text-fill: black;");
        Label lV = new Label(v); lV.setFont(Font.font("Courier New", FontWeight.BOLD, 22)); lV.setStyle("-fx-text-fill: black;");
        b.getChildren().addAll(lE, lV); return b;
    }

    private Label separador() {
        return new Label("------------------------------------------") {{ setFont(Font.font("Courier New", FontWeight.BOLD, 14)); setStyle("-fx-text-fill: black;"); }};
    }

    private TextField crearTextField(String p) {
        TextField t = new TextField(); t.setPromptText(p);
        t.setStyle("-fx-background-color: #1f2933; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; -fx-border-color: #374151; -fx-border-radius: 5; -fx-padding: 8;");
        return t;
    }

    private void obtenerUltimoFolioYDatosSesion() {
        try (Connection conn = ConexionDB.getInstance(); Statement stmt = conn.createStatement()) {
            ResultSet rsF = stmt.executeQuery("SELECT MAX(id_ticket) FROM tickets");
            if (rsF.next()) contadorTicket = rsF.getInt(1) + 1;
            String f = String.format("%06d", contadorTicket);
            txtIdTicket.setText(f); lblTicketIdValor.setText(f);
            
            ResultSet rsS = stmt.executeQuery("SELECT paciente FROM sesiones ORDER BY id_sesion DESC LIMIT 1");
            if (rsS.next()) txtPaciente.setText(rsS.getString("paciente"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void imprimirTicket() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
            PageLayout pl = job.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
            ticketPaper.setEffect(null);
            double scale = pl.getPrintableWidth() / ticketPaper.getWidth();
            Scale s = new Scale(scale, scale);
            ticketPaper.getTransforms().add(s);
            if (job.printPage(pl, ticketPaper)) job.endJob();
            ticketPaper.getTransforms().remove(s);
            ticketPaper.setEffect(new DropShadow(15, Color.color(0,0,0,0.5)));
        }
    }

    public BorderPane getRoot() { return root; }
}