package be.eafc.marwan.controller;

import be.eafc.marwan.model.Formation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/formations/*")
public class FormationServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/rechercher")) {
                Formation filtre = mapper.readValue(req.getInputStream(), Formation.class);

                List<Formation> list = filtre.rechercher();
                res.getWriter().write(mapper.writeValueAsString(list));
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                Formation f = mapper.readValue(req.getInputStream(), Formation.class);

                boolean cree = f.enregistrer();

                if (cree) {
                    res.getWriter().write("{\"success\": true, \"message\": \"Formation creee avec succes\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.getWriter().write("{\"success\": false, \"message\": \"Erreur de creation (champs manquants ou invalides)\"}");
                }
                return;
            }

            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("{\"success\": false, \"message\": \"URL introuvable\"}");

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"success\": false, \"message\": \"Format JSON malforme\"}");
        }
    }
}