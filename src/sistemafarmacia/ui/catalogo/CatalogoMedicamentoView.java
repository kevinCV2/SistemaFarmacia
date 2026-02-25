package sistemafarmacia.ui.catalogo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.net.URL;

public class CatalogoMedicamentoView {

    private BorderPane root;
    private Runnable actionVolver;
    private MediaPlayer mediaPlayer;

    public CatalogoMedicamentoView(Runnable actionVolver) {
        this.actionVolver = actionVolver;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        // --- BARRA SUPERIOR ---
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnVolver = new Button("⬅ Regresar");
        btnVolver.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: #374151;" +
                "-fx-border-radius: 5;" +
                "-fx-cursor: hand;"
        );
        btnVolver.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.stop(); // Detener video al salir
            if (this.actionVolver != null) this.actionVolver.run();
        });

        Label title = new Label("Catálogo de Medicamentos");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: white;");

        topBar.getChildren().addAll(btnVolver, title);
        root.setTop(topBar);

        // --- CONTENIDO ---
        VBox modoMantenimiento = new VBox(20);
        modoMantenimiento.setAlignment(Pos.CENTER);
        modoMantenimiento.setPadding(new Insets(50));

        // 🎬 CARGA DEL VIDEO (NOMBRE EXACTO CON EXTENSIÓN)
        String videoPath = "/sistemafarmacia/assets/PixVerse_V5.6_Image_Text_360P_gif_hamsters_mor.mp4";
        URL videoUrl = getClass().getResource(videoPath);

        if (videoUrl != null) {
            try {
                Media media = new Media(videoUrl.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setMute(true);
                mediaPlayer.play();

                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.setFitWidth(260);
                mediaView.setPreserveRatio(true);
                mediaView.setSmooth(true);

                modoMantenimiento.getChildren().add(mediaView);
            } catch (Exception e) {
                System.err.println("Error al inicializar el video: " + e.getMessage());
            }
        } else {
            // Fallback si falla la carga
            Label errorVideo = new Label("🐹\n⚠ Video de mantenimiento no encontrado");
            errorVideo.setTextAlignment(TextAlignment.CENTER);
            errorVideo.setStyle("-fx-text-fill: #f87171; -fx-font-size: 16px;");
            modoMantenimiento.getChildren().add(errorVideo);
        }

        // --- MENSAJE ---
        Label mensaje = new Label(
                "Esta sección se encuentra en desarrollo.\n\n" +
                "Estamos trabajando para que el catálogo de medicamentos\n" +
                "esté disponible lo antes posible.\n\n" +
                "Gracias por tu paciencia."
        );

        mensaje.setFont(Font.font("System", 18));
        mensaje.setTextFill(Color.web("#9ca3af"));
        mensaje.setTextAlignment(TextAlignment.CENTER);
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(500);

        Button btnInfo = new Button("Más información");
        btnInfo.setStyle(
                "-fx-background-color: #374151;" +
                "-fx-text-fill: #e5e7eb;" +
                "-fx-cursor: hand;"
        );
        btnInfo.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText(null);
            alert.setContentText("El módulo de catálogo estará disponible en una próxima actualización.");
            alert.showAndWait();
        });

        modoMantenimiento.getChildren().addAll(mensaje, btnInfo);
        root.setCenter(modoMantenimiento);
    }

    public BorderPane getRoot() {
        return root;
    }
}