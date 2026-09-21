package be.eafc.marwan.controller;

import be.eafc.marwan.model.Inscription;
import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

@WebServlet("/inscriptions")
public class InscriptionServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(res, Map.of("success", false, "message", "Connexion requise"));
            return;
        }

        if ("ADMIN".equals(user.getRole())) {
            writeJson(res, Inscription.findAll());
            return;
        }

        if ("ETUDIANT".equals(user.getRole())) {
            writeJson(res, Inscription.findByEtudiant(user.getId()));
            return;
        }

        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        writeJson(res, Map.of("success", false, "message", "Acces refuse"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null || !"ETUDIANT".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, Map.of("success", false, "message", "Acces refuse"));
            return;
        }

        try {
            JsonNode node = mapper.readTree(req.getInputStream());
            int sessionId = node.get("sessionId").asInt();

            Session session = new Session();
            session.setId(sessionId);

            Inscription inscription = new Inscription();
            inscription.setEtudiant(user);
            inscription.setSession(session);

            boolean ok = inscription.enregistrer();

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Inscription creee"));
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Inscription impossible"));
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Donnees invalides"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null || !"ADMIN".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, Map.of("success", false, "message", "Acces refuse"));
            return;
        }

        try {
            JsonNode node = mapper.readTree(req.getInputStream());

            int inscriptionId = node.get("inscriptionId").asInt();
            String statut = node.get("statut").asText();

            boolean ok = Inscription.modifierStatut(inscriptionId, statut);

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Statut modifie"));
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Statut invalide ou modification impossible"));
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Donnees invalides"));
        }
    }
}
