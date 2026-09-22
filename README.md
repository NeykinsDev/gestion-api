# Centre Formation API

API back-end sécurisée pour la gestion d'un centre de formation : catalogue de formations, sessions, inscriptions et suivi des paiements, avec trois niveaux d'accès (administrateur, formateur, étudiant).

## Stack technique

- **Java 21** / **Jakarta Servlets** (sans framework, déployé sur Apache Tomcat)
- **JDBC** (MySQL / MariaDB) avec le pattern **DAO** et une **Abstract Factory**
- **Jackson** pour la sérialisation JSON
- **jBCrypt** pour le hachage des mots de passe
- **Maven** pour le build

## Architecture

Le projet suit une architecture en couches classique :

```
controller/   Servlets HTTP (routage, authentification, sérialisation JSON)
model/        Entités métier (Utilisateur, Formation, Session, Inscription, Pole)
dao/          Interfaces DAO + implémentation MySQL (une connexion JDBC par appel)
```

- **MVC + DAO** : chaque entité expose un DAO (interface + implémentation MySQL) créé via `AbstractDAOFactory` / `MySqlDAOFactory`.
- **Authentification par session** : `AuthServlet` place l'utilisateur authentifié dans la session HTTP ; les autres servlets la relisent pour vérifier le rôle.
- **Rôles** : `ADMIN`, `FORMATEUR`, `ETUDIANT`. L'inscription publique (`POST /utilisateurs`) force toujours le rôle `ETUDIANT` ; seul un admin peut promouvoir un compte via `PUT /utilisateurs`.

## Modèle de données

| Table          | Description                                                                 |
|----------------|-------------------------------------------------------------------------------|
| `utilisateur`  | Comptes (admin, formateur, étudiant), mot de passe haché en BCrypt            |
| `pole`         | Domaines de formation (ex : Informatique, Langues, Management)                |
| `formation`    | Catalogue de formations (titre, durée, prix, pôle)                            |
| `session`      | Occurrences planifiées d'une formation (dates, horaire, modalité, formateur)   |
| `inscription`  | Inscription d'un étudiant à une session, avec suivi du paiement               |

Le script de création complet (schéma + jeu de données de démonstration) se trouve dans [`src/main/resources/sql`](src/main/resources/sql).

## API

Toutes les routes répondent en JSON. Sauf mention contraire, `id` se passe en query string (`?id=`).

| Route             | Méthodes             | Accès                              |
|--------------------|-----------------------|-------------------------------------|
| `/login`           | POST, GET, DELETE     | Public (connexion / statut / déconnexion) |
| `/utilisateurs`    | GET, POST, PUT        | GET/PUT : admin · POST : public (auto-inscription) |
| `/formations`      | GET, POST, PUT, DELETE| GET : public · écriture : admin     |
| `/poles`           | GET, POST, PUT, DELETE| GET : public · écriture : admin     |
| `/sessions`        | GET, POST, PUT, DELETE| GET : public · écriture : admin     |
| `/inscriptions`    | GET, POST, PUT        | étudiant (ses inscriptions) / admin (toutes) |
| `/paiements`       | POST, PUT              | POST : étudiant (signaler) · PUT : admin (valider) |
| `/formateur`       | GET                    | formateur (planning / historique)   |

## Lancer le projet en local

1. **Créer la base de données**
   ```bash
   mysql -u root -p < src/main/resources/sql
   ```
   Le script crée la base `centre_formations`, le schéma complet et des comptes de démonstration (mot de passe : `password123`).

2. **Configurer la connexion** dans `MySqlDAOFactory` (`URL`, `USER`, `PASSWORD`) si elle diffère de `jdbc:mysql://localhost:3306/centre_formations`.

## Comptes de démonstration

| Rôle       | Email                        | Mot de passe |
|------------|-------------------------------|---------------|
| Admin      | `admin@centre.be`             | `password123` |
| Formateur  | `jean.dupont@formateur.be`    | `password123` |
| Étudiant   | `lucas.peeters@student.be`    | `password123` |
