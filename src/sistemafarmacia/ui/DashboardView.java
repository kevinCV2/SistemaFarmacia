package sistemafarmacia.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import sistemafarmacia.utils.UIComponents;

public class DashboardView {

    private BorderPane root;

    public DashboardView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        root.setTop(UIComponents.createHeader());
        root.setCenter(createCenter());
    }

    private VBox createCenter() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));

        // Tarjetas pequeñas
        HBox stats = new HBox(20);
        stats.getChildren().addAll(
                UIComponents.statCard("Medicamentos", "128"),
                UIComponents.statCard("Ventas Hoy", "$3,450"),
                UIComponents.statCard("Clientes", "56"),
                UIComponents.statCard("Proveedores", "12")
        );

        // Tarjetas grandes
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        grid.add(UIComponents.bigCard("Gestión de Medicamentos", "#2563eb"), 0, 0);
        grid.add(UIComponents.bigCard("Ventas", "#16a34a"), 1, 0);
        grid.add(UIComponents.bigCard("Inventario", "#d97706"), 0, 1);
        grid.add(UIComponents.bigCard("Reportes", "#7c3aed"), 1, 1);

        container.getChildren().addAll(stats, grid);
        return container;
    }

    public BorderPane getRoot() {
        return root;
    }
}
