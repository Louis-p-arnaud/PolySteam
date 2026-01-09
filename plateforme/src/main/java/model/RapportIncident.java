package model;

import java.time.LocalDateTime;

public class RapportIncident {
    private String idIncident;
    private String titreJeu;
    private String versionJeu; // Important pour que l'éditeur sache quoi corriger
    private String os;         // Plateforme d'exécution (PC, PS5...)

    private String descriptionErreur; // Stacktrace ou code erreur
    private LocalDateTime dateSurvenue;
}