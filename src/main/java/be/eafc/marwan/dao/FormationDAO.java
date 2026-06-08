package be.eafc.marwan.dao;

import be.eafc.marwan.model.Formation;
import java.util.List;

public interface FormationDAO {
    List<Formation> findByCritere(Formation f);
    boolean insert(Formation f);
}