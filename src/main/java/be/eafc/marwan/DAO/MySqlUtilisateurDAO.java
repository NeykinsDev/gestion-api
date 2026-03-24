package be.eafc.marwan.DAO;

import be.eafc.marwan.model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlUtilisateurDAO implements UtilisateurDAO {

    private static MySqlUtilisateurDAO instance;
    private final Connection connection;

    private MySqlUtilisateurDAO(MySqlDAOFactory factory) {
        this.connection = factory.getConnection();
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
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM utilisateur");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Utilisateur findById(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM utilisateur WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public Utilisateur findByEmail(String email) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM utilisateur WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void insert(Utilisateur u) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getRole());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}