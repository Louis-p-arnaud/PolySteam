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
    private List<String> amis; // Liste des pseudos des amis

    public Joueur(String pseudo, String nom, String prenom, LocalDate dateNaissance) {
        this.pseudo = pseudo;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.dateInscription = LocalDate.now();
        this.bibliotheque = new ArrayList<>();
        this.mesEvaluations = new ArrayList<>();
        this.amis = new ArrayList<>();
    }

    // Getters
    public String getPseudo() {
        return pseudo;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateNaissance.getYear();
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public List<JeuPossede> getBibliotheque() {
        return bibliotheque;
    }

    public List<Evaluation> getMesEvaluations() {
        return mesEvaluations;
    }

    public List<String> getAmis() {
        return amis;
    }

    public boolean estAmiAvec(String pseudo) {
        return amis.contains(pseudo);
    }

    // Méthodes métier
    public void acheterJeu(JeuCatalogue jeu) {
        // logique d'ajout à la bibliothèque
        JeuPossede jeuPossede = new JeuPossede(jeu.getId(), jeu.getTitre(), jeu.getVersionActuelle());
        this.bibliotheque.add(jeuPossede);
    }

    public void ajouterEvaluation(Evaluation evaluation) {
        this.mesEvaluations.add(evaluation);
    }

    public void ajouterAmi(String pseudoAmi) {
        if (!amis.contains(pseudoAmi) && !pseudoAmi.equals(this.pseudo)) {
            amis.add(pseudoAmi);
        }
    }

    public void retirerAmi(String pseudoAmi) {
        amis.remove(pseudoAmi);
    }
}