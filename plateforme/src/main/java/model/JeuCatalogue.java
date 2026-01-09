package model;

import java.util.List;
import java.util.UUID;

public class JeuCatalogue {
    private UUID id;
    private String titre;
    private String editeur;

    private String versionActuelle;
    private List<String> historiqueCorrectifs; // Liste des versions/patchs reçus

    // Données commerciales & Sociales
    private double prixEditeur;
    private double prixActuel; // Calculé dynamiquement (qualité/demande)
    private List<Evaluation> evaluationsJoueurs;

    // Constructeur et Getters/Setters
    public JeuCatalogue(String titre, String editeur, double prixEditeur) {
        this.id = UUID.randomUUID();
        this.titre = titre;
        this.editeur = editeur;
        this.prixEditeur = prixEditeur;
        this.prixActuel = prixEditeur;
        this.versionActuelle = "1.0.0";
    }

    public UUID getId() {
        return id;
    }
    public String getTitre() {
        return titre;
    }
    public String getEditeur() {
        return editeur;
    }
    public String getVersionActuelle() {
        return versionActuelle;
    }
    public void setVersionActuelle(String versionActuelle) {
        this.versionActuelle = versionActuelle;
    }
    public double getPrixEditeur() {
        return prixEditeur;
    }
    public double getPrixActuel() {
        return prixActuel;
    }
    public void setPrixActuel(double prixActuel) {
        this.prixActuel = prixActuel;
    }

}