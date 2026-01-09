package model;

import java.util.UUID;

public class Extension {
    private UUID id;
    private String titre;
    private double prix;

    // Règle métier importante du cahier des charges
    private String versionJeuBaseRequise;

    // Lien vers le jeu parent
    private String titreJeuParent;

    public Extension(String titre, double prix, String versionRequise, String jeuParent) {
        this.titre = titre;
        this.prix = prix;
        this.versionJeuBaseRequise = versionRequise;
        this.titreJeuParent = jeuParent;
    }
}