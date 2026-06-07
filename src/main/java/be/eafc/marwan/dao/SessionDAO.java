package be.eafc.marwan.dao;

import be.eafc.marwan.model.Session;

import java.util.List;

public interface SessionDAO {
//    List<Session> findAll();
//    Session findById(int id);
//    List<Session> findByFormation(int formationId);
//    List<Session> findPlanningFormateur(int formateurId);
//    List<Session> findHistoriqueFormateur(int formateurId);
    boolean insert(Session s);
    boolean update(Session s);
    boolean delete(int id);
    List<Session> findByCritere(Session session);
}
