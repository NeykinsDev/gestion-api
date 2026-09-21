package be.eafc.marwan.dao;

public abstract class AbstractDAOFactory {

    private static AbstractDAOFactory factory;

    public static AbstractDAOFactory getFactory() {
        return factory;
    }

    public static void setFactory(AbstractDAOFactory f) {
        factory = f;
    }

    public abstract UtilisateurDAO createUtilisateurDAO();

    public abstract FormationDAO createFormationDAO();

    public abstract SessionDAO createSessionDAO();

    public abstract PoleDAO createPoleDAO();

    public abstract InscriptionDAO createInscriptionDAO();
}
