package be.eafc.marwan.controller;

import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/formateur/*") // Écoute le sous-routage
public class FormateurServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private void writeJson(HttpServletResponse res, String json) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        Utilisateur user = (session != null) ? (Utilisateur) session.getAttribute("user") : null;

        if (user == null || !"FORMATEUR".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        String pathInfo = req.getPathInfo();

        // Approche OOP : On instancie un objet Session filtre et on lui injecte le formateur connecté
        Session filtre = new Session();
        filtre.setFormateur(user);

        // Routage REST par URL, sans aucun req.getParameter()
        if (pathInfo != null && pathInfo.equals("/historique")) {
            filtre.setTypeRecherche("HISTORIQUE");
        } else {
            filtre.setTypeRecherche("PLANNING");
        }

        List<Session> list = filtre.rechercher();
        writeJson(res, mapper.writeValueAsString(list));
    }
}