package be.eafc.marwan.controller;

import be.eafc.marwan.model.Utilisateur;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

	protected final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	protected void writeJson(HttpServletResponse res, Object payload) throws IOException {
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		mapper.writeValue(res.getWriter(), payload);
	}

	protected Utilisateur getUser(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session == null)
			return null;

		Object obj = session.getAttribute("user");
		return obj instanceof Utilisateur ? (Utilisateur) obj : null;
	}

	protected boolean isAdmin(HttpServletRequest req) {
		Utilisateur u = getUser(req);
		return u != null && "ADMIN".equals(u.getRole());
	}

	protected Integer parseIntParam(HttpServletRequest req, String name) {
		String value = req.getParameter(name);
		if (value == null || value.isBlank())
			return null;

		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
