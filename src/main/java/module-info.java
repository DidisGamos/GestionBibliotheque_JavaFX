module com.biblio.gestionbiblio {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires transitive jbcrypt;
    requires transitive TrayTester;
    requires transitive itextpdf;
    requires java.desktop;
    requires java.activation;
    requires java.mail; // Gardez uniquement java.mail

    exports com.biblio.gestionbiblio;

    opens com.biblio.gestionbiblio to
            javafx.fxml;
}
