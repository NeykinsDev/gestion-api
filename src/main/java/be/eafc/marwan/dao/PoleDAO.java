package be.eafc.marwan.dao;

import be.eafc.marwan.model.Pole;

import java.util.List;

public interface PoleDAO {
    List<Pole> findAll();
    Pole findById(int id);
    boolean insert(Pole p);
    boolean update(Pole p);
    boolean delete(int id);
}