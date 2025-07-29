package com.biblio.gestionbiblio;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Optional;

import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;


public class MembresController {

    @FXML private TextField IdPers;
    @FXML private TextField NomPers;
    @FXML private TextField AgePers;
    @FXML private ComboBox SexePers;
    @FXML private TextField ContactPers;
    @FXML private TextField SearchMembre;

    @FXML private TableView<Membre> TableMembre;
    @FXML private TableColumn<Membre, String> RowIDMembre;
    @FXML private TableColumn<Membre, String> RowNomMembre;
    @FXML private TableColumn<Membre, String> RowSexeMembre;
    @FXML private TableColumn<Membre, Integer> RowAgeMembre;
    @FXML private TableColumn<Membre, String> RowContMembre;
    @FXML private TableColumn<Membre, Void> RowActMembre;
    @FXML private Button SavePers;
    @FXML private Button CancelPers;

    private Membre membreEnEdition = null;

    private ObservableList<Membre>membresList = FXCollections.observableArrayList();
    private ObservableList<Membre>searchMembreList = FXCollections.observableArrayList();

    @FXML
    public void CancelPersAction(javafx.event.ActionEvent actionEvent) {
        clearFields();
        membreEnEdition = null;
        SavePers.setText("Ajouter");
    }

    @FXML
    private void SearchMembreOnAction(){
        String SearchAllMembres = SearchMembre.getText().trim();
        if (SearchAllMembres.isEmpty()) {
            TableMembre.setItems(membresList);
            return;
        }
        DatabaseConnection db = new DatabaseConnection();
        try(Connection connectDB = db.getConnection();
        PreparedStatement preparedStatement = connectDB.prepareStatement("SELECT * FROM public.membre WHERE nom LIKE ? OR idpers LIKE ?"))
        {
            String SearchPattern = "%"+SearchAllMembres+"%";
            preparedStatement.setString(1, SearchPattern);
            preparedStatement.setString(2, SearchPattern);

            searchMembreList.clear();
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    searchMembreList.add(new Membre(
                            resultSet.getString("idpers"),
                            resultSet.getString("nom"),
                            resultSet.getString("sexe"),
                            resultSet.getInt("age"),
                            resultSet.getString("contact")
                    ));
                }
            }
            TableMembre.setItems(searchMembreList);
        }catch (SQLException e){
            e.printStackTrace();
            showNotification("Membre", "Erreur lors de la recherche", NotificationType.ERROR);
            System.out.println("Erreur lors de la recherche" + "Error");
        }
    }

    @FXML
    private void SavePersActionPerformed(javafx.event.ActionEvent actionEvent) throws SQLException {
        String idpers = IdPers.getText();
        String nom = NomPers.getText();
        String sexe = (String) SexePers.getValue();
        String ageStr = AgePers.getText();
        String contact = ContactPers.getText();

        if(idpers.isEmpty() || nom.isEmpty() || sexe == null || ageStr.isEmpty() || contact.isEmpty()){
            System.out.println("Veuillez remplir tous les champs !");
            showNotification("Membres", "Veuillez remplir tous les champs !", NotificationType.WARNING);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            System.out.println("L'âge doit être un nombre valide !");
            return;
        }

        DatabaseConnection db = new DatabaseConnection();
        Connection connectDB = db.getConnection();
        if(connectDB == null){
            System.out.println("Connexion impossible à la base de données !");
            return;
        }
        if (membreEnEdition == null){
            String AddMembre = "INSERT INTO public.membre (idpers, nom, sexe, age, contact) VALUES (?,?,?,?,?)";
            try(PreparedStatement ps = connectDB.prepareStatement(AddMembre)){
                ps.setString(1, idpers);
                ps.setString(2, nom);
                ps.setString(3, sexe);
                ps.setInt(4, age);
                ps.setString(5, contact);

                int i = ps.executeUpdate();
                if (i > 0){
                    System.out.println("Ajout membre avec succès !");
                    showNotification("Membres", "Ajout membre avec succès !",NotificationType.SUCCESS);
                    clearFields();
                    chargerDonneesMembres();
                }
            }
        }else {
            String UpdateMembre = "UPDATE public.membre SET nom=?,sexe=?,age=?,contact=? WHERE idpers=?";
            try(PreparedStatement preparedStatement = connectDB.prepareStatement(UpdateMembre)){
                preparedStatement.setString(1, nom);
                preparedStatement.setString(2, sexe);
                preparedStatement.setInt(3, age);
                preparedStatement.setString(4, contact);
                preparedStatement.setString(5, membreEnEdition.getId());

                int i = preparedStatement.executeUpdate();
                if (i > 0) {
                    showNotification("Membres", "Membre modifié avec succès !", NotificationType.SUCCESS);
                    clearFields();
                    resetEditMode();
                    chargerDonneesMembres();
                }
            }catch (SQLException e) {
                System.err.println("Erreur SQL !");
                e.printStackTrace();
                showNotification("Membres", "Erreur lors de l'opération !", NotificationType.ERROR);
            }
        }
    }
    private void modifierMembre(Membre membre) {
        IdPers.setText(membre.getId());
        NomPers.setText(membre.getNom());
        SexePers.setValue(membre.getSexe());
        AgePers.setText(String.valueOf(membre.getAge()));
        ContactPers.setText(membre.getContact());

        membreEnEdition = membre;
        SavePers.setText("Modifier");
        SavePers.getStyleClass().removeAll("button-primary");
        SavePers.getStyleClass().add("button-primary");
    }
    @FXML
    private void handleCancelAction(ActionEvent event) {
        clearFields();
        resetEditMode();
    }
    private void resetEditMode() {
        membreEnEdition = null;
        SavePers.setText("Enregistrer");
        SavePers.getStyleClass().removeAll("button-outline");
        SavePers.getStyleClass().add("button-primary");
    }

    public void initialize(){

        RowIDMembre.setCellValueFactory(new PropertyValueFactory<>("id"));
        RowNomMembre.setCellValueFactory(new PropertyValueFactory<>("nom"));
        RowSexeMembre.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        RowAgeMembre.setCellValueFactory(new PropertyValueFactory<>("age"));
        RowContMembre.setCellValueFactory(new PropertyValueFactory<>("contact"));

        RowIDMembre.setPrefWidth(50);
        RowNomMembre.setPrefWidth(155);
        RowSexeMembre.setPrefWidth(80);
        RowAgeMembre.setPrefWidth(50);
        RowContMembre.setPrefWidth(250);
        RowActMembre.setPrefWidth(250);

        RowIDMembre.setResizable(false);
        RowNomMembre.setResizable(false);
        RowSexeMembre.setResizable(false);
        RowAgeMembre.setResizable(false);
        RowContMembre.setResizable(false);
        RowActMembre.setResizable(false);

        setColumnAlignment(RowIDMembre, Pos.CENTER);
        setColumnAlignment(RowNomMembre, Pos.CENTER_LEFT);
        setColumnAlignment(RowSexeMembre, Pos.CENTER);
        setColumnAlignment(RowAgeMembre, Pos.CENTER);
        setColumnAlignment(RowContMembre, Pos.CENTER);


        SavePers.getStyleClass().add("button-primary");

        RowNomMembre.setCellFactory(tc -> {
            TableCell<Membre, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.getStyleClass().add("nom-cell");
            return cell;
        });

        RowActMembre.setCellFactory(param -> new TableCell<>() {
            private final Button ModifMembre = new Button("Modifier");
            private final Button DeleteMembre = new Button("Supprimer");
            private final HBox pane = new HBox(5, ModifMembre, DeleteMembre);

            {
                pane.getStyleClass().add("action-cell");
                ModifMembre.getStyleClass().addAll("button-outline");
                DeleteMembre.getStyleClass().addAll("button-primary");
                pane.setAlignment(Pos.CENTER);

                ModifMembre.setOnAction(event -> {
                    Membre membre = getTableView().getItems().get(getIndex());
                    modifierMembre(membre);
                });

                DeleteMembre.setOnAction(event -> {
                    Membre membre = getTableView().getItems().get(getIndex());
                    supprimerMembre(membre);
                });
            }
            @Override
                protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        chargerDonneesMembres();
        SearchMembre.textProperty().addListener((observable, oldValue, newValue) -> {
            SearchMembreOnAction();
        });
    }

    private <T> void setColumnAlignment(TableColumn<Membre, T> column, Pos position) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    setAlignment(position);
                }
            }
        });
    }

    void chargerDonneesMembres() {
        membresList.clear();
        DatabaseConnection db = new DatabaseConnection();
        try (Connection connectDB = db.getConnection();
             Statement statement = connectDB.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM membre")) {

            while (rs.next()) {
                membresList.add(new Membre(
                        rs.getString("idpers"),
                        rs.getString("nom"),
                        rs.getString("sexe"),
                        rs.getInt("age"),
                        rs.getString("contact")
                ));
            }
            TableMembre.setItems(membresList);
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            showNotification("Membres", "Erreur lors du chargement des données", NotificationType.ERROR);
        }
    }

    private void supprimerMembre(Membre membre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le membre");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + membre.getNom() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DatabaseConnection db = new DatabaseConnection();
            try (Connection connectDB = db.getConnection();
                 PreparedStatement ps = connectDB.prepareStatement(
                         "DELETE FROM membre WHERE idpers = ?")) {

                ps.setString(1, membre.getId());
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    membresList.remove(membre);
                    showNotification("Membres", "Membre supprimé avec succès", NotificationType.SUCCESS);
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la suppression: " + e.getMessage());
                showNotification("Membres", "Erreur lors de la suppression", NotificationType.ERROR);
            }
        }
    }

    private void clearFields() {
        IdPers.clear();
        NomPers.clear();
        SexePers.setValue(null);
        AgePers.clear();
        ContactPers.clear();
    }

    private void showNotification(String title, String message, NotificationType type) {
        TrayNotification tray = new TrayNotification();
        tray.setAnimationType(AnimationType.SLIDE);
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setNotificationType(type);
        tray.showAndDismiss(Duration.millis(3000));
    }

}