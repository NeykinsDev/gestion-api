package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;

import java.util.List;

public class Pole {

    private int id;
    private String nom;
    private String description;

    public Pole() {}

    public Pole(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    public boolean enregistrer() {
        if (nom == null || nom.isBlank()) return false;

        return AbstractDAOFactory.getFactory()
                .createPoleDAO()
                .insert(this);
    }

    public boolean modifier() {
        if (id == 0) return false;
        if (nom == null || nom.isBlank()) return false;

        return AbstractDAOFactory.getFactory()
                .createPoleDAO()
                .update(this);
    }

    public static boolean supprimer(int id) {
        if (id <= 0) return false;

        return AbstractDAOFactory.getFactory()
                .createPoleDAO()
                .delete(id);
    }

    public static List<Pole> findAll() {
        return AbstractDAOFactory.getFactory()
                .createPoleDAO()
                .findAll();
    }

    public static Pole findById(int id) {
        return AbstractDAOFactory.getFactory()
                .createPoleDAO()
                .findById(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}