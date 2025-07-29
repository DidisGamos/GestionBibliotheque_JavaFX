package com.biblio.gestionbiblio;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Livre {
    private final StringProperty id;
    private final StringProperty designation;
    private final StringProperty exemplaire;
    private final BooleanProperty disponible;

    public Livre(String id, String designation, String exemplaire, boolean disponible) {
        this.id = new SimpleStringProperty(id);
        this.designation = new SimpleStringProperty(designation);
        this.exemplaire = new SimpleStringProperty(exemplaire);
        this.disponible = new SimpleBooleanProperty(disponible);
    }

    public String getId() { return id.get(); }
    public String getDesignation() { return designation.get(); }
    public String getExemplaire() { return exemplaire.get(); }
    public boolean isDisponible() { return disponible.get(); }

    public StringProperty idProperty() { return id; }
    public StringProperty designationProperty() { return designation; }
    public StringProperty exemplaireProperty() { return exemplaire; }
    public BooleanProperty disponibleProperty() { return disponible; }

    public void setDisponible(boolean disponible) {
        this.disponible.set(disponible);
    }
}