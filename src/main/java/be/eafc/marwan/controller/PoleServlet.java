package be.eafc.marwan.controller;

import be.eafc.marwan.model.Pole;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/poles")
public class PoleServlet extends HttpServlet {

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

    private boolean isAdmin(HttpServletRequest req) {
        Utilisateur u = getUser(req);
        return u != null && "ADMIN".equals(u.getRole());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String idParam = req.getParameter("id");

        if (idParam != null && !idParam.isBlank()) {
            Pole p = Pole.findById(Integer.parseInt(idParam));

            if (p == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJson(res, "{\"success\": false, \"message\": \"Pole introuvable\"}");
                return;
            }

            writeJson(res, mapper.writeValueAsString(p));
            return;
        }

        writeJson(res, mapper.writeValueAsString(Pole.findAll()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        Pole p = mapper.readValue(req.getInputStream(), Pole.class);
        boolean ok = p.enregistrer();

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Pole cree\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Creation impossible\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        Pole p = mapper.readValue(req.getInputStream(), Pole.class);
        boolean ok = p.modifier();

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Pole modifie\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Modification impossible\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        int id = Integer.parseInt(req.getParameter("id"));
        boolean ok = Pole.supprimer(id);

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Pole supprime\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Suppression impossible\"}");
        }
    }
}