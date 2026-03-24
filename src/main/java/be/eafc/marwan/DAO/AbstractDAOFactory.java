package be.eafc.marwan.DAO;

public abstract class AbstractDAOFactory {

    private static AbstractDAOFactory factory;

    public static AbstractDAOFactory getFactory() {
        return factory;
    }

    public static void setFactory(AbstractDAOFactory f) {
        factory = f;
    }

    public abstract UtilisateurDAO createUtilisateurDAO();
}
