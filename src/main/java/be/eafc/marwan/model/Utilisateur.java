package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.UtilisateurDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class Utilisateur {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;
    private LocalDateTime dateCreation;

    public Utilisateur() {}

    public Utilisateur(int id, String nom, String prenom, String email, String motDePasse, String role, LocalDateTime dateCreation) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.dateCreation = dateCreation;
    }

    // !!
//    public List<Utilisateur> findAll() {
//        UtilisateurDAO dao = AbstractDAOFactory.getFactory().createUtilisateurDAO();
//        return dao.findAll();
//    }
//
//    public void insert() {
//        String mdpHache = BCrypt.hashpw(this.motDePasse, BCrypt.gensalt());
//        this.setMotDePasse(mdpHache);
//
//        UtilisateurDAO dao = AbstractDAOFactory.getFactory().createUtilisateurDAO();
//        dao.insert(this);
//    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean connecter() { // ZÉRO PARAMÈTRE
        UtilisateurDAO dao = AbstractDAOFactory.getFactory().createUtilisateurDAO();

        Utilisateur dbUser = dao.findByEmail(this.email);

        if (dbUser != null && BCrypt.checkpw(this.motDePasse, dbUser.getMotDePasse())) {
            this.id = dbUser.getId();
            this.nom = dbUser.getNom();
            this.prenom = dbUser.getPrenom();
            this.role = dbUser.getRole();
            this.dateCreation = dbUser.getDateCreation();
            return true;
        }

        return false;
    }

    public boolean enregistrer() {
        if (this.email == null || !this.email.contains("@")) {
            return false;
        }

        if (this.motDePasse == null || this.motDePasse.isBlank()) {
            return false;
        }

        if (this.role == null || this.role.isBlank()) {
            this.role = "ETUDIANT";
        }

        this.motDePasse = BCrypt.hashpw(this.motDePasse, BCrypt.gensalt());

        UtilisateurDAO dao = AbstractDAOFactory.getFactory().createUtilisateurDAO();
        return dao.insert(this);
    }

    public boolean verifMdp(String mdp){
        return BCrypt.checkpw(mdp, this.motDePasse);
    }

    public static Utilisateur authentifier(String email, String mdpSaisi) {
        UtilisateurDAO dao = AbstractDAOFactory.getFactory().createUtilisateurDAO();
        Utilisateur u = dao.findByEmail(email);

        if (u != null && u.verifMdp(mdpSaisi)) {
            return u;
        }

        return null;
    }

    public static List<Utilisateur> findAll() {
        return AbstractDAOFactory.getFactory()
                .createUtilisateurDAO()
                .findAll();
    }

    public static List<Utilisateur> findByRole(String role) {
        return AbstractDAOFactory.getFactory()
                .createUtilisateurDAO()
                .findByRole(role);
    }

    public static boolean modifierRole(int utilisateurId, String nouveauRole) {
        if (!"ETUDIANT".equals(nouveauRole)
                && !"ADMIN".equals(nouveauRole)
                && !"FORMATEUR".equals(nouveauRole)) {
            return false;
        }

        return AbstractDAOFactory.getFactory()
                .createUtilisateurDAO()
                .updateRole(utilisateurId, nouveauRole);
    }
}