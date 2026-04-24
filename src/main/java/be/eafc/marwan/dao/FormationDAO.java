package be.eafc.marwan.dao;

import be.eafc.marwan.model.Formation;

import java.util.List;

public interface FormationDAO {

    List<Formation> findAll();
    Formation findById(int id);
    List<Formation> findByCritere(Double maxPrix, Integer maxDuree, Integer poleId);
    void insert(Formation f);
}
