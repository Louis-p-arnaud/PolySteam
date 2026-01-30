package model;

import java.util.ArrayList;
import java.util.List;

public class JeuCatalogue {
    private String id;
    private String titre;
    private String editeur;
    private String plateforme; // PC, PS5, Xbox, etc.
    private List<String> genres; // simulation, stratégie, action, etc.

    private String versionActuelle;
    private boolean versionAnticipee; // true si version < 1.0
    private List<String> historiqueCorrectifs; // Liste des versions/patchs reçus

    // Données commerciales & Sociales
    private double prixEditeur;
    private double prixActuel; // Calculé dynamiquement (qualité/demande)
    private List<Evaluation> evaluationsJoueurs;

    // Constructeur et Getters/Setters
    public JeuCatalogue(String titre, String editeur, String plateforme, List<String> genres, double prixEditeur) {
        this.id = null; // Sera défini par la BDD
        this.titre = titre;
        this.editeur = editeur;
        this.plateforme = plateforme;
        this.genres = genres != null ? genres : new ArrayList<>();
        this.prixEditeur = prixEditeur;
        this.prixActuel = prixEditeur;
        this.versionActuelle = "1.0.0";
        this.versionAnticipee = false;
        this.historiqueCorrectifs = new ArrayList<>();
        this.evaluationsJoueurs = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public String getEditeur() {
        return editeur;
    }

    public String getNomEditeur() {
        return editeur;
    }

    public String getPlateforme() {
        return plateforme;
    }

    public List<String> getGenres() {
        return genres;
    }

    public String getVersionActuelle() {
        return versionActuelle;
    }

    public void setVersionActuelle(String versionActuelle) {
        this.versionActuelle = versionActuelle;
        // Vérifier si c'est une version anticipée (< 1.0)
        this.versionAnticipee = isVersionAnticipee(versionActuelle);
    }

    public boolean isVersionAnticipee() {
        return versionAnticipee;
    }

    public void setVersionAnticipee(boolean versionAnticipee) {
        this.versionAnticipee = versionAnticipee;
    }

    public List<String> getHistoriqueCorrectifs() {
        return historiqueCorrectifs;
    }

    public void ajouterCorrectif(String version) {
        this.historiqueCorrectifs.add(version);
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

    public List<Evaluation> getEvaluationsJoueurs() {
        return evaluationsJoueurs;
    }

    public void ajouterEvaluation(Evaluation evaluation) {
        this.evaluationsJoueurs.add(evaluation);
    }

    private boolean isVersionAnticipee(String version) {
        try {
            String[] parts = version.split("\\.");
            return Integer.parseInt(parts[0]) < 1;
        } catch (Exception e) {
            return false;
        }
    }
}