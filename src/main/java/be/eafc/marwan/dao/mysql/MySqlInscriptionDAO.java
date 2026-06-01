package be.eafc.marwan.dao.mysql;

import be.eafc.marwan.dao.InscriptionDAO;
import be.eafc.marwan.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySqlInscriptionDAO implements InscriptionDAO {

    private final Connection c;

    public MySqlInscriptionDAO(MySqlDAOFactory factory) {
        this.c = factory.getConnection();
    }

    private String genererCommunication(int etudiantId, int sessionId) {
        int a = etudiantId % 1000;
        int b = sessionId % 1000;
        int c = (int) (System.currentTimeMillis() % 10000);

        return String.format("+++%03d/%03d/%04d+++", a, b, c);
    }

    private boolean sessionEstComplete(int sessionId) throws SQLException {
        String sql = """
                SELECT s.capacite_max, COUNT(i.id) AS nb
                FROM session s
                LEFT JOIN inscription i ON i.session_id = s.id AND i.statut <> 'ABANDONNE'
                WHERE s.id = ?
                GROUP BY s.id, s.capacite_max
                """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nb") >= rs.getInt("capacite_max");
                }
            }
        }

        return true;
    }

    private Inscription map(ResultSet rs) throws SQLException {
        Utilisateur etudiant = new Utilisateur();
        etudiant.setId(rs.getInt("etudiant_id"));
        etudiant.setNom(rs.getString("etudiant_nom"));
        etudiant.setPrenom(rs.getString("etudiant_prenom"));
        etudiant.setEmail(rs.getString("etudiant_email"));
        etudiant.setRole("ETUDIANT");

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

        Session session = new Session();
        session.setId(rs.getInt("session_id"));
        session.setFormation(formation);
        session.setDateDebut(rs.getDate("date_debut").toLocalDate());
        session.setHoraire(rs.getString("horaire"));
        session.setModalite(rs.getString("modalite"));
        session.setCapaciteMax(rs.getInt("capacite_max"));

        Timestamp ts = rs.getTimestamp("date_inscription");
        LocalDateTime dateInscription = ts != null ? ts.toLocalDateTime() : null;

        return new Inscription(
                rs.getInt("id"),
                etudiant,
                session,
                dateInscription,
                rs.getString("statut"),
                rs.getString("communication_structuree"),
                rs.getBoolean("paiement_signale"),
                rs.getBoolean("paiement_valide")
        );
    }

    private String baseSql() {
        return """
                SELECT
                    i.*,
                    u.id AS etudiant_id,
                    u.nom AS etudiant_nom,
                    u.prenom AS etudiant_prenom,
                    u.email AS etudiant_email,
                    s.id AS session_id,
                    s.date_debut,
                    s.horaire,
                    s.modalite,
                    s.capacite_max,
                    f.id AS formation_id,
                    f.titre AS formation_titre,
                    f.description AS formation_description,
                    f.duree_heures,
                    f.prix,
                    p.id AS pole_id,
                    p.nom AS pole_nom
                FROM inscription i
                JOIN utilisateur u ON i.etudiant_id = u.id
                JOIN session s ON i.session_id = s.id
                JOIN formation f ON s.formation_id = f.id
                JOIN pole p ON f.pole_id = p.id
                """;
    }

    @Override
    public boolean insert(Inscription inscription) {
        String sql = """
                INSERT INTO inscription
                (etudiant_id, session_id, statut, communication_structuree, paiement_signale, paiement_valide)
                VALUES (?, ?, 'INSCRIT', ?, false, false)
                """;

        try {
            int etudiantId = inscription.getEtudiant().getId();
            int sessionId = inscription.getSession().getId();

            if (sessionEstComplete(sessionId)) {
                return false;
            }

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, etudiantId);
                ps.setInt(2, sessionId);
                ps.setString(3, genererCommunication(etudiantId, sessionId));

                return ps.executeUpdate() > 0;
            }

        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Inscription> findAll() {
        List<Inscription> list = new ArrayList<>();
        String sql = baseSql() + " ORDER BY i.date_inscription DESC";

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<Inscription> findByEtudiant(int etudiantId) {
        List<Inscription> list = new ArrayList<>();
        String sql = baseSql() + " WHERE i.etudiant_id = ? ORDER BY i.date_inscription DESC";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, etudiantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean signalerPaiement(int inscriptionId, int etudiantId) {
        String sql = """
                UPDATE inscription
                SET paiement_signale = true
                WHERE id = ? AND etudiant_id = ?
                """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, inscriptionId);
            ps.setInt(2, etudiantId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean validerPaiement(int inscriptionId, boolean valide) {
        String sql = """
                UPDATE inscription
                SET paiement_valide = ?, statut = ?
                WHERE id = ?
                """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, valide);
            ps.setString(2, valide ? "EN_COURS" : "INSCRIT");
            ps.setInt(3, inscriptionId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStatut(int inscriptionId, String statut) {
        String sql = "UPDATE inscription SET statut = ? WHERE id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, inscriptionId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}