package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class RapportIncident {
    private String idIncident;
    private String titreJeu;
    private String versionJeu; // Important pour que l'éditeur sache quoi corriger
    private String os;         // Plateforme d'exécution (PC, PS5...)
    private String pseudoJoueur; // Joueur ayant rencontré l'incident

    private String descriptionErreur; // Stacktrace ou code erreur
    private LocalDateTime dateSurvenue;

    public RapportIncident(String titreJeu, String versionJeu, String os, String pseudoJoueur, String descriptionErreur) {
        this.idIncident = UUID.randomUUID().toString();
        this.titreJeu = titreJeu;
        this.versionJeu = versionJeu;
        this.os = os;
        this.pseudoJoueur = pseudoJoueur;
        this.descriptionErreur = descriptionErreur;
        this.dateSurvenue = LocalDateTime.now();
    }

    public String getIdIncident() {
        return idIncident;
    }

    public String getTitreJeu() {
        return titreJeu;
    }

    public String getVersionJeu() {
        return versionJeu;
    }

    public String getOs() {
        return os;
    }

    public String getPseudoJoueur() {
        return pseudoJoueur;
    }

    public String getDescriptionErreur() {
        return descriptionErreur;
    }

    public LocalDateTime getDateSurvenue() {
        return dateSurvenue;
    }
}