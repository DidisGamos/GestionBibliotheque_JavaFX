package com.biblio.gestionbiblio;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class PretsController implements Initializable {

    @FXML private TableView<Prets> TablePrets;
    @FXML private TableColumn<Prets, String> ForIDPrets;
    @FXML private TableColumn<Prets, String> ForRecupMembre;
    @FXML private TableColumn<Prets, String> ForRecupLivre;
    @FXML private TableColumn<Prets, String> ForDatePrets;
    @FXML private TableColumn<Prets, String> ForDateRetourPrets;
    @FXML private TableColumn<Prets, String> ForStatutPrets;
    @FXML private TableColumn<Prets, Void> ForActPrets;
    @FXML private TableColumn<Prets, Void> ForGeneratePDF;

    @FXML private ComboBox<String> RecupMembre;
    @FXML private ComboBox<String> RecupLivre;
    @FXML private TextField IdPreter;
    @FXML private DatePicker DatePret;
    @FXML private DatePicker DateRetour;
    @FXML private Button SavePret;
    @FXML private Button CancelPret;

    private ObservableList<Prets> pretsList = FXCollections.observableArrayList();
    private Prets pretEnEdition;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        chargerMembres();
        chargerLivres();
        chargerDonneesPrets();
        DatePret.setValue(LocalDate.now());
    }

    private void configurerTableau() {
        ForIDPrets.setCellValueFactory(new PropertyValueFactory<>("idpret"));
        ForRecupMembre.setCellValueFactory(new PropertyValueFactory<>("membre"));
        ForRecupLivre.setCellValueFactory(new PropertyValueFactory<>("livre"));
        ForDatePrets.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDatePret().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        ForDateRetourPrets.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDateRetour();
            return new SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });
        ForStatutPrets.setCellValueFactory(cellData -> cellData.getValue().statutProperty());

        ForStatutPrets.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    getStyleClass().removeAll("retourner-success", "retourner-error");
                } else {
                    setText(item);
                    getStyleClass().add("badge");

                    if ("Retourné".equals(item)) {
                        getStyleClass().add("retourner-success");
                        getStyleClass().remove("retourner-error");
                    } else {
                        getStyleClass().add("retourner-error");
                        getStyleClass().remove("retourner-success");
                    }
                }
            }
        });
        ForActPrets.setCellFactory(param -> new TableCell<>() {
            private final Button modifierBtn = new Button("Modifier");
            private final Button supprimerBtn = new Button("Supprimer");
            private final Button retournerBtn = new Button("Retourner");
            private final HBox pane = new HBox(5, modifierBtn, supprimerBtn, retournerBtn);

            {
                pane.setAlignment(Pos.CENTER);
                modifierBtn.getStyleClass().add("button-outline");
                supprimerBtn.getStyleClass().add("button-primary");
                retournerBtn.getStyleClass().add("button-success");

                modifierBtn.setOnAction(event -> {
                    Prets pret = getTableView().getItems().get(getIndex());
                    modifierPret(pret);
                });

                supprimerBtn.setOnAction(event -> supprimerPret(getTableView().getItems().get(getIndex())));

                retournerBtn.setOnAction(event -> {
                    Prets pret = getTableView().getItems().get(getIndex());
                    if (!pret.isRendu()) {
                        marquerCommeRetourne(pret);
                    } else {
                        showNotification("Prêts", "Ce prêt est déjà retourné", NotificationType.WARNING);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        ForGeneratePDF.setCellFactory(param -> new TableCell<>() {
            private final Button genererPDFBtn = new Button();
            private final HBox pane = new HBox(genererPDFBtn);

            {
                pane.setAlignment(Pos.CENTER);
                genererPDFBtn.getStyleClass().add("button-generate");

                try {
                    InputStream imageStream = getClass().getResourceAsStream(
                            "/com/biblio/gestionbiblio/assets/pdf_480px.png");

                    if (imageStream != null) {
                        Image pdfImage = new Image(imageStream, 30, 30, true, true);
                        ImageView pdfIcon = new ImageView(pdfImage);
                        genererPDFBtn.setGraphic(pdfIcon);
                    } else {
                        genererPDFBtn.setText("PDF");
                        System.err.println("Fichier d'icône introuvable");
                    }
                } catch (Exception e) {
                    genererPDFBtn.setText("PDF");
                    e.printStackTrace();
                }

                genererPDFBtn.setOnAction(event -> {
                    Prets pret = getTableView().getItems().get(getIndex());
                    if (pret != null) genererPDF(pret);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void genererPDF(Prets pret) {
        if (pret == null || pret.getMembre() == null) {
            showNotification("Erreur", "Aucun prêt sélectionné", NotificationType.ERROR);
            return;
        }
        try {
            String dest = System.getProperty("user.home") + "/Desktop/pret_membre_" + pret.getMembre().replaceAll("\\s+", "_") + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(dest));
            document.open();

            try {
                String logoPath = "D:/HERLLANDYS/PROJECTS/IG ENI/JAVA JAVAFX/GestionBibliothequeAllFrancaise/src/main/resources/com/biblio/gestionbiblio/assets/Alliance-Francaise-Logo.png";
                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoPath);
                logo.scaleToFit(150, 150);
                logo.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
                document.add(logo);
                document.add(new Paragraph(" "));
            } catch (Exception e) {
                System.err.println("Erreur de chargement du logo: " + e.getMessage());
                document.add(new Paragraph("[LOGO ALLIANCE FRANÇAISE]"));
            }

            Paragraph title = new Paragraph("Alliance Française",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            String membreInfo = getMembreDetails(pret.getMembre());
            Paragraph membreParagraph = new Paragraph("Info Membre :\n" + membreInfo,
                    FontFactory.getFont(FontFactory.HELVETICA, 12));
            document.add(membreParagraph);

            document.add(new Paragraph("\n\n"));

            // Créer le tableau
            PdfPTable table = new PdfPTable(3);
            table.addCell("Code Livre");
            table.addCell("Intitulé Livre");
            table.addCell("Nombre prêté");

            // Charger tous les prêts de ce membre
            try (Connection conn = new DatabaseConnection().getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT p.idpret, l.designation, p.datepret, p.dateretour, l.idlivre " + // Sélection de l.idlivre
                                 "FROM preter p " +
                                 "JOIN membre m ON p.idpers = m.idpers " +
                                 "JOIN livre l ON p.idlivre = l.idlivre " +
                                 "WHERE m.nom = ?")) {

                ps.setString(1, pret.getMembre().split("\\(")[0].trim());
                ResultSet rs = ps.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

                while (rs.next()) {
                    table.addCell(rs.getString("idlivre"));
                    table.addCell(rs.getString("designation"));
                    table.addCell("1");
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors du chargement des prêts du membre: " + e.getMessage());
            }

            document.add(table);

            // Ajouter les dates après le tableau
            try (Connection conn = new DatabaseConnection().getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT p.datepret, p.dateretour " +
                                 "FROM preter p " +
                                 "JOIN membre m ON p.idpers = m.idpers " +
                                 "WHERE m.nom = ?")) {

                ps.setString(1, pret.getMembre().split("\\(")[0].trim());
                ResultSet rs = ps.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

                while (rs.next()) {
                    Paragraph dates = new Paragraph(
                            "Prêté le : " + rs.getDate("datepret").toLocalDate().format(formatter) + "\n" +
                                    "Doit être rendu le : " +
                                    (rs.getDate("dateretour") != null
                                            ? rs.getDate("dateretour").toLocalDate().format(formatter)
                                            : "Pas encore rendu"),
                            FontFactory.getFont(FontFactory.HELVETICA, 12)
                    );
                    document.add(new Paragraph("\n"));
                    document.add(dates);
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors du chargement des dates de prêt: " + e.getMessage());
            }

            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Merci d'avoir utilisé notre service.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));

            document.close();
            showNotification("PDF", "Reçu généré avec succès !", NotificationType.SUCCESS);
        } catch (Exception e) {
            showNotification("Erreur", "Échec de génération du PDF : " + e.getMessage(), NotificationType.ERROR);
        }
    }


    private String getMembreDetails(String membreNom) throws SQLException {
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nom, age, sexe, contact FROM membre WHERE nom = ?")) {
            ps.setString(1, membreNom.split("\\(")[0].trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nom") + "\n" +
                        rs.getInt("age") + " ans\n" +
                        rs.getString("sexe") + "\n" +
                        "Contact : " + rs.getString("contact");
            }
        }
        return "Membre non trouvé";
    }

    void chargerMembres() {
        RecupMembre.getItems().clear();
        try (Connection conn = new DatabaseConnection().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT idpers, nom FROM public.membre ORDER BY nom")) {

            while (rs.next()) {
                RecupMembre.getItems().add(rs.getString("nom") + " (ID:" + rs.getString("idpers") + ")");
            }
        } catch (SQLException e) {
            showNotification("Erreur", "Chargement membres échoué", NotificationType.ERROR);
        }
    }

    void chargerLivres() {
        RecupLivre.getItems().clear();
        try (Connection conn = new DatabaseConnection().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT l.idlivre, l.designation, l.exemplaire, " +
                             "(SELECT COUNT(*) FROM preter p WHERE p.idlivre = l.idlivre AND p.dateretour IS NULL) AS nb_prets_en_cours " +
                             "FROM livre l " +
                             "WHERE l.exemplaire > (SELECT COUNT(*) FROM preter p WHERE p.idlivre = l.idlivre AND p.dateretour IS NULL) " +
                             "ORDER BY l.designation")) {

            while (rs.next()) {
                int exemplaires = rs.getInt("exemplaire");
                int pretsEnCours = rs.getInt("nb_prets_en_cours");
                boolean disponible = pretsEnCours < exemplaires;

                if (disponible) {
                    RecupLivre.getItems().add(rs.getString("designation") + " (ID:" + rs.getString("idlivre") + ")");
                }
            }
        } catch (SQLException e) {
            showNotification("Erreur", "Chargement livres échoué", NotificationType.ERROR);
        }
    }

    @FXML
    private void handleSavePret() {
        String idPret = IdPreter.getText().trim();
        String membreSelectionne = RecupMembre.getSelectionModel().getSelectedItem();
        String livreSelectionne = RecupLivre.getSelectionModel().getSelectedItem();
        LocalDate datePret = DatePret.getValue();
        LocalDate dateRetour = DateRetour.getValue();

        if (idPret.isEmpty() || membreSelectionne == null || livreSelectionne == null || datePret == null || dateRetour == null) {
            showNotification("Prêts", "Veuillez remplir tous les champs obligatoires", NotificationType.WARNING);
            return;
        }

        String idMembre = extraireId(membreSelectionne);
        String idLivre = extraireId(livreSelectionne);

        try (Connection conn = new DatabaseConnection().getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!verifierDisponibiliteLivre(conn, idLivre)) {
                    showNotification("Prêt", "Plus d'exemplaires disponibles", NotificationType.WARNING);
                    return;
                }

                if (pretEnEdition == null) {
                    String sql = "INSERT INTO public.preter (idpret, idpers, idlivre, datepret, dateretour) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, idPret);
                        ps.setString(2, idMembre);
                        ps.setString(3, idLivre);
                        ps.setDate(4, Date.valueOf(datePret));
                        ps.setDate(5, DateRetour.getValue() != null ? Date.valueOf(DateRetour.getValue()) : null);
                        ps.executeUpdate();
                        actualiserToutPrets();
                    }

                    String updateLivre = "UPDATE public.livre SET exemplaire = exemplaire - 1 WHERE idlivre = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateLivre)) {
                        ps.setString(1, idLivre);
                        ps.executeUpdate();
                    }

                } else {
                    String sql = "UPDATE public.preter SET idpers=?, idlivre=?, datepret=?, dateretour=? WHERE idpret=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, idMembre);
                        ps.setString(2, idLivre);
                        ps.setDate(3, Date.valueOf(datePret));
                        ps.setDate(4, DateRetour.getValue() != null ? Date.valueOf(DateRetour.getValue()) : null);
                        ps.setString(5, pretEnEdition.getIdpret());
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                actualiserToutPrets();
                showNotification("Prêts", "Opération réussie", NotificationType.SUCCESS);
                clearFields();
                pretsList.clear();
                chargerLivres();
            } catch (SQLException e) {
                conn.rollback();
                showNotification("Erreur", "Erreur lors de l'opération: " + e.getMessage(), NotificationType.ERROR);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            showNotification("Erreur", "Erreur de connexion", NotificationType.ERROR);
        }
    }

    private boolean verifierDisponibiliteLivre(Connection conn, String idLivre) throws SQLException {
        String sql = "SELECT l.exemplaire, " +
                "(SELECT COUNT(*) FROM preter p WHERE p.idlivre = l.idlivre AND p.dateretour IS NULL) AS nb_prets_en_cours " +
                "FROM livre l WHERE l.idlivre = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idLivre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int exemplaires = rs.getInt("exemplaire");
                int pretsEnCours = rs.getInt("nb_prets_en_cours");
                return pretsEnCours < exemplaires;
            }
            return false;
        }
    }

    private void actualiserToutPrets() {
        pretsList.clear();
        chargerDonneesPrets();

        RecupMembre.getItems().clear();
        RecupLivre.getItems().clear();
        chargerMembres();
        chargerLivres();

        TablePrets.refresh();
    }

    void chargerDonneesPrets() {
        pretsList.clear();
        try (Connection conn = new DatabaseConnection().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT p.*, m.nom AS membre_nom, l.designation AS livre_designation " +
                             "FROM public.preter p JOIN public.membre m ON p.idpers = m.idpers " +
                             "JOIN public.livre l ON p.idlivre = l.idlivre")) {

            while (rs.next()) {
                Prets pret = new Prets(
                        rs.getString("idpret"),
                        rs.getString("membre_nom"),
                        rs.getString("livre_designation"),
                        rs.getDate("datepret").toLocalDate(),
                        rs.getDate("dateretour") != null ? rs.getDate("dateretour").toLocalDate() : null,
                        rs.getDate("dateretour") != null
                );
                pret.setIdlivre(rs.getString("idlivre"));
                pretsList.add(pret);
            }
            TablePrets.setItems(pretsList);
        } catch (SQLException e) {
            showNotification("Erreur", "Erreur lors du chargement", NotificationType.ERROR);
        }
    }

    private void modifierPret(Prets pret) {
        IdPreter.setText(pret.getIdpret());
        RecupMembre.getSelectionModel().select(findMembreItem(pret.getMembre()));
        RecupLivre.getSelectionModel().select(findLivreItem(pret.getLivre()));
        DatePret.setValue(pret.getDatePret());
        DateRetour.setValue(pret.getDateRetour());
        pretEnEdition = pret;
        SavePret.setText("Modifier");
        chargerDonneesPrets();
    }

    private String findMembreItem(String membreInfo) {
        for (String item : RecupMembre.getItems()) {
            if (item.contains(membreInfo.split("\\(")[0].trim())) {
                return item;
            }
        }
        return null;
    }

    private String findLivreItem(String livreInfo) {
        for (String item : RecupLivre.getItems()) {
            if (item.contains(livreInfo.split("\\(")[0].trim())) {
                return item;
            }
        }
        return null;
    }

    private void supprimerPret(Prets pret) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le prêt");
        alert.setContentText("Êtes-vous sûr ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = new DatabaseConnection().getConnection()) {
                conn.setAutoCommit(false);

                try {
                    String deletePret = "DELETE FROM public.preter WHERE idpret = ?";
                    try (PreparedStatement ps = conn.prepareStatement(deletePret)) {
                        ps.setString(1, pret.getIdpret());
                        ps.executeUpdate();
                    }

                    if (pret.getDateRetour() == null) {
                        String updateLivre = "UPDATE public.livre SET exemplaire = exemplaire + 1 WHERE idlivre = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updateLivre)) {
                            ps.setString(1, pret.getIdlivre());
                            ps.executeUpdate();
                        }
                    }

                    conn.commit();
                    showNotification("Prêts", "Prêt supprimé", NotificationType.SUCCESS);

                    actualiserToutPrets();
                    chargerLivres();
                } catch (SQLException e) {
                    conn.rollback();
                    showNotification("Erreur", "Erreur lors de la suppression: " + e.getMessage(), NotificationType.ERROR);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                showNotification("Erreur", "Erreur de connexion", NotificationType.ERROR);
            }
        }
    }

    private void marquerCommeRetourne(Prets pret) {
        try (Connection conn = new DatabaseConnection().getConnection()) {
            conn.setAutoCommit(false);

            try {
                String updatePret = "UPDATE public.preter SET dateretour = ? WHERE idpret = ?";
                try (PreparedStatement ps = conn.prepareStatement(updatePret)) {
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setString(2, pret.getIdpret());
                    ps.executeUpdate();
                }

                String updateLivre = "UPDATE public.livre SET exemplaire = exemplaire + 1 WHERE idlivre = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateLivre)) {
                    ps.setString(1, pret.getIdlivre());
                    ps.executeUpdate();
                }

                conn.commit();
                actualiserToutPrets();
                showNotification("Prêts", "Livre retourné", NotificationType.SUCCESS);
                chargerLivres();
            } catch (SQLException e) {
                conn.rollback();
                showNotification("Erreur", "Erreur lors du retour", NotificationType.ERROR);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            showNotification("Erreur", "Erreur de connexion", NotificationType.ERROR);
        }
    }

    private String extraireId(String selection) {
        int start = selection.indexOf("(ID:") + 4;
        int end = selection.indexOf(")");
        return selection.substring(start, end);
    }

    @FXML
    private void handleCancel() {
        clearFields();
    }

    private void clearFields() {
        IdPreter.clear();
        RecupMembre.getSelectionModel().clearSelection();
        RecupLivre.getSelectionModel().clearSelection();
        DatePret.setValue(LocalDate.now());
        DateRetour.setValue(null);
        pretEnEdition = null;
        SavePret.setText("Enregistrer");
    }

    private void showNotification(String title, String message, NotificationType type) {
        TrayNotification tray = new TrayNotification();
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setNotificationType(type);
        tray.showAndDismiss(Duration.millis(3000));
    }

}