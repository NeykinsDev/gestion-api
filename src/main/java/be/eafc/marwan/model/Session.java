package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.SessionDAO;

import java.time.LocalDate;
import java.util.List;

public class Session {

    private Integer id;
    private Formation formation;
    private Utilisateur formateur;
    private LocalDate dateDebut;
    private String horaire;
    private String modalite;
    private int capaciteMax;
    private String typeRecherche;

    public Session() {}

    public Session(int id, Formation formation, Utilisateur formateur, LocalDate dateDebut, String horaire, String modalite, int capaciteMax) {
        this.id = id;
        this.formation = formation;
        this.formateur = formateur;
        this.dateDebut = dateDebut;
        this.horaire = horaire;
        this.modalite = modalite;
        this.capaciteMax = capaciteMax;
    }

    public boolean enregistrer() {
        if (formation == null || formation.getId() == 0) return false;
        if (dateDebut == null) return false;
        if (horaire == null || horaire.isBlank()) return false;
        if (!"EN_LIGNE".equals(modalite) && !"PRESENTIEL".equals(modalite)) return false;
        if (capaciteMax <= 0) return false;

        return AbstractDAOFactory.getFactory()
                .createSessionDAO()
                .insert(this);
    }

    public boolean modifier() {
        if (id == 0) return false;
        if (formation == null || formation.getId() == 0) return false;
        if (dateDebut == null) return false;
        if (horaire == null || horaire.isBlank()) return false;
        if (!"EN_LIGNE".equals(modalite) && !"PRESENTIEL".equals(modalite)) return false;
        if (capaciteMax <= 0) return false;

        return AbstractDAOFactory.getFactory()
                .createSessionDAO()
                .update(this);
    }

    public static boolean supprimer(int id) {
        if (id <= 0) return false;

        return AbstractDAOFactory.getFactory()
                .createSessionDAO()
                .delete(id);
    }

    public List<Session> rechercher() {
        SessionDAO dao = AbstractDAOFactory.getFactory().createSessionDAO();
        return dao.findByCritere(this);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Formation getFormation() {
        return formation;
    }
    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public Utilisateur getFormateur() {
        return formateur;
    }
    public void setFormateur(Utilisateur formateur) {
        this.formateur = formateur;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getHoraire() {
        return horaire;
    }
    public void setHoraire(String horaire) {
        this.horaire = horaire;
    }

    public String getModalite() {
        return modalite;
    }
    public void setModalite(String modalite) {
        this.modalite = modalite;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }
    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public String getTypeRecherche(){
        return typeRecherche;
    }
    public void setTypeRecherche(String typeRecherche){
        this.typeRecherche = typeRecherche;
    }
}