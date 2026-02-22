package sistemafarmacia.ui.sesiones;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SesionesView {

    private BorderPane root;
    private VBox contenedorSesiones;
    private Runnable actionVolver;

    public SesionesView(Runnable actionVolver) {
        this.actionVolver = actionVolver;

        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        root.setTop(crearBarraFiltros());
        root.setCenter(crearListado());
    }

    /* ================= BARRA SUPERIOR ================= */

    private HBox crearBarraFiltros() {
        HBox barra = new HBox(15);
        barra.setPadding(new Insets(15));
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setStyle("-fx-background-color: #111827;");

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #9ca3af;
            -fx-border-color: #374151;
            -fx-border-radius: 6;
            -fx-padding: 6 12;
            -fx-cursor: hand;
        """);

        btnVolver.setOnAction(e -> {
            if (actionVolver != null) actionVolver.run();
        });

        VBox filtroPaciente = new VBox(5);
        filtroPaciente.getChildren().addAll(
                crearLabelGris("Filtrar por Paciente"),
                crearInput("Nombre del paciente...")
        );

        VBox filtroFecha = new VBox(5);
        filtroFecha.getChildren().addAll(
                crearLabelGris("Filtrar por Fecha"),
                crearInput("dd/mm/aaaa")
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNueva = new Button("+ Nueva Sesión");
        btnNueva.setStyle("""
            -fx-background-color: #2563eb;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 14;
            -fx-background-radius: 6;
        """);

        btnNueva.setOnAction(e -> abrirModalNuevaSesion());

        barra.getChildren().addAll(
                btnVolver,
                filtroPaciente,
                filtroFecha,
                spacer,
                btnNueva
        );

        return barra;
    }

    /* ================= LISTADO ================= */

    private ScrollPane crearListado() {
        contenedorSesiones = new VBox(20);
        contenedorSesiones.setPadding(new Insets(20));

        // 🔹 SESIONES INICIALES (solo visualización)
        contenedorSesiones.getChildren().addAll(
                crearCardSesion(
                        "María García López",
                        "Dolor de cabeza",
                        "21/01/2026",
                        "$110.50",
                        "Paracetamol 500mg",
                        "Ibuprofeno 400mg"
                ),
                crearCardSesion(
                        "Juan Pérez Martínez",
                        "Infección respiratoria",
                        "20/01/2026",
                        "$120.00",
                        "Amoxicilina 500mg"
                )
        );

        ScrollPane scroll = new ScrollPane(contenedorSesiones);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scroll;
    }

    /* ================= MODAL NUEVA SESIÓN ================= */

    private void abrirModalNuevaSesion() {

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Nueva Sesión");

        VBox rootModal = new VBox(14);
        rootModal.setPadding(new Insets(20));
        rootModal.setStyle("-fx-background-color: #111827;");

        Label title = new Label("Registrar Nueva Sesión");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(18));

        TextField txtPaciente = crearInput("Nombre del paciente");
        TextField txtConsulta = crearInput("Motivo de consulta");
        TextField txtFecha = crearInput("dd/mm/aaaa");
        TextField txtTotal = crearInput("$0.00");
        TextField txtMedicamentos = crearInput("Medicamentos separados por coma");

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setStyle("""
            -fx-background-color: #2563eb;
            -fx-text-fill: white;
            -fx-font-weight: bold;
        """);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("""
            -fx-background-color: #374151;
            -fx-text-fill: white;
        """);

        btnCancelar.setOnAction(e -> modal.close());

        btnGuardar.setOnAction(e -> {
            if (txtPaciente.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING,
                        "El nombre del paciente es obligatorio").show();
                return;
            }

            String[] meds = txtMedicamentos.getText().isBlank()
                    ? new String[]{}
                    : txtMedicamentos.getText().split(",");

            contenedorSesiones.getChildren().add(
                    crearCardSesion(
                            txtPaciente.getText(),
                            txtConsulta.getText(),
                            txtFecha.getText(),
                            txtTotal.getText(),
                            meds
                    )
            );

            modal.close();
        });

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        rootModal.getChildren().addAll(
                title,
                crearLabelGris("Paciente"), txtPaciente,
                crearLabelGris("Consulta"), txtConsulta,
                crearLabelGris("Fecha"), txtFecha,
                crearLabelGris("Total"), txtTotal,
                crearLabelGris("Medicamentos"), txtMedicamentos,
                botones
        );

        modal.setScene(new Scene(rootModal, 420, 520));
        modal.showAndWait();
    }

    /* ================= CARD SESIÓN ================= */

    private VBox crearCardSesion(
            String paciente,
            String consulta,
            String fecha,
            String total,
            String... medicamentos
    ) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(18));
        card.setStyle("""
            -fx-background-color: linear-gradient(#2a2a2a, #222);
            -fx-background-radius: 10;
            -fx-border-color: #2f3947;
            -fx-border-radius: 10;
        """);

        HBox fila = new HBox(60);
        fila.setAlignment(Pos.CENTER_LEFT);

        fila.getChildren().addAll(
                crearColumna("Paciente", paciente),
                crearColumna("Consulta", consulta),
                crearColumna("Fecha", fecha),
                crearColumnaTotal(total)
        );

        FlowPane chips = new FlowPane(8, 8);
        for (String med : medicamentos) {
            chips.getChildren().add(crearChip(med.trim()));
        }

        card.getChildren().addAll(
                fila,
                new Separator(),
                crearLabelGris("Medicamentos"),
                chips
        );

        return card;
    }

    /* ================= COMPONENTES ================= */

    private VBox crearColumna(String titulo, String valor) {
        VBox col = new VBox(6);
        col.getChildren().addAll(
                crearLabelGris(titulo),
                crearValor(valor)
        );
        return col;
    }

    private VBox crearColumnaTotal(String total) {
        VBox col = new VBox(6);
        Label lbl = new Label(total);
        lbl.setTextFill(Color.web("#22d3ee"));
        lbl.setFont(Font.font(null, FontWeight.BOLD, 14));
        col.getChildren().addAll(crearLabelGris("Total"), lbl);
        return col;
    }

    private Label crearValor(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setFont(Font.font(null, FontWeight.BOLD, 13));
        return l;
    }

    private Label crearLabelGris(String texto) {
        Label l = new Label(texto);
        l.setTextFill(Color.web("#9ca3af"));
        l.setFont(Font.font(12));
        return l;
    }

    private TextField crearInput(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("""
            -fx-background-color: #0f172a;
            -fx-text-fill: white;
            -fx-prompt-text-fill: #6b7280;
            -fx-border-color: #374151;
            -fx-background-radius: 6;
            -fx-border-radius: 6;
        """);
        return tf;
    }

    private Label crearChip(String texto) {
        Label chip = new Label(texto);
        chip.setStyle("""
            -fx-background-color: #374151;
            -fx-text-fill: white;
            -fx-padding: 4 10;
            -fx-background-radius: 6;
            -fx-font-size: 11;
        """);
        return chip;
    }

    public BorderPane getRoot() {
        return root;
    }
}