package model;

public class JeuPossede {
    private String titreJeu;
    private long tempsDeJeuEnMinutes; //
    private String versionInstallee;

    public void ajouterTempsDeJeu(long minutes) {
        this.tempsDeJeuEnMinutes += minutes;
    }
}