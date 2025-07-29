package com.biblio.gestionbiblio;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
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

public class RegisterController {
    @FXML private Hyperlink OpenLogin;
    @FXML private TextField ForNameRegister;
    @FXML private TextField ForEmailRegister;
    @FXML private TextField ForPwdRegister;

    @FXML
    public void initialize() {
        OpenLogin.setOnAction(event -> {
            try {
                Parent loginView = FXMLLoader.load(getClass().getResource("Login.fxml"));
                Scene loginScene = new Scene(loginView);
                Stage window = (Stage) OpenLogin.getScene().getWindow();
                window.setScene(loginScene);
                window.show();
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }
    @FXML
    public void ForRegister() throws SQLException {
        String username = ForNameRegister.getText();
        String email = ForEmailRegister.getText();
        String password = ForPwdRegister.getText();

        if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
            System.out.println("Veuillez remplir tous les champs !");
            showNotification("S'inscrire","Veuillez remplir tous les champs !",NotificationType.WARNING);
            return;
        }
        DatabaseConnection db = new DatabaseConnection();
        Connection connectDB = db.getConnection();
        if(connectDB == null){
            System.out.println("Connexion impossible à la base de données !");
            return;
        }

        if(emailExists(connectDB, email)) {
            showNotification("S'inscrire","Cet email est déjà utilisé",NotificationType.WARNING);
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String RegisterQuery = "INSERT INTO public.users (username, email, password) VALUES (?,?,?)";

        try(PreparedStatement stmt = connectDB.prepareStatement(RegisterQuery)){
            stmt.setString(1, username);
            stmt.setString(2,email);
            stmt.setString(3,hashedPassword);

            int rowsAffected = stmt.executeUpdate();

            if(rowsAffected > 0){
                System.out.println("S'inscrire d'un Utilisateur avec succès !");
                showNotification("S'inscrire","S'inscrire d'un Utilisateur avec succès !",NotificationType.SUCCESS);
            }else {
                System.out.println("Erreur lors de l'inscription !");
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
    private boolean emailExists(Connection conn, String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
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
