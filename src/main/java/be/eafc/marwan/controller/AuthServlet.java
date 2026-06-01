package be.eafc.marwan.controller;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.dao.UtilisateurDAO;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jdk.jshell.execution.Util;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

@WebServlet("/login")
public class AuthServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        try {
            JsonNode node = mapper.readTree(req.getInputStream());
            String email = node.get("email").asText();
            String mdpSaisi = node.get("motDePasse").asText();

            Utilisateur u = AbstractDAOFactory.getFactory().createUtilisateurDAO().findByEmail(email);

            if (u != null && u.verifMdp(mdpSaisi)) {
                HttpSession session = req.getSession();
                session.setAttribute("user", u);

                res.getWriter().write("{\"success\": true, \"role\": \"" + u.getRole() + "\"}");
            } else {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"success\": false, \"message\": \"Email ou mot de passe incorrect\"}");
            }

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"success\": false, \"message\": \"Erreur lors de la connexion\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("user") != null) {
            Utilisateur u = (Utilisateur) session.getAttribute("user");
            res.getWriter().write("{\"authenticated\": true, \"user\": \"" + u.getPrenom() + "\"}");
        } else {
            res.getWriter().write("{\"authenticated\": false}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        res.getWriter().write("{\"success\": true, \"message\": \"Deconnecte\"}");
    }
}