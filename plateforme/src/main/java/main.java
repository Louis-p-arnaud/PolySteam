import config.DatabaseConfig;
import dao.*;
import model.*;

import java.util.List;
import java.util.Scanner;

/**
 * Application principale PolySteam - Plateforme de distribution de jeux vidéo
 * Menu interactif avec chargement des données depuis la base de données
 */
public class main {

    private static Plateforme plateforme;
    private static Scanner scanner;

    // DAOs pour interagir avec la base de données
    private static EditeurDAO editeurDAO;
    private static JeuCatalogueDAO jeuDAO;
    private static JoueurDAO joueurDAO;
    private static EvaluationDAO evaluationDAO;
    private static RapportIncidentDAO incidentDAO;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        afficherBanniere();

        // Initialiser la connexion à la base de données
        if (!initialiserBaseDeDonnees()) {
            System.err.println("❌ Impossible de se connecter à la base de données. Fermeture de l'application.");
            return;
        }

        // Initialiser les DAOs
        initDAOs();

        // Créer la plateforme et charger les données
        plateforme = new Plateforme("PolySteam");
        chargerDonneesDepuisBDD();

        // Lancer le menu principal
        menuPrincipal();

        scanner.close();
        System.out.println("\n👋 Merci d'avoir utilisé PolySteam ! À bientôt !\n");
    }

    /**
     * Affiche la bannière de l'application
     */
    private static void afficherBanniere() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                🎮 PLATEFORME POLYSTEAM 🎮                 ║");
        System.out.println("║          Distribution de Jeux Vidéo en Ligne             ║");
        System.out.println("║                      Version 2.0                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Initialise la connexion à la base de données
     */
    private static boolean initialiserBaseDeDonnees() {
        System.out.println("🔌 Connexion à la base de données PostgreSQL...");
        try {
            DatabaseConfig.getConnection();
            System.out.println("✅ Connexion établie avec succès!\n");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            return false;
        }
    }

    /**
     * Initialise les Data Access Objects
     */
    private static void initDAOs() {
        editeurDAO = new EditeurDAO();
        jeuDAO = new JeuCatalogueDAO();
        joueurDAO = new JoueurDAO();
        evaluationDAO = new EvaluationDAO();
        incidentDAO = new RapportIncidentDAO();
    }

    /**
     * Charge toutes les données depuis la base de données
     */
    private static void chargerDonneesDepuisBDD() {
        System.out.println("📂 Chargement des données depuis la base de données...\n");

        // Charger les éditeurs
        System.out.print("   📝 Chargement des éditeurs... ");
        List<Editeur> editeurs = editeurDAO.findAll();
        editeurs.forEach(plateforme::ajouterEditeur);
        System.out.println("✅ " + editeurs.size() + " éditeurs chargés");

        // Charger les jeux
        System.out.print("   🎮 Chargement des jeux... ");
        List<JeuCatalogue> jeux = jeuDAO.findAll();
        jeux.forEach(plateforme::publierJeu);
        System.out.println("✅ " + jeux.size() + " jeux chargés");

        // Charger les joueurs
        System.out.print("   👤 Chargement des joueurs... ");
        List<Joueur> joueurs = joueurDAO.findAll();
        joueurs.forEach(plateforme::inscrireJoueur);
        System.out.println("✅ " + joueurs.size() + " joueurs chargés");

        // Charger les relations d'amitié
        System.out.print("   👥 Chargement des relations d'amitié... ");
        int totalAmis = 0;
        for (Joueur joueur : joueurs) {
            List<Joueur> amis = joueurDAO.findAmis(joueur.getPseudo());
            for (Joueur ami : amis) {
                Joueur joueurEnMemoire = plateforme.getJoueurByPseudo(joueur.getPseudo());
                if (joueurEnMemoire != null) {
                    joueurEnMemoire.ajouterAmi(ami.getPseudo());
                    totalAmis++;
                }
            }
        }
        System.out.println("✅ " + totalAmis + " relations d'amitié chargées");

        // Charger les évaluations
        System.out.print("   ⭐ Chargement des évaluations... ");
        int totalEvaluations = 0;
        for (JeuCatalogue jeu : jeux) {
            List<Evaluation> evaluations = evaluationDAO.findByJeuId(jeu.getId());
            JeuCatalogue jeuEnMemoire = plateforme.getJeuById(jeu.getId());
            if (jeuEnMemoire != null) {
                evaluations.forEach(jeuEnMemoire::ajouterEvaluation);
                totalEvaluations += evaluations.size();
            }
        }
        System.out.println("✅ " + totalEvaluations + " évaluations chargées");

        System.out.println("\n✅ Toutes les données ont été chargées avec succès!\n");
    }

    /**
     * Menu principal de l'application
     */
    private static void menuPrincipal() {
        while (true) {
            afficherMenuPrincipal();
            int choix = lireChoix();

            switch (choix) {
                case 0 -> {
                    return; // Quitter
                }
                case 1 -> menuCatalogue();
                case 2 -> menuJoueurs();
                case 3 -> menuEditeurs();
                case 4 -> menuIncidents();
                case 5 -> afficherStatistiques();
                case 6 -> rechercherJeu();
                default -> System.out.println("❌ Choix invalide. Réessayez.\n");
            }
        }
    }

    /**
     * Affiche le menu principal
     */
    private static void afficherMenuPrincipal() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                     🏠 MENU PRINCIPAL                      ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. 📚 Catalogue de jeux                                  ║");
        System.out.println("║  2. 👥 Gestion des joueurs                                ║");
        System.out.println("║  3. 🏢 Gestion des éditeurs                               ║");
        System.out.println("║  4. 🐛 Rapports d'incidents                               ║");
        System.out.println("║  5. 📊 Statistiques de la plateforme                      ║");
        System.out.println("║  6. 🔍 Rechercher un jeu                                  ║");
        System.out.println("║  0. 🚪 Quitter                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.print("Votre choix : ");
    }

    /**
     * Menu de gestion du catalogue
     */
    private static void menuCatalogue() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                  📚 CATALOGUE DE JEUX                      ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  1. 📋 Afficher tous les jeux                             ║");
            System.out.println("║  2. 🎮 Détails d'un jeu                                   ║");
            System.out.println("║  3. ⭐ Évaluations d'un jeu                               ║");
            System.out.println("║  4. 💰 Modifier le prix d'un jeu                          ║");
            System.out.println("║  0. ⬅️  Retour                                            ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();

            switch (choix) {
                case 0 -> { return; }
                case 1 -> afficherTousLesJeux();
                case 2 -> afficherDetailsJeu();
                case 3 -> afficherEvaluationsJeu();
                case 4 -> modifierPrixJeu();
                default -> System.out.println("❌ Choix invalide.\n");
            }
        }
    }

    /**
     * Menu de gestion des joueurs
     */
    private static void menuJoueurs() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                 👥 GESTION DES JOUEURS                     ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  1. 📋 Afficher tous les joueurs                          ║");
            System.out.println("║  2. 👤 Détails d'un joueur                                ║");
            System.out.println("║  3. 🎮 Bibliothèque d'un joueur                           ║");
            System.out.println("║  4. 👥 Amis d'un joueur                                   ║");
            System.out.println("║  5. ➕ Ajouter un nouveau joueur                          ║");
            System.out.println("║  0. ⬅️  Retour                                            ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();

            switch (choix) {
                case 0 -> { return; }
                case 1 -> afficherTousLesJoueurs();
                case 2 -> afficherDetailsJoueur();
                case 3 -> afficherBibliothequeJoueur();
                case 4 -> afficherAmisJoueur();
                case 5 -> ajouterNouveauJoueur();
                default -> System.out.println("❌ Choix invalide.\n");
            }
        }
    }

    /**
     * Menu de gestion des éditeurs
     */
    private static void menuEditeurs() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                 🏢 GESTION DES ÉDITEURS                    ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  1. 📋 Afficher tous les éditeurs                         ║");
            System.out.println("║  2. 🎮 Jeux d'un éditeur                                  ║");
            System.out.println("║  0. ⬅️  Retour                                            ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();

            switch (choix) {
                case 0 -> { return; }
                case 1 -> afficherTousLesEditeurs();
                case 2 -> afficherJeuxEditeur();
                default -> System.out.println("❌ Choix invalide.\n");
            }
        }
    }

    /**
     * Menu des incidents
     */
    private static void menuIncidents() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                🐛 RAPPORTS D'INCIDENTS                     ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  1. 📋 Afficher tous les incidents                        ║");
            System.out.println("║  2. 🎮 Incidents d'un jeu spécifique                      ║");
            System.out.println("║  3. ➕ Signaler un nouvel incident                        ║");
            System.out.println("║  0. ⬅️  Retour                                            ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.print("Votre choix : ");

            int choix = lireChoix();

            switch (choix) {
                case 0 -> { return; }
                case 1 -> afficherTousLesIncidents();
                case 2 -> afficherIncidentsJeu();
                case 3 -> signalerIncident();
                default -> System.out.println("❌ Choix invalide.\n");
            }
        }
    }

    // ===================================
    // MÉTHODES D'AFFICHAGE - CATALOGUE
    // ===================================

    private static void afficherTousLesJeux() {
        System.out.println("\n📚 ═══════════════ CATALOGUE COMPLET ═══════════════\n");

        List<JeuCatalogue> jeux = plateforme.getCatalogueList();

        if (jeux.isEmpty()) {
            System.out.println("   Aucun jeu dans le catalogue.");
            return;
        }

        for (int i = 0; i < jeux.size(); i++) {
            JeuCatalogue jeu = jeux.get(i);
            double noteMoyenne = evaluationDAO.getNoteMoyenne(jeu.getId());
            int nbEvaluations = evaluationDAO.countByJeuId(jeu.getId());

            System.out.printf("%d. %s\n", i + 1, jeu.getTitre());
            System.out.printf("   📝 Éditeur: %s\n", jeu.getNomEditeur());
            System.out.printf("   🎯 Plateforme: %s | Version: %s\n", jeu.getPlateforme(), jeu.getVersionActuelle());
            System.out.printf("   🏷️  Genres: %s\n", String.join(", ", jeu.getGenres()));
            System.out.printf("   💰 Prix: %.2f€\n", jeu.getPrixActuel());

            if (nbEvaluations > 0) {
                System.out.printf("   ⭐ Note: %.1f/10 (%d évaluations)\n", noteMoyenne, nbEvaluations);
            } else {
                System.out.println("   ⭐ Pas encore d'évaluation");
            }
            System.out.println();
        }
    }

    private static void afficherDetailsJeu() {
        System.out.print("\n🔍 Entrez le titre du jeu (ou une partie) : ");
        scanner.nextLine(); // Consommer le retour à la ligne
        String titre = scanner.nextLine();

        List<JeuCatalogue> jeux = jeuDAO.findByTitre(titre);

        if (jeux.isEmpty()) {
            System.out.println("❌ Aucun jeu trouvé avec ce titre.\n");
            return;
        }

        if (jeux.size() > 1) {
            System.out.println("\n📋 Plusieurs jeux trouvés :\n");
            for (int i = 0; i < jeux.size(); i++) {
                System.out.printf("%d. %s (%s)\n", i + 1, jeux.get(i).getTitre(), jeux.get(i).getPlateforme());
            }
            System.out.print("\nChoisissez un jeu (numéro) : ");
            int choix = lireChoix() - 1;
            if (choix >= 0 && choix < jeux.size()) {
                afficherDetailsCompletJeu(jeux.get(choix));
            }
        } else {
            afficherDetailsCompletJeu(jeux.get(0));
        }
    }

    private static void afficherDetailsCompletJeu(JeuCatalogue jeu) {
        System.out.println("\n🎮 ═══════════════ DÉTAILS DU JEU ═══════════════");
        System.out.println("\n📦 " + jeu.getTitre());
        System.out.println("   ═══════════════════════════════════════════════");
        System.out.printf("   📝 Éditeur: %s\n", jeu.getNomEditeur());
        System.out.printf("   🎯 Plateforme: %s\n", jeu.getPlateforme());
        System.out.printf("   📌 Version: %s%s\n",
            jeu.getVersionActuelle(),
            jeu.isVersionAnticipee() ? " (Accès anticipé)" : "");
        System.out.printf("   🏷️  Genres: %s\n", String.join(", ", jeu.getGenres()));
        System.out.printf("   💰 Prix: %.2f€\n", jeu.getPrixActuel());

        double noteMoyenne = evaluationDAO.getNoteMoyenne(jeu.getId());
        int nbEvaluations = evaluationDAO.countByJeuId(jeu.getId());

        if (nbEvaluations > 0) {
            System.out.printf("   ⭐ Note moyenne: %.1f/10 (%d évaluations)\n", noteMoyenne, nbEvaluations);
        } else {
            System.out.println("   ⭐ Pas encore d'évaluation");
        }

        int nbIncidents = incidentDAO.countByJeuId(jeu.getId());
        System.out.printf("   🐛 Incidents signalés: %d\n", nbIncidents);

        System.out.println("   ═══════════════════════════════════════════════\n");
    }

    private static void afficherEvaluationsJeu() {
        System.out.print("\n🔍 Entrez le titre du jeu : ");
        scanner.nextLine();
        String titre = scanner.nextLine();

        List<JeuCatalogue> jeux = jeuDAO.findByTitre(titre);

        if (jeux.isEmpty()) {
            System.out.println("❌ Aucun jeu trouvé.\n");
            return;
        }

        JeuCatalogue jeu = jeux.get(0);
        List<Evaluation> evaluations = evaluationDAO.findByJeuId(jeu.getId());

        if (evaluations.isEmpty()) {
            System.out.println("❌ Aucune évaluation pour ce jeu.\n");
            return;
        }

        System.out.println("\n⭐ ═══════════════ ÉVALUATIONS ═══════════════");
        System.out.println("Jeu: " + jeu.getTitre() + "\n");

        for (Evaluation eval : evaluations) {
            System.out.printf("👤 %s - Note: %d/10\n", eval.getJoueurPseudo(), eval.getNote());
            System.out.printf("💬 %s\n", eval.getCommentaire());
            System.out.printf("👍 %d utile | 👎 %d pas utile\n",
                eval.getNombreVotesUtile(), eval.getNombreVotesPasUtile());
            System.out.println("─────────────────────────────────────────────");
        }
        System.out.println();
    }

    private static void modifierPrixJeu() {
        System.out.print("\n🔍 Entrez le titre du jeu : ");
        scanner.nextLine();
        String titre = scanner.nextLine();

        List<JeuCatalogue> jeux = jeuDAO.findByTitre(titre);

        if (jeux.isEmpty()) {
            System.out.println("❌ Aucun jeu trouvé.\n");
            return;
        }

        JeuCatalogue jeu = jeux.get(0);
        System.out.printf("💰 Prix actuel: %.2f€\n", jeu.getPrixActuel());
        System.out.print("💰 Nouveau prix: ");

        try {
            double nouveauPrix = Double.parseDouble(scanner.nextLine());
            if (nouveauPrix < 0) {
                System.out.println("❌ Le prix ne peut pas être négatif.\n");
                return;
            }

            if (jeuDAO.updatePrix(jeu.getId(), nouveauPrix)) {
                jeu.setPrixActuel(nouveauPrix);
                System.out.println("✅ Prix mis à jour avec succès!\n");
            } else {
                System.out.println("❌ Erreur lors de la mise à jour du prix.\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Prix invalide.\n");
        }
    }

    // ===================================
    // MÉTHODES D'AFFICHAGE - JOUEURS
    // ===================================

    private static void afficherTousLesJoueurs() {
        System.out.println("\n👥 ═══════════════ LISTE DES JOUEURS ═══════════════\n");

        List<Joueur> joueurs = plateforme.getJoueursList();

        if (joueurs.isEmpty()) {
            System.out.println("   Aucun joueur inscrit.");
            return;
        }

        for (int i = 0; i < joueurs.size(); i++) {
            Joueur joueur = joueurs.get(i);
            System.out.printf("%d. %s (%s %s)\n",
                i + 1, joueur.getPseudo(), joueur.getPrenom(), joueur.getNom());
            System.out.printf("   🎂 Âge: %d ans\n", joueur.getAge());
            System.out.printf("   👥 Amis: %d\n", joueur.getAmis().size());
            System.out.println();
        }
    }

    private static void afficherDetailsJoueur() {
        System.out.print("\n🔍 Entrez le pseudo du joueur : ");
        scanner.nextLine();
        String pseudo = scanner.nextLine();

        Joueur joueur = plateforme.getJoueurByPseudo(pseudo);

        if (joueur == null) {
            System.out.println("❌ Joueur introuvable.\n");
            return;
        }

        System.out.println("\n👤 ═══════════════ PROFIL JOUEUR ═══════════════");
        System.out.printf("\n🎮 %s\n", joueur.getPseudo());
        System.out.println("   ═══════════════════════════════════════════════");
        System.out.printf("   📝 Nom: %s %s\n", joueur.getPrenom(), joueur.getNom());
        System.out.printf("   🎂 Âge: %d ans\n", joueur.getAge());
        System.out.printf("   👥 Nombre d'amis: %d\n", joueur.getAmis().size());
        System.out.printf("   🎮 Jeux possédés: %d\n", joueur.getBibliotheque().size());
        System.out.println("   ═══════════════════════════════════════════════\n");
    }

    private static void afficherBibliothequeJoueur() {
        System.out.print("\n🔍 Entrez le pseudo du joueur : ");
        scanner.nextLine();
        String pseudo = scanner.nextLine();

        Joueur joueur = plateforme.getJoueurByPseudo(pseudo);

        if (joueur == null) {
            System.out.println("❌ Joueur introuvable.\n");
            return;
        }

        List<JeuPossede> bibliotheque = joueur.getBibliotheque();

        if (bibliotheque.isEmpty()) {
            System.out.println("❌ Ce joueur ne possède aucun jeu.\n");
            return;
        }

        System.out.println("\n🎮 ═══════════════ BIBLIOTHÈQUE ═══════════════");
        System.out.println("Joueur: " + joueur.getPseudo() + "\n");

        for (int i = 0; i < bibliotheque.size(); i++) {
            JeuPossede jeu = bibliotheque.get(i);
            System.out.printf("%d. %s\n", i + 1, jeu.getJeuCatalogue().getTitre());
            System.out.printf("   📌 Version installée: %s\n", jeu.getVersionInstallee());
            System.out.printf("   ⏱️  Temps de jeu: %d heures\n", jeu.getTempsJeuHeures());
            System.out.println();
        }
    }

    private static void afficherAmisJoueur() {
        System.out.print("\n🔍 Entrez le pseudo du joueur : ");
        scanner.nextLine();
        String pseudo = scanner.nextLine();

        Joueur joueur = plateforme.getJoueurByPseudo(pseudo);

        if (joueur == null) {
            System.out.println("❌ Joueur introuvable.\n");
            return;
        }

        List<String> amisPseudos = joueur.getAmis();

        if (amisPseudos.isEmpty()) {
            System.out.println("❌ Ce joueur n'a pas encore d'amis.\n");
            return;
        }

        System.out.println("\n👥 ═══════════════ LISTE D'AMIS ═══════════════");
        System.out.println("Joueur: " + joueur.getPseudo() + "\n");

        for (int i = 0; i < amisPseudos.size(); i++) {
            String pseudoAmi = amisPseudos.get(i);
            Joueur ami = plateforme.getJoueurByPseudo(pseudoAmi);
            if (ami != null) {
                System.out.printf("%d. %s (%s %s)\n",
                    i + 1, ami.getPseudo(), ami.getPrenom(), ami.getNom());
            }
        }
        System.out.println();
    }

    private static void ajouterNouveauJoueur() {
        System.out.println("\n➕ ═══════════════ NOUVEAU JOUEUR ═══════════════\n");

        scanner.nextLine(); // Consommer le retour à la ligne

        System.out.print("Pseudo : ");
        String pseudo = scanner.nextLine();

        System.out.print("Nom : ");
        String nom = scanner.nextLine();

        System.out.print("Prénom : ");
        String prenom = scanner.nextLine();

        System.out.print("Date de naissance (AAAA-MM-JJ) : ");
        String dateStr = scanner.nextLine();

        try {
            java.time.LocalDate dateNaissance = java.time.LocalDate.parse(dateStr);
            Joueur joueur = new Joueur(pseudo, nom, prenom, dateNaissance);

            if (joueurDAO.insert(joueur)) {
                plateforme.inscrireJoueur(joueur);
                System.out.println("✅ Joueur ajouté avec succès!\n");
            } else {
                System.out.println("❌ Erreur lors de l'ajout du joueur.\n");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage() + "\n");
        }
    }

    // ===================================
    // MÉTHODES D'AFFICHAGE - ÉDITEURS
    // ===================================

    private static void afficherTousLesEditeurs() {
        System.out.println("\n🏢 ═══════════════ LISTE DES ÉDITEURS ═══════════════\n");

        List<Editeur> editeurs = plateforme.getEditeursList();

        if (editeurs.isEmpty()) {
            System.out.println("   Aucun éditeur enregistré.");
            return;
        }

        for (int i = 0; i < editeurs.size(); i++) {
            Editeur editeur = editeurs.get(i);
            System.out.printf("%d. %s %s\n",
                i + 1,
                editeur.getNom(),
                editeur.isEstIndependant() ? "🌟 (Indépendant)" : "");
            System.out.printf("   🎮 Jeux publiés: %d\n", editeur.getJeuxPublies().size());
            System.out.println();
        }
    }

    private static void afficherJeuxEditeur() {
        System.out.print("\n🔍 Entrez le nom de l'éditeur : ");
        scanner.nextLine();
        String nom = scanner.nextLine();

        Editeur editeur = plateforme.getEditeurByNom(nom);

        if (editeur == null) {
            System.out.println("❌ Éditeur introuvable.\n");
            return;
        }

        // Récupérer les jeux de cet éditeur depuis le catalogue de la plateforme
        List<JeuCatalogue> jeux = plateforme.getCatalogueList().stream()
            .filter(j -> j.getNomEditeur().equals(nom))
            .collect(java.util.stream.Collectors.toList());

        if (jeux.isEmpty()) {
            System.out.println("❌ Cet éditeur n'a pas encore publié de jeux.\n");
            return;
        }

        System.out.println("\n🎮 ═══════════════ JEUX PUBLIÉS ═══════════════");
        System.out.println("Éditeur: " + editeur.getNom() + "\n");

        for (int i = 0; i < jeux.size(); i++) {
            JeuCatalogue jeu = jeux.get(i);
            double noteMoyenne = evaluationDAO.getNoteMoyenne(jeu.getId());
            int nbEvaluations = evaluationDAO.countByJeuId(jeu.getId());

            System.out.printf("%d. %s (%s)\n", i + 1, jeu.getTitre(), jeu.getPlateforme());
            System.out.printf("   💰 Prix: %.2f€\n", jeu.getPrixActuel());

            if (nbEvaluations > 0) {
                System.out.printf("   ⭐ Note: %.1f/10\n", noteMoyenne);
            }
            System.out.println();
        }
    }

    // ===================================
    // MÉTHODES D'AFFICHAGE - INCIDENTS
    // ===================================

    private static void afficherTousLesIncidents() {
        System.out.println("\n🐛 ═══════════════ TOUS LES INCIDENTS ═══════════════\n");

        int totalIncidents = incidentDAO.count();

        if (totalIncidents == 0) {
            System.out.println("   ✅ Aucun incident signalé!");
            return;
        }

        System.out.printf("Total: %d incidents signalés\n\n", totalIncidents);

        // Afficher les incidents par jeu
        List<JeuCatalogue> jeux = plateforme.getCatalogueList();
        for (JeuCatalogue jeu : jeux) {
            List<RapportIncident> incidents = incidentDAO.findByJeuId(jeu.getId());
            if (!incidents.isEmpty()) {
                System.out.printf("🎮 %s : %d incident(s)\n", jeu.getTitre(), incidents.size());
            }
        }
        System.out.println();
    }

    private static void afficherIncidentsJeu() {
        System.out.print("\n🔍 Entrez le titre du jeu : ");
        scanner.nextLine();
        String titre = scanner.nextLine();

        List<JeuCatalogue> jeux = jeuDAO.findByTitre(titre);

        if (jeux.isEmpty()) {
            System.out.println("❌ Aucun jeu trouvé.\n");
            return;
        }

        JeuCatalogue jeu = jeux.get(0);
        List<RapportIncident> incidents = incidentDAO.findByJeuId(jeu.getId());

        if (incidents.isEmpty()) {
            System.out.println("✅ Aucun incident signalé pour ce jeu.\n");
            return;
        }

        System.out.println("\n🐛 ═══════════════ INCIDENTS ═══════════════");
        System.out.println("Jeu: " + jeu.getTitre() + "\n");

        for (int i = 0; i < incidents.size(); i++) {
            RapportIncident incident = incidents.get(i);
            System.out.printf("%d. Signalé par: %s\n", i + 1, incident.getJoueurPseudo());
            System.out.printf("   📌 Version: %s | Plateforme: %s\n",
                incident.getVersionJeu(), incident.getPlateforme());
            System.out.printf("   📝 %s\n", incident.getDescriptionErreur());
            System.out.println("─────────────────────────────────────────────");
        }
        System.out.println();
    }

    private static void signalerIncident() {
        System.out.println("\n🐛 ═══════════════ SIGNALER UN INCIDENT ═══════════════\n");

        scanner.nextLine();

        System.out.print("Titre du jeu : ");
        String titre = scanner.nextLine();

        List<JeuCatalogue> jeux = jeuDAO.findByTitre(titre);

        if (jeux.isEmpty()) {
            System.out.println("❌ Aucun jeu trouvé.\n");
            return;
        }

        JeuCatalogue jeu = jeux.get(0);

        System.out.print("Pseudo du joueur : ");
        String pseudo = scanner.nextLine();

        Joueur joueur = plateforme.getJoueurByPseudo(pseudo);
        if (joueur == null) {
            System.out.println("❌ Joueur introuvable.\n");
            return;
        }

        System.out.print("Version du jeu : ");
        String version = scanner.nextLine();

        System.out.print("Description de l'erreur : ");
        String description = scanner.nextLine();

        RapportIncident incident = new RapportIncident(
            pseudo, jeu.getId(), version, jeu.getPlateforme(), description
        );

        if (incidentDAO.insert(incident)) {
            // Ajouter aussi dans la plateforme en mémoire
            plateforme.signalerIncident(incident);
            System.out.println("✅ Incident signalé avec succès!\n");
        } else {
            System.out.println("❌ Erreur lors du signalement de l'incident.\n");
        }
    }

    // ===================================
    // AUTRES MÉTHODES
    // ===================================

    private static void afficherStatistiques() {
        System.out.println("\n📊 ═══════════════ STATISTIQUES POLYSTEAM ═══════════════\n");

        int nbEditeurs = editeurDAO.count();
        int nbJeux = jeuDAO.count();
        int nbJoueurs = joueurDAO.count();
        int nbIncidents = incidentDAO.count();

        System.out.printf("🏢 Éditeurs enregistrés : %d\n", nbEditeurs);
        System.out.printf("🎮 Jeux au catalogue : %d\n", nbJeux);
        System.out.printf("👥 Joueurs inscrits : %d\n", nbJoueurs);
        System.out.printf("🐛 Incidents signalés : %d\n", nbIncidents);

        // Jeu le mieux noté
        List<JeuCatalogue> jeux = plateforme.getCatalogueList();
        double meilleureNote = 0;
        JeuCatalogue meilleurJeu = null;

        for (JeuCatalogue jeu : jeux) {
            double note = evaluationDAO.getNoteMoyenne(jeu.getId());
            int nbEvals = evaluationDAO.countByJeuId(jeu.getId());
            if (nbEvals > 0 && note > meilleureNote) {
                meilleureNote = note;
                meilleurJeu = jeu;
            }
        }

        if (meilleurJeu != null) {
            System.out.printf("\n⭐ Jeu le mieux noté : %s (%.1f/10)\n",
                meilleurJeu.getTitre(), meilleureNote);
        }

        System.out.println();
    }

    private static void rechercherJeu() {
        System.out.print("\n🔍 Rechercher un jeu (titre) : ");
        scanner.nextLine();
        String titre = scanner.nextLine();

        List<JeuCatalogue> jeux = jeuDAO.findByTitre(titre);

        if (jeux.isEmpty()) {
            System.out.println("❌ Aucun jeu trouvé.\n");
            return;
        }

        System.out.println("\n📋 Résultats de la recherche :\n");

        for (int i = 0; i < jeux.size(); i++) {
            JeuCatalogue jeu = jeux.get(i);
            double noteMoyenne = evaluationDAO.getNoteMoyenne(jeu.getId());
            int nbEvaluations = evaluationDAO.countByJeuId(jeu.getId());

            System.out.printf("%d. %s\n", i + 1, jeu.getTitre());
            System.out.printf("   📝 %s | %s\n", jeu.getNomEditeur(), jeu.getPlateforme());
            System.out.printf("   💰 %.2f€", jeu.getPrixActuel());

            if (nbEvaluations > 0) {
                System.out.printf(" | ⭐ %.1f/10", noteMoyenne);
            }
            System.out.println("\n");
        }
    }

    /**
     * Lit le choix de l'utilisateur
     */
    private static int lireChoix() {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // Nettoyer le buffer
            return -1;
        }
    }
}

