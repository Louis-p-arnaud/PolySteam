package model;

import java.util.List;
import java.util.UUID;

public class Jeu {
    private UUID id;
    private String nom;
    private Editeur editeur;
    private Enums.PLATEFORME_EXECUTION plateformeExecution;
    private List<Enums.GENRE> genres;
    private String numeroVersion;
    private boolean versionAnticipe;
    private double prixEditeur; // Ajouté selon la table

    public Jeu(String nom, Editeur editeur, Enums.PLATEFORME_EXECUTION plateformeExecution,
               List<Enums.GENRE> genres, String numeroVersion, boolean versionAnticipe, double prixEditeur) {
        this.id = UUID.randomUUID();
        this.nom = nom;
        this.editeur = editeur;
        this.plateformeExecution = plateformeExecution;
        this.genres = genres;
        this.numeroVersion = numeroVersion;
        this.versionAnticipe = versionAnticipe;
        this.prixEditeur = prixEditeur;
    }

    public Jeu(String nom, Enums.PLATEFORME_EXECUTION plateformeExecution,
               List<Enums.GENRE> genres, String numeroVersion, boolean versionAnticipe) {
        this.nom = nom;
        this.plateformeExecution = plateformeExecution;
        this.genres = genres;
        this.numeroVersion = numeroVersion;
        this.versionAnticipe = versionAnticipe;
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public UUID getId() {
        return id;
    }

    public Enums.PLATEFORME_EXECUTION getPlateformeExecution() {
        return plateformeExecution;
    }

    public List<Enums.GENRE> getGenres() {
        return genres;
    }

    public String getNumeroVersion() {
        return numeroVersion;
    }

    public boolean isVersionAnticipe() {
        return versionAnticipe;
    }

    public double getPrixEditeur() {
        return prixEditeur;
    }
}
