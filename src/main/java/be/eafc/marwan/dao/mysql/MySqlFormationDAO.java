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
    public List<Formation> findByCritere(Formation f) {
        List<Formation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, p.nom as pole_nom FROM formation f " +
                        "JOIN pole p ON f.pole_id = p.id WHERE 1=1"
        );

        if (f.getPrix() != null) sql.append(" AND f.prix <= ?");
        if (f.getDureeHeure() != null) sql.append(" AND f.duree_heures <= ?");
        if (f.getPole() != null && f.getPole().getId() != 0) sql.append(" AND f.pole_id = ?");

        try(PreparedStatement ps = c.prepareStatement(sql.toString())){
            int paramIndex = 1;
            if (f.getPrix() != null) ps.setDouble(paramIndex++, f.getPrix());
            if (f.getDureeHeure() != null) ps.setInt(paramIndex++, f.getDureeHeure());
            if (f.getPole() != null && f.getPole().getId() != 0) ps.setInt(paramIndex++, f.getPole().getId());

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
    public boolean insert(Formation f) {
        String sql = "INSERT INTO formation (pole_id, titre, description, duree_heures, prix) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, f.getPole().getId());
            ps.setString(2, f.getTitre());
            ps.setString(3, f.getDescription());
            ps.setInt(4, f.getDureeHeure());
            ps.setDouble(5, f.getPrix());

            int rows = ps.executeUpdate();
            return rows > 0; // Sécurise le retour pour éviter le "faux success"
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}