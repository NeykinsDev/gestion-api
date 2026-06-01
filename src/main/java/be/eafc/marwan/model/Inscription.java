package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;

import java.time.LocalDateTime;
import java.util.List;

public class Inscription {

    private int id;
    private Utilisateur etudiant;
    private Session session;
    private LocalDateTime dateInscription;
    private String statut;
    private String communicationStructuree;
    private boolean paiementSignale;
    private boolean paiementValide;

    public Inscription() {}

    public Inscription(int id, Utilisateur etudiant, Session session, LocalDateTime dateInscription, String statut,
                       String communicationStructuree, boolean paiementSignale, boolean paiementValide) {
        this.id = id;
        this.etudiant = etudiant;
        this.session = session;
        this.dateInscription = dateInscription;
        this.statut = statut;
        this.communicationStructuree = communicationStructuree;
        this.paiementSignale = paiementSignale;
        this.paiementValide = paiementValide;
    }

    public boolean enregistrer() {
        if (etudiant == null || etudiant.getId() == 0) return false;
        if (session == null || session.getId() == 0) return false;

        return AbstractDAOFactory.getFactory()
                .createInscriptionDAO()
                .insert(this);
    }

    public static List<Inscription> findAll() {
        return AbstractDAOFactory.getFactory()
                .createInscriptionDAO()
                .findAll();
    }

    public static List<Inscription> findByEtudiant(int etudiantId) {
        return AbstractDAOFactory.getFactory()
                .createInscriptionDAO()
                .findByEtudiant(etudiantId);
    }

    public static boolean signalerPaiement(int inscriptionId, int etudiantId) {
        return AbstractDAOFactory.getFactory()
                .createInscriptionDAO()
                .signalerPaiement(inscriptionId, etudiantId);
    }

    public static boolean validerPaiement(int inscriptionId, boolean valide) {
        return AbstractDAOFactory.getFactory()
                .createInscriptionDAO()
                .validerPaiement(inscriptionId, valide);
    }

    public static boolean modifierStatut(int inscriptionId, String statut) {
        if (!"INSCRIT".equals(statut)
                && !"EN_COURS".equals(statut)
                && !"TERMINE".equals(statut)
                && !"ABANDONNE".equals(statut)) {
            return false;
        }

        return AbstractDAOFactory.getFactory()
                .createInscriptionDAO()
                .updateStatut(inscriptionId, statut);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Utilisateur getEtudiant() {
        return etudiant;
    }

    public void setEtudiant(Utilisateur etudiant) {
        this.etudiant = etudiant;
    }


    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }


    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }


    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }


    public String getCommunicationStructuree() {
        return communicationStructuree;
    }

    public void setCommunicationStructuree(String communicationStructuree) {
        this.communicationStructuree = communicationStructuree;
    }


    public boolean isPaiementSignale() {
        return paiementSignale;
    }

    public void setPaiementSignale(boolean paiementSignale) {
        this.paiementSignale = paiementSignale;
    }


    public boolean isPaiementValide() {
        return paiementValide;
    }

    public void setPaiementValide(boolean paiementValide) {
        this.paiementValide = paiementValide;
    }
}