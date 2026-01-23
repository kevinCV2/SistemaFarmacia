package sistemafarmacia.ui.catalogo;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class CatalogoView {

    private BorderPane root;

    public CatalogoView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #1f2933;");

        Label title = new Label("Catálogo de Medicamentos");
        title.setFont(Font.font(28));
        title.setStyle("-fx-text-fill: white;");

        root.setCenter(title);
        BorderPane.setAlignment(title, Pos.CENTER);
    }

    public BorderPane getRoot() {
        return root;
    }
}
