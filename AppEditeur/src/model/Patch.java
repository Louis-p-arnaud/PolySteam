package model;

import java.util.List;
import java.util.UUID;

public class Patch {
    private UUID id;
    private String nomJeu;
    private String commentaireEditeur;
    private String nouvelleVersion;
    private List<String> modifications;

    public Patch(String nomJeu, String commentaireEditeur,
                 String nouvelleVersion, List<String> modifications) {
        this.id = UUID.randomUUID();
        this.nomJeu = nomJeu;
        this.commentaireEditeur = commentaireEditeur;
        this.nouvelleVersion = nouvelleVersion;
        this.modifications = modifications;
    }
    @Override
    public String toString() {
        return "model.Patch{" +
                "jeu=" + nomJeu +
                ", idPatch=" + id.toString() +
                ", commentaireEditeur='" + commentaireEditeur + '\'' +
                ", nouvelleVersion='" + nouvelleVersion + '\'' +
                ", modifications=" + modifications +
                '}';
    }

    // Getters

    public String getNomJeu() {return nomJeu;}

    public UUID getIdPatch() {
        return id;
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
