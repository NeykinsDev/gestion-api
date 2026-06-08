package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.UtilisateurDAO;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.util.List;

public class Utilisateur {

    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;
    private LocalDateTime dateCreation;

    public Utilisateur() {}

    public Utilisateur(Integer id, String nom, String prenom, String email, String motDePasse, String role, LocalDateTime dateCreation) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.dateCreation = dateCreation;
    }

    public boolean connecter() {
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

    public List<Utilisateur> rechercher() {
        UtilisateurDAO dao = AbstractDAOFactory.getFactory().createUtilisateurDAO();
        if (this.role != null && !this.role.isBlank()) {
            return dao.findByRole(this.role);
        }
        return dao.findAll();
    }

    public boolean modifierRole() {
        if (this.id == null || this.id <= 0 || this.role == null || this.role.isBlank()) {
            return false;
        }
        UtilisateurDAO dao = AbstractDAOFactory.getFactory().createUtilisateurDAO();
        return dao.updateRole(this.id, this.role);
    }

    public boolean verifMdp(String mdp){
        return BCrypt.checkpw(mdp, this.motDePasse);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

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
}