package be.eafc.marwan.controller;

import be.eafc.marwan.model.Pole;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

@WebServlet("/poles")
public class PoleServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String idParam = req.getParameter("id");

        if (idParam != null && !idParam.isBlank()) {
            Integer id = parseIntParam(req, "id");
            if (id == null) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, Map.of("success", false, "message", "Identifiant invalide"));
                return;
            }

            Pole p = Pole.findById(id);

            if (p == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJson(res, Map.of("success", false, "message", "Pole introuvable"));
                return;
            }

            writeJson(res, p);
            return;
        }

        writeJson(res, Pole.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, Map.of("success", false, "message", "Acces refuse"));
            return;
        }

        try {
            Pole p = mapper.readValue(req.getInputStream(), Pole.class);
            boolean ok = p.enregistrer();

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Pole cree"));
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
            Pole p = mapper.readValue(req.getInputStream(), Pole.class);
            boolean ok = p.modifier();

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Pole modifie"));
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
        boolean ok = id != null && Pole.supprimer(id);

        if (ok) {
            writeJson(res, Map.of("success", true, "message", "Pole supprime"));
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Suppression impossible"));
        }
    }
}
