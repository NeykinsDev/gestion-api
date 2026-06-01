package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.FormationDAO;

import java.util.List;

public class Formation {

    private int id;
    private String titre;
    private String description;
    private int dureeHeure;
    private double prix;
    private Pole pole;

    public Formation() {}

    public Formation(int id, Pole pole, String titre, String description, int dureeHeure, double prix) {
        this.id = id;
        this.pole = pole;
        this.titre = titre;
        this.description = description;
        this.dureeHeure = dureeHeure;
        this.prix = prix;
    }

    public boolean enregistrer() {
        if (titre == null || titre.isBlank()) return false;
        if (dureeHeure <= 0) return false;
        if (prix < 0) return false;
        if (pole == null || pole.getId() == 0) return false;

        return AbstractDAOFactory.getFactory()
                .createFormationDAO()
                .insert(this);
    }

    public boolean modifier() {
        if (id == 0) return false;
        if (titre == null || titre.isBlank()) return false;
        if (dureeHeure <= 0) return false;
        if (prix < 0) return false;
        if (pole == null || pole.getId() == 0) return false;

        return AbstractDAOFactory.getFactory()
                .createFormationDAO()
                .update(this);
    }

    public static boolean supprimer(int id) {
        if (id <= 0) return false;

        return AbstractDAOFactory.getFactory()
                .createFormationDAO()
                .delete(id);
    }

    public static List<Formation> findAll() {
        return AbstractDAOFactory.getFactory()
                .createFormationDAO()
                .findAll();
    }

    public static Formation findById(int id) {
        return AbstractDAOFactory.getFactory()
                .createFormationDAO()
                .findById(id);
    }

    public static List<Formation> findByCritere(Double maxPrix, Integer maxDuree, Integer poleId, String modalite) {
        FormationDAO dao = AbstractDAOFactory.getFactory().createFormationDAO();
        return dao.findByCritere(maxPrix, maxDuree, poleId, modalite);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public int getDureeHeure() {
        return dureeHeure;
    }

    public void setDureeHeure(int dureeHeure) {
        this.dureeHeure = dureeHeure;
    }


    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }


    public Pole getPole() {
        return pole;
    }

    public void setPole(Pole pole) {
        this.pole = pole;
    }
}