package be.eafc.marwan.controller;

import be.eafc.marwan.model.Inscription;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

@WebServlet("/paiements")
public class PaiementServlet extends BaseServlet {

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
            int inscriptionId = node.get("inscriptionId").asInt();

            boolean ok = Inscription.signalerPaiement(inscriptionId, user.getId());

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Paiement signale"));
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Signalement impossible"));
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
            boolean valide = node.get("valide").asBoolean();

            boolean ok = Inscription.validerPaiement(inscriptionId, valide);

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Paiement traite"));
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Validation impossible"));
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Donnees invalides"));
        }
    }
}
