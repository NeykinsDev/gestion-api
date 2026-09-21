package be.eafc.marwan.dao.mysql;

import be.eafc.marwan.dao.SessionDAO;
import be.eafc.marwan.model.Formation;
import be.eafc.marwan.model.Pole;
import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlSessionDAO implements SessionDAO {

    private final MySqlDAOFactory factory;

    public MySqlSessionDAO(MySqlDAOFactory factory) {
        this.factory = factory;
    }

    private Session map(ResultSet rs) throws SQLException {
        Pole pole = new Pole(
                rs.getInt("pole_id"),
                rs.getString("pole_nom"),
                null
        );

        Formation formation = new Formation(
                rs.getInt("formation_id"),
                pole,
                rs.getString("formation_titre"),
                rs.getString("formation_description"),
                rs.getInt("duree_heures"),
                rs.getDouble("prix")
        );

        Utilisateur formateur = null;

        int formateurId = rs.getInt("formateur_id");
        if (!rs.wasNull()) {
            formateur = new Utilisateur();
            formateur.setId(formateurId);
            formateur.setNom(rs.getString("formateur_nom"));
            formateur.setPrenom(rs.getString("formateur_prenom"));
            formateur.setEmail(rs.getString("formateur_email"));
            formateur.setRole("FORMATEUR");
        }

        return new Session(
                rs.getInt("id"),
                formation,
                formateur,
                rs.getDate("date_debut").toLocalDate(),
                rs.getString("horaire"),
                rs.getString("modalite"),
                rs.getInt("capacite_max")
        );
    }

    private String baseSql() {
        return """
                SELECT
                    s.*,
                    f.id AS formation_id,
                    f.titre AS formation_titre,
                    f.description AS formation_description,
                    f.duree_heures,
                    f.prix,
                    p.id AS pole_id,
                    p.nom AS pole_nom,
                    u.nom AS formateur_nom,
                    u.prenom AS formateur_prenom,
                    u.email AS formateur_email
                FROM session s
                JOIN formation f ON s.formation_id = f.id
                JOIN pole p ON f.pole_id = p.id
                LEFT JOIN utilisateur u ON s.formateur_id = u.id
                """;
    }

    @Override
    public List<Session> findAll() {
        List<Session> list = new ArrayList<>();
        String sql = baseSql() + " ORDER BY s.date_debut";

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Session findById(int id) {
        String sql = baseSql() + " WHERE s.id = ?";

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
    public List<Session> findByFormation(int formationId) {
        List<Session> list = new ArrayList<>();
        String sql = baseSql() + " WHERE s.formation_id = ? ORDER BY s.date_debut";

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, formationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<Session> findPlanningFormateur(int formateurId) {
        List<Session> list = new ArrayList<>();
        String sql = baseSql() + """
                WHERE s.formateur_id = ?
                AND s.date_debut >= CURDATE()
                ORDER BY s.date_debut
                """;

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, formateurId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<Session> findHistoriqueFormateur(int formateurId) {
        List<Session> list = new ArrayList<>();
        String sql = baseSql() + """
                WHERE s.formateur_id = ?
                AND s.date_debut < CURDATE()
                ORDER BY s.date_debut DESC
                """;

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, formateurId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean insert(Session s) {
        String sql = """
                INSERT INTO session (formation_id, formateur_id, date_debut, horaire, modalite, capacite_max)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, s.getFormation().getId());

            if (s.getFormateur() != null && s.getFormateur().getId() != 0) {
                ps.setInt(2, s.getFormateur().getId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setDate(3, Date.valueOf(s.getDateDebut()));
            ps.setString(4, s.getHoraire());
            ps.setString(5, s.getModalite());
            ps.setInt(6, s.getCapaciteMax());

            return ps.executeUpdate() > 0;

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Session s) {
        String sql = """
                UPDATE session
                SET formation_id = ?, formateur_id = ?, date_debut = ?, horaire = ?, modalite = ?, capacite_max = ?
                WHERE id = ?
                """;

        try (Connection c = factory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, s.getFormation().getId());

            if (s.getFormateur() != null && s.getFormateur().getId() != 0) {
                ps.setInt(2, s.getFormateur().getId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setDate(3, Date.valueOf(s.getDateDebut()));
            ps.setString(4, s.getHoraire());
            ps.setString(5, s.getModalite());
            ps.setInt(6, s.getCapaciteMax());
            ps.setInt(7, s.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM session WHERE id = ?";

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
