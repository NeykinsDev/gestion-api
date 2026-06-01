package be.eafc.marwan.controller;

import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/sessions")
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
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String idParam = req.getParameter("id");
        String formationParam = req.getParameter("formationId");

        if (idParam != null && !idParam.isBlank()) {
            Session s = Session.findById(Integer.parseInt(idParam));

            if (s == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJson(res, "{\"success\": false, \"message\": \"Session introuvable\"}");
                return;
            }

            writeJson(res, mapper.writeValueAsString(s));
            return;
        }

        List<Session> sessions;

        if (formationParam != null && !formationParam.isBlank()) {
            sessions = Session.findByFormation(Integer.parseInt(formationParam));
        } else {
            sessions = Session.findAll();
        }

        writeJson(res, mapper.writeValueAsString(sessions));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        Session s = mapper.readValue(req.getInputStream(), Session.class);
        boolean ok = s.enregistrer();

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Session creee\"}");
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

        Session s = mapper.readValue(req.getInputStream(), Session.class);
        boolean ok = s.modifier();

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Session modifiee\"}");
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
        boolean ok = Session.supprimer(id);

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Session supprimee\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Suppression impossible\"}");
        }
    }
}