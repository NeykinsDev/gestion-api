package be.eafc.marwan.dao.mysql;

import be.eafc.marwan.dao.PoleDAO;
import be.eafc.marwan.model.Pole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlPoleDAO implements PoleDAO {

    private final MySqlDAOFactory factory;

    public MySqlPoleDAO(MySqlDAOFactory factory) {
        this.factory = factory;
    }

    private Pole map(ResultSet rs) throws SQLException {
        return new Pole(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description")
        );
    }

    @Override
    public List<Pole> findAll() {
        List<Pole> list = new ArrayList<>();

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM pole ORDER BY nom");
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
    public Pole findById(int id) {
        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM pole WHERE id = ?")) {

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
    public boolean insert(Pole p) {
        String sql = "INSERT INTO pole (nom, description) VALUES (?, ?)";

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Pole p) {
        String sql = "UPDATE pole SET nom = ?, description = ? WHERE id = ?";

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setInt(3, p.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM pole WHERE id = ?";

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
