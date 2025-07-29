package com.biblio.gestionbiblio;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Rapports {
    private final StringProperty type;
    private final StringProperty livre;
    private final StringProperty datePret;
    private final StringProperty dateRetourPrevue;
    private final StringProperty dateRetourReelle;
    private final StringProperty statut;

    public Rapports(String type, String livre, String datePret,
                    String dateRetourPrevue, String dateRetourReelle,
                    String statut) {
        this.type = new SimpleStringProperty(type);
        this.livre = new SimpleStringProperty(livre);
        this.datePret = new SimpleStringProperty(datePret);
        this.dateRetourPrevue = new SimpleStringProperty(dateRetourPrevue);
        this.dateRetourReelle = new SimpleStringProperty(dateRetourReelle);
        this.statut = new SimpleStringProperty(statut);
    }

    public String getType() { return type.get(); }
    public String getLivre() { return livre.get(); }
    public String getDatePret() { return datePret.get(); }
    public String getDateRetourPrevue() { return dateRetourPrevue.get(); }
    public String getDateRetourReelle() { return dateRetourReelle.get(); }
    public String getStatut() { return statut.get(); }

    public StringProperty typeProperty() { return type; }
    public StringProperty livreProperty() { return livre; }
    public StringProperty datePretProperty() { return datePret; }
    public StringProperty dateRetourPrevueProperty() { return dateRetourPrevue; }
    public StringProperty dateRetourReelleProperty() { return dateRetourReelle; }
    public StringProperty statutProperty() { return statut; }
}