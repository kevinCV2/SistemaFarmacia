package sistemafarmacia.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
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
        VBox container = new VBox(30);
        container.setPadding(new Insets(25));

        // Estadísticas
        HBox stats = new HBox(20);
        stats.getChildren().addAll(
                UIComponents.statCard("Medicamentos", "128"),
                UIComponents.statCard("Ventas Hoy", "$3,450"),
                UIComponents.statCard("Clientes", "56"),
                UIComponents.statCard("Proveedores", "12")
        );

        // Módulos principales
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);

        grid.add(UIComponents.bigCard("Catálogo de Medicamentos", "#2563eb"), 0, 0);
        grid.add(UIComponents.bigCard("Nuevo Producto", "#16a34a"), 1, 0);
        grid.add(UIComponents.bigCard("Sesiones y Ventas", "#0ea5e9"), 2, 0);

        grid.add(UIComponents.bigCard("Generar Ticket", "#d97706"), 0, 1);
        grid.add(UIComponents.bigCard("Corte Semanal", "#7c3aed"), 1, 1);
        grid.add(UIComponents.bigCard("Filtros", "#dc2626"), 2, 1);

        container.getChildren().addAll(stats, grid);
        return container;
    }

    public BorderPane getRoot() {
        return root;
    }
}
