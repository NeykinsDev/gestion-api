package be.eafc.marwan.controller;

import be.eafc.marwan.model.Inscription;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/paiements")
public class PaiementServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

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
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null || !"ETUDIANT".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        JsonNode node = mapper.readTree(req.getInputStream());
        int inscriptionId = node.get("inscriptionId").asInt();

        boolean ok = Inscription.signalerPaiement(inscriptionId, user.getId());

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Paiement signale\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Signalement impossible\"}");
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
        boolean valide = node.get("valide").asBoolean();

        boolean ok = Inscription.validerPaiement(inscriptionId, valide);

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Paiement traite\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Validation impossible\"}");
        }
    }
}