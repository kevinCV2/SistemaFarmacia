module SistemaFarmacia {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;

    exports sistemafarmacia;
    exports sistemafarmacia.ui;
    exports sistemafarmacia.ui.cortes;
    exports sistemafarmacia.utils;

    opens sistemafarmacia.ui.cortes to javafx.base;
    opens sistemafarmacia.assets.icons;
}