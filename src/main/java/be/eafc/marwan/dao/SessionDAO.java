package be.eafc.marwan.dao;

import be.eafc.marwan.model.Session;

import java.util.List;

public interface SessionDAO {
    boolean insert(Session s);
    boolean update(Session s);
    boolean delete(int id);
    List<Session> findByCritere(Session session);
}
