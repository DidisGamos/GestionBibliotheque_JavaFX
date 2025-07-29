package com.biblio.gestionbiblio;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Membre {
    private final StringProperty id;
    private final StringProperty nom;
    private final StringProperty sexe;
    private final IntegerProperty age;
    private final StringProperty contact;

    public Membre(String id, String nom, String sexe, int age, String contact) {
        this.id = new SimpleStringProperty(id);
        this.nom = new SimpleStringProperty(nom);
        this.sexe = new SimpleStringProperty(sexe);
        this.age = new SimpleIntegerProperty(age);
        this.contact = new SimpleStringProperty(contact);
    }

    public String getId() { return id.get(); }
    public String getNom() { return nom.get(); }
    public String getSexe() { return sexe.get(); }
    public int getAge() { return age.get(); }
    public String getContact() { return contact.get(); }

    public StringProperty idProperty() { return id; }
    public StringProperty nomProperty() { return nom; }
    public StringProperty sexeProperty() { return sexe; }
    public IntegerProperty ageProperty() { return age; }
    public StringProperty contactProperty() { return contact; }
}