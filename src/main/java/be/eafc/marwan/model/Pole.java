package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import java.util.List;

public class Pole {

    private Integer id;
    private String nom;
    private String description;

    public Pole() {}

    public Pole(Integer id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }


    public List<Pole> rechercher() {
        return AbstractDAOFactory.getFactory().createPoleDAO().findAll();
    }

    public Pole trouverParId() {
        if (this.id == null || this.id == 0) return null;
        return AbstractDAOFactory.getFactory().createPoleDAO().findById(this.id);
    }

    public boolean enregistrer() {
        if (nom == null || nom.isBlank()) return false;
        return AbstractDAOFactory.getFactory().createPoleDAO().insert(this);
    }

    public boolean modifier() {
        if (id == null || id == 0) return false;
        if (nom == null || nom.isBlank()) return false;
        return AbstractDAOFactory.getFactory().createPoleDAO().update(this);
    }

    public boolean supprimer() {
        if (this.id == null || this.id <= 0) return false;
        return AbstractDAOFactory.getFactory().createPoleDAO().delete(this.id);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}