package be.eafc.marwan.controller;

import be.eafc.marwan.model.Inscription;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/paiements")
public class PaiementServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

//    private void writeJson(HttpServletResponse res, String json) throws IOException {
//        res.setContentType("application/json");
//        res.setCharacterEncoding("UTF-8");
//        res.getWriter().write(json);
//    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        Utilisateur user = (session != null) ? (Utilisateur) session.getAttribute("user") : null;

        if (user == null || !"ETUDIANT".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write("{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        try {
            Inscription ins = mapper.readValue(req.getInputStream(), Inscription.class);
            ins.setEtudiant(user); // Protection : l'étudiant ne peut notifier que son propre paiement

            if (ins.signalerPaiement()) {
                res.getWriter().write("{\"success\": true, \"message\": \"Paiement signale avec succes\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("{\"success\": false, \"message\": \"Signalement impossible\"}");
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"success\": false}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        Utilisateur user = (session != null) ? (Utilisateur) session.getAttribute("user") : null;

        if (user == null || !"ADMIN".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write("{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        try {
            Inscription ins = mapper.readValue(req.getInputStream(), Inscription.class);
            if (ins.validerPaiement()) {
                res.getWriter().write("{\"success\": true, \"message\": \"Paiement enregistre et valide\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("{\"success\": false, \"message\": \"Validation impossible\"}");
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"success\": false}");
        }
    }
}