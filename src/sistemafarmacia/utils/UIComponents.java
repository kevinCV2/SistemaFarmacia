package sistemafarmacia.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class UIComponents {

    // ================= HEADER =================
    public static HBox createHeader() {

        HBox header = new HBox(20);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2563eb;");

        // ---- IZQUIERDA ----
        VBox left = new VBox(5);

        Label title = new Label("Sistema de Gestión - Farmacia");
        title.setFont(Font.font(26));
        title.setTextFill(Color.WHITE);

        Label date = new Label();
        date.setTextFill(Color.WHITE);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm:ss");

        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, e ->
                        date.setText(LocalDateTime.now().format(formatter))
                ),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        left.getChildren().addAll(title, date);

        // ---- ESPACIADOR ----
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ---- LOGO DERECHA ----
        ImageView logo = loadIcon("/sistemafarmacia/assets/icons/logo.png", 250);

        header.getChildren().addAll(left, spacer, logo);
        return header;
    }

    // ================= TARJETA SIMPLE =================
    public static VBox statCard(String title, String value) {

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setPrefWidth(220);
        box.setStyle("""
                -fx-background-color: #111827;
                -fx-background-radius: 14;
                """);

        Label l1 = new Label(title);
        l1.setTextFill(Color.LIGHTGRAY);
        l1.setFont(Font.font(16));

        Label l2 = new Label(value);
        l2.setFont(Font.font(30));
        l2.setTextFill(Color.WHITE);

        box.getChildren().addAll(l1, l2);
        return box;
    }

    // ================= TARJETA CON ICONO =================
    public static VBox statCard(String title, String value, String iconPath) {

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setPrefWidth(220);
        box.setStyle("""
                -fx-background-color: #111827;
                -fx-background-radius: 14;
                """);

        ImageView icon = loadIcon(iconPath, 32);

        Label l1 = new Label(title);
        l1.setTextFill(Color.LIGHTGRAY);
        l1.setFont(Font.font(16));

        Label l2 = new Label(value);
        l2.setFont(Font.font(28));
        l2.setTextFill(Color.WHITE);

        box.getChildren().addAll(icon, l1, l2);
        return box;
    }

    // ================= TARJETA GRANDE =================
    public static Region bigCard(
            String title,
            String color,
            String iconPath,
            Runnable action
    ) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setCursor(Cursor.HAND);

        card.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 16;
        """.formatted(color));

        ImageView icon = new ImageView(
                new Image(UIComponents.class.getResourceAsStream(iconPath))
        );
        icon.setFitWidth(64);
        icon.setFitHeight(64);

        Label label = new Label(title);
        label.setFont(Font.font(16));
        label.setStyle("-fx-text-fill: white;");

        card.getChildren().addAll(icon, label);

        // EVENTO CLICK
        card.setOnMouseClicked(e -> action.run());

        // Hover bonito
        card.setOnMouseEntered(e ->
                card.setStyle("""
                    -fx-background-color: derive(%s, -10%%);
                    -fx-background-radius: 16;
                """.formatted(color))
        );

        card.setOnMouseExited(e ->
                card.setStyle("""
                    -fx-background-color: %s;
                    -fx-background-radius: 16;
                """.formatted(color))
        );

        return card;
    }

    // ================= CARGA SEGURA DE ICONOS =================
    private static ImageView loadIcon(String path, int size) {

        var stream = UIComponents.class.getResourceAsStream(path);

        if (stream == null) {
            throw new RuntimeException(
                    " ICONO NO ENCONTRADO: " + path +
                    "\nVerifica que esté en: src" + path
            );
        }

        ImageView icon = new ImageView(new Image(stream));
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        icon.setPreserveRatio(true);
        return icon;
    }

}