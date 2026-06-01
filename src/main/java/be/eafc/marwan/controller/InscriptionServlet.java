package be.eafc.marwan.controller;

import be.eafc.marwan.model.Inscription;
import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/inscriptions")
public class InscriptionServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private void writeJson(HttpServletResponse res, String json) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);
    }

    private Utilisateur getUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;

        Object obj = session.getAttribute("user");
        if (obj instanceof Utilisateur) return (Utilisateur) obj;

        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(res, "{\"success\": false, \"message\": \"Connexion requise\"}");
            return;
        }

        if ("ADMIN".equals(user.getRole())) {
            writeJson(res, mapper.writeValueAsString(Inscription.findAll()));
            return;
        }

        if ("ETUDIANT".equals(user.getRole())) {
            writeJson(res, mapper.writeValueAsString(Inscription.findByEtudiant(user.getId())));
            return;
        }

        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null || !"ETUDIANT".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        JsonNode node = mapper.readTree(req.getInputStream());
        int sessionId = node.get("sessionId").asInt();

        Session session = new Session();
        session.setId(sessionId);

        Inscription inscription = new Inscription();
        inscription.setEtudiant(user);
        inscription.setSession(session);

        boolean ok = inscription.enregistrer();

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Inscription creee\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Inscription impossible\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null || !"ADMIN".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        JsonNode node = mapper.readTree(req.getInputStream());

        int inscriptionId = node.get("inscriptionId").asInt();
        String statut = node.get("statut").asText();

        boolean ok = Inscription.modifierStatut(inscriptionId, statut);

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Statut modifie\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Statut invalide ou modification impossible\"}");
        }
    }
}