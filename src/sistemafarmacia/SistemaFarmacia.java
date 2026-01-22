package sistemafarmacia;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sistemafarmacia.ui.DashboardView;

public class SistemaFarmacia extends Application {

    @Override
    public void start(Stage stage) {
        DashboardView dashboard = new DashboardView();
        Scene scene = new Scene(dashboard.getRoot(), 1200, 720);

        stage.setTitle("Sistema de Gestión - Farmacia");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
