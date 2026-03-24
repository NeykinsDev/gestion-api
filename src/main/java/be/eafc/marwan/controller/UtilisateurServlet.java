package be.eafc.marwan.controller;

import be.eafc.marwan.DAO.AbstractDAOFactory;
import be.eafc.marwan.DAO.UtilisateurDAO;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/utilisateurs")
public class UtilisateurServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        UtilisateurDAO dao = AbstractDAOFactory.getFactory().createUtilisateurDAO();
        List<Utilisateur> liste = dao.findAll();

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(mapper.writeValueAsString(liste));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur u = mapper.readValue(req.getInputStream(), Utilisateur.class);
        AbstractDAOFactory.getFactory().createUtilisateurDAO().insert(u);

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"success\":true}");
    }
}