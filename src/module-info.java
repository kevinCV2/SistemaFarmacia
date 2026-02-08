module SistemaFarmacia {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;

    exports sistemafarmacia;
    exports sistemafarmacia.ui;
    exports sistemafarmacia.utils;

    opens sistemafarmacia.assets.icons;
    opens sistemafarmacia;
}