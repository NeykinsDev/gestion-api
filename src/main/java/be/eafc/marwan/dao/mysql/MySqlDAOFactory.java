package be.eafc.marwan.dao.mysql;

import be.eafc.marwan.dao.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlDAOFactory extends AbstractDAOFactory {

    private static MySqlDAOFactory instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/centre_formations";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private MySqlDAOFactory(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException | ClassNotFoundException e){
            throw new RuntimeException("Connexion BDD impossible", e);
        }
    }

    public static MySqlDAOFactory getInstance(){
        if(instance == null){
            instance = new MySqlDAOFactory();
        }
        return instance;
    }

    public Connection getConnection(){
        return connection;
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
