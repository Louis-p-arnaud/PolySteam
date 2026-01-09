package model;

import java.util.ArrayList;
import java.util.List;

public class Patch {
    private String idPatch;
    private String titreJeu;
    private String plateforme;
    private String nouvelleVersion;
    private String versionCiblee; // Version jusqu'à laquelle le patch s'applique
    private String commentaireEditeur;
    private List<Modification> modifications;

    public Patch(String idPatch, String titreJeu, String plateforme, String nouvelleVersion,
                 String versionCiblee, String commentaireEditeur) {
        this.idPatch = idPatch;
        this.titreJeu = titreJeu;
        this.plateforme = plateforme;
        this.nouvelleVersion = nouvelleVersion;
        this.versionCiblee = versionCiblee;
        this.commentaireEditeur = commentaireEditeur;
        this.modifications = new ArrayList<>();
    }

    public String getIdPatch() {
        return idPatch;
    }

    public String getTitreJeu() {
        return titreJeu;
    }

    public String getPlateforme() {
        return plateforme;
    }

    public String getNouvelleVersion() {
        return nouvelleVersion;
    }

    public String getVersionCiblee() {
        return versionCiblee;
    }

    public String getCommentaireEditeur() {
        return commentaireEditeur;
    }

    public List<Modification> getModifications() {
        return modifications;
    }

    public void ajouterModification(Modification modification) {
        this.modifications.add(modification);
    }

    // Classe interne pour représenter une modification
    public static class Modification {
        private TypeModification type;
        private String description;

        public Modification(TypeModification type, String description) {
            this.type = type;
            this.description = description;
        }

        public TypeModification getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TypeModification {
        CORRECTION,
        AJOUT,
        OPTIMISATION
    }
}

