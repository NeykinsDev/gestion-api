package be.eafc.marwan.controller;

import be.eafc.marwan.model.Session;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/sessions")
public class SessionServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String idParam = req.getParameter("id");
        String formationParam = req.getParameter("formationId");

        if (idParam != null && !idParam.isBlank()) {
            Integer id = parseIntParam(req, "id");
            if (id == null) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Identifiant invalide"));
                return;
            }

            Session s = Session.findById(id);

            if (s == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJson(res, Map.of("success", false, "message", "Session introuvable"));
                return;
            }

            writeJson(res, s);
            return;
        }

        List<Session> sessions;

        if (formationParam != null && !formationParam.isBlank()) {
            Integer formationId = parseIntParam(req, "formationId");
            if (formationId == null) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Identifiant de formation invalide"));
                return;
            }
            sessions = Session.findByFormation(formationId);
        } else {
            sessions = Session.findAll();
        }

        writeJson(res, sessions);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, Map.of("success", false, "message", "Acces refuse"));
            return;
        }

        try {
            Session s = mapper.readValue(req.getInputStream(), Session.class);
            boolean ok = s.enregistrer();

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Session creee"));
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Creation impossible"));
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Donnees invalides"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, Map.of("success", false, "message", "Acces refuse"));
            return;
        }

        try {
            Session s = mapper.readValue(req.getInputStream(), Session.class);
            boolean ok = s.modifier();

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Session modifiee"));
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Modification impossible"));
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Donnees invalides"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, Map.of("success", false, "message", "Acces refuse"));
            return;
        }

        Integer id = parseIntParam(req, "id");
        boolean ok = id != null && Session.supprimer(id);

        if (ok) {
            writeJson(res, Map.of("success", true, "message", "Session supprimee"));
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Suppression impossible"));
        }
    }
}
