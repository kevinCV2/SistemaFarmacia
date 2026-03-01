package sistemafarmacia.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
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
import sistemafarmacia.ui.inversion.InversionAdicionalView;
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
        restaurarDashboard();
    }

    private VBox createCenter() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        // --- SECCIÓN DE ESTADÍSTICAS ---
        HBox stats = new HBox(20);
        
        // 1. Total de artículos (Suma simple de ambas tablas)
        String sqlTotal = "SELECT (SELECT COUNT(*) FROM medicamentos) + (SELECT COUNT(*) FROM insumos)";
        String totalItems = obtenerConteoBase(sqlTotal);
        
        // 2. Stock Bajo (Calculado según Stock Final Semanal < 10)
        // Esta consulta replica exactamente la lógica de tu CatalogoView
        String sqlBajo = """
            SELECT COUNT(*) FROM (
                SELECT 
                    m.id_medicamento,
                    (m.existencia + 
                     COALESCE((SELECT SUM(cantidad) FROM movimientos_inventario WHERE id_medicamento = m.id_medicamento AND tipo = 'ENTRADA' AND fecha >= date_trunc('week', CURRENT_DATE)), 0) - 
                     COALESCE((SELECT SUM(cantidad) FROM movimientos_inventario WHERE id_medicamento = m.id_medicamento AND tipo = 'SALIDA' AND fecha >= date_trunc('week', CURRENT_DATE)), 0)
                    ) as stock_final
                FROM medicamentos m
            ) subconsulta WHERE stock_final < 10
        """;
        String stockBajo = obtenerConteoBase(sqlBajo);

        Region card1 = UIComponents.statCard("Artículos en Inventario", totalItems, "/sistemafarmacia/assets/icons/Productos1.png");
        Region card2 = UIComponents.statCard("Stock Bajo)", stockBajo, "/sistemafarmacia/assets/icons/Basura2.png");
        
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        stats.getChildren().addAll(card1, card2);

        // --- GRILLA DE ACCESOS DIRECTOS ---
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.33);
        grid.getColumnConstraints().addAll(col, col, col);

        // Acciones referenciando a restaurarDashboard para refrescar el conteo al volver
        grid.add(UIComponents.bigCard("Catálogo de insumos", "#374151", "/sistemafarmacia/assets/icons/Catálogo.png",
            () -> actualizarCentro(new CatalogoView(this::restaurarDashboard).getRoot())), 0, 0);

        grid.add(UIComponents.bigCard("Catálogo de medicamentos", "#374151", "/sistemafarmacia/assets/icons/Catálogo.png",
            () -> actualizarCentro(new CatalogoMedicamentoView(this::restaurarDashboard).getRoot())), 1, 0);

        grid.add(UIComponents.bigCard("Nuevo Producto", "#374151", "/sistemafarmacia/assets/icons/Nuevo producto.png",
            () -> actualizarCentro(new NuevoProductoView(this::restaurarDashboard).getRoot())), 2, 0);

        grid.add(UIComponents.bigCard("Sesiones y Ventas", "#374151", "/sistemafarmacia/assets/icons/Sesiones y ventas.png",
            () -> actualizarCentro(new SesionesView(this::restaurarDashboard).getRoot())), 0, 1);

        grid.add(UIComponents.bigCard("Generar Ticket", "#374151", "/sistemafarmacia/assets/icons/Generar ticket.png",
            () -> actualizarCentro(new GenerarTicketView(this::restaurarDashboard).getRoot())), 1, 1);

        grid.add(UIComponents.bigCard("Cortes Semanales", "#374151", "/sistemafarmacia/assets/icons/Cortes semanales.png",
            () -> actualizarCentro(new CortesSemanalesView(this::restaurarDashboard).getRoot())), 2, 1);

        grid.add(UIComponents.bigCard("Cortes de venta", "#374151", "/sistemafarmacia/assets/icons/Cortes semanales.png",
            () -> actualizarCentro(new CortesSesiones(this::restaurarDashboard).getRoot())), 0, 2);

        grid.add(UIComponents.bigCard("Filtros", "#374151", "/sistemafarmacia/assets/icons/Filtros.png",
            () -> actualizarCentro(new FiltrosView(this::restaurarDashboard).getRoot())), 1, 2);

        grid.add(UIComponents.bigCard("Registrar Gastos", "#374151", "/sistemafarmacia/assets/icons/Nuevo producto.png",
            () -> actualizarCentro(new GastosView(this::restaurarDashboard).getRoot())), 2, 2);

        grid.add(UIComponents.bigCard("Inversión Adicional", "#374151", "/sistemafarmacia/assets/icons/Nuevo producto.png",
            () -> actualizarCentro(new InversionAdicionalView(this::restaurarDashboard).getRoot())), 0, 3);

        VBox.setVgrow(grid, Priority.ALWAYS);
        container.getChildren().addAll(stats, grid);
        return container;
    }

    private void actualizarCentro(javafx.scene.Node nodo) {
        root.setCenter(nodo);
    }

    private void restaurarDashboard() {
        ScrollPane sp = new ScrollPane(createCenter());
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #1f2933; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent;");
        root.setCenter(sp);
    }

    private String obtenerConteoBase(String sql) {
        try (Connection conn = ConexionDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception e) {
            System.err.println("Error en Dashboard: " + e.getMessage());
        }
        return "0";
    }

    public BorderPane getRoot() { return root; }
}