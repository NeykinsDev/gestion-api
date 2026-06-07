package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.InscriptionDAO;
import java.time.LocalDateTime;
import java.util.List;

public class Inscription {

    private Integer id; // Changé en Integer pour accepter la valeur null
    private Utilisateur etudiant;
    private Session session;
    private LocalDateTime dateInscription;
    private String statut;
    private String communicationStructuree;
    private boolean paiementSignale;
    private boolean paiementValide;

    public Inscription() {}

    public Inscription(Integer id, Utilisateur etudiant, Session session, LocalDateTime dateInscription, String statut,
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

    // --- LOGIQUE MÉTIER OOP (ZÉRO STATIC) ---

    public boolean enregistrer() {
        if (etudiant == null || etudiant.getId() == 0) return false;
        if (session == null || session.getId() == null || session.getId() == 0) return false;

        return AbstractDAOFactory.getFactory().createInscriptionDAO().insert(this);
    }

    /** * Recherche dynamique (Query by Example) : si l'étudiant est configuré dans l'objet,
     * renvoie son historique personnel. Sinon, renvoie l'intégralité du centre (Pour l'Admin). */
    public List<Inscription> rechercher() {
        InscriptionDAO dao = AbstractDAOFactory.getFactory().createInscriptionDAO();
        if (this.etudiant != null && this.etudiant.getId() != 0) {
            return dao.findByEtudiant(this.etudiant.getId());
        }
        return dao.findAll();
    }

    public boolean signalerPaiement() {
        if (this.id == null || this.id == 0 || this.etudiant == null || this.etudiant.getId() == 0) return false;
        return AbstractDAOFactory.getFactory().createInscriptionDAO().signalerPaiement(this.id, this.etudiant.getId());
    }

    public boolean validerPaiement() {
        if (this.id == null || this.id == 0) return false;
        return AbstractDAOFactory.getFactory().createInscriptionDAO().validerPaiement(this.id, this.paiementValide);
    }

    public boolean modifierStatut() {
        if (this.id == null || this.id == 0) return false;
        if (!"INSCRIT".equals(statut) && !"EN_COURS".equals(statut) && !"TERMINE".equals(statut) && !"ABANDONNE".equals(statut)) {
            return false;
        }
        return AbstractDAOFactory.getFactory().createInscriptionDAO().updateStatut(this.id, this.statut);
    }

    // --- GETTERS & SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Utilisateur getEtudiant() { return etudiant; }
    public void setEtudiant(Utilisateur etudiant) { this.etudiant = etudiant; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommunicationStructuree() { return communicationStructuree; }
    public void setCommunicationStructuree(String communicationStructuree) { this.communicationStructuree = communicationStructuree; }

    public boolean isPaiementSignale() { return paiementSignale; }
    public void setPaiementSignale(boolean paiementSignale) { this.paiementSignale = paiementSignale; }

    public boolean isPaiementValide() { return paiementValide; }
    public void setPaiementValide(boolean paiementValide) { this.paiementValide = paiementValide; }
}