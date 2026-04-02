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

    private Label lblTicketDireccion;
    private Label lblTicketNumero;
    private Label lblTicketPacienteValor;

    private VBox contenedorProductosFormulario;
    private VBox contenedorTicketProductos;
    private Label lblTicketTotalNum;

    public GenerarTicketView(Runnable actionVolver) {
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
        splitLayout.setPadding(new Insets(20));
        splitLayout.setAlignment(Pos.TOP_CENTER);

        VBox panelDerecho = crearPanelTicket();
        HBox.setHgrow(panelDerecho, Priority.ALWAYS);
        panelDerecho.setMaxWidth(450);

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

        VBox cardDatos = new VBox(15);
        cardDatos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");

        Label lblDatos = new Label("Datos de la Venta");
        lblDatos.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblDireccion = new Label("Dirección");
        lblDireccion.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
        TextField txtDireccion = crearTextField("C. valle embrujado 131...");
        txtDireccion.textProperty().addListener((obs, old, newValue) -> lblTicketDireccion.setText(newValue));

        String numDefault = "xxx-xxx-xxxx";
        Label lblNum = new Label("Numero");
        lblNum.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
        TextField txtNumero = crearTextField(numDefault);
        txtNumero.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") && change.getControlNewText().length() <= 10 ? change : null));
        txtNumero.textProperty().addListener((obs, old, newValue) -> lblTicketNumero.setText(newValue.trim().isEmpty() ? numDefault : newValue));

        Label lblNom = new Label("Nombre del Paciente");
        lblNom.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
        TextField txtPaciente = crearTextField("Ingresa el nombre del Paciente");
        txtPaciente.textProperty().addListener((obs, old, newValue) -> lblTicketPacienteValor.setText(newValue.trim().isEmpty() ? "MOSTRADOR" : newValue.toUpperCase()));

        cardDatos.getChildren().addAll(lblDatos, lblDireccion, txtDireccion, lblNum, txtNumero, lblNom, txtPaciente);

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

        Button btnImprimir = new Button("🖨 Imprimir Ticket");
        btnImprimir.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
        btnImprimir.setMaxWidth(Double.MAX_VALUE);
        btnImprimir.setOnAction(e -> imprimirTicket());

        panelPrincipal.getChildren().addAll(scrollPane, btnImprimir);
        return panelPrincipal;
    }

    private void agregarFilaProducto() {
        HBox fila = new HBox(10);
        TextField txtId = crearTextField("ID");
        txtId.setPrefWidth(60);
        txtId.setAlignment(Pos.CENTER);

        TextField txtNomProd = crearTextField("Producto");
        HBox.setHgrow(txtNomProd, Priority.ALWAYS);

        TextField txtCant = crearTextField("Cant");
        txtCant.setPrefWidth(50);
        txtCant.setText("1");

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

        fila.getChildren().addAll(txtId, txtNomProd, txtCant, txtPrecio, btnEliminar);
        contenedorProductosFormulario.getChildren().add(fila);
        actualizarTablaTicket();
    }

    private void actualizarTablaTicket() {
        if (contenedorTicketProductos == null || lblTicketTotalNum == null) return;
        contenedorTicketProductos.getChildren().clear();
        double granTotal = 0.0;

        for (Node nodo : contenedorProductosFormulario.getChildren()) {
            HBox fila = (HBox) nodo;
            TextField txtNom = (TextField) fila.getChildren().get(1);
            TextField txtCant = (TextField) fila.getChildren().get(2);
            TextField txtPrecio = (TextField) fila.getChildren().get(3);

            String nombre = txtNom.getText().trim();
            String cantStr = txtCant.getText().trim();
            String precioStr = txtPrecio.getText().trim();

            if (nombre.isEmpty() && precioStr.isEmpty()) continue;

            int cant = 1; double precio = 0.0;
            try { if (!cantStr.isEmpty()) cant = Integer.parseInt(cantStr); } catch (Exception ignored) {}
            try { if (!precioStr.isEmpty()) precio = Double.parseDouble(precioStr); } catch (Exception ignored) {}

            double subtotal = cant * precio;
            granTotal += subtotal;

            HBox ticketRow = new HBox();
            Label lNom = new Label(nombre.isEmpty() ? "---" : nombre.toUpperCase());
            lNom.setPrefWidth(120); lNom.setFont(Font.font("Courier New", 10)); lNom.setStyle("-fx-text-fill: black;");

            Label lCant = new Label(String.valueOf(cant));
            lCant.setPrefWidth(40); lCant.setAlignment(Pos.CENTER); lCant.setFont(Font.font("Courier New", 10)); lCant.setStyle("-fx-text-fill: black;");

            Label lPrecio = new Label(String.format("$%.2f", precio));
            lPrecio.setPrefWidth(60); lPrecio.setAlignment(Pos.CENTER_RIGHT); lPrecio.setFont(Font.font("Courier New", 10)); lPrecio.setStyle("-fx-text-fill: black;");

            Label lSubtotal = new Label(String.format("$%.2f", subtotal));
            lSubtotal.setPrefWidth(60); lSubtotal.setAlignment(Pos.CENTER_RIGHT); lSubtotal.setFont(Font.font("Courier New", FontWeight.BOLD, 10)); lSubtotal.setStyle("-fx-text-fill: black;");

            ticketRow.getChildren().addAll(lNom, lCant, lPrecio, lSubtotal);
            contenedorTicketProductos.getChildren().add(ticketRow);
        }
        lblTicketTotalNum.setText(String.format("$%.2f", granTotal));
    }

    private TextField crearTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #1f2933; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; -fx-border-color: #374151; -fx-border-radius: 5; -fx-padding: 8;");
        return tf;
    }

    private VBox crearPanelTicket() {
        VBox contenedor = new VBox();
        contenedor.setAlignment(Pos.TOP_CENTER);

        ticketPaper = new VBox(5);
        ticketPaper.setAlignment(Pos.TOP_CENTER);
        ticketPaper.setPadding(new Insets(30));
        ticketPaper.setStyle("-fx-background-color: #ffffff;");
        ticketPaper.setMaxWidth(350); // Diseño original

        DropShadow shadow = new DropShadow(15, Color.color(0,0,0, 0.5));
        ticketPaper.setEffect(shadow);

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
            InputStream imageStream = getClass().getResourceAsStream("/sistemafarmacia/assets/icons/logo.png");
            if (imageStream != null) {
                logoView.setImage(new Image(imageStream));
                logoView.setFitWidth(120);
                logoView.setPreserveRatio(true);
                VBox.setMargin(logoView, new Insets(0, 0, 10, 0));
            }
        } catch (Exception e) {}

        Label lblNombre = new Label("UNIDAD DE HEMODIÁLISIS\nINTEGRAL SAN RAFAEL");
        lblNombre.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        lblNombre.setTextAlignment(TextAlignment.CENTER);
        lblNombre.setMaxWidth(Double.MAX_VALUE);
        lblNombre.setAlignment(Pos.CENTER);
        lblNombre.setStyle("-fx-text-fill: black;");

        lblTicketNumero = new Label("xxx-xxx-xxxx");
        lblTicketNumero.setFont(Font.font("Courier New", 11));
        lblTicketNumero.setAlignment(Pos.CENTER);
        lblTicketNumero.setStyle("-fx-text-fill: black;");

        lblTicketDireccion = new Label("C. valle embrujado 131, Pachuca, Hidalgo");
        lblTicketDireccion.setFont(Font.font("Courier New", 9));
        lblTicketDireccion.setTextAlignment(TextAlignment.CENTER);
        lblTicketDireccion.setStyle("-fx-text-fill: black;");

        VBox infoBox = new VBox(3);
        infoBox.setPadding(new Insets(15, 0, 10, 0));
        infoBox.getChildren().add(crearFilaTicket("# Ticket:", "009028", true));
        infoBox.getChildren().add(crearFilaTicket("Fecha:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), false));

        lblTicketPacienteValor = new Label("MOSTRADOR");
        lblTicketPacienteValor.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
        lblTicketPacienteValor.setStyle("-fx-text-fill: black;");
        
        HBox filaC = new HBox(new Label("Cliente:") {{ setStyle("-fx-text-fill: #666666;"); setFont(Font.font("Courier New", 11)); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, lblTicketPacienteValor);
        infoBox.getChildren().add(filaC);

        contenedorTicketProductos = new VBox(2);
        lblTicketTotalNum = new Label("$0.00");
        lblTicketTotalNum.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        lblTicketTotalNum.setStyle("-fx-text-fill: black;");

        ticketPaper.getChildren().addAll(
                logoView, lblNombre, lblTicketDireccion, lblTicketNumero,
                separadorTicket(), infoBox, separadorTicket(),
                new HBox() {{ 
                    getChildren().addAll(new Label("Producto") {{ setPrefWidth(120); }}, new Label("Cantidad") {{ setPrefWidth(40); }}, new Label("Costo") {{ setPrefWidth(60); }}, new Label("Total") {{ setPrefWidth(65); }});
                    getChildren().forEach(n -> { ((Label)n).setFont(Font.font("Courier New", FontWeight.BOLD, 11)); ((Label)n).setStyle("-fx-text-fill: black;"); });
                }},
                separadorTicket(), contenedorTicketProductos, separadorTicket(),
                new HBox(new Label("TOTAL:") {{ setFont(Font.font("Courier New", FontWeight.BOLD, 14)); setStyle("-fx-text-fill: black;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, lblTicketTotalNum),
                new Label("\n¡Gracias por su compra!") {{ setFont(Font.font("Courier New", 10)); setStyle("-fx-text-fill: #666666;"); }}
        );
    }

    private void imprimirTicket() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            // Configuramos la impresora térmica (58mm suelen ser 164-180 puntos de ancho)
            Printer printer = job.getPrinter();
            PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);

            boolean continuar = job.showPrintDialog(root.getScene().getWindow());
            if (continuar) {
                // 1. Quitar efectos visuales
                ticketPaper.setEffect(null);

                // 2. Lógica de ESCALADO para POS-58
                // Calculamos cuánto debemos encoger el diseño original (350px) para que quepa en la térmica (aprox 180px)
                double anchoImprimible = pageLayout.getPrintableWidth();
                double anchoActual = ticketPaper.getBoundsInParent().getWidth();
                double factorEscala = anchoImprimible / anchoActual;

                Scale escala = new Scale(factorEscala, factorEscala);
                ticketPaper.getTransforms().add(escala);

                // 3. Imprimir
                boolean impreso = job.printPage(pageLayout, ticketPaper);
                if (impreso) job.endJob();

                // 4. Restaurar diseño original en pantalla
                ticketPaper.getTransforms().remove(escala);
                ticketPaper.setEffect(new DropShadow(15, Color.color(0,0,0, 0.5)));
            }
        }
    }

    private Label separadorTicket() {
        return new Label("----------------------------------------") {{ setFont(Font.font("Courier New", 12)); setStyle("-fx-text-fill: #999999;"); }};
    }

    private HBox crearFilaTicket(String etiqueta, String valor, boolean negrita) {
        return new HBox(new Label(etiqueta) {{ setFont(Font.font("Courier New", 11)); setStyle("-fx-text-fill: #666666;"); }}, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, new Label(valor) {{ setFont(Font.font("Courier New", negrita ? FontWeight.BOLD : FontWeight.NORMAL, 11)); setStyle("-fx-text-fill: black;"); }});
    }

    public BorderPane getRoot() { return root; }
}