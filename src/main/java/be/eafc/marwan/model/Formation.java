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

    public Formation(){}

    public Formation(int id, Pole pole, String titre, String description, int dureeHeure, double prix){
        this.id = id;
        this.pole = pole;
        this.titre = titre;
        this.description = description;
        this.dureeHeure = dureeHeure;
        this.prix = prix;
    }

    public int getId() {
        return id;
    }
    public String getTitre() {
        return titre;
    }
    public String getDescription() {
        return description;
    }
    public int getDureeHeure() {
        return dureeHeure;
    }
    public double getPrix() {
        return prix;
    }
    public Pole getPole() {
        return pole;
    }

    public static List<Formation> findByCritere(Double maxPrix, Integer maxDuree, Integer poleId){
        FormationDAO dao = AbstractDAOFactory.getFactory().createFormationDAO();
        return dao.findByCritere(maxPrix, maxDuree, poleId);
    }

    public void insert(){
        FormationDAO dao = AbstractDAOFactory.getFactory().createFormationDAO();
        dao.insert(this);
    }
}
