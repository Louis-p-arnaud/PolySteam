package model;

import java.time.LocalDateTime;

public class RapportIncident {
    private String id;
    private String jeuId;        // ID du jeu concerné
    private String versionJeu;    // Version du jeu
    private String plateforme;    // Plateforme d'exécution (PC, PS5...)
    private String joueurPseudo;  // Joueur ayant rencontré l'incident
    private String descriptionErreur; // Description de l'erreur
    private LocalDateTime dateSurvenue;

    // Constructeur utilisé par le DAO (avec tous les champs de la BDD)
    public RapportIncident(String joueurPseudo, String jeuId, String versionJeu, String plateforme, String descriptionErreur) {
        this.id = generateId();
        this.joueurPseudo = joueurPseudo;
        this.jeuId = jeuId;
        this.versionJeu = versionJeu;
        this.plateforme = plateforme;
        this.descriptionErreur = descriptionErreur;
        this.dateSurvenue = LocalDateTime.now();
    }

    // Génère un ID unique basé sur le timestamp et un nombre aléatoire
    private String generateId() {
        return "i" + System.currentTimeMillis() + "-" + ((int)(Math.random() * 10000));
    }

    // Getters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJeuId() {
        return jeuId;
    }

    public String getVersionJeu() {
        return versionJeu;
    }

    public String getPlateforme() {
        return plateforme;
    }

    public String getJoueurPseudo() {
        return joueurPseudo;
    }

    public String getDescriptionErreur() {
        return descriptionErreur;
    }

    public LocalDateTime getDateSurvenue() {
        return dateSurvenue;
    }
}

