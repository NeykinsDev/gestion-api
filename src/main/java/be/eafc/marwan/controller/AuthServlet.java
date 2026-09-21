package be.eafc.marwan.controller;

import be.eafc.marwan.dao.AbstractDAOFactory;
import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/login")
public class AuthServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            JsonNode node = mapper.readTree(req.getInputStream());
            String email = node.get("email").asText();
            String mdpSaisi = node.get("motDePasse").asText();

            Utilisateur u = AbstractDAOFactory.getFactory().createUtilisateurDAO().findByEmail(email);

            if (u != null && u.verifMdp(mdpSaisi)) {
                HttpSession session = req.getSession();
                session.setAttribute("user", u);

                writeJson(res, Map.of("success", true, "role", u.getRole()));
            } else {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeJson(res, Map.of("success", false, "message", "Email ou mot de passe incorrect"));
            }

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(res, Map.of("success", false, "message", "Erreur lors de la connexion"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session != null && session.getAttribute("user") != null) {
            Utilisateur u = (Utilisateur) session.getAttribute("user");
            writeJson(res, Map.of("authenticated", true, "user", u.getPrenom()));
        } else {
            writeJson(res, Map.of("authenticated", false));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        writeJson(res, Map.of("success", true, "message", "Deconnecte"));
    }
}
