package sistemafarmacia.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UIComponents {

    public static VBox createHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2563eb;");

        Label title = new Label("Sistema de Gestión - Farmacia");
        title.setFont(Font.font(22));
        title.setTextFill(Color.WHITE);

        Label date = new Label("jueves, 22 de enero de 2026 - 2:25 a.m.");
        date.setTextFill(Color.WHITE);

        header.getChildren().addAll(title, date);
        return header;
    }

    public static VBox statCard(String title, String value) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(15));
        box.setPrefWidth(200);
        box.setStyle("""
                -fx-background-color: #111827;
                -fx-background-radius: 12;
                """);

        Label l1 = new Label(title);
        l1.setTextFill(Color.LIGHTGRAY);

        Label l2 = new Label(value);
        l2.setFont(Font.font(26));
        l2.setTextFill(Color.WHITE);

        box.getChildren().addAll(l1, l2);
        return box;
    }

    public static StackPane bigCard(String text, String color) {
        StackPane pane = new StackPane();
        pane.setPrefSize(350, 180);
        pane.setStyle("""
                -fx-background-color: %s;
                -fx-background-radius: 16;
                """.formatted(color));

        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(18));

        StackPane.setAlignment(label, Pos.BOTTOM_LEFT);
        StackPane.setMargin(label, new Insets(10));

        pane.getChildren().add(label);
        return pane;
    }
}
