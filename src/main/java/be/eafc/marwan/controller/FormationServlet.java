package be.eafc.marwan.controller;

import be.eafc.marwan.model.Formation;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/formations")
public class FormationServlet extends BaseServlet {

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

            Formation f = Formation.findById(id);

            if (f == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJson(res, Map.of("success", false, "message", "Formation introuvable"));
                return;
            }

            writeJson(res, f);
            return;
        }

        Double maxPrix;
        Integer maxDuree;
        Integer poleId;

        try {
            String prixParam = req.getParameter("maxPrix");
            String dureeParam = req.getParameter("maxDuree");

            maxPrix = prixParam != null && !prixParam.isBlank() ? Double.parseDouble(prixParam) : null;
            maxDuree = dureeParam != null && !dureeParam.isBlank() ? Integer.parseInt(dureeParam) : null;
            poleId = parseIntParam(req, "poleId");
        } catch (NumberFormatException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Parametres de recherche invalides"));
            return;
        }

        String modalite = req.getParameter("modalite");
        List<Formation> formations = Formation.findByCritere(maxPrix, maxDuree, poleId, modalite);

        writeJson(res, formations);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, Map.of("success", false, "message", "Acces refuse"));
            return;
        }

        try {
            Formation f = mapper.readValue(req.getInputStream(), Formation.class);
            boolean ok = f.enregistrer();

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Formation creee"));
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
            Formation f = mapper.readValue(req.getInputStream(), Formation.class);
            boolean ok = f.modifier();

            if (ok) {
                writeJson(res, Map.of("success", true, "message", "Formation modifiee"));
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
        boolean ok = id != null && Formation.supprimer(id);

        if (ok) {
            writeJson(res, Map.of("success", true, "message", "Formation supprimee"));
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Suppression impossible"));
        }
    }
}
