package be.eafc.marwan.dao;

import be.eafc.marwan.model.Inscription;

import java.util.List;

public interface InscriptionDAO {
    boolean insert(Inscription inscription);
    List<Inscription> findAll();
    List<Inscription> findByEtudiant(int etudiantId);
    boolean signalerPaiement(int inscriptionId, int etudiantId);
    boolean validerPaiement(int inscriptionId, boolean valide);
    boolean updateStatut(int inscriptionId, String statut);
}
