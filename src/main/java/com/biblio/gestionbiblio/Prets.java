package com.biblio.gestionbiblio;

import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Prets {
    private final StringProperty idpret;
    private final StringProperty membre;
    private final StringProperty livre;
    private final ObjectProperty<LocalDate> datePret;
    private final ObjectProperty<LocalDate> dateRetour;
    private final BooleanProperty rendu;
    private String idlivre;

    public Prets(String idpret, String membre, String livre, LocalDate datePret, LocalDate dateRetour, boolean rendu) {
        this.idpret = new SimpleStringProperty(idpret);
        this.membre = new SimpleStringProperty(membre);
        this.livre = new SimpleStringProperty(livre);
        this.datePret = new SimpleObjectProperty(datePret);
        this.dateRetour = new SimpleObjectProperty(dateRetour);
        this.rendu = new SimpleBooleanProperty(rendu);
    }

    public String getIdpret() {
        return (String)this.idpret.get();
    }

    public String getMembre() {
        return (String)this.membre.get();
    }

    public String getLivre() {
        return (String)this.livre.get();
    }

    public LocalDate getDatePret() {
        return (LocalDate)this.datePret.get();
    }

    public LocalDate getDateRetour() {
        return (LocalDate)this.dateRetour.get();
    }

    public boolean isRendu() {
        return this.rendu.get();
    }

    public String getIdlivre() {
        return this.idlivre;
    }

    public StringProperty idpretProperty() {
        return this.idpret;
    }

    public StringProperty membreProperty() {
        return this.membre;
    }

    public StringProperty livreProperty() {
        return this.livre;
    }

    public ObjectProperty<LocalDate> datePretProperty() {
        return this.datePret;
    }

    public ObjectProperty<LocalDate> dateRetourProperty() {
        return this.dateRetour;
    }

    public StringProperty statutProperty() {
        return new SimpleStringProperty(this.rendu.get() ? "Retourné" : "En cours");
    }

    public void setIdlivre(String idlivre) {
        this.idlivre = idlivre;
    }
}
