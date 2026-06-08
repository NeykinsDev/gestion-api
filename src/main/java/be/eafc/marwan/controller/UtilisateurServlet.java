package be.eafc.marwan.controller;

import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/utilisateurs/*")
public class UtilisateurServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private void writeJson(HttpServletResponse res, String json) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);
    }

    private Utilisateur getConnectedUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object obj = session.getAttribute("user");
        if (obj instanceof Utilisateur) return (Utilisateur) obj;
        return null;
    }

    private boolean isAdmin(HttpServletRequest req) {
        Utilisateur u = getConnectedUser(req);
        return u != null && "ADMIN".equals(u.getRole());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            Utilisateur u = mapper.readValue(req.getInputStream(), Utilisateur.class);
            boolean cree = u.enregistrer();

            if (cree) {
                writeJson(res, "{\"success\": true, \"message\": \"Utilisateur cree avec succes\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, "{\"success\": false, \"message\": \"Email deja utilise ou donnees invalides\"}");
            }
            return;
        }

        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        if (pathInfo.equals("/rechercher")) {
            Utilisateur filtre = mapper.readValue(req.getInputStream(), Utilisateur.class);
            List<Utilisateur> list = filtre.rechercher();
            writeJson(res, mapper.writeValueAsString(list));
            return;
        }

        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        writeJson(res, "{\"success\": false, \"message\": \"URL introuvable\"}");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/modifier-role")) {
            Utilisateur u = mapper.readValue(req.getInputStream(), Utilisateur.class);
            boolean ok = u.modifierRole();

            if (ok) {
                writeJson(res, "{\"success\": true, \"message\": \"Role modifie avec succes\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, "{\"success\": false, \"message\": \"Modification impossible (role ou id invalide)\"}");
            }
            return;
        }

        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        writeJson(res, "{\"success\": false, \"message\": \"URL introuvable\"}");
    }
}