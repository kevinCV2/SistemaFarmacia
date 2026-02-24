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

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GenerarTicketView {

    private BorderPane root;
    private VBox ticketPaper;
    private Runnable actionVolver;

    // Elementos de la vista previa del Ticket
    private Label lblTicketDireccion;
    private Label lblTicketNumero;
    private Label lblTicketPacienteValor;
    private Label lblTicketIdValor;

    // Campos del Formulario (Panel Izquierdo)
    private TextField txtIdTicket;
    private VBox contenedorProductosFormulario;
    private VBox contenedorTicketProductos;
    private Label lblTicketTotalNum;

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
        btnVolver.setOnAction(e -> {
            if (this.actionVolver != null) this.actionVolver.run();
        });

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
    }

    private VBox crearPanelFormulario() {
        VBox panelPrincipal = new VBox(20);
        VBox contenidoScroll = new VBox(20);
        contenidoScroll.setPadding(new Insets(0, 10, 0, 0));

        // CARD: DATOS DE VENTA
        VBox cardDatos = new VBox(15);
        cardDatos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");

        Label lblDatos = new Label("Datos de la Venta");
        lblDatos.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Campo ID sincronizado
        txtIdTicket = crearTextField("Folio del ticket");
        txtIdTicket.setText(String.format("%06d", contadorTicket));
        txtIdTicket.textProperty().addListener((obs, old, newValue) -> {
            if (lblTicketIdValor != null) lblTicketIdValor.setText(newValue);
        });

        TextField txtDireccion = crearTextField("C. valle embrujado 131...");
        txtDireccion.textProperty().addListener((obs, old, newValue) -> lblTicketDireccion.setText(newValue.replace(", ", ",\n")));

        TextField txtNumero = crearTextField("Teléfono");
        txtNumero.textProperty().addListener((obs, old, newValue) -> lblTicketNumero.setText("TEL: " + newValue));

        TextField txtPaciente = crearTextField("Nombre del Paciente");
        txtPaciente.textProperty().addListener((obs, old, newValue) -> lblTicketPacienteValor.setText(newValue.trim().isEmpty() ? "MOSTRADOR" : newValue.toUpperCase()));

        cardDatos.getChildren().addAll(
            lblDatos, 
            new Label("Folio / ID Ticket") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtIdTicket,
            new Label("Dirección") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtDireccion, 
            new Label("Teléfono") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtNumero, 
            new Label("Paciente") {{ setStyle("-fx-text-fill: #9ca3af;"); }}, txtPaciente
        );

        // CARD: PRODUCTOS
        VBox cardProductos = new VBox(15);
        cardProductos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");

        HBox headerProd = new HBox();
        headerProd.setAlignment(Pos.CENTER_LEFT);
        Label lblProd = new Label("Productos");
        lblProd.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        contenedorProductosFormulario = new VBox(10);
        Button btnAgregar = new Button("+ Agregar");
        btnAgregar.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
        btnAgregar.setOnAction(e -> agregarFilaProducto());

        headerProd.getChildren().addAll(lblProd, spacer, btnAgregar);
        cardProductos.getChildren().addAll(headerProd, contenedorProductosFormulario);

        agregarFilaProducto();

        contenidoScroll.getChildren().addAll(cardDatos, cardProductos);
        ScrollPane scrollPane = new ScrollPane(contenidoScroll);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-base: #1f2933; -fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // BOTÓN IMPRIMIR
        Button btnImprimir = new Button("🖨 Imprimir ticket");
        btnImprimir.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-font-size: 16px; -fx-background-radius: 8; -fx-cursor: hand;");
        btnImprimir.setMaxWidth(Double.MAX_VALUE);
        btnImprimir.setOnAction(e -> imprimirTicket());

        panelPrincipal.getChildren().addAll(scrollPane, btnImprimir);
        return panelPrincipal;
    }

    private void agregarFilaProducto() {
        HBox fila = new HBox(10);
        
        TextField txtNomProd = crearTextField("Producto");
        HBox.setHgrow(txtNomProd, Priority.ALWAYS);

        TextField txtCant = crearTextField("Cant");
        txtCant.setPrefWidth(50); txtCant.setText("1");

        TextField txtPrecio = crearTextField("Precio");
        txtPrecio.setPrefWidth(80);

        Button btnEliminar = new Button("×");
        btnEliminar.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
        btnEliminar.setOnAction(e -> {
            if(contenedorProductosFormulario.getChildren().size() > 1) {
                contenedorProductosFormulario.getChildren().remove(fila);
                actualizarTablaTicket();
            }
        });

        txtNomProd.textProperty().addListener((obs, old, newV) -> actualizarTablaTicket());
        txtCant.textProperty().addListener((obs, old, newV) -> actualizarTablaTicket());
        txtPrecio.textProperty().addListener((obs, old, newV) -> actualizarTablaTicket());

        fila.getChildren().addAll(txtNomProd, txtCant, txtPrecio, btnEliminar);
        contenedorProductosFormulario.getChildren().add(fila);
        actualizarTablaTicket();
    }

    private void actualizarTablaTicket() {
        if (contenedorTicketProductos == null || lblTicketTotalNum == null) return;
        contenedorTicketProductos.getChildren().clear();
        double granTotal = 0.0;

        for (Node nodo : contenedorProductosFormulario.getChildren()) {
            HBox fila = (HBox) nodo;
            TextField txtNom = (TextField) fila.getChildren().get(0);
            TextField txtCant = (TextField) fila.getChildren().get(1);
            TextField txtPrecio = (TextField) fila.getChildren().get(2);

            String nombre = txtNom.getText().trim();
            String cantStr = txtCant.getText().trim();
            String precioStr = txtPrecio.getText().trim();

            if (nombre.isEmpty() && precioStr.isEmpty()) continue;

            int cant = 1; double precio = 0.0;
            try { if (!cantStr.isEmpty()) cant = Integer.parseInt(cantStr); } catch (Exception ignored) {}
            try { if (!precioStr.isEmpty()) precio = Double.parseDouble(precioStr); } catch (Exception ignored) {}

            granTotal += (cant * precio);

            HBox ticketRow = new HBox();
            ticketRow.setAlignment(Pos.CENTER_LEFT);

            Label lNom = new Label(nombre.isEmpty() ? "---" : nombre.toUpperCase());
            lNom.setPrefWidth(180); 
            lNom.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); 
            lNom.setStyle("-fx-text-fill: black;");
            lNom.setWrapText(true);

            Label lCant = new Label(String.valueOf(cant));
            lCant.setPrefWidth(40); 
            lCant.setAlignment(Pos.CENTER); 
            lCant.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); 
            lCant.setStyle("-fx-text-fill: black;");

            Label lPrecio = new Label(String.format("$%.2f", precio));
            lPrecio.setPrefWidth(100); 
            lPrecio.setAlignment(Pos.CENTER_RIGHT); 
            lPrecio.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); 
            lPrecio.setStyle("-fx-text-fill: black;");

            ticketRow.getChildren().addAll(lNom, lCant, lPrecio);
            contenedorTicketProductos.getChildren().add(ticketRow);
        }
        lblTicketTotalNum.setText(String.format("$%.2f", granTotal));
    }

    private VBox crearPanelTicket() {
        VBox contenedor = new VBox();
        contenedor.setAlignment(Pos.TOP_CENTER);

        ticketPaper = new VBox(8);
        ticketPaper.setAlignment(Pos.TOP_CENTER);
        ticketPaper.setPadding(new Insets(40));
        ticketPaper.setStyle("-fx-background-color: #ffffff;");
        ticketPaper.setMaxWidth(420); 
        ticketPaper.setEffect(new DropShadow(15, Color.color(0,0,0, 0.5)));

        construirDisenoTicket();

        ScrollPane scroll = new ScrollPane(ticketPaper);
        scroll.setStyle("-fx-background: #1f2933; -fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setFitToWidth(true);

        StackPane centrarScroll = new StackPane(scroll);
        centrarScroll.setAlignment(Pos.TOP_CENTER);
        contenedor.getChildren().add(centrarScroll);
        VBox.setVgrow(centrarScroll, Priority.ALWAYS);
        return contenedor;
    }

    private void construirDisenoTicket() {
        ImageView logoView = new ImageView();
        try {
            InputStream imageStream = getClass().getResourceAsStream("/sistemafarmacia/assets/icons/ticket.jpeg");
            if (imageStream != null) {
                logoView.setImage(new Image(imageStream));
                logoView.setFitWidth(240); 
                logoView.setPreserveRatio(true);
            }
        } catch (Exception e) {}

        Label lblNombre = new Label("UNIDAD DE HEMODIÁLISIS\nINTEGRAL SAN RAFAEL");
        lblNombre.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
        lblNombre.setTextAlignment(TextAlignment.CENTER);
        lblNombre.setStyle("-fx-text-fill: black;");

        lblTicketDireccion = new Label("C. valle embrujado 131,\nPachuca, Hidalgo");
        lblTicketDireccion.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        lblTicketDireccion.setTextAlignment(TextAlignment.CENTER);
        lblTicketDireccion.setStyle("-fx-text-fill: black;");

        lblTicketNumero = new Label("TEL: xxx-xxx-xxxx");
        lblTicketNumero.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        lblTicketNumero.setStyle("-fx-text-fill: black;");

        VBox infoBox = new VBox(15);
        infoBox.setPadding(new Insets(20, 0, 20, 0));
        infoBox.setAlignment(Pos.CENTER);

        VBox bTicket = crearBloqueListaCentrado("No. TICKET:", String.format("%06d", contadorTicket));
        VBox bFecha = crearBloqueListaCentrado("FECHA DE EMISIÓN:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        VBox bCliente = crearBloqueListaCentrado("PACIENTE / CLIENTE:", "MOSTRADOR");
        
        lblTicketIdValor = (Label) bTicket.getChildren().get(1); 
        lblTicketPacienteValor = (Label) bCliente.getChildren().get(1); 
        
        infoBox.getChildren().addAll(bTicket, bFecha, bCliente);

        contenedorTicketProductos = new VBox(10);
        HBox headerTabla = new HBox() {{
            getChildren().addAll(
                new Label("PRODUCTO") {{ setPrefWidth(180); }}, 
                new Label("CANT") {{ setPrefWidth(40); setAlignment(Pos.CENTER); }}, 
                new Label("COSTO") {{ setPrefWidth(100); setAlignment(Pos.CENTER_RIGHT); }}
            );
            getChildren().forEach(n -> { 
                ((Label)n).setFont(Font.font("Courier New", FontWeight.BOLD, 15)); 
                ((Label)n).setStyle("-fx-text-fill: black;"); 
            });
        }};

        lblTicketTotalNum = new Label("$0.00");
        lblTicketTotalNum.setFont(Font.font("Courier New", FontWeight.BOLD, 26)); 
        lblTicketTotalNum.setStyle("-fx-text-fill: black;");

        Label lblDespedida = new Label("\n¡Gracias por su confianza!");
        lblDespedida.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        lblDespedida.setStyle("-fx-text-fill: black;");

        ticketPaper.getChildren().addAll(
                logoView, lblNombre, lblTicketDireccion, lblTicketNumero,
                separadorTicket(), infoBox, separadorTicket(),
                headerTabla, separadorTicket(), 
                contenedorTicketProductos, separadorTicket(),
                new HBox(new Label("TOTAL:") {{ setFont(Font.font("Courier New", FontWeight.BOLD, 20)); setStyle("-fx-text-fill: black;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, lblTicketTotalNum),
                lblDespedida
        );
    }

    private VBox crearBloqueListaCentrado(String etiqueta, String valor) {
        VBox bloque = new VBox(2);
        bloque.setAlignment(Pos.CENTER);
        
        Label e = new Label(etiqueta); 
        e.setFont(Font.font("Courier New", FontWeight.BOLD, 14)); 
        e.setStyle("-fx-text-fill: #000000;");
        
        Label v = new Label(valor); 
        v.setFont(Font.font("Courier New", FontWeight.BOLD, 22)); 
        v.setStyle("-fx-text-fill: black;");
        
        bloque.getChildren().addAll(e, v);
        return bloque;
    }

    private void imprimirTicket() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
            if (job.showPrintDialog(root.getScene().getWindow())) {
                ticketPaper.setEffect(null);
                double scale = pageLayout.getPrintableWidth() / ticketPaper.getBoundsInParent().getWidth();
                Scale escala = new Scale(scale, scale);
                ticketPaper.getTransforms().add(escala);

                if (job.printPage(pageLayout, ticketPaper)) {
                    job.endJob();
                    
                    // INCREMENTO DEL CONTADOR
                    contadorTicket++;
                    String nuevoFolio = String.format("%06d", contadorTicket);
                    txtIdTicket.setText(nuevoFolio);
                    lblTicketIdValor.setText(nuevoFolio);
                }

                ticketPaper.getTransforms().remove(escala);
                ticketPaper.setEffect(new DropShadow(15, Color.color(0,0,0, 0.5)));
            }
        }
    }

    private Label separadorTicket() {
        return new Label("------------------------------------------") {{ 
            setFont(Font.font("Courier New", FontWeight.BOLD, 14)); 
            setStyle("-fx-text-fill: black;"); 
        }};
    }

    private TextField crearTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #1f2933; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; -fx-border-color: #374151; -fx-border-radius: 5; -fx-padding: 8;");
        return tf;
    }

    public BorderPane getRoot() { return root; }
}