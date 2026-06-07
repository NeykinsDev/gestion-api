package be.eafc.marwan.model;

import be.eafc.marwan.dao.AbstractDAOFactory;
import java.util.List;

public class Administrateur extends Utilisateur {

    public Administrateur(){
        super();
    }

    public List<Utilisateur> recupererTousUtilisateurs(){
        return AbstractDAOFactory.getFactory().createUtilisateurDAO().findAll();
    }

    public List<Utilisateur> recupererUtilisateursParRole(String role) {
        return AbstractDAOFactory.getFactory().createUtilisateurDAO().findByRole(role);
    }

    public boolean changerRoleUtilisateur(int cibleId, String nouveauRole) {
        if (!"ETUDIANT".equals(nouveauRole) && !"ADMIN".equals(nouveauRole) && !"FORMATEUR".equals(nouveauRole)) {
            return false;
        }
        return AbstractDAOFactory.getFactory().createUtilisateurDAO().updateRole(cibleId, nouveauRole);
    }

    public boolean creerNouvelleSession(Session sess){
        return sess.enregistrer();
    }
}