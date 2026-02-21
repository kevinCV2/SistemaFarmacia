package sistemafarmacia.ui.ticket;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.print.PrinterJob;

public class GenerarTicketView {

    private BorderPane root;
    private VBox ticketPaper;
    private Runnable actionVolver;

    private Label lblTicketDireccion;
    private Label lblTicketNumero;
    private Label lblTicketPacienteValor;

    // --- VARIABLES GLOBALES NUEVAS PARA EL TICKET ---
    private VBox contenedorProductosFormulario; // El VBox de la izquierda (Formulario)
    private VBox contenedorTicketProductos;     // El VBox de la derecha (Ticket)
    private Label lblTicketTotalNum;            // Para cambiar el gran total

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

        // --- TARJETA 1: DATOS ---
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

        // --- TARJETA 2: PRODUCTOS ---
        VBox cardProductos = new VBox(15);
        cardProductos.setStyle("-fx-background-color: #111827; -fx-padding: 20; -fx-background-radius: 10;");

        HBox headerProd = new HBox();
        headerProd.setAlignment(Pos.CENTER_LEFT);
        Label lblProd = new Label("Productos");
        lblProd.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // INICIALIZAMOS EL VBOX GLOBAL DEL FORMULARIO
        contenedorProductosFormulario = new VBox(10);

        Button btnAgregar = new Button("+ Agregar");
        btnAgregar.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
        btnAgregar.setOnAction(e -> agregarFilaProducto());

        headerProd.getChildren().addAll(lblProd, spacer, btnAgregar);
        cardProductos.getChildren().addAll(headerProd, contenedorProductosFormulario);

        agregarFilaProducto(); // Fila inicial

        contenidoScroll.getChildren().addAll(cardDatos, cardProductos);

        ScrollPane scrollPane = new ScrollPane(contenidoScroll);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-base: #1f2933; -fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.getStylesheets().add("data:text/css,.scroll-bar:vertical { -fx-background-color: transparent; -fx-pref-width: 8; } .scroll-bar:vertical .thumb { -fx-background-color: rgb(75, 85, 99); -fx-background-radius: 5; }");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button btnImprimir = new Button("🖨 Imprimir Ticket");
        btnImprimir.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
        btnImprimir.setMaxWidth(Double.MAX_VALUE);

        btnImprimir.setOnAction(e -> imprimirTicket());

        panelPrincipal.getChildren().addAll(scrollPane, btnImprimir);
        return panelPrincipal;
    }

    // ==========================================
    // MÉTODOS PARA MANEJAR PRODUCTOS
    // ==========================================
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
        txtCant.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

        TextField txtPrecio = crearTextField("Precio");
        txtPrecio.setPrefWidth(80);
        // Validación extra: Que el precio solo acepte números y un punto decimal
        txtPrecio.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*\\.?\\d*") ? change : null));

        Button btnEliminar = new Button("×");
        btnEliminar.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

        btnEliminar.setOnAction(e -> {
            if(contenedorProductosFormulario.getChildren().size() > 1) {
                contenedorProductosFormulario.getChildren().remove(fila);
                actualizarTablaTicket(); // MAGIA: Si borras, se actualiza el ticket
            }
        });

        // Escáner de código de barras
        txtId.setOnAction(e -> verificarIdEscaneado(txtId, fila));
        txtId.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) verificarIdEscaneado(txtId, fila);
        });

        // --- MAGIA NUEVA: ACTUALIZACIÓN EN TIEMPO REAL ---
        // Le decimos a los campos "Si cambias de texto, avísale al ticket para que se vuelva a dibujar"
        txtNomProd.textProperty().addListener((obs, old, newV) -> actualizarTablaTicket());
        txtCant.textProperty().addListener((obs, old, newV) -> actualizarTablaTicket());
        txtPrecio.textProperty().addListener((obs, old, newV) -> actualizarTablaTicket());

        fila.getChildren().addAll(txtId, txtNomProd, txtCant, txtPrecio, btnEliminar);
        contenedorProductosFormulario.getChildren().add(fila);

        actualizarTablaTicket(); // Actualizamos al agregar la fila
    }

    private void verificarIdEscaneado(TextField txtIdActual, HBox filaActual) {
        String idBuscado = txtIdActual.getText().trim();
        if (idBuscado.isEmpty()) return;

        for (Node nodo : contenedorProductosFormulario.getChildren()) {
            HBox otraFila = (HBox) nodo;
            if (otraFila != filaActual) {
                TextField txtOtroId = (TextField) otraFila.getChildren().get(0);
                if (idBuscado.equalsIgnoreCase(txtOtroId.getText().trim())) {
                    TextField txtOtraCant = (TextField) otraFila.getChildren().get(2);
                    try {
                        int cantActual = Integer.parseInt(txtOtraCant.getText());
                        txtOtraCant.setText(String.valueOf(cantActual + 1));
                        // Al hacer .setText(), el listener dispara actualizarTablaTicket() automáticamente. ¡Magia pura!
                    } catch (NumberFormatException ex) {
                        txtOtraCant.setText("2");
                    }
                    txtIdActual.clear();
                    txtIdActual.requestFocus();
                    return;
                }
            }
        }
    }

    // ==========================================
    // MAGIA NUEVA: SINCRONIZAR TICKET CON FORMULARIO
    // ==========================================
    private void actualizarTablaTicket() {
        // Prevención de errores si se llama antes de que el ticket se construya
        if (contenedorTicketProductos == null || lblTicketTotalNum == null) return;

        // 1. Limpiamos la tabla del ticket actual
        contenedorTicketProductos.getChildren().clear();
        double granTotal = 0.0;

        // 2. Recorremos todas las filas del formulario (izquierda)
        for (Node nodo : contenedorProductosFormulario.getChildren()) {
            HBox fila = (HBox) nodo;

            // Extraemos los campos (0: ID, 1: Nombre, 2: Cant, 3: Precio)
            TextField txtNom = (TextField) fila.getChildren().get(1);
            TextField txtCant = (TextField) fila.getChildren().get(2);
            TextField txtPrecio = (TextField) fila.getChildren().get(3);

            String nombre = txtNom.getText().trim();
            String cantStr = txtCant.getText().trim();
            String precioStr = txtPrecio.getText().trim();

            // Si no tiene nombre y no tiene precio, no lo imprimimos en el ticket
            if (nombre.isEmpty() && precioStr.isEmpty()) continue;

            int cant = 1;
            double precio = 0.0;

            // Convertimos los textos a números (atrapando errores si escriben cosas raras o dejan vacío)
            try { if (!cantStr.isEmpty()) cant = Integer.parseInt(cantStr); } catch (Exception ignored) {}
            try { if (!precioStr.isEmpty()) precio = Double.parseDouble(precioStr); } catch (Exception ignored) {}

            // Calculamos el subtotal de esta fila
            double subtotal = cant * precio;
            granTotal += subtotal; // Sumamos a la cuenta total

            // 3. Dibujamos la fila en el Ticket
            HBox ticketRow = new HBox();

            Label lNom = new Label(nombre.isEmpty() ? "---" : nombre.toUpperCase());
            lNom.setPrefWidth(120);
            lNom.setFont(Font.font("Courier New", 10));
            lNom.setStyle("-fx-text-fill: black;");

            Label lCant = new Label(String.valueOf(cant));
            lCant.setPrefWidth(40);
            lCant.setAlignment(Pos.CENTER);
            lCant.setFont(Font.font("Courier New", 10));
            lCant.setStyle("-fx-text-fill: black;");

            Label lPrecio = new Label(String.format("$%.2f", precio));
            lPrecio.setPrefWidth(60);
            lPrecio.setAlignment(Pos.CENTER_RIGHT);
            lPrecio.setFont(Font.font("Courier New", 10));
            lPrecio.setStyle("-fx-text-fill: black;");

            Label lSubtotal = new Label(String.format("$%.2f", subtotal));
            lSubtotal.setPrefWidth(60);
            lSubtotal.setAlignment(Pos.CENTER_RIGHT);
            lSubtotal.setFont(Font.font("Courier New", FontWeight.BOLD, 10)); // Subtotal en negrita
            lSubtotal.setStyle("-fx-text-fill: black;");

            ticketRow.getChildren().addAll(lNom, lCant, lPrecio, lSubtotal);
            contenedorTicketProductos.getChildren().add(ticketRow); // Añadimos al ticket
        }

        // 4. Actualizamos el Gran Total en la pantalla
        lblTicketTotalNum.setText(String.format("$%.2f", granTotal));
    }

    private TextField crearTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #1f2933; -fx-text-fill: white; -fx-prompt-text-fill: #6b7280; -fx-border-color: #374151; -fx-border-radius: 5; -fx-padding: 8;");
        return tf;
    }

    // ==========================================
    // PARTE DERECHA: VISTA PREVIA DEL TICKET
    // ==========================================
    private VBox crearPanelTicket() {
        VBox contenedor = new VBox();
        contenedor.setAlignment(Pos.TOP_CENTER);

        ticketPaper = new VBox(5);
        ticketPaper.setPadding(new Insets(30));
        ticketPaper.setStyle("-fx-background-color: #ffffff;");
        ticketPaper.setMaxWidth(350);

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
        Label lblNombre = new Label("UNIDAD DE HEMODIÁLISIS\nINTEGRAL SAN RAFAEL");
        lblNombre.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        lblNombre.setTextAlignment(TextAlignment.CENTER);
        lblNombre.setMaxWidth(Double.MAX_VALUE);
        lblNombre.setAlignment(Pos.CENTER);
        lblNombre.setStyle("-fx-text-fill: black;");

        lblTicketNumero = new Label("xxx-xxx-xxxx");
        lblTicketNumero.setFont(Font.font("Courier New", 11));
        lblTicketNumero.setTextAlignment(TextAlignment.CENTER);
        lblTicketNumero.setMaxWidth(Double.MAX_VALUE);
        lblTicketNumero.setAlignment(Pos.CENTER);
        lblTicketNumero.setStyle("-fx-text-fill: black;");

        lblTicketDireccion = new Label("C. valle embrujado 131, Esquina Santa Ana,\nvalle de San Javier, CP 42086,\nPachuca de Soto, Hidalgo");
        lblTicketDireccion.setFont(Font.font("Courier New", 9));
        lblTicketDireccion.setTextAlignment(TextAlignment.CENTER);
        lblTicketDireccion.setMaxWidth(Double.MAX_VALUE);
        lblTicketDireccion.setAlignment(Pos.CENTER);
        lblTicketDireccion.setStyle("-fx-text-fill: black;");

        VBox infoBox = new VBox(3);
        infoBox.setPadding(new Insets(15, 0, 10, 0));
        infoBox.getChildren().add(crearFilaTicket("# Ticket:", "009028", true));
        infoBox.getChildren().add(crearFilaTicket("Fecha:", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), false));

        HBox filaPaciente = new HBox();
        Label lblEtiquetaPaciente = new Label("Cliente:");
        lblEtiquetaPaciente.setFont(Font.font("Courier New", 11));
        lblEtiquetaPaciente.setStyle("-fx-text-fill: #666666;");

        Region spacerPaciente = new Region();
        HBox.setHgrow(spacerPaciente, Priority.ALWAYS);

        lblTicketPacienteValor = new Label("MOSTRADOR");
        lblTicketPacienteValor.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
        lblTicketPacienteValor.setStyle("-fx-text-fill: black;");

        filaPaciente.getChildren().addAll(lblEtiquetaPaciente, spacerPaciente, lblTicketPacienteValor);
        infoBox.getChildren().add(filaPaciente);

        HBox headerTabla = new HBox();
        headerTabla.setPadding(new Insets(10, 0, 5, 0));
        Label hProd = new Label("Producto"); hProd.setFont(Font.font("Courier New", FontWeight.BOLD, 11)); hProd.setStyle("-fx-text-fill: black;");
        Label hCant = new Label("Cant"); hCant.setFont(Font.font("Courier New", FontWeight.BOLD, 11)); hCant.setStyle("-fx-text-fill: black;");
        Label hPrecio = new Label("Precio"); hPrecio.setFont(Font.font("Courier New", FontWeight.BOLD, 11)); hPrecio.setStyle("-fx-text-fill: black;");
        Label hTotal = new Label("Total"); hTotal.setFont(Font.font("Courier New", FontWeight.BOLD, 11)); hTotal.setStyle("-fx-text-fill: black;");

        hProd.setPrefWidth(120);
        hCant.setPrefWidth(40); hCant.setAlignment(Pos.CENTER);
        hPrecio.setPrefWidth(60); hPrecio.setAlignment(Pos.CENTER_RIGHT);
        hTotal.setPrefWidth(60); hTotal.setAlignment(Pos.CENTER_RIGHT);
        headerTabla.getChildren().addAll(hProd, hCant, hPrecio, hTotal);

        // --- CONTENEDOR NUEVO PARA LAS FILAS DEL TICKET ---
        contenedorTicketProductos = new VBox(2);

        HBox totalBox = new HBox();
        totalBox.setPadding(new Insets(20, 0, 20, 0));
        Label lblTotalTxt = new Label("TOTAL:");
        lblTotalTxt.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        lblTotalTxt.setStyle("-fx-text-fill: black;");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // INICIALIZAMOS EL TOTAL
        lblTicketTotalNum = new Label("$0.00");
        lblTicketTotalNum.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        lblTicketTotalNum.setStyle("-fx-text-fill: black;");
        totalBox.getChildren().addAll(lblTotalTxt, spacer, lblTicketTotalNum);

        Label lblFooter = new Label("¡Gracias por su compra!\nConserve su ticket");
        lblFooter.setFont(Font.font("Courier New", 10));
        lblFooter.setTextAlignment(TextAlignment.CENTER);
        lblFooter.setMaxWidth(Double.MAX_VALUE);
        lblFooter.setAlignment(Pos.CENTER);
        lblFooter.setStyle("-fx-text-fill: #666666;");

        ticketPaper.getChildren().addAll(
                lblNombre,
                lblTicketDireccion,
                lblTicketNumero,
                separadorTicket(),
                infoBox,
                separadorTicket(),
                headerTabla,
                separadorTicket(),
                contenedorTicketProductos, // <- Agregamos el contenedor de los productos aquí
                separadorTicket(),
                totalBox,
                lblFooter
        );
    }

    private Label separadorTicket() {
        Label sep = new Label("----------------------------------------");
        sep.setFont(Font.font("Courier New", 12));
        sep.setStyle("-fx-text-fill: #999999;");
        sep.setMaxWidth(Double.MAX_VALUE);
        sep.setAlignment(Pos.CENTER);
        return sep;
    }

    private HBox crearFilaTicket(String etiqueta, String valor, boolean negritaValor) {
        HBox row = new HBox();
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setFont(Font.font("Courier New", 11));
        lblEtiqueta.setStyle("-fx-text-fill: #666666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Courier New", negritaValor ? FontWeight.BOLD : FontWeight.NORMAL, 11));
        lblValor.setStyle("-fx-text-fill: black;");

        row.getChildren().addAll(lblEtiqueta, spacer, lblValor);
        return row;
    }

    //IMPRIMIR EL TICKET
    private void imprimirTicket() {
        // 1. Creamos un "Trabajo de Impresión"
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            // 2. Mostramos el cuadro de diálogo de Windows para elegir impresora
            // Necesitamos pasarle la "ventana" actual
            boolean continuar = job.showPrintDialog(root.getScene().getWindow());

            if (continuar) {
                // TRUCO PRO: Quitamos la sombra 3D para que la impresora no intente imprimirla
                ticketPaper.setEffect(null);

                // 3. ¡Mandamos el papel blanco a la impresora!
                boolean impreso = job.printPage(ticketPaper);

                if (impreso) {
                    job.endJob(); // Finalizamos el proceso
                    System.out.println("¡Ticket impreso correctamente!");
                } else {
                    System.out.println("Error al intentar imprimir.");
                }

                // Le devolvemos su sombra para que se siga viendo bonito en pantalla
                DropShadow shadow = new DropShadow(15, Color.color(0,0,0, 0.5));
                ticketPaper.setEffect(shadow);
            }
        } else {
            // Si entra aquí, es porque la computadora no tiene ni el "Microsoft Print to PDF" instalado
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de Impresora");
            alerta.setHeaderText(null);
            alerta.setContentText("No se detectó ninguna impresora instalada en el sistema.");
            alerta.showAndWait();
        }
    }

    public BorderPane getRoot() {
        return root;
    }


}