<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Ajouter un utilisateur</title></head>
<body>
<h2>Ajouter un utilisateur</h2>
<% if ("true".equals(request.getParameter("success"))) { %>
    <p style="color:green">Utilisateur sauvegarde !</p>
<% } %>
<form method="post" action="/WebAppTomcat/users">
    <label>Nom : <input type="text" name="lastName" required></label><br><br>
    <label>Prenom : <input type="text" name="firstName" required></label><br><br>
    <label>Email : <input type="email" name="email" required></label><br><br>
    <label>Mot de passe : <input type="password" name="password" required></label><br><br>
    <label>Telephone : <input type="text" name="phone"></label><br><br>
    <label>Role :
        <select name="role">
            <option value="client">Client</option>
            <option value="employee">Employe</option>
            <option value="boss">Boss</option>
        </select>
    </label><br><br>
    <button type="submit">Enregistrer</button>
</form>
</body>
</html>
