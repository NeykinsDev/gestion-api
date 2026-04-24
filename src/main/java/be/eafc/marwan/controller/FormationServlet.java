package be.eafc.marwan.controller;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.FormationDAO;
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

@WebServlet("/formations")
public class FormationServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String prixParam = req.getParameter("maxPrix");
        String dureeParam = req.getParameter("maxDuree");
        String poleParam = req.getParameter("poleId");

        Double maxPrix = (prixParam != null && !prixParam.isEmpty()) ? Double.parseDouble(prixParam) : null;
        Integer maxDuree = (dureeParam != null && !dureeParam.isEmpty()) ? Integer.parseInt(dureeParam) : null;
        Integer poleId = (poleParam != null && !poleParam.isEmpty()) ? Integer.parseInt(poleParam) : null;

        List<Formation> list = Formation.findByCritere(maxPrix, maxDuree, poleId);

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(mapper.writeValueAsString(list));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Formation f = mapper.readValue(req.getInputStream(), Formation.class);

        f.insert();

        res.setContentType("application/json");
        res.getWriter().write("{\"success\": true, \"message\": \"Formation crees\"}");
    }
}
