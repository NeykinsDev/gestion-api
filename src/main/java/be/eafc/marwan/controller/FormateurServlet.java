package be.eafc.marwan.controller;

import be.eafc.marwan.model.Session;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/formateur")
public class FormateurServlet extends HttpServlet {

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur user = getUser(req);

        if (user == null || !"FORMATEUR".equals(user.getRole())) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(res, "{\"success\": false, \"message\": \"Acces refuse\"}");
            return;
        }

        String action = req.getParameter("action");

        if ("historique".equals(action)) {
            writeJson(res, mapper.writeValueAsString(Session.findHistoriqueFormateur(user.getId())));
        } else {
            writeJson(res, mapper.writeValueAsString(Session.findPlanningFormateur(user.getId())));
        }
    }
}