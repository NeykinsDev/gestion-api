package be.eafc.marwan.model;

public class Pole {

    private int id;
    private String nom;
    private String description;

    public Pole(){}

    public Pole(int id, String nom, String description){
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    public int getId() {
        return id;
    }
    public String getNom() {
        return nom;
    }
    public String getDescription() {
        return description;
    }


}
