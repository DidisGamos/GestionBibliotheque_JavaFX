package com.biblio.gestionbiblio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AboutApp {
    @FXML
    private Button CloseAbout;
    private double xOffset = (double)0.0F;
    private double yOffset = (double)0.0F;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            this.CloseAbout.getScene().getRoot().setOnMousePressed((event) -> {
                this.xOffset = event.getSceneX();
                this.yOffset = event.getSceneY();
            });
            this.CloseAbout.getScene().getRoot().setOnMouseDragged((event) -> {
                Stage stage = (Stage)this.CloseAbout.getScene().getWindow();
                stage.setX(event.getScreenX() - this.xOffset);
                stage.setY(event.getScreenY() - this.yOffset);
            });
        });
    }

    @FXML
    private void CloseAboutApp() {
        Stage stage = (Stage)this.CloseAbout.getScene().getWindow();
        stage.close();
    }
}
