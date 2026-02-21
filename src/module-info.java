module SistemaFarmacia {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;
    requires org.postgresql.jdbc;

    exports sistemafarmacia;
    exports sistemafarmacia.ui;
    exports sistemafarmacia.ui.cortes; // <--- Exporta el paquete
    exports sistemafarmacia.utils;

    opens sistemafarmacia.ui.cortes to javafx.base;

    opens sistemafarmacia.assets.icons;
}