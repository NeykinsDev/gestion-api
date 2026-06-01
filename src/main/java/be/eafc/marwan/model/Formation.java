package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.FormationDAO;
import java.util.List;

public class Formation {

    private Integer id;
    private String titre;
    private String description;
    private Integer dureeHeure;
    private Double prix;
    private Pole pole;

    public Formation(){}

    public Formation(Integer id, Pole pole, String titre, String description, Integer dureeHeure, Double prix){
        this.id = id;
        this.pole = pole;
        this.titre = titre;
        this.description = description;
        this.dureeHeure = dureeHeure;
        this.prix = prix;
    }

    public List<Formation> rechercher(){
        FormationDAO dao = AbstractDAOFactory.getFactory().createFormationDAO();
        return dao.findByCritere(this);
    }

    public boolean enregistrer(){
        if (this.titre == null || this.titre.isEmpty() || this.prix == null || this.prix < 0) {
            return false;
        }
        FormationDAO dao = AbstractDAOFactory.getFactory().createFormationDAO();
        return dao.insert(this);
    }

    // --- GETTERS & SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDureeHeure() { return dureeHeure; }
    public void setDureeHeure(Integer dureeHeure) { this.dureeHeure = dureeHeure; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public Pole getPole() { return pole; }
    public void setPole(Pole pole) { this.pole = pole; }
}