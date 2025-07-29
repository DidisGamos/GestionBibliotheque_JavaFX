package com.biblio.gestionbiblio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class RendusController implements Initializable {
    @FXML private ComboBox<String> RecupMembreRendu;
    @FXML private ComboBox<String> RecupLivreRendu;
    @FXML private TextField IDRendu;
    @FXML private DatePicker DateRendu;
    @FXML private Button SaveBtnRendu;
    @FXML private Button CancelButtonRendu;

    @FXML private TableView<Rendu> TableRenduShow;
    @FXML private TableColumn<Rendu, String> RowIDRendu;
    @FXML private TableColumn<Rendu, String> RowMembreRendu;
    @FXML private TableColumn<Rendu, String> RowLivreRendu;
    @FXML private TableColumn<Rendu, String> RowDateRendu;
    @FXML private TableColumn<Rendu, String> RowStatutRendu;
    @FXML private TableColumn<Rendu, Void> RowActionRendu;

    private ObservableList<Rendu> listRendu = FXCollections.observableArrayList();
    private Rendu renduEnEdition;

    @Override
    public void initialize(URL url, ResourceBundle rb){
        ChargerMembresRendu();
        ChargerLivresRendu();
        configurationTableauRendu();
        ChargerDonneesRendu();
    }

    void configurationTableauRendu(){
        RowIDRendu.setCellValueFactory(new PropertyValueFactory<>("idrendu"));
        RowMembreRendu.setCellValueFactory(new PropertyValueFactory<>("membre"));
        RowLivreRendu.setCellValueFactory(new PropertyValueFactory<>("livre"));
        RowDateRendu.setCellValueFactory(cellData -> new
                SimpleStringProperty (cellData.getValue().getDateRendu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
//        RowStatutRendu.setCellValueFactory(cellData->cellData.getValue().statutProperty());
        RowStatutRendu.setCellFactory(column -> new TableCell<>(){
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    getStyleClass().removeAll("badge-success", "badge-warning");
                }else{
                    setText(item);
                    getStyleClass().add("badge-success");
                    if ("À temps".equals(item)) {
                        getStyleClass().add("badge-success");
                        getStyleClass().remove("badge-warning");
                    }else{
                        getStyleClass().add("badge-warning");
                        getStyleClass().remove("badge-success");
                    }
                }
            }
        });
        RowActionRendu.setCellFactory(param -> new TableCell<>(){
            private final Button ModifRendu = new Button("Modifier");
            private final Button DeleteRendu = new Button("Supprimer");
            private final HBox pane = new HBox(5,ModifRendu,DeleteRendu);
            {
                pane.setAlignment(Pos.CENTER);
                ModifRendu.getStyleClass().add("button-outline");
                DeleteRendu.getStyleClass().add("button-primary");

                ModifRendu.setOnAction(event -> {
                    Rendu rendu=getTableView().getItems().get(getIndex());
                    ModifRendu(rendu);
                });

                DeleteRendu.setOnAction(event -> DeleteRendu(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    private void handleCancel() {
        clearFieldsRendu();
    }

    private void clearFieldsRendu(){
        IDRendu.clear();
        RecupMembreRendu.getSelectionModel().clearSelection();
        RecupLivreRendu.getSelectionModel().clearSelection();
        DateRendu.setValue(LocalDate.now());
        renduEnEdition=null;
        SaveBtnRendu.setText("Enregistrer");
    }

    private void ModifRendu(Rendu rendu){
        IDRendu.setText(rendu.getIdrendu());
        RecupMembreRendu.getSelectionModel().select(findMembreItem(rendu.getMembre()));
        RecupLivreRendu.getSelectionModel().select(findLivreItem(rendu.getLivre()));
        DateRendu.setValue(rendu.getDateRendu());
        renduEnEdition = rendu;
        SaveBtnRendu.setText("Modifier");
        ChargerDonneesRendu();
    }

    private String findMembreItem(String membreInfo) {
        for (String item : RecupMembreRendu.getItems()) {
            if (item.contains(membreInfo.split("\\(")[0].trim())) {
                return item;
            }
        }
        return null;
    }

    private String findLivreItem(String livreInfo) {
        for (String item : RecupLivreRendu.getItems()) {
            if (item.contains(livreInfo.split("\\(")[0].trim())) {
                return item;
            }
        }
        return null;
    }

    private void DeleteRendu(Rendu rendu){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le prêt");
        alert.setContentText("Êtes-vous sûr ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            try(Connection db = new DatabaseConnection().getConnection()){
                db.setAutoCommit(false);
                try{
                    String DeleteRendu = "DELETE FROM public.rendre WHERE idrendu = ?";
                    try(PreparedStatement preparedStatement = db.prepareStatement(DeleteRendu)){
                        preparedStatement.setString(1, rendu.getIdrendu());
                        preparedStatement.executeUpdate();
                    }
                    if(rendu.getIdlivre() == null){
                        String UpdateExemplaire = "UPDATE public.livre SET exemplaire = exemplaire - 1 WHERE idlivre = ?";
                        try(PreparedStatement preparedStatement = db.prepareStatement(UpdateExemplaire)){
                            preparedStatement.setString(1, rendu.getIdlivre());
                            preparedStatement.executeUpdate();
                        }
                    }
                    db.commit();
                    actualiserAutoRendu();
                    showNotification("Rendu", "Supprimer Rendu avec succès",NotificationType.SUCCESS);
                    ChargerLivresRendu();
                }catch(SQLException e){
                    e.printStackTrace();
                }finally {
                    db.setAutoCommit(true);
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    private void actualiserAutoRendu(){
        listRendu.clear();
        RecupMembreRendu.getItems().clear();
        RecupLivreRendu.getItems().clear();
        ChargerDonneesRendu();
        ChargerMembresRendu();
        ChargerLivresRendu();
        TableRenduShow.refresh();
    }

    void ChargerDonneesRendu(){
        listRendu.clear();
        try(Connection db = new DatabaseConnection().getConnection();
        Statement stmt = db.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT r.*,m.nom AS membre_nom, l.designation AS " +
                "livre_designation FROM rendre r JOIN membre m ON r.idpers = m.idpers\n" +
                "JOIN livre l ON r.idlivre = l.idlivre;")){
            while (rs.next()){
                Rendu rendu = new Rendu(
                  rs.getString("idrendu"),
                  rs.getString("membre_nom"),
                  rs.getString("livre_designation"),
                  rs.getDate("daterendu").toLocalDate()
                );
                rendu.setIdlivre(rs.getString("idlivre"));
                listRendu.add(rendu);
            }
            TableRenduShow.setItems(listRendu);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SaveBtnRenduOnAction() {
        String idRendu = IDRendu.getText().trim();
        String membreSelect = RecupMembreRendu.getSelectionModel().getSelectedItem();
        String livreSelect = RecupLivreRendu.getSelectionModel().getSelectedItem();
        LocalDate dateRendu = DateRendu.getValue();

        if (idRendu.isEmpty() || membreSelect == null || livreSelect == null || dateRendu == null) {
            showNotification("Rendre", "Veuillez remplir tous les champs obligatoires", NotificationType.WARNING);
        }

        String idMembre = extraireId(membreSelect);
        String idLivre = extraireId(livreSelect);

        System.out.println("Extracted member ID: " + idMembre);
        System.out.println("Extracted book ID: " + idLivre);

        try(Connection conn = new DatabaseConnection().getConnection()){
            conn.setAutoCommit(false);
            try{
                if (!verifierExistanceMembre(conn, idMembre)) {
                    showNotification("Erreur", "Membre introuvable", NotificationType.ERROR);
                    System.out.println("Membre introuvable");
                    return;
                }

                if (!verifierDisponibiliteLivre(conn, idLivre)){
                    showNotification("Rendu","Plus d'exemplaires disponibles", NotificationType.WARNING);
                    System.out.println("Plus d'exemplaires disponibles");
                    return;
                }
                if (renduEnEdition == null){
                    String AddRendu =  "INSERT INTO public.rendre (idrendu, idpers, idlivre, daterendu) VALUES (?,?,?,?)";
                    try(PreparedStatement preparedStatement = conn.prepareStatement(AddRendu)){
                        preparedStatement.setString(1, idRendu);
                        preparedStatement.setString(2, idMembre);
                        preparedStatement.setString(3, idLivre);
                        preparedStatement.setDate(4, Date.valueOf(dateRendu));
                        preparedStatement.executeUpdate();
                    }
                    String UpdateLivre = "UPDATE public.livre SET exemplaire = exemplaire + 1 WHERE idlivre = ?";
                    try(PreparedStatement preparedStatement = conn.prepareStatement(UpdateLivre)){
                        preparedStatement.setString(1, idLivre);
                        preparedStatement.executeUpdate();
                    }
                }else{
                    String UpdateRendu = "UPDATE public.rendre SET idpers=?, idlivre=?, daterendu=? WHERE idrendu = ?";
                    try(PreparedStatement ps = conn.prepareStatement(UpdateRendu)){
                        ps.setString(1, idMembre);
                        ps.setString(2, idLivre);
                        ps.setDate(3, Date.valueOf(dateRendu));
                        ps.setString(4, renduEnEdition.getIdrendu());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                actualiserAutoRendu();
                showNotification("Rendu", "Opération réussie", NotificationType.SUCCESS);
                System.out.println("Opération réussie");
                clearFieldsRendu();
                ChargerLivresRendu();
            }catch (SQLException e){
                conn.rollback();
                showNotification("Erreur", "Erreur lors de l'opération: " + e.getMessage(), NotificationType.ERROR);
            }finally{
                conn.setAutoCommit(true);
            }
        }catch (SQLException e){
            showNotification("Erreur", "Erreur de connexion", NotificationType.ERROR);
            e.printStackTrace();
        }

    }

    private String extraireId(String selection) {
        if(selection == null || !selection.contains("(ID:")) return "";
        int start = selection.indexOf("(ID:") + 4;
        int end = selection.indexOf(")", start);
        return selection.substring(start, end);
    }

    private boolean verifierExistanceMembre(Connection conn, String idMembre) throws SQLException {
        if (idMembre == null || idMembre.isEmpty()) return false;
        String sql = "SELECT * FROM public.membre WHERE idpers = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idMembre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    void ChargerMembresRendu() {
        RecupMembreRendu.getItems().clear();
        try(Connection db = new DatabaseConnection().getConnection();
            Statement stmt = db.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT DISTINCT m.idpers,m.nom FROM membre m JOIN preter p " +
                    "ON p.idpers=m.idpers WHERE p.dateretour IS NOT NULL\n" +
                    "ORDER BY M.nom")) {
            while (resultSet.next()) {
                RecupMembreRendu.getItems().add(resultSet.getString("nom") + " (ID:" + resultSet.getString("idpers") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur", "Erreur lors du chargement des membres", NotificationType.ERROR);
        }
    }

    void ChargerLivresRendu() {
        RecupLivreRendu.getItems().clear();
        String query = "SELECT DISTINCT l.idlivre, l.designation " +
                "FROM livre l JOIN preter p ON l.idlivre = p.idlivre " +
                "WHERE p.dateretour IS NOT NULL " +
                "ORDER BY l.designation";

        try (Connection conn = new DatabaseConnection().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                RecupLivreRendu.getItems().add(
                        String.format("%s (ID:%s)", rs.getString("designation"), rs.getString("idlivre"))
                );
            }
        } catch (SQLException e) {
            showNotification("Erreur", "Erreur de chargement: " + e.getMessage(), NotificationType.ERROR);
        }
    }

    private boolean verifierDisponibiliteLivre(Connection conn, String idLivre) throws SQLException {
        String query = "SELECT l.exemplaire > " +
                "(SELECT COUNT(*) FROM preter p WHERE p.idlivre = l.idlivre AND p.dateretour IS NOT NULL) " +
                "AS disponible FROM livre l WHERE l.idlivre = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, idLivre);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean("disponible");
        }
    }
    private void showNotification(String title, String message, NotificationType type) {
        TrayNotification tray = new TrayNotification();
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setNotificationType(type);
        tray.showAndDismiss(Duration.millis(3000));
    }
}
