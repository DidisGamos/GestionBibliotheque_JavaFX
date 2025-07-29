package com.biblio.gestionbiblio;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RapportsRetard {
    private final StringProperty membre;
    private final StringProperty livre;
    private final StringProperty dateLimite;
    private final IntegerProperty joursRetard;
    private final StringProperty contact;

    public RapportsRetard(String membre, String livre, String dateLimite, int joursRetard, String contact) {
        this.membre = new SimpleStringProperty(membre);
        this.livre = new SimpleStringProperty(livre);
        this.dateLimite = new SimpleStringProperty(dateLimite);
        this.joursRetard = new SimpleIntegerProperty(joursRetard);
        this.contact = new SimpleStringProperty(contact);
    }

    public String getMembre() {
        return (String)this.membre.get();
    }

    public String getLivre() {
        return (String)this.livre.get();
    }

    public String getDateLimite() {
        return (String)this.dateLimite.get();
    }

    public int getJoursRetard() {
        return this.joursRetard.get();
    }

    public String getContact() {
        return (String)this.contact.get();
    }

    public StringProperty membreProperty() {
        return this.membre;
    }

    public StringProperty livreProperty() {
        return this.livre;
    }

    public StringProperty dateLimiteProperty() {
        return this.dateLimite;
    }

    public IntegerProperty joursRetardProperty() {
        return this.joursRetard;
    }

    public StringProperty contactProperty() {
        return this.contact;
    }
}