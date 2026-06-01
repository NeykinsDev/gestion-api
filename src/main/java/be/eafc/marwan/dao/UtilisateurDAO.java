package be.eafc.marwan.dao;

import be.eafc.marwan.model.Utilisateur;
import java.util.List;

public interface UtilisateurDAO {
    List<Utilisateur> findAll();
    List<Utilisateur> findByRole(String role);
    Utilisateur findById(int id);
    Utilisateur findByEmail(String email);
    boolean insert(Utilisateur u);
    boolean updateRole(int utilisateurId, String role);
}