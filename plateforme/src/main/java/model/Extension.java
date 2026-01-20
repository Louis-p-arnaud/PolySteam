package model;

import java.util.ArrayList;
import java.util.List;

public class Extension {
    private String id;
    private String titre;
    private double prix;

    // Règle métier importante du cahier des charges
    private String versionJeuBaseRequise;

    // Lien vers le jeu parent
    private String titreJeuParent;

    // Évaluations de l'extension
    private List<Evaluation> evaluations;

    // Constructeur sans ID (pour création depuis l'application)
    public Extension(String titre, double prix, String versionRequise, String jeuParent) {
        this.id = null; // Sera défini par la BDD
        this.titre = titre;
        this.prix = prix;
        this.versionJeuBaseRequise = versionRequise;
        this.titreJeuParent = jeuParent;
        this.evaluations = new ArrayList<>();
    }

    // Constructeur avec ID (pour chargement depuis la BDD)
    public Extension(String id, String titre, double prix, String versionRequise, String jeuParent) {
        this.id = id;
        this.titre = titre;
        this.prix = prix;
        this.versionJeuBaseRequise = versionRequise;
        this.titreJeuParent = jeuParent;
        this.evaluations = new ArrayList<>();
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

    public double getPrix() {
        return prix;
    }

    public String getVersionJeuBaseRequise() {
        return versionJeuBaseRequise;
    }

    public String getTitreJeuParent() {
        return titreJeuParent;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void ajouterEvaluation(Evaluation evaluation) {
        this.evaluations.add(evaluation);
    }
}