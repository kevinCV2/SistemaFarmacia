package sistemafarmacia.ui.catalogo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import sistemafarmacia.utils.ConexionDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

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

        Label subtitle = new Label("Medicamentos y Estudios");
        subtitle.setTextFill(Color.web("#9ca3af"));

        headerText.getChildren().addAll(title, subtitle);
        topBar.getChildren().addAll(btnVolver, headerText);

        // ─── TABLA ───────────────────────────────────────────────
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-base: #111827; -fx-control-inner-background: #111827; -fx-background-color: #111827;");

        // Personalización de filas para el color azul de categoría
        table.setRowFactory(tv -> new TableRow<ObservableList<Object>>() {
            @Override
            protected void updateItem(ObservableList<Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.get(3).equals(true)) { // El índice 3 indica si es cabecera
                    setStyle("-fx-background-color: #1e40af; -fx-font-weight: bold;"); // Azul intenso
                } else {
                    setStyle(""); 
                }
            }
        });

        table.getColumns().add(createColumn("No.", 0, Pos.CENTER, 50));
        table.getColumns().add(createColumn("Nombre / Concepto", 1, Pos.CENTER_LEFT, -1));
        table.getColumns().add(createColumn("Precio", 2, Pos.CENTER, 150));

        cargarDatosDesdeDB();

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(topBar, table);
        root.setCenter(content);
    }

    private void cargarDatosDesdeDB() {
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();

        // Mantenemos el orden para que los bloques salgan agrupados
        String sql = "SELECT nombre, precio, categoria FROM public.insumos ORDER BY categoria DESC, nombre ASC";

        try {
            Connection conn = ConexionDB.getInstance();
            if (conn != null) {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql);

                String categoriaActual = "";
                int contador = 1;

                while (rs.next()) {
                    String catRaw = rs.getString("categoria");
                    if (catRaw == null) {
                        catRaw = "INSUMO";
                    }

                    // Lógica de visualización de categorías
                    String catDisplay;
                    if (catRaw.equalsIgnoreCase("INSUMO")) {
                        catDisplay = "MEDICAMENTOS";
                    } else if (catRaw.equalsIgnoreCase("ANÁLISIS DE LABORATORIO") || catRaw.equalsIgnoreCase("ESTUDIO")) {
                        catDisplay = "ANÁLISIS DE LABORATORIO";
                    } else {
                        catDisplay = catRaw.toUpperCase();
                    }

                    // Si cambia la categoría, insertamos la fila azul de cabecera
                    if (!catDisplay.equals(categoriaActual)) {
                        categoriaActual = catDisplay;
                        ObservableList<Object> filaCabecera = FXCollections.observableArrayList();
                        filaCabecera.add("");              // No.
                        filaCabecera.add(categoriaActual); // Nombre de la Categoría
                        filaCabecera.add("");              // Precio
                        filaCabecera.add(true);            // ES_CABECERA = true
                        data.add(filaCabecera);
                        contador = 1; // Reiniciar contador para la nueva sección
                    }

                    ObservableList<Object> fila = FXCollections.observableArrayList();
                    fila.add(contador++);
                    fila.add(rs.getString("nombre"));
                    fila.add("$" + String.format("%.2f", rs.getDouble("precio")));
                    fila.add(false); // ES_CABECERA = false

                    data.add(fila);
                }
                table.setItems(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TableColumn<ObservableList<Object>, Object> createColumn(String title, int index, Pos alignment, double width) {
        TableColumn<ObservableList<Object>, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().get(index)));
        if (width > 0) col.setPrefWidth(width);
        
        col.setCellFactory(column -> new TableCell<ObservableList<Object>, Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setAlignment(alignment);
                    // Si es cabecera, el texto va en blanco
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