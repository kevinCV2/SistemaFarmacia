package sistemafarmacia.ui.sesiones;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sistemafarmacia.utils.ConexionDB;

import java.sql.*;

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
        
        cargarSesionesDesdeBD();
    }

    private HBox crearBarraFiltros() {
        HBox barra = new HBox(15);
        barra.setPadding(new Insets(15, 20, 15, 20));
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setStyle("-fx-background-color: #111827;");

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-border-color: #374151; -fx-border-radius: 6; -fx-padding: 6 12; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        VBox filtroPaciente = new VBox(5);
        TextField txtFiltroPaciente = crearInput("Nombre del paciente...");
        txtFiltroPaciente.setPrefWidth(250);
        filtroPaciente.getChildren().addAll(crearLabelGris("Filtrar por Paciente"), txtFiltroPaciente);
        
        txtFiltroPaciente.textProperty().addListener((obs, old, nv) -> filtrarSesiones(nv));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNuevaSesion = new Button("+ Nueva Sesión");
        btnNuevaSesion.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnNuevaSesion.setOnAction(e -> abrirModalNuevaSesion());

        barra.getChildren().addAll(btnVolver, filtroPaciente, spacer, btnNuevaSesion);
        return barra;
    }

    private ScrollPane crearListado() {
        contenedorSesiones = new VBox(20);
        contenedorSesiones.setPadding(new Insets(20));
        contenedorSesiones.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(contenedorSesiones);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        
        return scrollPane;
    }

    private void abrirModalNuevaSesion() {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Registrar Nueva Sesión");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #111827;");

        TextField txtPaciente = crearInput("Nombre completo del paciente");
        TextField txtConsulta = crearInput("Motivo o diagnóstico");
        TextField txtTotal = crearInput("0.00");

        // --- OPCIÓN DE CRÉDITO ---
        CheckBox chkCredito = new CheckBox("Marcar como pago a CRÉDITO");
        chkCredito.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 13px; -fx-cursor: hand;");

        Label lblListaMeds = new Label("Medicamentos seleccionados: Ninguno");
        lblListaMeds.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        lblListaMeds.setWrapText(true);

        TextField txtMedsFinal = new TextField(); 
        txtMedsFinal.setVisible(false);
        txtMedsFinal.setManaged(false);

        TextField txtBuscadorMeds = crearInput("Escriba para buscar y añadir...");
        ContextMenu suggestionsMenu = new ContextMenu();

        txtBuscadorMeds.textProperty().addListener((obs, old, nv) -> {
            if (nv.isEmpty()) { suggestionsMenu.hide(); return; }
            suggestionsMenu.getItems().clear();
            try (Connection conn = ConexionDB.getInstance();
                 PreparedStatement ps = conn.prepareStatement("SELECT nombre FROM medicamentos WHERE nombre ILIKE ? LIMIT 5")) {
                ps.setString(1, "%" + nv + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    MenuItem item = new MenuItem(rs.getString("nombre"));
                    item.setOnAction(e -> {
                        String actual = txtMedsFinal.getText();
                        if (actual.isEmpty()) {
                            txtMedsFinal.setText(item.getText());
                        } else {
                            if (!actual.contains(item.getText())) {
                                txtMedsFinal.setText(actual + ", " + item.getText());
                            }
                        }
                        lblListaMeds.setText("Seleccionados: " + txtMedsFinal.getText());
                        txtBuscadorMeds.clear();
                        suggestionsMenu.hide();
                    });
                    suggestionsMenu.getItems().add(item);
                }
                if (!suggestionsMenu.getItems().isEmpty()) {
                    if (!suggestionsMenu.isShowing()) suggestionsMenu.show(txtBuscadorMeds, Side.BOTTOM, 0, 0);
                } else { suggestionsMenu.hide(); }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button btnLimpiar = new Button("Limpiar Medicamentos");
        btnLimpiar.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px;");
        btnLimpiar.setOnAction(e -> {
            txtMedsFinal.clear();
            lblListaMeds.setText("Medicamentos seleccionados: Ninguno");
        });

        Button btnGuardar = new Button("Confirmar y Guardar Sesión");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand;");
        
        btnGuardar.setOnAction(e -> {
            if (validarYGuardar(txtPaciente.getText(), txtConsulta.getText(), txtTotal.getText(), txtMedsFinal.getText(), chkCredito.isSelected())) {
                cargarSesionesDesdeBD();
                modal.close();
            }
        });

        layout.getChildren().addAll(
            crearLabelGris("Paciente"), txtPaciente,
            crearLabelGris("Motivo de Consulta"), txtConsulta,
            crearLabelGris("Total ($)"), txtTotal,
            chkCredito,
            new Separator(){{ setStyle("-fx-opacity: 0.1;"); }},
            crearLabelGris("Añadir Medicamento (Busque y haga clic)"), txtBuscadorMeds,
            lblListaMeds, btnLimpiar,
            new Separator(){{ setStyle("-fx-opacity: 0.2;"); }},
            btnGuardar
        );

        modal.setScene(new Scene(layout, 450, 650));
        modal.showAndWait();
    }

    private boolean validarYGuardar(String pac, String con, String tot, String meds, boolean esCredito) {
        if (pac.isEmpty() || meds.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Paciente y medicamentos son obligatorios.").show();
            return false;
        }

        String sql = "INSERT INTO sesiones (paciente, consulta, total, medicamentos, fecha, estado_pago) VALUES (?, ?, ?, ?, CURRENT_DATE, ?)";
        try (Connection conn = ConexionDB.getInstance(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pac.toUpperCase());
            ps.setString(2, con);
            ps.setDouble(3, Double.parseDouble(tot.replace("$", "")));
            ps.setString(4, meds.toUpperCase());
            ps.setString(5, esCredito ? "CREDITO" : "PAGADO");
            ps.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private void cargarSesionesDesdeBD() {
        contenedorSesiones.getChildren().clear();
        String sql = "SELECT * FROM sesiones ORDER BY id_sesion DESC";
        try (Connection conn = ConexionDB.getInstance(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                contenedorSesiones.getChildren().add(crearCardSesion(
                    rs.getString("paciente"), rs.getString("consulta"),
                    rs.getString("fecha"), "$" + rs.getDouble("total"),
                    rs.getString("medicamentos"),
                    rs.getString("estado_pago")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void filtrarSesiones(String nombre) {
        contenedorSesiones.getChildren().clear();
        String sql = "SELECT * FROM sesiones WHERE paciente ILIKE ? ORDER BY id_sesion DESC";
        try (Connection conn = ConexionDB.getInstance(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nombre + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                contenedorSesiones.getChildren().add(crearCardSesion(
                    rs.getString("paciente"), rs.getString("consulta"),
                    rs.getString("fecha"), "$" + rs.getDouble("total"),
                    rs.getString("medicamentos"),
                    rs.getString("estado_pago")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox crearCardSesion(String paciente, String consulta, String fecha, String total, String medicamentos, String estado) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e293b, #111827); " +
                      "-fx-background-radius: 12; -fx-border-color: #334155; -fx-border-radius: 12; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        HBox header = new HBox(30);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox colPac = new VBox(5, crearLabelGris("PACIENTE"), new Label(paciente){{ setTextFill(Color.WHITE); setFont(Font.font("System", FontWeight.BOLD, 15)); }});
        VBox colCon = new VBox(5, crearLabelGris("CONSULTA"), new Label(consulta){{ setTextFill(Color.web("#cbd5e1")); }});
        VBox colFec = new VBox(5, crearLabelGris("FECHA"), new Label(fecha){{ setTextFill(Color.web("#94a3b8")); }});

        // Etiqueta de Estado (Badge)
        Label badgeEstado = new Label(estado);
        if ("CREDITO".equals(estado)) {
            badgeEstado.setStyle("-fx-background-color: #78350f; -fx-text-fill: #fbbf24; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 9; -fx-font-weight: bold;");
        } else {
            badgeEstado.setStyle("-fx-background-color: #064e3b; -fx-text-fill: #34d399; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 9; -fx-font-weight: bold;");
        }

        VBox colTot = new VBox(5, crearLabelGris("TOTAL"), new HBox(8, new Label(total){{ setTextFill(Color.web("#38bdf8")); setFont(Font.font("System", FontWeight.BOLD, 16)); }}, badgeEstado));
        colTot.setAlignment(Pos.CENTER_RIGHT);

        HBox.setHgrow(colCon, Priority.ALWAYS);
        header.getChildren().addAll(colPac, colCon, colFec, colTot);

        Label lblMeds = new Label("💊 MEDICAMENTOS: " + (medicamentos != null ? medicamentos : "N/A"));
        lblMeds.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #38bdf8; -fx-padding: 8 12; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblMeds.setWrapText(true);

        card.getChildren().addAll(header, new Separator(){{ setStyle("-fx-opacity: 0.1;"); }}, lblMeds);
        return card;
    }

    private Label crearLabelGris(String t) {
        Label l = new Label(t);
        l.setTextFill(Color.web("#64748b"));
        l.setFont(Font.font("System", FontWeight.BOLD, 10));
        return l;
    }

    private TextField crearInput(String p) {
        TextField tf = new TextField();
        tf.setPromptText(p);
        tf.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-prompt-text-fill: #475569; -fx-border-color: #1e293b; -fx-background-radius: 6; -fx-border-radius: 6; -fx-padding: 10;");
        return tf;
    }

    public BorderPane getRoot() { return root; }
}