package be.eafc.marwan.controller;

import be.eafc.marwan.model.Pole;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/poles/*")
public class PoleServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    private void writeJson(HttpServletResponse res, String json) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Object obj = session.getAttribute("user");
        return obj instanceof Utilisateur && "ADMIN".equals(((Utilisateur) obj).getRole());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/rechercher")) {
            Pole filtre = mapper.readValue(req.getInputStream(), Pole.class);
            if (filtre.getId() != null && filtre.getId() > 0) {
                writeJson(res, mapper.writeValueAsString(filtre.trouverParId()));
            } else {
                writeJson(res, mapper.writeValueAsString(filtre.rechercher()));
            }
            return;
        }

        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        if (pathInfo == null || pathInfo.equals("/")) {
            Pole p = mapper.readValue(req.getInputStream(), Pole.class);
            if (p.enregistrer()) writeJson(res, "{\"success\": true, \"message\": \"Pole cree\"}");
            else { res.setStatus(HttpServletResponse.SC_BAD_REQUEST); writeJson(res, "{\"success\": false}"); }
            return;
        }

        if (pathInfo.equals("/modifier")) {
            Pole p = mapper.readValue(req.getInputStream(), Pole.class);
            if (p.modifier()) writeJson(res, "{\"success\": true, \"message\": \"Pole modifie\"}");
            else { res.setStatus(HttpServletResponse.SC_BAD_REQUEST); writeJson(res, "{\"success\": false}"); }
            return;
        }

        if (pathInfo.equals("/supprimer")) {
            Pole p = mapper.readValue(req.getInputStream(), Pole.class);
            if (p.supprimer()) writeJson(res, "{\"success\": true, \"message\": \"Pole supprime\"}");
            else { res.setStatus(HttpServletResponse.SC_BAD_REQUEST); writeJson(res, "{\"success\": false}"); }
            return;
        }
    }
}