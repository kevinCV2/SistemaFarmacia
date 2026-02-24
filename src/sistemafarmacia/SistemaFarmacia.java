package sistemafarmacia;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sistemafarmacia.ui.DashboardView;
import sistemafarmacia.utils.ConexionDB;


public class SistemaFarmacia extends Application {

    @Override
    public void start(Stage stage) {

        System.out.println("Iniciando sistema | Iniciando conexion de BD al sistema");
        ConexionDB.getInstance();

        // ================= ICONO DE LA APP =================
        try {
            Image icon = new Image(
                    getClass().getResourceAsStream("/sistemafarmacia/assets/app_icon.jpg")
            );
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("⚠️ No se encontró el icono de la app, usando el default.");
        }

        // ================= DASHBOARD =================
        DashboardView dashboard = new DashboardView();
        Scene scene = new Scene(dashboard.getRoot(), 1200, 720);

        stage.setTitle("Sistema Integral de Gestión Médica y Administrativa");
        stage.setScene(scene);

        // ================= MODO KIOSCO =================
        stage.initStyle(StageStyle.UNDECORATED); // sin barra superior
        stage.setFullScreen(true);               // pantalla completa real
        stage.setFullScreenExitHint("");          // sin mensaje al salir

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Cerrando la aplicación | Cerrando conexion a la Base de Datos");
        ConexionDB.cerrarConexion();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
