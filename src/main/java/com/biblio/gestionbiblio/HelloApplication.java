package com.biblio.gestionbiblio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 550);
        Image image = new Image("file:/D:/HERLLANDYS/PROJECTS/IG ENI/JAVA JAVAFX/GestionBibliothequeAllFrancaise/src/main/resources/com/biblio/gestionbiblio/assets/Alliance_Francaise-logo-EEC1EE1CC6-seeklogo.com.png");
        stage.getIcons().add(image);
        stage.setResizable(false);
        stage.setTitle("Système de Gestion de Bibliothèque");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}