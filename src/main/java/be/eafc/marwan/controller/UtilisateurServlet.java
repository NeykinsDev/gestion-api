package be.eafc.marwan.controller;

import be.eafc.marwan.model.Administrateur;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Object obj = session.getAttribute("user");
        return obj instanceof Utilisateur && "ADMIN".equals(((Utilisateur) obj).getRole());
    }

    // 1. RECHERCHE & FILTRAGE (100% JSON)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        // Si c'est l'inscription d'un nouvel étudiant, pas besoin d'être Admin
        if (pathInfo == null || pathInfo.equals("/")) {
            Utilisateur u = mapper.readValue(req.getInputStream(), Utilisateur.class);
            boolean cree = u.enregistrer();
            if (cree) {
                writeJson(res, "{\"success\": true, \"message\": \"Utilisateur cree\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJson(res, "{\"success\": false, \"message\": \"Email deja utilise ou donnees invalides\"}");
            }
            return;
        }

        // Sécurité Admin pour la recherche globale
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        if (pathInfo.equals("/rechercher")) {
            JsonNode node = mapper.readTree(req.getInputStream());
            String roleSaisi = node.has("role") ? node.get("role").asText() : "";

            Administrateur admin = new Administrateur();
            List<Utilisateur> list;

            if (roleSaisi != null && !roleSaisi.isBlank()) {
                list = admin.recupererUtilisateursParRole(roleSaisi);
            } else {
                list = admin.recupererTousUtilisateurs();
            }
            writeJson(res, mapper.writeValueAsString(list));
        }
    }

    // 2. MODIFICATION DE RÔLE (100% JSON)
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        JsonNode node = mapper.readTree(req.getInputStream());
        int utilisateurId = node.get("utilisateurId").asInt();
        String role = node.get("role").asText();

        Administrateur admin = new Administrateur();
        boolean ok = admin.changerRoleUtilisateur(utilisateurId, role);

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Role modifie\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Role invalide ou modification impossible\"}");
        }
    }
}