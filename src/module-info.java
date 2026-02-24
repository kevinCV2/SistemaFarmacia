module SistemaFarmacia {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires javafx.swt;
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;
    requires jdk.jsobject;
    requires jfx.incubator.input;
    requires jfx.incubator.richtext;
    requires org.checkerframework.checker.qual;

    exports sistemafarmacia;
    exports sistemafarmacia.ui;
    exports sistemafarmacia.ui.cortes;
    exports sistemafarmacia.utils;

    opens sistemafarmacia.ui.cortes to javafx.base;
    opens sistemafarmacia.assets.icons;
}