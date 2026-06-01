package be.eafc.marwan.controller;

import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/utilisateurs")
public class UtilisateurServlet extends HttpServlet {

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
        if (!isAdmin(req)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        String role = req.getParameter("role");
        List<Utilisateur> utilisateurs;

        if (role != null && !role.isBlank()) {
            utilisateurs = Utilisateur.findByRole(role);
        } else {
            utilisateurs = Utilisateur.findAll();
        }

        writeJson(res, mapper.writeValueAsString(utilisateurs));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur u = mapper.readValue(req.getInputStream(), Utilisateur.class);

        boolean cree = u.enregistrer();

        if (cree) {
            writeJson(res, "{\"success\": true, \"message\": \"Utilisateur cree\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Email deja utilise ou donnees invalides\"}");
        }
    }

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

        boolean ok = Utilisateur.modifierRole(utilisateurId, role);

        if (ok) {
            writeJson(res, "{\"success\": true, \"message\": \"Role modifie\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, "{\"success\": false, \"message\": \"Role invalide ou modification impossible\"}");
        }
    }
}