package com.biblio.gestionbiblio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import tray.notification.TrayNotification;
import tray.notification.NotificationType;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RapportsController {
    @FXML private ComboBox < String > ListMembreRapports ;
    @FXML private Button BtnShowHistory ;
    @FXML private TableView<Rapports> TableMembreRapports ;
    @FXML private TableColumn<Rapports, String> TypeColumnR;
    @FXML private TableColumn<Rapports, String> LivreColumnR;
    @FXML private TableColumn<Rapports, String> DatePretColumnR;
    @FXML private TableColumn<Rapports, String> DateRetourPrevueColumnR;
    @FXML private TableColumn<Rapports, String> DateRetourReelleColumnR;
    @FXML private TableColumn<Rapports, String> StatutColumnR;

    @FXML private DatePicker DateDebutList;
    @FXML private DatePicker DateFinList;
    @FXML private Button GenererRapportList;

    @FXML private TableView < RapportsRetard > TableauNonRenduEmail ;
    @FXML private TableColumn < RapportsRetard , String > MembreEmail ;
    @FXML private TableColumn < RapportsRetard , String > LivreEmail ;
    @FXML private TableColumn < RapportsRetard , String > DateLimiteEmail ;
    @FXML private TableColumn < RapportsRetard , Integer > JourRetardEmail ;
    @FXML private TableColumn < RapportsRetard , Void > ActionEmail ;

    private ObservableList<Rapports> ListHistorique = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        ChargerMembreHistorique();
        ConfigTableauHistorique();

        BtnShowHistory.setOnAction(e -> AfficheHistoriqueMembre());

        BtnShowHistory.setOnAction(e -> AfficheHistoriqueMembre());
        GenererRapportList.setOnAction(e -> genererRapportParPeriode());

        ConfigTableauRetard();
        chargerLivresEnRetard();
    }
    @FXML
    private void handleShowAllHistory() {
        AfficheHistoriqueTousMembres();
    }

    private void genererRapportParPeriode() {
        LocalDate dateDebut = DateDebutList.getValue();
        LocalDate dateFin = DateFinList.getValue();

        if (dateDebut == null || dateFin == null) {
            showNotification("Erreur", "Veuillez sélectionner les deux dates", NotificationType.ERROR);
            return;
        }

        if (dateDebut.isAfter(dateFin)) {
            showNotification("Erreur", "La date de début doit être avant la date de fin", NotificationType.ERROR);
            return;
        }

        ListHistorique.clear();

        String query = "SELECT 'Prêt' AS type, m.nom AS membre, l.designation AS livre, " +
                "p.datepret, p.dateretour AS date_retour_prevue, " +
                "NULL AS date_retour_reelle, " +
                "CASE WHEN p.dateretour IS NULL THEN 'En cours' " +
                "WHEN CURRENT_DATE > p.dateretour THEN 'En retard' " +
                "ELSE 'À temps' END AS statut " +
                "FROM preter p " +
                "JOIN membre m ON p.idpers = m.idpers " +
                "JOIN livre l ON p.idlivre = l.idlivre " +
                "WHERE p.datepret BETWEEN ? AND ? " +
                "ORDER BY p.datepret DESC";

        try (Connection db = new DatabaseConnection().getConnection();
             PreparedStatement stmt = db.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(dateDebut));
            stmt.setDate(2, Date.valueOf(dateFin));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                String membre = rs.getString("membre");
                String livre = rs.getString("livre");
                String datePret = rs.getDate("datepret").toString();
                String dateRetourPrevue = rs.getDate("date_retour_prevue") != null ?
                        rs.getDate("date_retour_prevue").toString() : "";
                String dateRetourReelle = rs.getDate("date_retour_reelle") != null ?
                        rs.getDate("date_retour_reelle").toString() : "";
                String statut = rs.getString("statut");

                ListHistorique.add(new Rapports(
                        type,
                        membre + " - " + livre, // Combine membre et livre pour l'affichage
                        datePret,
                        dateRetourPrevue,
                        dateRetourReelle,
                        statut
                ));
            }

            TableMembreRapports.setItems(ListHistorique);
            showNotification("Succès", "Rapport généré avec succès", NotificationType.SUCCESS);

        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur", "Erreur lors de la génération du rapport: " + e.getMessage(), NotificationType.ERROR);
        }
    }

    private void AfficheHistoriqueTousMembres() {
        ListHistorique.clear();

        String query =
                "SELECT 'Prêt' AS type, m.nom AS membre, l.designation AS livre, p.datepret, p.dateretour AS date_retour_prevue, " +
                        " NULL AS date_retour_reelle, CASE WHEN p.dateretour IS NULL THEN 'En cours' WHEN CURRENT_DATE > p.dateretour THEN 'En retard' " +
                        " ELSE 'À temps' END AS statut FROM preter p JOIN membre m ON p.idpers = m.idpers JOIN " +
                        " livre l ON p.idlivre = l.idlivre WHERE p.dateretour IS NULL UNION ALL SELECT 'Rendu' AS type, " +
                        " m.nom AS membre, l.designation AS livre, p.datepret, p.dateretour AS date_retour_prevue,  r.daterendu AS date_retour_reelle, " +
                        " CASE WHEN r.daterendu > p.dateretour THEN 'En retard' ELSE 'À temps' END AS statut FROM rendre r JOIN " +
                        " preter p ON r.idpret = p.idpret JOIN membre m ON r.idpers = m.idpers JOIN " +
                        " livre l ON r.idlivre = l.idlivre ORDER BY datepret DESC";

        try (Connection db = new DatabaseConnection().getConnection();
             PreparedStatement stmt = db.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String type = rs.getString("type");
                String livre = rs.getString("livre");
                String datePret = rs.getDate("datepret").toString();
                String dateRetourPrevue = rs.getDate("date_retour_prevue") != null ?
                        rs.getDate("date_retour_prevue").toString() : "";
                String dateRetourReelle = rs.getDate("date_retour_reelle") != null ?
                        rs.getDate("date_retour_reelle").toString() : "";
                String statut = rs.getString("statut");

                ListHistorique.add(new Rapports(
                        type,
                        livre,
                        datePret,
                        dateRetourPrevue,
                        dateRetourReelle,
                        statut
                ));
            }

            TableMembreRapports.setItems(ListHistorique);
            showNotification("Succès", "Historique de tous les membres chargé", NotificationType.SUCCESS);

        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur", "Erreur lors du chargement de l'historique: " + e.getMessage(), NotificationType.ERROR);
        }
    }

    void ChargerMembreHistorique() {
        ListMembreRapports.getItems().clear();
        try(Connection db = new DatabaseConnection().getConnection();
            Statement stmt = db.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT idpers, nom FROM public.membre ORDER BY nom")) {

            while(resultSet.next()) {
                ListMembreRapports.getItems().add(
                        resultSet.getString("nom") + " (ID:" + resultSet.getString("idpers") + ")"
                );
            }
            System.out.println("Membres récupérés: " + ListMembreRapports.getItems().size());
        } catch (SQLException e) {
            e.printStackTrace();
            showNotification("Erreur", "Erreur de chargement des membres", NotificationType.ERROR);
        }
    }

    void ConfigTableauHistorique() {
        TypeColumnR.setCellValueFactory(new PropertyValueFactory<>("type"));
        LivreColumnR.setCellValueFactory(new PropertyValueFactory<>("livre"));
        DatePretColumnR.setCellValueFactory(new PropertyValueFactory<>("datePret"));
        DateRetourPrevueColumnR.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        DateRetourReelleColumnR.setCellValueFactory(new PropertyValueFactory<>("dateRetourReelle"));
        StatutColumnR.setCellValueFactory(new PropertyValueFactory<>("statut"));
        DatePretColumnR.setCellFactory(column -> new TableCell<>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : formatDate(item));
            }
        });
        DateRetourPrevueColumnR.setCellFactory(column -> new TableCell<>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : formatDate(item));
            }
        });
        DateRetourReelleColumnR.setCellFactory(column -> new TableCell<>(){
           @Override
           protected void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               setText(empty ? null : formatDate(item));
           }
        });
        StatutColumnR.setCellFactory(column -> new TableCell<>(){
           @Override
           protected void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               if (empty || item == null) {
                   setText(null);
                   setStyle("");
               }else{
                   setText(item);
                   if ("En retard".equals(item)) {
                       setStyle("-fx-text-fill: red ; -fx-font-weight: bold");
                   } else if ("À temps".equals(item)) {
                       setStyle("-fx-text-fill: green; -fx-font-weight: bold");
                   }else{
                       setStyle("-fx-text-fill: orange; -fx-font-weight: bold");
                   }
               }
           }
        });
    }

    private String formatDate(String dateStr){
        if (dateStr == null ||dateStr.isEmpty()) return "";
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }catch (Exception e){
            return dateStr;
        }
    }

    private void AfficheHistoriqueMembre(){
        String SelectMembreHistorique = ListMembreRapports.getSelectionModel().getSelectedItem();
        if(SelectMembreHistorique == null || SelectMembreHistorique.isEmpty()){
            showNotification("Erreur", "Veuillez sélectionner un membre",NotificationType.ERROR);
            return;
        }
        String IDMembre = extractId(SelectMembreHistorique);
        if(IDMembre.isEmpty()){
            showNotification("Erreur", "ID membre invalide",NotificationType.ERROR);
            return;
        }
        ChargerHistoriqueMembre(IDMembre);
    }

    private String extractId(String Selectionner){
        if(Selectionner == null || !Selectionner.contains("(ID:")) return "";
        int start = Selectionner.indexOf("(ID:") + 4;
        int end = Selectionner.indexOf(")", start);
        return Selectionner.substring(start, end);
    }

    private void ChargerHistoriqueMembre(String IDMembre){
        ListHistorique.clear();
        String AffichageHistorique =
                "SELECT 'Prêt' AS type, l.designation AS livre, p.datepret, p.dateretour AS date_retour_prevue, NULL AS date_retour_reelle, " +
                        " CASE WHEN p.dateretour IS NULL THEN 'En cours' WHEN CURRENT_DATE > p.dateretour THEN 'En retard' ELSE 'À temps' " +
                        " END AS statut FROM preter p JOIN livre l ON p.idlivre = l.idlivre WHERE p.idpers = ? AND (p.dateretour IS NULL OR NOT EXISTS " +
                        "(SELECT 1 FROM rendre r WHERE r.idlivre = p.idlivre AND r.idpers = p.idpers AND r.daterendu >= p.datepret)) " +
                        "UNION ALL " +
                        "SELECT 'Rendu' AS type, l.designation AS livre, " +
                        " p.datepret, p.dateretour AS date_retour_prevue, r.daterendu AS date_retour_reelle, CASE " +
                        " WHEN r.daterendu > p.dateretour THEN 'En retard' ELSE 'À temps' END AS statut FROM rendre r " +
                        "JOIN preter p ON (r.idlivre = p.idlivre AND r.idpers = p.idpers AND r.daterendu >= p.datepret) " +
                        "JOIN livre l ON r.idlivre = l.idlivre WHERE r.idpers = ? ORDER BY datepret DESC";

        try(Connection db = new DatabaseConnection().getConnection();
            PreparedStatement preparedStatement = db.prepareStatement(AffichageHistorique)) {

            preparedStatement.setString(1, IDMembre);
            preparedStatement.setString(2, IDMembre);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                String livre = resultSet.getString("livre");
                String datePret = resultSet.getDate("datepret").toString();
                String dateRetourPrevue = resultSet.getDate("date_retour_prevue") != null ?
                        resultSet.getDate("date_retour_prevue").toString() : "";
                String dateRetourReelle = resultSet.getDate("date_retour_reelle") != null ?
                        resultSet.getDate("date_retour_reelle").toString() : "";
                String statut = resultSet.getString("statut");

                ListHistorique.add(new Rapports(
                        type,
                        livre,
                        datePret,
                        dateRetourPrevue,
                        dateRetourReelle,
                        statut
                ));
            }

            TableMembreRapports.setItems(ListHistorique);
            showNotification("Succès", "Historique du membre chargé", NotificationType.SUCCESS);

        } catch (SQLException e) {
            System.err.println("Erreur SQL détaillée:");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Code d'erreur: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            showNotification("Erreur", "Erreur de chargement de l'historique: " + e.getMessage(), NotificationType.ERROR);
        }
    }

    private String CalculeStatuts(String type, String dateRetourPrevue, String dateRetourReelle) {
        if (dateRetourPrevue == null || dateRetourPrevue.isEmpty()) {
            return "N/A";
        }
        LocalDate retourPrevue = LocalDate.parse(dateRetourPrevue);

        if ("Rendu".equals(type)) {
            if (dateRetourReelle == null || dateRetourReelle.isEmpty()) {
                return "N/A";
            }
            LocalDate retourReelle = LocalDate.parse(dateRetourReelle);
            return retourReelle.isAfter(retourPrevue) ? "En retard" : "À temps";
        } else {
            return LocalDate.now().isAfter(retourPrevue) ? "En retard" : "En cours";
        }
    }

    void ConfigTableauRetard(){
        MembreEmail.setCellValueFactory(new PropertyValueFactory<>("membre"));
        LivreEmail.setCellValueFactory(new PropertyValueFactory<>("livre"));
        DateLimiteEmail.setCellValueFactory(new PropertyValueFactory<>("dateLimite"));
        JourRetardEmail.setCellValueFactory(new PropertyValueFactory<>("joursRetard"));

        DateLimiteEmail.setCellFactory(column-> new TableCell<>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : formatDate(item));
            }
        });

        ActionEmail.setCellFactory(param-> new TableCell<>(){
            private final Button EmailBtn = new Button();
            private final HBox pane = new HBox(EmailBtn);
            {
                pane.setAlignment(Pos.CENTER);
                EmailBtn.getStyleClass().add("button-generate");
                try{
                    InputStream imageStream = getClass().getResourceAsStream("/com/biblio/gestionbiblio/assets/envelope_144px.png");
                    if (imageStream != null) {
                        Image EmailImage = new Image(imageStream,25,25,true,true);
                        ImageView IconEmail = new ImageView(EmailImage);
                        EmailBtn.setGraphic(IconEmail);
                    }else{
                        EmailBtn.setText("Email");
                        System.err.println("Fichier icône introuvable");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                EmailBtn.setOnAction(event->{
                    RapportsRetard Retard = getTableView().getItems().get(getIndex());
                    EnvoyerEmailRappel(Retard);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                }else{
                    setGraphic(pane);
                }
            }
        });
    }

    private void EnvoyerEmailRappel(RapportsRetard retard) {
        String host = "smtp.gmail.com";
        String port = "465";
        String user = "herllandysamoroschristy@gmail.com";
        String password = "wric trsn rxmp ixph";
        String to = retard.getContact(); // <-- On garde l'email du membre concerné

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Rappel de retour de livre 📚");

            // Corps du message
            String plainText = String.format(
                    "Bonjour %s,\n\nCeci est un rappel concernant le livre \"%s\" que vous avez emprunté.\n" +
                            "Il vous reste %d jour(s) avant la date limite de retour (%s).\n\n" +
                            "Merci de bien vouloir le rendre à temps pour éviter toute pénalité.\n\n" +
                            "Cordialement,\nVotre bibliothèque.",
                    retard.getMembre(), retard.getLivre(), retard.getJoursRetard(), retard.getDateLimite()
            );
            message.setText(plainText);

            Transport.send(message);
            System.out.println("📧 Email envoyé à " + to);
            this.showNotification("Email", "Email envoyé à " + to, NotificationType.SUCCESS);

        } catch (MessagingException e) {
            System.out.println("❌ Échec de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
            this.showNotification("Email", "❌ Erreur lors de l'envoi à " + to + " : " + e.getMessage(), NotificationType.ERROR);
        }
    }

    void chargerLivresEnRetard(){
        ObservableList<RapportsRetard> retards = FXCollections.observableArrayList();
        String SQLRetard = "SELECT m.nom, m.contact, l.designation AS livre, p.dateretour AS date_limite, \n" +
                "       CURRENT_DATE - p.dateretour AS jours_retard \n" +
                "FROM preter p \n" +
                "JOIN membre m ON p.idpers = m.idpers \n" +
                "JOIN livre l ON p.idlivre = l.idlivre \n" +
                "WHERE p.dateretour < CURRENT_DATE \n" +
                "AND NOT EXISTS (SELECT 1 FROM rendre r WHERE r.idlivre = p.idlivre AND r.idpers = p.idpers) \n" +
                "ORDER BY jours_retard DESC";
        try(Connection db = new DatabaseConnection().getConnection();
        Statement stmt = db.createStatement();
        ResultSet resultSet = stmt.executeQuery(SQLRetard)){
            while(resultSet.next()){

                String dateLimite = resultSet.getDate("date_limite") != null ?
                        resultSet.getDate("date_limite").toString() : "N/A";

                int joursRetard = resultSet.getObject("jours_retard") != null ?
                        resultSet.getInt("jours_retard") : 0;

                RapportsRetard Retard = new RapportsRetard(
                        resultSet.getString("nom"),
                        resultSet.getString("livre"),
                        resultSet.getDate("date_limite").toString(),
                        resultSet.getInt("jours_retard"),
                        resultSet.getString("contact")
                );
                retards.add(Retard);
            }
            TableauNonRenduEmail.setItems(retards);
        }catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            System.err.println("État SQL: " + e.getSQLState());
            System.err.println("Code d'erreur: " + e.getErrorCode());
            e.printStackTrace();
            showNotification("Erreur", "Erreur SQL: " + e.getMessage(), NotificationType.ERROR);
        }
    }

    private void showNotification ( String title , String message , NotificationType type ) {
        TrayNotification tray = new TrayNotification ( ) ;
        tray . setTitle ( title ) ;
        tray . setMessage ( message ) ;
        tray . setNotificationType ( type ) ;
        tray . showAndDismiss ( Duration. millis ( 3000 ) ) ;
    }
}