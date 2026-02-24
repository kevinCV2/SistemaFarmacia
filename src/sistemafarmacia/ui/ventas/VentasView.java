package sistemafarmacia.ui.ventas;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class VentasView {

    private BorderPane root;

    public VentasView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        Label title = new Label("Sesiones y Ventas");
        title.setFont(Font.font(28));
        title.setStyle("-fx-text-fill: white;");

        root.setCenter(title);
        BorderPane.setAlignment(title, Pos.CENTER);
    }

    public BorderPane getRoot() {
        return root;
    }
}