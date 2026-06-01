package be.eafc.marwan.dao;

import be.eafc.marwan.model.Formation;

import java.text.Normalizer;
import java.util.List;

public interface FormationDAO {

    List<Formation> findAll();
    Formation findById(int id);
    List<Formation> findByCritere(Double maxPrix, Integer maxDuree, Integer poleId, String modalite);
    boolean insert(Formation f);
    boolean update(Formation f);
    boolean delete(int id);
}
