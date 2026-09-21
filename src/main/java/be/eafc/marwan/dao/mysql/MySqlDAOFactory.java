package be.eafc.marwan.dao.mysql;

import be.eafc.marwan.dao.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlDAOFactory extends AbstractDAOFactory {

    private static MySqlDAOFactory instance;

    private static final String URL = "jdbc:mysql://localhost:3306/centre_formations";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private MySqlDAOFactory(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e){
            throw new RuntimeException("Driver JDBC MySQL introuvable", e);
        }
    }

    public static MySqlDAOFactory getInstance(){
        if(instance == null){
            instance = new MySqlDAOFactory();
        }
        return instance;
    }

    // Une connexion par appel : Tomcat traite les requetes sur des threads
    // concurrents, or java.sql.Connection n'est pas thread-safe. Partager une
    // seule connexion (comme avant) corrompt les requetes sous charge et ne
    // se remet jamais d'une coupure reseau avec la BDD.
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @Override
    public UtilisateurDAO createUtilisateurDAO() {
        return MySqlUtilisateurDAO.getInstance(this);
    }

    @Override
    public FormationDAO createFormationDAO() {
        return new MySqlFormationDAO(this);
    }

    @Override
    public SessionDAO createSessionDAO() {
        return new MySqlSessionDAO(this);
    }

    @Override
    public PoleDAO createPoleDAO() {
        return new MySqlPoleDAO(this);
    }

    @Override
    public InscriptionDAO createInscriptionDAO() {
        return new MySqlInscriptionDAO(this);
    }
}
