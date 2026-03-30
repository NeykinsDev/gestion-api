package be.eafc.marwan.dao;

import be.eafc.marwan.model.Utilisateur;
import java.util.List;

public interface UtilisateurDAO {
    List<Utilisateur> findAll();
    Utilisateur findById(int id);
    Utilisateur findByEmail(String email);
    void insert(Utilisateur u);
}