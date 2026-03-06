package sistemafarmacia.ui.catalogo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import sistemafarmacia.utils.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class CatalogoMedicamentoView {

    private BorderPane root;
    private TableView<ObservableList<Object>> table;
    private Runnable actionVolver;

    public CatalogoMedicamentoView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // ─── CABECERA ─────────────────────────────────────────────
        HBox topBar = new HBox(25);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Volver");
        btnVolver.setStyle("-fx-background-color: transparent; -fx-text-fill: #9ca3af; -fx-border-color: #374151; -fx-border-radius: 5; -fx-cursor: hand;");
        btnVolver.setOnAction(e -> { if (actionVolver != null) actionVolver.run(); });

        VBox headerText = new VBox(5);
        Label title = new Label("Catálogo General");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Gestión de Precios y Conceptos");
        subtitle.setTextFill(Color.web("#9ca3af"));

        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        // ─── TABLA ───────────────────────────────────────────────
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-base: #111827; -fx-control-inner-background: #111827; -fx-background-color: #111827;");

        table.setRowFactory(tv -> new TableRow<ObservableList<Object>>() {
            @Override
            protected void updateItem(ObservableList<Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.size() > 3 && item.get(3).equals(true)) {
                    setStyle("-fx-background-color: #1e40af; -fx-font-weight: bold;");
                } else {
                    setStyle(""); 
                }
            }
        });

        table.getColumns().add(createColumn("No.", 0, Pos.CENTER, 50));
        table.getColumns().add(createColumn("Nombre / Concepto", 1, Pos.CENTER_LEFT, -1));
        table.getColumns().add(createColumn("Precio", 2, Pos.CENTER, 120));

        // ─── COLUMNA DE ACCIONES ──────────────────────────────────
        TableColumn<ObservableList<Object>, Void> accionesCol = new TableColumn<>("Acciones");
        accionesCol.setPrefWidth(120);
        accionesCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("📝");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(10, btnEdit, btnDelete);
            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setStyle("-fx-background-color:#3b82f6; -fx-text-fill:white; -fx-cursor:hand;");
                btnDelete.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-cursor:hand;");

                btnEdit.setOnAction(e -> {
                    ObservableList<Object> fila = getTableView().getItems().get(getIndex());
                    abrirVentanaEdicion(fila);
                });

                btnDelete.setOnAction(e -> {
                    ObservableList<Object> fila = getTableView().getItems().get(getIndex());
                    confirmarEliminacion(fila.get(1).toString()); // Usamos el nombre como identificador
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || (getTableRow().getItem() != null && (boolean)getTableRow().getItem().get(3))) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
        table.getColumns().add(accionesCol);

        cargarDatosDesdeDB();

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(topBar, table);
        root.setCenter(content);
    }

    private void cargarDatosDesdeDB() {
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        String sql = "SELECT nombre, precio, categoria FROM public.insumos ORDER BY categoria DESC, nombre ASC";

        try (Connection conn = ConexionDB.getInstance();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            String categoriaActual = "";
            int contador = 1;

            while (rs.next()) {
                String catRaw = rs.getString("categoria") == null ? "INSUMO" : rs.getString("categoria");
                String catDisplay = catRaw.equalsIgnoreCase("INSUMO") ? "MEDICAMENTOS" : 
                                   (catRaw.equalsIgnoreCase("ESTUDIO") ? "ANÁLISIS DE LABORATORIO" : catRaw.toUpperCase());

                if (!catDisplay.equals(categoriaActual)) {
                    categoriaActual = catDisplay;
                    ObservableList<Object> filaCabecera = FXCollections.observableArrayList("", categoriaActual, "", true);
                    data.add(filaCabecera);
                    contador = 1;
                }

                ObservableList<Object> fila = FXCollections.observableArrayList(
                    contador++, 
                    rs.getString("nombre"), 
                    "$" + String.format("%.2f", rs.getDouble("precio")), 
                    false
                );
                data.add(fila);
            }
            table.setItems(data);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── LÓGICA DE EDICIÓN ──────────────────────────────────────
    private void abrirVentanaEdicion(ObservableList<Object> row) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Modificar Concepto");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1f2933;");
        
        TextField txtNombre = new TextField(row.get(1).toString());
        // Limpiamos el "$" del precio para el TextField
        String precioLimpio = row.get(2).toString().replace("$", "");
        TextField txtPrecio = new TextField(precioLimpio);

        Button btnGuardar = new Button("Actualizar");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        
        btnGuardar.setOnAction(e -> {
            actualizarInsumoDB(row.get(1).toString(), txtNombre.getText(), txtPrecio.getText());
            modal.close();
            cargarDatosDesdeDB();
        });

        Label lbl1 = new Label("Nombre / Concepto:"); lbl1.setTextFill(Color.WHITE);
        Label lbl2 = new Label("Precio ($):"); lbl2.setTextFill(Color.WHITE);

        layout.getChildren().addAll(lbl1, txtNombre, lbl2, txtPrecio, btnGuardar);
        modal.setScene(new Scene(layout, 300, 250));
        modal.showAndWait();
    }

    private void actualizarInsumoDB(String nombreOriginal, String nuevoNombre, String nuevoPrecio) {
        String sql = "UPDATE insumos SET nombre = ?, precio = ?::numeric WHERE nombre = ?";
        try (Connection conn = ConexionDB.getInstance(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setDouble(2, Double.parseDouble(nuevoPrecio));
            ps.setString(3, nombreOriginal);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── LÓGICA DE ELIMINACIÓN ──────────────────────────────────
    private void confirmarEliminacion(String nombre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.setContentText("¿Está seguro de eliminar '" + nombre + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eliminarInsumoDB(nombre);
            cargarDatosDesdeDB();
        }
    }

    private void eliminarInsumoDB(String nombre) {
        String sql = "DELETE FROM insumos WHERE nombre = ?";
        try (Connection conn = ConexionDB.getInstance(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<Object>, Object> createColumn(String title, int index, Pos alignment, double width) {
        TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().get(index)));
        if (width > 0) col.setPrefWidth(width);
        
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setAlignment(alignment);
                    if (getTableRow() != null && getTableRow().getItem() != null && (boolean)getTableRow().getItem().get(3)) {
                        setTextFill(Color.WHITE);
                    } else {
                        setTextFill(Color.web("#e5e7eb"));
                    }
                }
            }
        });
        return col;
    }

    public BorderPane getRoot() { return root; }
}