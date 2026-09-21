package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.UtilisateurDAO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;

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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @JsonIgnore
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean enregistrer() {
        if (this.email == null || !this.email.contains("@")) {
            return false;
        }

        if (this.motDePasse == null || this.motDePasse.isBlank()) {
            return false;
        }

        // enregistrer() est l'auto-inscription publique (UtilisateurServlet.doPost n'est
        // pas protege par isAdmin) : le role est toujours force a ETUDIANT, jamais pris
        // depuis le JSON du client, sinon n'importe qui pourrait se creer un compte ADMIN.
        this.role = "ETUDIANT";

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