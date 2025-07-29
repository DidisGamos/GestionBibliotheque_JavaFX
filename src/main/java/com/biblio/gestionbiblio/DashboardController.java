package com.biblio.gestionbiblio;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DashboardController {
    @FXML
    private Button DeconnectDash;
    @FXML
    private Button AboutApp;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab membresTab;
    @FXML
    private Tab livresTab;
    @FXML
    private Tab pretsTab;
    @FXML
    private Tab rendusTab;
    @FXML
    private Tab rapportsTab;
    @FXML
    private Tab tablesTab;


    private MembresController membresController;
    private LivreController livresController;
    private PretsController pretsController;
    private RendusController rendusController;
    private RapportsController rapportsController;
    private TableBoardsController tableBoardsController;

    @FXML
    public void initialize() {
        this.DeconnectDash.setOnAction((event) -> {
            try {
                Parent LoginView = (Parent)FXMLLoader.load(this.getClass().getResource("Login.fxml"));
                Scene LoginScene = new Scene(LoginView);
                Stage window = (Stage)this.DeconnectDash.getScene().getWindow();
                window.setResizable(false);
                window.setScene(LoginScene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        this.AboutApp.setOnAction((event) -> {
            try {
                Parent aboutView = (Parent)FXMLLoader.load(this.getClass().getResource("AboutApp.fxml"));
                Stage stage = new Stage();
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.initModality(Modality.APPLICATION_MODAL);
                Scene scene = new Scene(aboutView);
                scene.setFill(Color.TRANSPARENT);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        try {

            FXMLLoader tablesLoader = new FXMLLoader(getClass().getResource("TableBoards.fxml"));
            Parent tablesContent = tablesLoader.load();
            tableBoardsController = tablesLoader.getController();
            tablesTab.setContent(tablesContent);

            // Charger Membres.fxml
            FXMLLoader membresLoader = new FXMLLoader(getClass().getResource("Membres.fxml"));
            Parent membresContent = membresLoader.load();
            membresController = membresLoader.getController();
            membresTab.setContent(membresContent);

            // Charger Livres.fxml
            FXMLLoader livresLoader = new FXMLLoader(getClass().getResource("Livres.fxml"));
            Parent livresContent = livresLoader.load();
            livresController = livresLoader.getController();
            livresTab.setContent(livresContent);

            // Charger Prets.fxml
            FXMLLoader pretsLoader = new FXMLLoader(getClass().getResource("Prets.fxml"));
            Parent pretsContent = pretsLoader.load();
            pretsController = pretsLoader.getController();
            pretsTab.setContent(pretsContent);

            // Charger Rendus.fxml
            FXMLLoader rendusLoader = new FXMLLoader(getClass().getResource("Rendus.fxml"));
            Parent rendusContent = rendusLoader.load();
            rendusController = rendusLoader.getController();
            rendusTab.setContent(rendusContent);

            // Charger Rapports.fxml
            FXMLLoader rapportsLoader = new FXMLLoader(getClass().getResource("Rapports.fxml"));
            Parent rapportsContent = rapportsLoader.load();
            rapportsController = rapportsLoader.getController();
            rapportsTab.setContent(rapportsContent);

        } catch (Exception e) {
            e.printStackTrace();
        }

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == membresTab && membresController != null) {
                membresController.chargerDonneesMembres();
            } else if (newTab == livresTab && livresController != null) {
                livresController.chargerDonneesLivre();
            } else if (newTab == pretsTab && pretsController != null) {
                pretsController.chargerDonneesPrets();
                pretsController.chargerMembres();
                pretsController.chargerLivres();
            } else if (newTab == rendusTab && rendusController != null) {
                rendusController.ChargerDonneesRendu();
                rendusController.ChargerMembresRendu();
                rendusController.ChargerLivresRendu();
                rendusController.configurationTableauRendu();
            } else if (newTab == rapportsTab && rapportsController != null){
                rapportsController.ChargerMembreHistorique();
                rapportsController.ConfigTableauHistorique();
                rapportsController.ConfigTableauRetard();
                rapportsController.chargerLivresEnRetard();
            } else if(newTab == tablesTab && tableBoardsController != null){
                tableBoardsController.initialize();
            }

        });
    }
}