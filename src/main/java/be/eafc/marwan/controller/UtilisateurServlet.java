package be.eafc.marwan.controller;

import be.eafc.marwan.dao.AbstractDAOFactory;
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
        //List<Utilisateur> list = Utilisateur.findAll();
        var list = AbstractDAOFactory.getFactory().createUtilisateurDAO().findAll();

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(mapper.writeValueAsString(list));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Utilisateur u = mapper.readValue(req.getInputStream(), Utilisateur.class);
        boolean cree = u.enregistrer();

        res.setContentType("application/json");
        if(cree){
            res.getWriter().write("{\"success\": true}");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"success\": false, \"message\": \"Email deja utilise ou erreur technique\"}");
        }

//        res.setCharacterEncoding("UTF-8");
//        res.getWriter().write("{\"success\":true}");
    }
}