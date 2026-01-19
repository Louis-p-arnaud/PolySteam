import java.util.ArrayList;
import java.util.List;

public class Editeur {
    private String nom;
    private Enums.TYPE_EDITEUR type;
    private List<Jeu> jeuxPublies;
    private List<Patch> patchesPublies;

    public Editeur(String nom, Enums.TYPE_EDITEUR type) {
        this.nom = nom;
        this.type = type;
        this.jeuxPublies = new ArrayList<>();
        this.patchesPublies = new ArrayList<>();
    }

    public void publierJeu(Jeu jeu) {
        jeuxPublies.add(jeu);
        System.out.println("L'éditeur " + nom + " publie le jeu " + jeu.getNom());
    }

    public void publierPatch(Patch patch) {
        patchesPublies.add(patch);
        System.out.println("L'éditeur " + nom +
                " publie le patch " + patch.getNouvelleVersion() + " pour le jeu " + patch.getJeu().getNom());
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public Enums.TYPE_EDITEUR getType() {
        return type;
    }

    public List<Jeu> getJeuxPublies() {
        return jeuxPublies;
    }

    public List<Patch> getPatchesPublies() {
        return patchesPublies;
    }
}
