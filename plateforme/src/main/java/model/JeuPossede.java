package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JeuPossede {
    private String jeuId; // Référence au jeu dans le catalogue
    private JeuCatalogue jeuCatalogue; // Référence complète au jeu
    private String titreJeu;
    private long tempsDeJeuEnMinutes;
    private String versionInstallee;
    private LocalDate dateAchat;
    private List<Extension> extensionsPossedees;

    public JeuPossede(String jeuId, String titreJeu, String versionInstallee) {
        this.jeuId = jeuId;
        this.titreJeu = titreJeu;
        this.versionInstallee = versionInstallee;
        this.tempsDeJeuEnMinutes = 0;
        this.dateAchat = LocalDate.now();
        this.extensionsPossedees = new ArrayList<>();
    }

    public void setJeuCatalogue(JeuCatalogue jeuCatalogue) {
        this.jeuCatalogue = jeuCatalogue;
    }

    public void ajouterTempsDeJeu(long minutes) {
        this.tempsDeJeuEnMinutes += minutes;
    }

    public String getJeuId() {
        return jeuId;
    }

    public JeuCatalogue getJeuCatalogue() {
        return jeuCatalogue;
    }

    public String getTitreJeu() {
        return titreJeu;
    }

    public long getTempsDeJeuEnMinutes() {
        return tempsDeJeuEnMinutes;
    }

    public long getTempsJeuHeures() {
        return tempsDeJeuEnMinutes / 60;
    }

    public String getVersionInstallee() {
        return versionInstallee;
    }

    public void setVersionInstallee(String versionInstallee) {
        this.versionInstallee = versionInstallee;
    }

    public LocalDate getDateAchat() {
        return dateAchat;
    }

    public List<Extension> getExtensionsPossedees() {
        return extensionsPossedees;
    }

    public void ajouterExtension(Extension extension) {
        this.extensionsPossedees.add(extension);
    }
}