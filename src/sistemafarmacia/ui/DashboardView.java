package sistemafarmacia.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import sistemafarmacia.utils.UIComponents;

public class DashboardView {

    private BorderPane root;

    public DashboardView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        // HEADER
        root.setTop(UIComponents.createHeader());

        // CONTENIDO
        root.setCenter(createCenter());
    }

    private VBox createCenter() {

        VBox container = new VBox(25);
        container.setPadding(new Insets(20));

        // 🔹 TARJETAS SUPERIORES (OCUPAN TODO EL ANCHO)
        HBox stats = new HBox(20);
        stats.setPrefWidth(Double.MAX_VALUE);

        Region card1 = UIComponents.statCard(
                "Total Medicamentos",
                "128",
                "/sistemafarmacia/assets/icons/Productos1.png"
        );

        Region card2 = UIComponents.statCard(
                "Stock Bajo",
                "7",
                "/sistemafarmacia/assets/icons/Basura2.png"
        );

        Region card3 = UIComponents.statCard(
                "Ventas Hoy",
                "$3,450",
                "/sistemafarmacia/assets/icons/Ventas2.png"
        );

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);

        stats.getChildren().addAll(card1, card2, card3);

        // 🔹 TARJETAS GRANDES (SIN MODIFICAR)
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        grid.add(
                UIComponents.bigCard(
                        "Catálogo de Medicamentos",
                        "#2563eb",
                        "/sistemafarmacia/assets/icons/Catálogo.png"
                ), 0, 0
        );

        grid.add(
                UIComponents.bigCard(
                        "Nuevo Producto",
                        "#16a34a",
                        "/sistemafarmacia/assets/icons/Nuevo producto.png"
                ), 1, 0
        );

        grid.add(
                UIComponents.bigCard(
                        "Sesiones y Ventas",
                        "#d97706",
                        "/sistemafarmacia/assets/icons/Sesiones y ventas.png"
                ), 2, 0
        );

        grid.add(
                UIComponents.bigCard(
                        "Generar Ticket",
                        "#14b8a6",
                        "/sistemafarmacia/assets/icons/Generar ticket.png"
                ), 0, 1
        );

        grid.add(
                UIComponents.bigCard(
                        "Cortes semanales",
                        "#7c3aed",
                        "/sistemafarmacia/assets/icons/Cortes semanales.png"
                ), 1, 1
        );

        grid.add(
                UIComponents.bigCard(
                        "Filtros",
                        "#9ca3af",
                        "/sistemafarmacia/assets/icons/Filtros.png"
                ), 2, 1
        );

        container.getChildren().addAll(stats, grid);
        return container;
    }

    public BorderPane getRoot() {
        return root;
    }
}
