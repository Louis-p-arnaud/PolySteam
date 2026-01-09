package model;

import java.time.LocalDateTime;

public class Evaluation {
    private String pseudoJoueur; // Auteur de l'avis
    private String titreJeu;     // Jeu concerné

    // Contenu de l'évaluation
    private int note;            // Ex: sur 5 ou 10 ou 20
    private String commentaire;  // Texte de l'avis
    private LocalDateTime datePublication;

    // La "Méta-évaluation" (Jugement des autres joueurs sur cet avis)
    // C'est demandé explicitement ici :
    private int nombreVotesUtile;
    private int nombreVotesPasUtile;

    public Evaluation(String pseudoJoueur, String titreJeu, int note, String commentaire) {
        this.pseudoJoueur = pseudoJoueur;
        this.titreJeu = titreJeu;
        this.note = note;
        this.commentaire = commentaire;
        this.datePublication = LocalDateTime.now();
        this.nombreVotesUtile = 0;
        this.nombreVotesPasUtile = 0;
    }

    // Méthode pour incrémenter l'utilité (appelée suite à un event Kafka)
    public void voterUtile() {
        this.nombreVotesUtile++;
    }

}