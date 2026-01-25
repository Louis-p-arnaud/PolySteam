package model;

import java.util.List;

public class Patch {
    private Jeu jeu;
    private int idPatch;
    private String commentaireEditeur;
    private String nouvelleVersion;
    private List<String> modifications;

    public Patch(Jeu jeu, int idPatch, String commentaireEditeur,
                 String nouvelleVersion, List<String> modifications) {
        this.jeu = jeu;
        this.idPatch = idPatch;
        this.commentaireEditeur = commentaireEditeur;
        this.nouvelleVersion = nouvelleVersion;
        this.modifications = modifications;
    }

    @Override
    public String toString() {
        return "model.Patch{" +
                "jeu=" + jeu +
                ", idPatch=" + idPatch +
                ", commentaireEditeur='" + commentaireEditeur + '\'' +
                ", nouvelleVersion='" + nouvelleVersion + '\'' +
                ", modifications=" + modifications +
                '}';
    }

    // Getters
    public Jeu getJeu() {
        return jeu;
    }

    public int getIdPatch() {
        return idPatch;
    }

    public String getCommentaireEditeur() {
        return commentaireEditeur;
    }

    public String getNouvelleVersion() {
        return nouvelleVersion;
    }

    public List<String> getModifications() {
        return modifications;
    }
}
