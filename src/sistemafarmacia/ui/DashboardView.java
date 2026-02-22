package sistemafarmacia.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import sistemafarmacia.ui.catalogo.CatalogoView;
import sistemafarmacia.ui.cortes.CortesSemanalesView;
import sistemafarmacia.ui.filtros.FiltrosView;
import sistemafarmacia.ui.nuevoproducto.NuevoProductoView;
import sistemafarmacia.ui.sesiones.SesionesView;
import sistemafarmacia.ui.ticket.GenerarTicketView;
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

        // TARJETAS SUPERIORES (OCUPAN TODO EL ANCHO)
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

        // TARJETAS GRANDES (OCUPAN TODO EL ANCHO)
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setMaxWidth(Double.MAX_VALUE);

        // COLUMNAS AL 100% DEL ANCHO (3 POR FILA)
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.33);
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col, col);

            grid.add(
                    UIComponents.bigCard(
                            "Catálogo de Medicamentos",
                            "#374151",
                            "/sistemafarmacia/assets/icons/Catálogo.png",
                            () -> root.setCenter(
                                    new CatalogoView(
                                            () -> root.setCenter(createCenter())
                                    ).getRoot()
                            )
                    ), 0, 0
            );

        grid.add(
                UIComponents.bigCard(
                        "Nuevo Producto",
                        "#374151",
                        "/sistemafarmacia/assets/icons/Nuevo producto.png",
                        () -> root.setCenter(new NuevoProductoView().getRoot())
                ), 1, 0
        );

        grid.add(
                UIComponents.bigCard(
                        "Sesiones y Ventas",
                        "#374151",
                        "/sistemafarmacia/assets/icons/Sesiones y ventas.png",
                       () -> root.setCenter(new SesionesView().getRoot())
                ), 2, 0
        );

        grid.add(
                UIComponents.bigCard(
                        "Generar Ticket",
                        "#37415",
                        "/sistemafarmacia/assets/icons/Generar ticket.png",
                        () -> root.setCenter(new GenerarTicketView(
                                () ->root.setCenter(createCenter())
                        ).getRoot())
                ), 0, 1
        );

        grid.add(
                UIComponents.bigCard(
                        "Cortes semanales",
                        "#374151",
                        "/sistemafarmacia/assets/icons/Cortes semanales.png",
                        () -> {
                            root.setCenter(new CortesSemanalesView(
                                    () -> root.setCenter(createCenter())
                            ).getRoot());
                        }
                ), 1, 1
        );

        grid.add(
                UIComponents.bigCard(
                        "Filtros",
                        "#374151",
                        "/sistemafarmacia/assets/icons/Filtros.png",
                        () -> root.setCenter(new FiltrosView().getRoot())
                ), 2, 1
        );

        VBox.setVgrow(grid, Priority.ALWAYS);

        container.getChildren().addAll(stats, grid);
        return container;
    }

    public BorderPane getRoot() {
        return root;
    }
}
