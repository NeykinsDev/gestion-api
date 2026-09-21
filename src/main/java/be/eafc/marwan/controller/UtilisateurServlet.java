package be.eafc.marwan.controller;

import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/utilisateurs")
public class UtilisateurServlet extends BaseServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (!isAdmin(req)) {
			res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			writeJson(res, Map.of("success", false, "message", "Acces refuse"));
			return;
		}

		String role = req.getParameter("role");
		List<Utilisateur> utilisateurs;

		if (role != null && !role.isBlank()) {
			utilisateurs = Utilisateur.findByRole(role);
		} else {
			utilisateurs = Utilisateur.findAll();
		}

		writeJson(res, utilisateurs);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		try {
			Utilisateur u = mapper.readValue(req.getInputStream(), Utilisateur.class);
			boolean cree = u.enregistrer();

			if (cree) {
				writeJson(res, Map.of("success", true, "message", "Utilisateur cree"));
			} else {
				res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				writeJson(res, Map.of("success", false, "message", "Email deja utilise ou donnees invalides"));
			}
		} catch (Exception e) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			writeJson(res, Map.of("success", false, "message", "Donnees invalides"));
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (!isAdmin(req)) {
			res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			writeJson(res, Map.of("success", false, "message", "Acces refuse"));
			return;
		}

		try {
			JsonNode node = mapper.readTree(req.getInputStream());

			int utilisateurId = node.get("utilisateurId").asInt();
			String role = node.get("role").asText();

			boolean ok = Utilisateur.modifierRole(utilisateurId, role);

			if (ok) {
				writeJson(res, Map.of("success", true, "message", "Role modifie"));
			} else {
				res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				writeJson(res, Map.of("success", false, "message", "Role invalide ou modification impossible"));
			}
		} catch (Exception e) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			writeJson(res, Map.of("success", false, "message", "Donnees invalides"));
		}
	}
}
