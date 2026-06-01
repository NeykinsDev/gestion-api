package be.eafc.marwan.controller;

import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/sessions/*")
public class SessionServlet extends HttpServlet {

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

    private boolean isAdmin(HttpServletRequest req) {
        Utilisateur u = getUser(req);
        return u != null && "ADMIN".equals(u.getRole());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            // 1. RECHERCHE / FILTRAGE / LISTAGE (Accessible par tous)
            if (pathInfo != null && pathInfo.equals("/rechercher")) {
                // L'objet filtre encaisse le JSON (peut contenir un id ou un formationId)
                Session filtre = mapper.readValue(req.getInputStream(), Session.class);
                List<Session> list = filtre.rechercher();

                writeJson(res, mapper.writeValueAsString(list));
                return;
            }

            // --- SÉCURITÉ : TOUTES LES AUTRES ACTIONS DEMANDENT LE RÔLE ADMIN ---
            if (!isAdmin(req)) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
                return;
            }

            // 2. CRÉATION
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/creer")) {
                Session s = mapper.readValue(req.getInputStream(), Session.class);
                boolean ok = s.enregistrer();

                if (ok) {
                    writeJson(res, "{\"success\": true, \"message\": \"Session creee\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJson(res, "{\"success\": false, \"message\": \"Creation impossible\"}");
                }
                return;
            }

            // 3. MODIFICATION
            if (pathInfo.equals("/modifier")) {
                Session s = mapper.readValue(req.getInputStream(), Session.class);
                boolean ok = s.modifier();

                if (ok) {
                    writeJson(res, "{\"success\": true, \"message\": \"Session modifiee\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJson(res, "{\"success\": false, \"message\": \"Modification impossible\"}");
                }
                return;
            }

            // 4. SUPPRESSION
            if (pathInfo.equals("/supprimer")) {
                // OOP : On mappe le JSON {"id": X} directement dans l'instance
                Session s = mapper.readValue(req.getInputStream(), Session.class);
                boolean ok = s.supprimer();

                if (ok) {
                    writeJson(res, "{\"success\": true, \"message\": \"Session supprimee\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJson(res, "{\"success\": false, \"message\": \"Suppression impossible\"}");
                }
                return;
            }

            // Route inconnue
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeJson(res, "{\"success\": false, \"message\": \"URL introuvable\"}");

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Format JSON malforme ou erreur interne\"}");
        }
    }
}