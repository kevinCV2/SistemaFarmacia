package sistemafarmacia;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sistemafarmacia.ui.DashboardView;

public class SistemaFarmacia extends Application {

    @Override
    public void start(Stage stage) {

        // ================= ICONO DE LA APP =================
        Image icon = new Image(
                SistemaFarmacia.class.getResourceAsStream(
                        "/sistemafarmacia/assets/app_icon.jpg"
                )
        );
        stage.getIcons().add(icon);

        // ================= DASHBOARD =================
        DashboardView dashboard = new DashboardView();
        Scene scene = new Scene(dashboard.getRoot(), 1200, 720);

        stage.setTitle("Sistema de Gestión - Farmacia");
        stage.setScene(scene);

        // ================= PANTALLA COMPLETA =================
        stage.setFullScreenExitHint(""); // Quita el mensaje de ESC
        stage.setFullScreen(true);       // Oculta barra de tareas

        // ================= BLOQUEAR TECLA ESC =================
        stage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                e.consume(); // Bloquea ESC
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
