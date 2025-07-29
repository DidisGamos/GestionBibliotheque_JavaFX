package com.biblio.gestionbiblio;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Rendu {
    private final StringProperty idrendu;
    private final StringProperty membre;
    private final StringProperty livre;
    private final ObjectProperty<LocalDate> dateRendu;
//    private final BooleanProperty statutRendu;
    private String idlivre;
    private String idpers;

    public Rendu(String idrendu, String membre, String livre, LocalDate dateRendu) {
        this.idrendu = new SimpleStringProperty(idrendu);
        this.membre = new SimpleStringProperty(membre);
        this.livre = new SimpleStringProperty(livre);
        this.dateRendu = new SimpleObjectProperty<>(dateRendu);
//        this.statutRendu = new SimpleBooleanProperty(statutRendu);
    }
    public String getIdrendu() {return idrendu.get();}
    public String getMembre() {return membre.get();}
    public String getLivre() {return livre.get();}
    public LocalDate getDateRendu() {return dateRendu.get();}
//    public boolean getStatutRendu() {return statutRendu.get();}
    public String getIdlivre() {return idlivre;}
    public String getIdMembre(){return idpers;}

    public StringProperty idrenduProperty() {return idrendu;}
    public StringProperty membreProperty() {return membre;}
    public StringProperty livreProperty() {return livre;}
    public ObjectProperty<LocalDate> dateRenduProperty() {return dateRendu;}
//    public StringProperty statutProperty() {
//        return new SimpleStringProperty(statutRendu.get() ? "À temps" : "Retard");
//    }
    public void setIdlivre(String idlivre) {this.idlivre = idlivre;}
    public void setIdmembre(String idpers) {this.idpers = idpers;}
}
