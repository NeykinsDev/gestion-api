package be.eafc.marwan.controller;

import be.eafc.marwan.model.Inscription;
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
            // 1. RECHERCHE / HISTORIQUE CONSOLIDÉ (Query by Example)
            if (pathInfo != null && pathInfo.equals("/rechercher")) {
                Inscription query = new Inscription();
                // Sécurité : Si l'utilisateur connecté est un étudiant, il ne peut voir QUE son historique
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

            // 2. CRÉATION D'UNE INSCRIPTION A UNE SESSION
            if (pathInfo == null || pathInfo.equals("/")) {
                if (!"ETUDIANT".equals(user.getRole())) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    writeJson(res, "{\"success\": false, \"message\": \"Action reservee aux etudiants\"}");
                    return;
                }

                Inscription inscription = mapper.readValue(req.getInputStream(), Inscription.class);
                inscription.setEtudiant(user); // Injection forcée de l'étudiant de session

                if (inscription.enregistrer()) {
                    writeJson(res, "{\"success\": true, \"message\": \"Inscription reussie\"}");
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

    // 3. LOGIQUE ADMINISTRATIVE : MODIFICATION DU STATUT DU PARCOURS
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