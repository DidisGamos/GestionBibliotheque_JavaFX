package com.biblio.gestionbiblio;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class LoginController {
    @FXML private Hyperlink OpenRegister;
    @FXML private TextField ForNamesLogin;
    @FXML private TextField ForPwdLogin;

    @FXML
    public void initialize() {
        OpenRegister.setOnAction(event -> {
            try {
                Parent registerView = FXMLLoader.load(getClass().getResource("Register.fxml"));
                Scene registerScene = new Scene(registerView);
                Stage window = (Stage) OpenRegister.getScene().getWindow();
                window.setResizable(false);
                window.setScene(registerScene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML
    private void ForConnected(){

        String username = ForNamesLogin.getText();
        String password = ForPwdLogin.getText();

        if (username.isEmpty() || password.isEmpty()){
            System.out.println("Veuillez remplir tous les champs !");
            showNotification("Se Connecté !", "Veuillez remplir tous les champs !", NotificationType.WARNING);
            return;
        }

        DatabaseConnection db = new DatabaseConnection();
        Connection connectDB = db.getConnection();
        if(connectDB == null){
            System.out.println("Connexion impossible à la base de données !");
            return;
        }
        String LoginQuery = "SELECT password FROM users WHERE username = ?";

        try(PreparedStatement stmt = connectDB.prepareStatement(LoginQuery)){
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){

                String storedHash = rs.getString("password");

                if (!storedHash.startsWith("$2a$")) {
                    System.err.println("Hash invalide dans la base pour l'utilisateur: " + username);
                    return;
                }

                if (BCrypt.checkpw(password, storedHash)) {
                    showNotification("Se Connecté !", "Connexion réussie !", NotificationType.SUCCESS);
                    openDashboard();
                    Stage login = (Stage) OpenRegister.getScene().getWindow();
                    login.close();
                } else {
                    System.out.println("Nom d'utilisateur ou mot de passe incorrect !");
                    showNotification("Se Connecté !", "Nom d'utilisateur ou mot de passe incorrect !", NotificationType.ERROR);
                }
            }else{
                System.out.println("Nom d'utilisateur ou mot de passe incorrect !");
            }
        }catch (SQLException e){
            System.err.println("Erreur SQL !");
            e.printStackTrace();
        }finally {
            try{
                connectDB.close();
            }catch (SQLException e){
                System.err.println("Erreur lors de la fermeture de la connexion !");
                e.printStackTrace();
            }
        }
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            Parent dashboardView = loader.load();

            Scene dashboardScene = new Scene(dashboardView);
            Stage dashboardStage = new Stage();
            Image image = new Image("file:/D:/HERLLANDYS/PROJECTS/IG ENI/JAVA JAVAFX/GestionBibliothequeAllFrancaise/src/main/resources/com/biblio/gestionbiblio/assets/Alliance_Francaise-logo-EEC1EE1CC6-seeklogo.com.png");
            dashboardStage.getIcons().add(image);
            dashboardStage.setResizable(false);
            dashboardStage.setScene(dashboardScene);
            dashboardStage.setTitle("Système de Gestion de Bibliothèque");
            dashboardStage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du tableau de bord !");
            e.printStackTrace();
        }
    }
    private void showNotification(String title, String message, NotificationType type) {
        TrayNotification tray = new TrayNotification();
        tray.setAnimationType(AnimationType.POPUP);
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setNotificationType(type);
        tray.showAndDismiss(Duration.millis(3000));
    }
}