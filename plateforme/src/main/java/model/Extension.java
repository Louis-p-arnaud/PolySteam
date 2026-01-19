package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Extension {
    private UUID id;
    private String titre;
    private double prix;

    // Règle métier importante du cahier des charges
    private String versionJeuBaseRequise;

    // Lien vers le jeu parent
    private String titreJeuParent;

    // Évaluations de l'extension
    private List<Evaluation> evaluations;

    public Extension(String titre, double prix, String versionRequise, String jeuParent) {
        this.id = UUID.randomUUID();
        this.titre = titre;
        this.prix = prix;
        this.versionJeuBaseRequise = versionRequise;
        this.titreJeuParent = jeuParent;
        this.evaluations = new ArrayList<>();
    }

    public UUID getId() {
        return id;
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