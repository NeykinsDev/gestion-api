package be.eafc.marwan;

import be.eafc.marwan.DAO.AbstractDAOFactory;
import be.eafc.marwan.DAO.MySqlDAOFactory;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.ServletContextListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(jakarta.servlet.ServletContextEvent sce) {
        AbstractDAOFactory.setFactory(MySqlDAOFactory.getInstance());
        System.out.println("Factory initialisée");
    }
}