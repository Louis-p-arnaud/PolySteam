package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Joueur {
    // Informations RGPD & Identification
    private String pseudo; // Doit être unique
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;

    // Données d'activité
    private LocalDate dateInscription;
    private List<JeuPossede> bibliotheque; // Liste de jeux
    private List<Evaluation> mesEvaluations;

    public Joueur(String pseudo, String nom, String prenom, LocalDate dateNaissance) {
        this.pseudo = pseudo;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.dateInscription = LocalDate.now();
        this.bibliotheque = new ArrayList<>();
    }

    // Méthodes métier
    public void acheterJeu(JeuCatalogue jeu) {
        // logique d'ajout à la bibliothèque
    }
}