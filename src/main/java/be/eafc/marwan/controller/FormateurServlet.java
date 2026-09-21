package be.eafc.marwan.controller;

import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

@WebServlet("/formateur")
public class FormateurServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null || !"FORMATEUR".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, Map.of("success", false, "message", "Acces refuse"));
            return;
        }

        String action = req.getParameter("action");

        if ("historique".equals(action)) {
            writeJson(res, Session.findHistoriqueFormateur(user.getId()));
        } else {
            writeJson(res, Session.findPlanningFormateur(user.getId()));
        }
    }
}
