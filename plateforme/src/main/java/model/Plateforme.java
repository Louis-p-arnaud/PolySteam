package model;

import service.CatalogueService;
import service.PricingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe centrale représentant la plateforme de jeux vidéo
 * Gère le catalogue, les joueurs, les éditeurs, etc.
 */
public class Plateforme {
    private String nom;
    private Map<String, Joueur> joueurs; // key = pseudo
    private Map<String, Editeur> editeurs; // key = id
    private Map<String, JeuCatalogue> catalogue; // key = id jeu
    private List<RapportIncident> rapportsIncidents;
    private List<Patch> patches;
    private Map<String, List<Extension>> extensions; // key = id jeu parent

    private CatalogueService catalogueService;
    private PricingService pricingService;

    public Plateforme(String nom) {
        this.nom = nom;
        this.joueurs = new HashMap<>();
        this.editeurs = new HashMap<>();
        this.catalogue = new HashMap<>();
        this.rapportsIncidents = new ArrayList<>();
        this.patches = new ArrayList<>();
        this.extensions = new HashMap<>();
        this.catalogueService = new CatalogueService();
        this.pricingService = new PricingService();
    }

    // === GESTION DES JOUEURS ===

    public void inscrireJoueur(Joueur joueur) {
        if (joueurs.containsKey(joueur.getPseudo())) {
            System.out.println("❌ Le pseudo '" + joueur.getPseudo() + "' est déjà pris.");
        } else {
            joueurs.put(joueur.getPseudo(), joueur);
            // Message désactivé pour éviter le spam lors du chargement depuis la BDD
            // System.out.println("✅ Joueur inscrit : " + joueur.getPseudo());
        }
    }

    public Joueur getJoueur(String pseudo) {
        return joueurs.get(pseudo);
    }

    /**
     * Ajoute une relation d'amitié entre deux joueurs (bidirectionnelle)
     */
    public void ajouterAmi(String pseudo1, String pseudo2) {
        Joueur joueur1 = joueurs.get(pseudo1);
        Joueur joueur2 = joueurs.get(pseudo2);

        if (joueur1 == null || joueur2 == null) {
            System.out.println("❌ Un des joueurs n'existe pas.");
            return;
        }

        if (pseudo1.equals(pseudo2)) {
            System.out.println("❌ Un joueur ne peut pas être ami avec lui-même.");
            return;
        }

        joueur1.ajouterAmi(pseudo2);
        joueur2.ajouterAmi(pseudo1);
        System.out.println("✅ " + pseudo1 + " et " + pseudo2 + " sont maintenant amis.");
    }

    /**
     * Retire une relation d'amitié entre deux joueurs
     */
    public void retirerAmi(String pseudo1, String pseudo2) {
        Joueur joueur1 = joueurs.get(pseudo1);
        Joueur joueur2 = joueurs.get(pseudo2);

        if (joueur1 != null && joueur2 != null) {
            joueur1.retirerAmi(pseudo2);
            joueur2.retirerAmi(pseudo1);
            System.out.println("✅ " + pseudo1 + " et " + pseudo2 + " ne sont plus amis.");
        }
    }

    /**
     * Affiche le profil d'un joueur (version sans visiteur - affichage complet)
     */
    public void afficherProfilJoueur(String pseudo) {
        afficherProfilJoueur(pseudo, null);
    }

    /**
     * Affiche le profil d'un joueur avec affichage adapté selon la relation
     * @param pseudo Le pseudo du joueur dont on veut voir le profil
     * @param pseudoVisiteur Le pseudo du joueur qui consulte le profil (null = vue publique)
     */
    public void afficherProfilJoueur(String pseudo, String pseudoVisiteur) {
        Joueur joueur = joueurs.get(pseudo);
        if (joueur == null) {
            System.out.println("❌ Joueur introuvable.");
            return;
        }

        // Déterminer la relation
        boolean estProprietaire = pseudo.equals(pseudoVisiteur);
        boolean estAmi = false;
        if (pseudoVisiteur != null && !estProprietaire) {
            Joueur visiteur = joueurs.get(pseudoVisiteur);
            estAmi = visiteur != null && visiteur.estAmiAvec(pseudo);
        }

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("📋 PROFIL DE " + joueur.getPseudo().toUpperCase());
        if (estProprietaire) {
            System.out.println("(Votre profil)");
        } else if (estAmi) {
            System.out.println("👥 (Ami)");
        }
        System.out.println("═══════════════════════════════════════");

        // Informations de base (toujours visibles)
        if (estProprietaire || estAmi) {
            System.out.println("Nom: " + joueur.getNom() + " " + joueur.getPrenom());
            System.out.println("Date de naissance: " + joueur.getDateNaissance());
        }
        System.out.println("Inscrit depuis: " + joueur.getDateInscription());

        // Amis (visible seulement par le propriétaire et les amis)
        if (estProprietaire || estAmi) {
            System.out.println("\n👥 Amis (" + joueur.getAmis().size() + ") :");
            if (joueur.getAmis().isEmpty()) {
                System.out.println("  Aucun ami pour le moment.");
            } else {
                for (String ami : joueur.getAmis()) {
                    System.out.println("  • " + ami);
                }
            }
        }

        // Bibliothèque (détails variables selon la relation)
        System.out.println("\n📚 Bibliothèque (" + joueur.getBibliotheque().size() + " jeux) :");
        for (JeuPossede jeu : joueur.getBibliotheque()) {
            long heures = jeu.getTempsDeJeuEnMinutes() / 60;
            long minutes = jeu.getTempsDeJeuEnMinutes() % 60;

            if (estProprietaire) {
                // Vue complète pour le propriétaire
                System.out.println("  • " + jeu.getTitreJeu() + " (v" + jeu.getVersionInstallee() + ") - "
                    + heures + "h" + minutes + "min jouées");
            } else if (estAmi) {
                // Vue partielle pour les amis (avec temps de jeu)
                System.out.println("  • " + jeu.getTitreJeu() + " - " + heures + "h" + minutes + "min");
            } else {
                // Vue minimale pour les autres (juste le titre)
                System.out.println("  • " + jeu.getTitreJeu());
            }
        }

        // Évaluations (toujours visibles)
        System.out.println("\n⭐ Évaluations postées (" + joueur.getMesEvaluations().size() + ") :");
        for (Evaluation eval : joueur.getMesEvaluations()) {
            System.out.println("  • " + eval.getTitreJeu() + " : " + eval.getNote() + "/10");
            if (estProprietaire || estAmi || eval.getCommentaire() != null) {
                System.out.println("    \"" + eval.getCommentaire() + "\"");
            }
            System.out.println("    👍 " + eval.getNombreVotesUtile() + " | 👎 " + eval.getNombreVotesPasUtile());
        }
        System.out.println("═══════════════════════════════════════\n");
    }

    // === GESTION DU CATALOGUE ===

    public void ajouterEditeur(Editeur editeur) {
        editeurs.put(editeur.getId(), editeur);
        // Message désactivé pour éviter le spam lors du chargement depuis la BDD
        // System.out.println("✅ Éditeur ajouté : " + editeur.getNom() +
        //     (editeur.isEstIndependant() ? " (Indépendant)" : " (Entreprise)"));
    }

    public void publierJeu(JeuCatalogue jeu) {
        catalogue.put(jeu.getId(), jeu);

        // Ajouter à la liste des jeux de l'éditeur
        for (Editeur editeur : editeurs.values()) {
            if (editeur.getNom().equals(jeu.getEditeur())) {
                editeur.ajouterJeu(jeu.getTitre());
                break;
            }
        }

        // Message désactivé pour éviter le spam lors du chargement depuis la BDD
        // System.out.println("✅ Jeu publié dans le catalogue : " + jeu.getTitre() +
        //     " (" + jeu.getPlateforme() + ") - " + jeu.getPrixActuel() + "€");
    }

    public void afficherCatalogue() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("🎮 CATALOGUE DE JEUX (" + catalogue.size() + " jeux)");
        System.out.println("═══════════════════════════════════════");

        for (JeuCatalogue jeu : catalogue.values()) {
            double moyenneNotes = calculerMoyenneEvaluations(jeu);
            System.out.println("\n📦 " + jeu.getTitre() + " (v" + jeu.getVersionActuelle() + ")");
            System.out.println("   Éditeur: " + jeu.getEditeur() + " | Plateforme: " + jeu.getPlateforme());
            System.out.println("   Genres: " + String.join(", ", jeu.getGenres()));
            System.out.println("   Prix: " + jeu.getPrixActuel() + "€" +
                (jeu.isVersionAnticipee() ? " (Accès Anticipé)" : ""));
            System.out.println("   Note moyenne: " +
                (moyenneNotes > 0 ? String.format("%.1f/10", moyenneNotes) + " (" +
                jeu.getEvaluationsJoueurs().size() + " avis)" : "Aucune évaluation"));

            // Afficher les extensions disponibles
            List<Extension> exts = extensions.get(jeu.getId());
            if (exts != null && !exts.isEmpty()) {
                System.out.println("   🎁 Extensions disponibles: " + exts.size());
            }
        }
        System.out.println("═══════════════════════════════════════\n");
    }

    public JeuCatalogue getJeuParTitre(String titre) {
        return catalogue.values().stream()
            .filter(jeu -> jeu.getTitre().equalsIgnoreCase(titre))
            .findFirst()
            .orElse(null);
    }

    // === FONCTIONNALITÉS JOUEUR ===

    public void acheterJeu(String pseudoJoueur, String titreJeu) {
        Joueur joueur = joueurs.get(pseudoJoueur);
        JeuCatalogue jeu = getJeuParTitre(titreJeu);

        if (joueur == null) {
            System.out.println("❌ Joueur introuvable.");
            return;
        }
        if (jeu == null) {
            System.out.println("❌ Jeu introuvable dans le catalogue.");
            return;
        }

        // Vérifier si le joueur possède déjà ce jeu
        boolean dejaPos = joueur.getBibliotheque().stream()
            .anyMatch(j -> j.getJeuId().equals(jeu.getId()));

        if (dejaPos) {
            System.out.println("❌ Vous possédez déjà ce jeu !");
            return;
        }

        joueur.acheterJeu(jeu);
        System.out.println("✅ " + pseudoJoueur + " a acheté " + titreJeu + " pour " + jeu.getPrixActuel() + "€");
    }

    public void jouer(String pseudoJoueur, String titreJeu, long minutesJouees) {
        Joueur joueur = joueurs.get(pseudoJoueur);
        if (joueur == null) return;

        JeuPossede jeuPossede = joueur.getBibliotheque().stream()
            .filter(j -> j.getTitreJeu().equalsIgnoreCase(titreJeu))
            .findFirst()
            .orElse(null);

        if (jeuPossede == null) {
            System.out.println("❌ Vous ne possédez pas ce jeu.");
            return;
        }

        jeuPossede.ajouterTempsDeJeu(minutesJouees);
        System.out.println("🎮 " + pseudoJoueur + " a joué à " + titreJeu + " pendant " + minutesJouees + " minutes.");
    }

    public void evaluerJeu(String pseudoJoueur, String titreJeu, int note, String commentaire) {
        Joueur joueur = joueurs.get(pseudoJoueur);
        JeuCatalogue jeu = getJeuParTitre(titreJeu);

        if (joueur == null || jeu == null) {
            System.out.println("❌ Joueur ou jeu introuvable.");
            return;
        }

        // Vérifier que le joueur possède le jeu
        JeuPossede jeuPossede = joueur.getBibliotheque().stream()
            .filter(j -> j.getTitreJeu().equalsIgnoreCase(titreJeu))
            .findFirst()
            .orElse(null);

        if (jeuPossede == null) {
            System.out.println("❌ Vous devez posséder le jeu pour l'évaluer.");
            return;
        }

        // Vérifier le temps de jeu minimum (ex: 30 minutes)
        if (jeuPossede.getTempsDeJeuEnMinutes() < 30) {
            System.out.println("❌ Vous devez jouer au moins 30 minutes pour évaluer ce jeu.");
            return;
        }

        Evaluation evaluation = new Evaluation(pseudoJoueur, titreJeu, note, commentaire);
        joueur.ajouterEvaluation(evaluation);
        jeu.ajouterEvaluation(evaluation);

        System.out.println("✅ Évaluation postée pour " + titreJeu + " : " + note + "/10");

        // Recalculer le prix en fonction des évaluations
        pricingService.recalculerPrix(jeu);
    }

    /**
     * Permet à un joueur d'évaluer une extension (DLC) qu'il possède
     */
    public void evaluerExtension(String pseudoJoueur, String titreJeuParent, String titreExtension, int note, String commentaire) {
        Joueur joueur = joueurs.get(pseudoJoueur);
        if (joueur == null) {
            System.out.println("❌ Joueur introuvable.");
            return;
        }

        // Vérifier que le joueur possède le jeu parent
        JeuPossede jeuPossede = joueur.getBibliotheque().stream()
            .filter(j -> j.getTitreJeu().equalsIgnoreCase(titreJeuParent))
            .findFirst()
            .orElse(null);

        if (jeuPossede == null) {
            System.out.println("❌ Vous devez posséder le jeu de base pour évaluer cette extension.");
            return;
        }

        // Vérifier que le joueur possède l'extension
        Extension extension = jeuPossede.getExtensionsPossedees().stream()
            .filter(ext -> ext.getTitre().equalsIgnoreCase(titreExtension))
            .findFirst()
            .orElse(null);

        if (extension == null) {
            System.out.println("❌ Vous devez posséder cette extension pour l'évaluer.");
            return;
        }

        // Vérifier le temps de jeu minimum sur le jeu de base
        if (jeuPossede.getTempsDeJeuEnMinutes() < 30) {
            System.out.println("❌ Vous devez jouer au moins 30 minutes pour évaluer cette extension.");
            return;
        }

        Evaluation evaluation = new Evaluation(pseudoJoueur, titreExtension + " (DLC de " + titreJeuParent + ")", note, commentaire);
        joueur.ajouterEvaluation(evaluation);
        extension.ajouterEvaluation(evaluation);

        System.out.println("✅ Évaluation postée pour l'extension " + titreExtension + " : " + note + "/10");
    }

    public void voterUtiliteEvaluation(String titreJeu, String pseudoAuteur, boolean utile) {
        JeuCatalogue jeu = getJeuParTitre(titreJeu);
        if (jeu == null) return;

        Evaluation eval = jeu.getEvaluationsJoueurs().stream()
            .filter(e -> e.getPseudoJoueur().equals(pseudoAuteur))
            .findFirst()
            .orElse(null);

        if (eval != null) {
            if (utile) {
                eval.voterUtile();
                System.out.println("👍 Évaluation marquée comme utile.");
            } else {
                eval.voterPasUtile();
                System.out.println("👎 Évaluation marquée comme pas utile.");
            }
        }
    }

    // === GESTION DES PATCHES ===

    public void publierPatch(Patch patch) {
        patches.add(patch);

        // Mettre à jour le jeu dans le catalogue
        JeuCatalogue jeu = getJeuParTitre(patch.getTitreJeu());
        if (jeu != null) {
            jeu.setVersionActuelle(patch.getNouvelleVersion());
            jeu.ajouterCorrectif(patch.getNouvelleVersion() + " - " + patch.getCommentaireEditeur());

            System.out.println("✅ Patch publié pour " + patch.getTitreJeu() + " : v" + patch.getNouvelleVersion());
            System.out.println("   Modifications :");
            for (Patch.Modification modif : patch.getModifications()) {
                System.out.println("     • [" + modif.getType() + "] " + modif.getDescription());
            }

            // Notifier les joueurs possédant ce jeu
            notifierJoueursDeMAJ(patch.getTitreJeu(), patch.getNouvelleVersion());
        }
    }

    private void notifierJoueursDeMAJ(String titreJeu, String nouvelleVersion) {
        List<String> joueursNotifies = new ArrayList<>();

        for (Joueur joueur : joueurs.values()) {
            boolean possede = joueur.getBibliotheque().stream()
                .anyMatch(j -> j.getTitreJeu().equalsIgnoreCase(titreJeu));

            if (possede) {
                joueursNotifies.add(joueur.getPseudo());
            }
        }

        if (!joueursNotifies.isEmpty()) {
            System.out.println("   📢 Notification envoyée à " + joueursNotifies.size() + " joueur(s) : "
                + String.join(", ", joueursNotifies));
        }
    }

    // === GESTION DES EXTENSIONS (DLC) ===

    public void publierExtension(Extension extension) {
        JeuCatalogue jeuParent = getJeuParTitre(extension.getTitreJeuParent());
        if (jeuParent == null) {
            System.out.println("❌ Jeu parent introuvable.");
            return;
        }

        extensions.computeIfAbsent(jeuParent.getId(), k -> new ArrayList<>()).add(extension);
        System.out.println("✅ Extension publiée : " + extension.getTitre() + " pour " +
            extension.getTitreJeuParent() + " (" + extension.getPrix() + "€)");
        System.out.println("   Nécessite la version " + extension.getVersionJeuBaseRequise() + " minimum");
    }

    public void acheterExtension(String pseudoJoueur, String titreJeu, String titreExtension) {
        Joueur joueur = joueurs.get(pseudoJoueur);
        JeuCatalogue jeu = getJeuParTitre(titreJeu);

        if (joueur == null || jeu == null) return;

        JeuPossede jeuPossede = joueur.getBibliotheque().stream()
            .filter(j -> j.getTitreJeu().equalsIgnoreCase(titreJeu))
            .findFirst()
            .orElse(null);

        if (jeuPossede == null) {
            System.out.println("❌ Vous devez posséder le jeu de base pour acheter cette extension.");
            return;
        }

        List<Extension> exts = extensions.get(jeu.getId());
        if (exts == null) return;

        Extension ext = exts.stream()
            .filter(e -> e.getTitre().equalsIgnoreCase(titreExtension))
            .findFirst()
            .orElse(null);

        if (ext == null) {
            System.out.println("❌ Extension introuvable.");
            return;
        }

        // Vérifier la version du jeu
        if (!verifierVersionCompatible(jeuPossede.getVersionInstallee(), ext.getVersionJeuBaseRequise())) {
            System.out.println("❌ Votre version du jeu (" + jeuPossede.getVersionInstallee() +
                ") est incompatible. Version requise : " + ext.getVersionJeuBaseRequise());
            return;
        }

        jeuPossede.ajouterExtension(ext);
        System.out.println("✅ Extension achetée : " + ext.getTitre() + " pour " + ext.getPrix() + "€");
    }

    // === GESTION DES RAPPORTS D'INCIDENTS ===

    public void rapporterIncident(RapportIncident rapport) {
        rapportsIncidents.add(rapport);
        // Récupérer le jeu pour afficher son titre
        JeuCatalogue jeu = getJeuById(rapport.getJeuId());
        String titreJeu = jeu != null ? jeu.getTitre() : "Jeu inconnu";

        System.out.println("🐛 Rapport d'incident enregistré pour " + titreJeu +
            " (v" + rapport.getVersionJeu() + ")");
        System.out.println("   ID: " + rapport.getId());
        System.out.println("   Joueur: " + rapport.getJoueurPseudo());
        System.out.println("   Description: " + rapport.getDescriptionErreur());
    }

    // Alias pour compatibilité
    public void signalerIncident(RapportIncident rapport) {
        rapporterIncident(rapport);
    }

    public void afficherRapportsIncidentsPourJeu(String jeuId) {
        List<RapportIncident> rapports = rapportsIncidents.stream()
            .filter(r -> r.getJeuId().equals(jeuId))
            .collect(Collectors.toList());

        JeuCatalogue jeu = getJeuById(jeuId);
        String titreJeu = jeu != null ? jeu.getTitre() : "Jeu inconnu";

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("🐛 RAPPORTS D'INCIDENTS : " + titreJeu);
        System.out.println("═══════════════════════════════════════");
        System.out.println("Total : " + rapports.size() + " rapport(s)\n");

        for (RapportIncident r : rapports) {
            System.out.println("ID: " + r.getId());
            System.out.println("Version: " + r.getVersionJeu() + " | Plateforme: " + r.getPlateforme());
            System.out.println("Joueur: " + r.getJoueurPseudo());
            System.out.println("Date: " + r.getDateSurvenue());
            System.out.println("Erreur: " + r.getDescriptionErreur());
            System.out.println("---");
        }
        System.out.println("═══════════════════════════════════════\n");
    }

    // === PAGES DESCRIPTIVES ===

    public void afficherPageEditeur(String nomEditeur) {
        Editeur editeur = editeurs.values().stream()
            .filter(e -> e.getNom().equalsIgnoreCase(nomEditeur))
            .findFirst()
            .orElse(null);

        if (editeur == null) {
            System.out.println("❌ Éditeur introuvable.");
            return;
        }

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("🏢 ÉDITEUR : " + editeur.getNom().toUpperCase());
        System.out.println("═══════════════════════════════════════");
        System.out.println("Type: " + (editeur.isEstIndependant() ? "Indépendant" : "Entreprise"));
        System.out.println("\n📚 Jeux publiés (" + editeur.getJeuxPublies().size() + ") :");

        for (String titreJeu : editeur.getJeuxPublies()) {
            JeuCatalogue jeu = getJeuParTitre(titreJeu);
            if (jeu != null) {
                double moyenne = calculerMoyenneEvaluations(jeu);
                System.out.println("  • " + titreJeu + " (" + jeu.getPlateforme() + ") - v" +
                    jeu.getVersionActuelle() + " - " +
                    (moyenne > 0 ? String.format("%.1f/10", moyenne) : "Pas encore évalué"));
            }
        }
        System.out.println("═══════════════════════════════════════\n");
    }

    public void afficherPageJeu(String titreJeu) {
        JeuCatalogue jeu = getJeuParTitre(titreJeu);
        if (jeu == null) {
            System.out.println("❌ Jeu introuvable.");
            return;
        }

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("🎮 " + jeu.getTitre().toUpperCase());
        System.out.println("═══════════════════════════════════════");
        System.out.println("Éditeur: " + jeu.getEditeur());
        System.out.println("Plateforme: " + jeu.getPlateforme());
        System.out.println("Genres: " + String.join(", ", jeu.getGenres()));
        System.out.println("Version actuelle: " + jeu.getVersionActuelle() +
            (jeu.isVersionAnticipee() ? " (Accès Anticipé)" : ""));
        System.out.println("Prix: " + jeu.getPrixActuel() + "€ (prix éditeur: " + jeu.getPrixEditeur() + "€)");

        double moyenne = calculerMoyenneEvaluations(jeu);
        System.out.println("\n⭐ Note moyenne: " +
            (moyenne > 0 ? String.format("%.1f/10", moyenne) + " (" +
            jeu.getEvaluationsJoueurs().size() + " avis)" : "Aucune évaluation"));

        if (!jeu.getHistoriqueCorrectifs().isEmpty()) {
            System.out.println("\n📝 Historique des correctifs:");
            for (String correctif : jeu.getHistoriqueCorrectifs()) {
                System.out.println("  • " + correctif);
            }
        }

        List<Extension> exts = extensions.get(jeu.getId());
        if (exts != null && !exts.isEmpty()) {
            System.out.println("\n🎁 Extensions disponibles:");
            for (Extension ext : exts) {
                System.out.println("  • " + ext.getTitre() + " - " + ext.getPrix() + "€ (nécessite v" +
                    ext.getVersionJeuBaseRequise() + ")");
            }
        }

        if (!jeu.getEvaluationsJoueurs().isEmpty()) {
            System.out.println("\n💬 Évaluations des joueurs:");
            for (Evaluation eval : jeu.getEvaluationsJoueurs()) {
                System.out.println("  " + eval.getPseudoJoueur() + " - " + eval.getNote() + "/10");
                System.out.println("  \"" + eval.getCommentaire() + "\"");
                System.out.println("  👍 " + eval.getNombreVotesUtile() + " | 👎 " +
                    eval.getNombreVotesPasUtile() + " - " + eval.getDatePublication());
                System.out.println();
            }
        }

        System.out.println("═══════════════════════════════════════\n");
    }

    // === UTILITAIRES ===

    private double calculerMoyenneEvaluations(JeuCatalogue jeu) {
        if (jeu.getEvaluationsJoueurs().isEmpty()) {
            return 0;
        }

        double somme = jeu.getEvaluationsJoueurs().stream()
            .mapToInt(Evaluation::getNote)
            .sum();

        return somme / jeu.getEvaluationsJoueurs().size();
    }

    private boolean verifierVersionCompatible(String versionJeu, String versionRequise) {
        try {
            String[] partsJeu = versionJeu.split("\\.");
            String[] partsReq = versionRequise.split("\\.");

            for (int i = 0; i < Math.min(partsJeu.length, partsReq.length); i++) {
                int numJeu = Integer.parseInt(partsJeu[i]);
                int numReq = Integer.parseInt(partsReq[i]);

                if (numJeu > numReq) return true;
                if (numJeu < numReq) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getNom() {
        return nom;
    }

    public Map<String, Joueur> getJoueurs() {
        return joueurs;
    }

    public List<Joueur> getJoueursList() {
        return new ArrayList<>(joueurs.values());
    }

    public Joueur getJoueurByPseudo(String pseudo) {
        return joueurs.get(pseudo);
    }

    public Map<String, JeuCatalogue> getCatalogue() {
        return catalogue;
    }

    public List<JeuCatalogue> getCatalogueList() {
        return new ArrayList<>(catalogue.values());
    }

    public JeuCatalogue getJeuById(String id) {
        return catalogue.get(id);
    }

    public List<Editeur> getEditeursList() {
        return new ArrayList<>(editeurs.values());
    }

    public Editeur getEditeurByNom(String nom) {
        return editeurs.values().stream()
            .filter(e -> e.getNom().equalsIgnoreCase(nom))
            .findFirst()
            .orElse(null);
    }
}

