package be.eafc.marwan.dao.mysql;

import be.eafc.marwan.dao.UtilisateurDAO;
import be.eafc.marwan.model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlUtilisateurDAO implements UtilisateurDAO {

    private static MySqlUtilisateurDAO instance;
    private final MySqlDAOFactory factory;

    private MySqlUtilisateurDAO(MySqlDAOFactory factory) {
        this.factory = factory;
    }

    public static MySqlUtilisateurDAO getInstance(MySqlDAOFactory factory) {
        if (instance == null) {
            instance = new MySqlUtilisateurDAO(factory);
        }
        return instance;
    }

    private Utilisateur map(ResultSet rs) throws SQLException {
        return new Utilisateur(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("role"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null
        );
    }

    @Override
    public List<Utilisateur> findAll() {
        List<Utilisateur> list = new ArrayList<>();

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM utilisateur");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Utilisateur findById(int id) {
        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM utilisateur WHERE id = ?")) {

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
    public Utilisateur findByEmail(String email) {
        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM utilisateur WHERE email = ?")) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean insert(Utilisateur u) {
        if (this.findByEmail(u.getEmail()) != null) {
            return false;
        }

        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getRole());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Utilisateur> findByRole(String role) {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = ? ORDER BY nom, prenom";

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, role);

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
    public boolean updateRole(int utilisateurId, String role) {
        String sql = "UPDATE utilisateur SET role = ? WHERE id = ?";

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, role);
            ps.setInt(2, utilisateurId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
