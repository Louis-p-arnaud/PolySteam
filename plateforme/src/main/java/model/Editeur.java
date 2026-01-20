package model;

import java.util.ArrayList;
import java.util.List;

public class Editeur {
    private String id;
    private String nom;
    private boolean estIndependant; // true si indépendant, false si entreprise
    private List<String> jeuxPublies; // Liste des titres de jeux publiés

    // Constructeur sans ID (pour création depuis l'application)
    public Editeur(String nom, boolean estIndependant) {
        this.id = null; // Sera défini par la BDD
        this.nom = nom;
        this.estIndependant = estIndependant;
        this.jeuxPublies = new ArrayList<>();
    }

    // Constructeur avec ID (pour chargement depuis la BDD)
    public Editeur(String id, String nom, boolean estIndependant) {
        this.id = id;
        this.nom = nom;
        this.estIndependant = estIndependant;
        this.jeuxPublies = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public boolean isEstIndependant() {
        return estIndependant;
    }

    public List<String> getJeuxPublies() {
        return jeuxPublies;
    }

    public void ajouterJeu(String titreJeu) {
        this.jeuxPublies.add(titreJeu);
    }
}

