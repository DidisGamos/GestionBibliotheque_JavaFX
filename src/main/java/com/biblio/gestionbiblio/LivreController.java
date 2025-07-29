package com.biblio.gestionbiblio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;

import java.sql.*;
import java.util.Optional;

public class LivreController {
    @FXML private TextField IdLivre;
    @FXML private TextField TitleLivre;
    @FXML private TextField NbrExemplaire;
    @FXML private TextField SearchLivre;
    @FXML private Button SaveLivre;

    @FXML private TableView<Livre> livresTable;
    @FXML private TableColumn<Livre, String> RowIDLivre;
    @FXML private TableColumn<Livre, String> RowDesignLivre;
    @FXML private TableColumn<Livre, Integer> RowExpLivre;
    @FXML private TableColumn<Livre, String> RowDispoLivre;
    @FXML private TableColumn<Livre, Void> RowActLivre;

    private Livre livreEnEdition = null;

    private ObservableList<Livre> listLivre = FXCollections.observableArrayList();
    ObservableList<Livre> searchLivreList = FXCollections.observableArrayList();

    @FXML
    private void SaveLivreAction(ActionEvent event) throws SQLException {
        String idlivre = IdLivre.getText();
        String titre = TitleLivre.getText();
        String exemplaireStr = NbrExemplaire.getText();

        if(idlivre.isEmpty() || titre.isEmpty() || exemplaireStr.isEmpty()){
            System.out.println("Veuillez remplir tous les champs !");
            showNotification("Livre", "Veuillez remplir tous les champs !", NotificationType.WARNING);
            return;
        }

        int exemplaire;
        try{
            exemplaire = Integer.parseInt(exemplaireStr);
        }catch (NumberFormatException e){
            System.out.println("L'âge doit être un nombre valide !");
            return;
        }
        DatabaseConnection db = new DatabaseConnection();
        Connection connectDB = db.getConnection();
        if (connectDB == null) {
            System.out.println("Connexion impossible à la base de données !");
            return;
        }
        if (livreEnEdition == null){
            String AddLivre = "INSERT INTO public.livre (idlivre, designation, exemplaire) VALUES (?,?,?)";
            try(PreparedStatement preparedStatement = connectDB.prepareStatement(AddLivre)){
                preparedStatement.setString(1, idlivre);
                preparedStatement.setString(2, titre);
                preparedStatement.setInt(3, exemplaire);

                int result = preparedStatement.executeUpdate();
                if (result > 0) {
                    System.out.println("Ajout Livre avec succès !");
                    showNotification("Livre", "Ajout Livre avec succès !", NotificationType.SUCCESS);
                    clearField();
                    actualiserToutLivre();
                }
            }
        }else {
            String UpdateLivre ="UPDATE public.livre SET designation=?, exemplaire=? WHERE idlivre=?";
            try(PreparedStatement preparedStatement = connectDB.prepareStatement(UpdateLivre)){
                preparedStatement.setString(1, titre);
                preparedStatement.setInt(2, exemplaire);
                preparedStatement.setString(3, livreEnEdition.getId());

                int result = preparedStatement.executeUpdate();
                if (result > 0) {
                    showNotification("Livre", "Membre modifié avec succès !",NotificationType.SUCCESS);
                    clearField();
                    resetEditMode();
                    actualiserToutLivre();
                }
            }catch (SQLException e){
                System.out.println("Erreur SQL !");
                e.printStackTrace();
                showNotification("Livre", "Erreur lors de l'opération !", NotificationType.ERROR);
            }
        }
    }

    private void ModifLivre(Livre livre){
        IdLivre.setText(livre.getId());
        TitleLivre.setText(livre.getDesignation());
        NbrExemplaire.setText(livre.getExemplaire());

        livreEnEdition = livre;
        SaveLivre.setText("Modifier");
        SaveLivre.getStyleClass().removeAll("button-primary");
        SaveLivre.getStyleClass().add("button-primary");
    }
    private void DeleteLivre(Livre livre){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le membre");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + livre.getDesignation() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            DatabaseConnection db = new DatabaseConnection();
            try(Connection connectDB=db.getConnection();
            PreparedStatement ps = connectDB.prepareStatement("DELETE FROM livre WHERE idlivre=?")){
                ps.setString(1, livre.getId());
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    listLivre.remove(livre);
                    showNotification("Livres", "Livre supprimé avec succès", NotificationType.SUCCESS);
                }
            }catch (SQLException e){
                System.out.println("Erreur lors de la suppression: " + e.getMessage());
                showNotification("Livre", "Erreur lors de la suppression", NotificationType.ERROR);
            }
        }
    }

    private void clearField() {
        IdLivre.clear();
        TitleLivre.clear();
        NbrExemplaire.clear();
    }

    @FXML
    private void handleCancelAction(ActionEvent event) {
        clearField();
        resetEditMode();
    }
    private void resetEditMode() {
        livreEnEdition = null;
        SaveLivre.setText("Enregistrer");
        SaveLivre.getStyleClass().remove("button-outline");
        SaveLivre.getStyleClass().add("button-primary");
    }

    public void initialize(){
        configureDisponibiliteColumn();
        RowIDLivre.setCellValueFactory(new PropertyValueFactory<>("id"));
        RowDesignLivre.setCellValueFactory(new PropertyValueFactory<>("designation"));
        RowExpLivre.setCellValueFactory(new PropertyValueFactory<>("exemplaire"));

        RowIDLivre.setResizable(false);
        RowDesignLivre.setResizable(false);
        RowExpLivre.setResizable(false);
        RowDispoLivre.setResizable(false);
        RowActLivre.setResizable(false);

        setColumnAlignment(RowIDLivre, Pos.CENTER);
        setColumnAlignment(RowDesignLivre, Pos.CENTER);
        setColumnAlignment(RowExpLivre, Pos.CENTER);
        setColumnAlignment(RowDispoLivre, Pos.CENTER);
        setColumnAlignment(RowActLivre, Pos.CENTER);

        SaveLivre.getStyleClass().add("button-primary");

        RowDesignLivre.setCellFactory(tc->{
            TableCell<Livre, String>cell = new TableCell<>(){
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.getStyleClass().add("design-cell");
            return cell;
        });
        RowActLivre.setCellFactory(param -> new TableCell<>(){
            private final Button ModifLivre = new Button ("Modifier");
            private final Button DeleteLivre = new Button ("Supprimer");
            private final HBox pane = new HBox (5, ModifLivre, DeleteLivre);
            {
                pane.getStyleClass().add("action-cell");
                ModifLivre.getStyleClass().add("button-outline");
                DeleteLivre.getStyleClass().add("button-primary");
                pane.setAlignment(Pos.CENTER);

                ModifLivre.setOnAction(event -> {
                    Livre livre = getTableView().getItems().get(getIndex());
                    ModifLivre(livre);
                });
                DeleteLivre.setOnAction(event -> {
                    Livre livre = getTableView().getItems().get(getIndex());
                    DeleteLivre(livre);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty){
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        SearchLivre.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercherLivresEnTempsReel(newValue);
        });
        actualiserToutLivre();
    }


    private <T> void setColumnAlignment(TableColumn<Livre, T> column, Pos position) {
        column.setCellFactory(tc->new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty){
                super.updateItem(item, empty);
                    if (empty || item == null){
                        setText(null);
                        setGraphic(null);
                    }else{
                        setText(item.toString());
                        setAlignment(position);
                    }
                }
        });
    }
    void chargerDonneesLivre() {
        listLivre.clear();
        DatabaseConnection db = new DatabaseConnection();
        try (Connection connectDB = db.getConnection();
             Statement statement = connectDB.createStatement();
             ResultSet rs = statement.executeQuery("SELECT l.*, " +
                     "(SELECT COUNT(*) FROM preter p WHERE p.idlivre = l.idlivre AND p.dateretour IS NULL) AS nb_prets_en_cours " +
                     "FROM livre l")) {

            while (rs.next()) {
                int exemplaires = Integer.parseInt(rs.getString("exemplaire"));
                int pretsEnCours = rs.getInt("nb_prets_en_cours");
                boolean disponible = pretsEnCours < exemplaires;

                listLivre.add(new Livre(
                        rs.getString("idlivre"),
                        rs.getString("designation"),
                        rs.getString("exemplaire"),
                        disponible
                ));
            }
            livresTable.setItems(listLivre);
        } catch (SQLException e) {
            System.out.println("Erreur SQL: " + e.getMessage());
            showNotification("Livres", "Erreur lors du chargement des données", NotificationType.ERROR);
        }
    }
    private void actualiserToutLivre() {
        listLivre.clear();
        chargerDonneesLivre();
        livresTable.refresh();
    }
    private void configureDisponibiliteColumn() {
        RowDispoLivre.setCellValueFactory(cellData -> {
            Livre livre = cellData.getValue();
            return new SimpleStringProperty(livre.isDisponible() ? "Disponible" : "Emprunté");
        });

        RowDispoLivre.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Livre livre = getTableView().getItems().get(getIndex());
                    if (livre.isDisponible()) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void rechercherLivresEnTempsReel(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            livresTable.setItems(listLivre); // Afficher la liste complète si le champ est vide
            return;
        }
        DatabaseConnection db = new DatabaseConnection();
        try (Connection connectDB = db.getConnection();
             PreparedStatement preparedStatement = connectDB.prepareStatement("SELECT l.*, " +
                     "(SELECT COUNT(*) FROM preter p WHERE p.idlivre = l.idlivre AND p.dateretour IS NULL) AS nb_prets_en_cours " +
                     "FROM public.livre l WHERE l.designation LIKE ? OR l.idlivre LIKE ?")) {

            String SearchPattern = "%" + searchText + "%";
            preparedStatement.setString(1, SearchPattern);
            preparedStatement.setString(2, SearchPattern);

            ObservableList<Livre> searchLivreList = FXCollections.observableArrayList();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int exemplaires = Integer.parseInt(resultSet.getString("exemplaire"));
                    int pretsEnCours = resultSet.getInt("nb_prets_en_cours");
                    boolean disponible = pretsEnCours < exemplaires;

                    searchLivreList.add(new Livre(
                            resultSet.getString("idlivre"),
                            resultSet.getString("designation"),
                            resultSet.getString("exemplaire"),
                            disponible
                    ));
                }
            }
            livresTable.setItems(searchLivreList);
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Livre", "Erreur lors de la recherche", NotificationType.ERROR);
            System.out.println("Erreur lors de la recherche de livre en temps réel : " + e.getMessage());
        }
    }

    @FXML
    public void CancelPersAction(javafx.event.ActionEvent event){
        clearField();
        livreEnEdition = null;
        SaveLivre.setText("Ajouter");
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
