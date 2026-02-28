package sistemafarmacia.ui;

import javafx.geometry.Insets;
import javafx.scene.layout.*;
import sistemafarmacia.ui.catalogo.CatalogoView;
import sistemafarmacia.ui.catalogo.CatalogoMedicamentoView;
import sistemafarmacia.ui.cortes.CortesSemanalesView;
import sistemafarmacia.ui.cortes.CortesSesiones;
import sistemafarmacia.ui.filtros.FiltrosView;
import sistemafarmacia.ui.nuevoproducto.NuevoProductoView;
import sistemafarmacia.ui.sesiones.SesionesView;
import sistemafarmacia.ui.ticket.GenerarTicketView;
import sistemafarmacia.ui.gastos.GastosView;
import sistemafarmacia.ui.inversion.InversionAdicionalView; // Nueva Importación
import sistemafarmacia.utils.ConexionDB;
import sistemafarmacia.utils.UIComponents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardView {

    private BorderPane root;

    public DashboardView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");
        root.setTop(UIComponents.createHeader());
        root.setCenter(createCenter());
    }

    private VBox createCenter() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(20));

        // --- SECCIÓN DE ESTADÍSTICAS ---
        HBox stats = new HBox(20);
        
        String sqlTotal = "SELECT (SELECT COUNT(*) FROM medicamentos) + (SELECT COUNT(*) FROM insumos)";
        String totalItems = obtenerConteoBase(sqlTotal);
        
        String sqlBajo = "SELECT (SELECT COUNT(*) FROM medicamentos WHERE existencia < 10) + (SELECT COUNT(*) FROM insumos WHERE stock < 10)";
        String stockBajo = obtenerConteoBase(sqlBajo);

        Region card1 = UIComponents.statCard("Artículos en Inventario", totalItems, "/sistemafarmacia/assets/icons/Productos1.png");
        Region card2 = UIComponents.statCard("Productos con Stock Bajo", stockBajo, "/sistemafarmacia/assets/icons/Basura2.png");
        
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        
        stats.getChildren().addAll(card1, card2);

        // --- GRILLA DE ACCESOS DIRECTOS ---
        GridPane grid = new GridPane();
        grid.setHgap(20); 
        grid.setVgap(20);
        
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col, col, col);

        // FILA 0
        grid.add(UIComponents.bigCard("Catálogo de insumos", "#374151", "/sistemafarmacia/assets/icons/Catálogo.png",
            () -> root.setCenter(new CatalogoView(() -> root.setCenter(createCenter())).getRoot())), 0, 0);

        grid.add(UIComponents.bigCard("Catálogo de medicamentos", "#374151", "/sistemafarmacia/assets/icons/Catálogo.png",
            () -> root.setCenter(new CatalogoMedicamentoView(() -> root.setCenter(createCenter())).getRoot())), 1, 0);

        grid.add(UIComponents.bigCard("Nuevo Producto", "#374151", "/sistemafarmacia/assets/icons/Nuevo producto.png",
            () -> root.setCenter(new NuevoProductoView(() -> root.setCenter(createCenter())).getRoot())), 2, 0);

        // FILA 1
        grid.add(UIComponents.bigCard("Sesiones y Ventas", "#374151", "/sistemafarmacia/assets/icons/Sesiones y ventas.png",
            () -> root.setCenter(new SesionesView(() -> root.setCenter(createCenter())).getRoot())), 0, 1);

        grid.add(UIComponents.bigCard("Generar Ticket", "#374151", "/sistemafarmacia/assets/icons/Generar ticket.png",
            () -> root.setCenter(new GenerarTicketView(() -> root.setCenter(createCenter())).getRoot())), 1, 1);

        grid.add(UIComponents.bigCard("Cortes Semanales", "#374151", "/sistemafarmacia/assets/icons/Cortes semanales.png",
            () -> root.setCenter(new CortesSemanalesView(() -> root.setCenter(createCenter())).getRoot())), 2, 1);

        // FILA 2
        grid.add(UIComponents.bigCard("Cortes de venta", "#374151", "/sistemafarmacia/assets/icons/Cortes semanales.png",
            () -> root.setCenter(new CortesSesiones(() -> root.setCenter(createCenter())).getRoot())), 0, 2);

        grid.add(UIComponents.bigCard("Filtros", "#374151", "/sistemafarmacia/assets/icons/Filtros.png",
            () -> root.setCenter(new FiltrosView(() -> root.setCenter(createCenter())).getRoot())), 1, 2);

        grid.add(UIComponents.bigCard("Registrar Gastos", "#374151", "/sistemafarmacia/assets/icons/Nuevo producto.png",
            () -> root.setCenter(new GastosView(() -> root.setCenter(createCenter())).getRoot())), 2, 2);

        // FILA 3 - NUEVA TARJETA: Inversión Adicional
        grid.add(UIComponents.bigCard("Inversión Adicional", "#374151", "/sistemafarmacia/assets/icons/Nuevo producto.png",
            () -> root.setCenter(new InversionAdicionalView(() -> root.setCenter(createCenter())).getRoot())), 0, 3);

        VBox.setVgrow(grid, Priority.ALWAYS);
        container.getChildren().addAll(stats, grid);
        return container;
    }

    private String obtenerConteoBase(String sql) {
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception e) {
            System.err.println("Error en conteo Dashboard: " + e.getMessage());
        }
        return "0";
    }

    public BorderPane getRoot() { return root; }
}