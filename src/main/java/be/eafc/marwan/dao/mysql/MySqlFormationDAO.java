package be.eafc.marwan.dao.mysql;

import be.eafc.marwan.dao.FormationDAO;
import be.eafc.marwan.model.Formation;
import be.eafc.marwan.model.Pole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlFormationDAO implements FormationDAO {

    private final Connection c;

    public MySqlFormationDAO(MySqlDAOFactory factory){
        this.c = factory.getConnection();
    }

    private Formation map(ResultSet rs) throws SQLException {
        Pole p = new Pole(rs.getInt("pole_id"), rs.getString("pole_nom"), null);
        return new Formation(
                rs.getInt("id"),
                p,
                rs.getString("titre"),
                rs.getString("description"),
                rs.getInt("duree_heures"),
                rs.getDouble("prix")
        );
    }

    @Override
    public List<Formation> findAll() {
        return List.of();
    }

    @Override
    public Formation findById(int id) {
        return null;
    }

    @Override
    public List<Formation> findByCritere(Double maxPrix, Integer maxDuree, Integer poleId) {
        List<Formation> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT f.*, p.nom as pole_nom FROM formation f " +
                        "JOIN pole p ON f.pole_id = p.id WHERE 1=1"
        );

        if(maxPrix != null) sql.append(" AND f.prix <= ?");
        if(maxDuree != null) sql.append(" AND f.duree_heure <= ?");
        if(poleId != null) sql.append(" AND f.pole_id = ?");

        try(PreparedStatement ps = c.prepareStatement(sql.toString())){
            int paramIndex = 1;
            if(maxPrix != null) ps.setDouble(paramIndex++, maxPrix);
            if(maxDuree != null) ps.setInt(paramIndex++, maxDuree);
            if(poleId != null) ps.setInt(paramIndex++, poleId);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                list.add(map(rs));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void insert(Formation f) {

    }
}
