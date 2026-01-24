import java.util.ArrayList;
import java.util.List;

import kafka.PublicationJeuEventProducer;
import kafka.PublicationPatchEventProducer;

public class Editeur {
    private String nom;
    private Enums.TYPE_EDITEUR type;
    private List<Jeu> jeuxPublies;
    private List<Patch> patchesPublies;

    // Producteur Kafka pour publier les événements de publication de jeu
    private static PublicationJeuEventProducer publicationProducer;
    // Producteur Kafka pour publier les événements de patch
    private static PublicationPatchEventProducer publicationPatchProducer;

    public Editeur(String nom, Enums.TYPE_EDITEUR type) {
        this.nom = nom;
        this.type = type;
        this.jeuxPublies = new ArrayList<>();
        this.patchesPublies = new ArrayList<>();
    }

    public void publierJeu(Jeu jeu) {
        jeuxPublies.add(jeu);
        System.out.println("L'éditeur " + nom + " publie le jeu " + jeu.getNom());

        // Envoi d'un événement Kafka décrivant la publication du jeu
        try {
            if (publicationProducer == null) {
                // Initialisation paresseuse avec configuration par défaut
                publicationProducer = new PublicationJeuEventProducer();
            }

            // Préparer les données à envoyer
            List<String> genresAsString = new ArrayList<>();
            if (jeu.getGenres() != null) {
                for (Enums.GENRE g : jeu.getGenres()) {
                    genresAsString.add(g.name());
                }
            }

            publicationProducer.publierPublication(
                    this.nom, // editeurId : ici on utilise le nom de l'éditeur
                    jeu.getNom(),
                    jeu.getPlateformeExecution() != null ? jeu.getPlateformeExecution().name() : "",
                    genresAsString,
                    jeu.getNumeroVersion(),
                    jeu.isVersionAnticipe()
            );

        } catch (Exception e) {
            System.err.println("Erreur lors de la publication Kafka du jeu " + jeu.getNom() + " : " + e.getMessage());
        }
    }

    public void publierPatch(Patch patch) {
        patchesPublies.add(patch);
        System.out.println("L'éditeur " + nom +
                " publie le patch " + patch.getNouvelleVersion() + " pour le jeu " + patch.getJeu().getNom());

        try {
            if (publicationPatchProducer == null) {
                publicationPatchProducer = new PublicationPatchEventProducer();
            }

            publicationPatchProducer.publierPublicationPatch(
                    this.nom, // editeurId
                    patch.getJeu().getNom(),
                    patch.getIdPatch(),
                    patch.getCommentaireEditeur(),
                    patch.getNouvelleVersion(),
                    patch.getModifications()
            );

        } catch (Exception e) {
            System.err.println("Erreur lors de la publication Kafka du patch pour " + patch.getJeu().getNom() + " : " + e.getMessage());
        }
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
