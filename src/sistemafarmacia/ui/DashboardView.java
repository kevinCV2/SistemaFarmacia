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
import sistemafarmacia.utils.ConexionDB;
import sistemafarmacia.utils.UIComponents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Vista principal del Dashboard con 8 módulos y estadísticas en tiempo real.
 */
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

        // --- TARJETAS DE ESTADÍSTICAS ---
        HBox stats = new HBox(20);
        String totalMeds = obtenerConteoBase("SELECT COUNT(*) FROM medicamentos");
        String stockBajo = obtenerConteoBase("SELECT COUNT(*) FROM medicamentos WHERE stock <= stock_minimo");
        String ventasHoy = obtenerVentasHoy();

        Region card1 = UIComponents.statCard("Total Medicamentos", totalMeds, "/sistemafarmacia/assets/icons/Productos1.png");
        Region card2 = UIComponents.statCard("Stock Bajo", stockBajo, "/sistemafarmacia/assets/icons/Basura2.png");
        Region card3 = UIComponents.statCard("Ventas Hoy", ventasHoy, "/sistemafarmacia/assets/icons/Ventas2.png");
        
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        stats.getChildren().addAll(card1, card2, card3);

        // --- GRID DE MÓDULOS (8 Módulos) ---
        GridPane grid = new GridPane();
        grid.setHgap(20); 
        grid.setVgap(20);
        
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col, col, col);

        // FILA 0
        grid.add(UIComponents.bigCard("Catálogo Insumos", "#374151", "/sistemafarmacia/assets/icons/Catálogo.png",
            () -> root.setCenter(new CatalogoView(() -> root.setCenter(createCenter())).getRoot())), 0, 0);

        grid.add(UIComponents.bigCard("Catálogo Medicamento", "#374151", "/sistemafarmacia/assets/icons/Catálogo.png",
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
        grid.add(UIComponents.bigCard("Cortes Semanales Sesiones", "#374151", "/sistemafarmacia/assets/icons/Cortes semanales.png",
            () -> root.setCenter(new CortesSesiones(() -> root.setCenter(createCenter())).getRoot())), 0, 2);

        grid.add(UIComponents.bigCard("Filtros", "#374151", "/sistemafarmacia/assets/icons/Filtros.png",
            () -> root.setCenter(new FiltrosView(() -> root.setCenter(createCenter())).getRoot())), 1, 2);

        VBox.setVgrow(grid, Priority.ALWAYS);
        container.getChildren().addAll(stats, grid);
        return container;
    }

    // --- MÉTODOS DE LÓGICA DE DATOS ---

    private String obtenerConteoBase(String sql) {
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception e) {
            System.err.println("Error en conteo: " + e.getMessage());
        }
        return "0";
    }

    private String obtenerVentasHoy() {
        String sql = "SELECT SUM(total) FROM tickets WHERE CAST(fecha AS DATE) = CURRENT_DATE";
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double total = rs.getDouble(1);
                return NumberFormat.getCurrencyInstance(Locale.US).format(total);
            }
        } catch (Exception e) {
            System.err.println("Error en ventas hoy: " + e.getMessage());
        }
        return "$0.00";
    }

    public BorderPane getRoot() {
        return root;
    }
}