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

    public boolean creerNouvelleSession(Session sess){
        return sess.enregistrer();
    }
}
