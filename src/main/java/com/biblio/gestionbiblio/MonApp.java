package com.biblio.gestionbiblio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MonApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
        Scene scene = new Scene(loader.load(),1366,768);
        Image image = new Image("file:/D:/HERLLANDYS/PROJECTS/IG ENI/JAVA JAVAFX/GestionBibliothequeAllFrancaise/src/main/resources/com/biblio/gestionbiblio/assets/Alliance_Francaise-logo-EEC1EE1CC6-seeklogo.com.png");
        stage.getIcons().add(image);
        stage.setResizable(false);
        stage.setTitle("Système de Gestion de Bibliothèque");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
