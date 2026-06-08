package be.eafc.marwan.controller;

import be.eafc.marwan.model.Inscription;
import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/inscriptions/*")
public class InscriptionServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private void writeJson(HttpServletResponse res, String json) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);
    }

    private Utilisateur getConnectedUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (Utilisateur) session.getAttribute("user") : null;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();
        Utilisateur user = getConnectedUser(req);

        if (user == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(res, "{\"success\": false, \"message\": \"Connexion requise\"}");
            return;
        }

        try {
            if (pathInfo != null && pathInfo.equals("/rechercher")) {
                Inscription query = new Inscription();
                if ("ETUDIANT".equals(user.getRole())) {
                    query.setEtudiant(user);
                } else if (!"ADMIN".equals(user.getRole())) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
                    return;
                }

                List<Inscription> list = query.rechercher();
                writeJson(res, mapper.writeValueAsString(list));
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                if (!"ETUDIANT".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    writeJson(res, "{\"success\": false, \"message\": \"Action non autorisée\"}");
                    return;
                }

                Inscription inscription = mapper.readValue(req.getInputStream(), Inscription.class);

                if ("ETUDIANT".equals(user.getRole())) {
                    inscription.setEtudiant(user);
                }

                if (inscription.enregistrer()) {
                    Session s = inscription.getSession().rechercher().stream()
                            .filter(sess -> sess.getId() == inscription.getSession().getId())
                            .findFirst().orElse(null);

                    double montant = (s != null) ? s.getFormation().getPrix() : 0.0;
                    String comm = inscription.getCommunicationStructuree(); // Générée par le DAO

                    writeJson(res, "{"
                            + "\"success\": true,"
                            + "\"message\": \"Inscription réussie\","
                            + "\"montant\": " + montant + ","
                            + "\"iban\": \"BE96 3630 1234 5678\","
                            + "\"communication\": \"" + comm + "\""
                            + "}");
                    return;
                } else {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJson(res, "{\"success\": false, \"message\": \"Capacite maximale atteinte ou session invalide\"}");
                }
                return;
            }

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Payload JSON malforme\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getConnectedUser(req);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        try {
            Inscription ins = mapper.readValue(req.getInputStream(), Inscription.class);
            if (ins.modifierStatut()) {
                writeJson(res, "{\"success\": true, \"message\": \"Statut du parcours etudiant mis a jour\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, "{\"success\": false, \"message\": \"Statut incorrect\"}");
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false}");
        }
    }
}