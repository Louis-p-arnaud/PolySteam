package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Editeur {
    private UUID id;
    private String nom;
    private boolean estIndependant; // true si indépendant, false si entreprise
    private List<String> jeuxPublies; // Liste des titres de jeux publiés

    public Editeur(String nom, boolean estIndependant) {
        this.id = UUID.randomUUID();
        this.nom = nom;
        this.estIndependant = estIndependant;
        this.jeuxPublies = new ArrayList<>();
    }

    public UUID getId() {
        return id;
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

