package be.eafc.marwan.dao.mysql;

import be.eafc.marwan.dao.FormationDAO;
import be.eafc.marwan.model.Formation;
import be.eafc.marwan.model.Pole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlFormationDAO implements FormationDAO {

    private final MySqlDAOFactory factory;

    public MySqlFormationDAO(MySqlDAOFactory factory) {
        this.factory = factory;
    }

    private Formation map(ResultSet rs) throws SQLException {
        Pole p = new Pole(
                rs.getInt("pole_id"),
                rs.getString("pole_nom"),
                rs.getString("pole_description")
        );

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
        List<Formation> list = new ArrayList<>();

        String sql = """
                SELECT f.*, p.nom AS pole_nom, p.description AS pole_description
                FROM formation f
                JOIN pole p ON f.pole_id = p.id
                ORDER BY f.titre
                """;

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Formation findById(int id) {
        String sql = """
                SELECT f.*, p.nom AS pole_nom, p.description AS pole_description
                FROM formation f
                JOIN pole p ON f.pole_id = p.id
                WHERE f.id = ?
                """;

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Formation> findByCritere(Double maxPrix, Integer maxDuree, Integer poleId, String modalite) {
        List<Formation> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT f.*, p.nom AS pole_nom, p.description AS pole_description
                FROM formation f
                JOIN pole p ON f.pole_id = p.id
                LEFT JOIN session s ON s.formation_id = f.id
                WHERE 1=1
                """);

        if (maxPrix != null) sql.append(" AND f.prix <= ?");
        if (maxDuree != null) sql.append(" AND f.duree_heures <= ?");
        if (poleId != null) sql.append(" AND f.pole_id = ?");
        if (modalite != null && !modalite.isBlank()) sql.append(" AND s.modalite = ?");

        sql.append(" ORDER BY f.titre");

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;

            if (maxPrix != null) ps.setDouble(i++, maxPrix);
            if (maxDuree != null) ps.setInt(i++, maxDuree);
            if (poleId != null) ps.setInt(i++, poleId);
            if (modalite != null && !modalite.isBlank()) ps.setString(i++, modalite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean insert(Formation f) {
        String sql = """
                INSERT INTO formation (pole_id, titre, description, duree_heures, prix)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, f.getPole().getId());
            ps.setString(2, f.getTitre());
            ps.setString(3, f.getDescription());
            ps.setInt(4, f.getDureeHeure());
            ps.setDouble(5, f.getPrix());

            return ps.executeUpdate() > 0;

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Formation f) {
        String sql = """
                UPDATE formation
                SET pole_id = ?, titre = ?, description = ?, duree_heures = ?, prix = ?
                WHERE id = ?
                """;

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, f.getPole().getId());
            ps.setString(2, f.getTitre());
            ps.setString(3, f.getDescription());
            ps.setInt(4, f.getDureeHeure());
            ps.setDouble(5, f.getPrix());
            ps.setInt(6, f.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM formation WHERE id = ?";

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
