import model.*;
import kafka.IncidentEventProducer;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Classe principale de démonstration de la plateforme PolySteam
 */
public class test {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║      🎮 PLATEFORME POLYSTEAM 🎮           ║");
        System.out.println("║   Distribution de Jeux Vidéo en Ligne     ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        Plateforme polysteam = new Plateforme("PolySteam");

        // ====================================
        // 1. CRÉATION DES ÉDITEURS
        // ====================================
        System.out.println("\n📝 === ÉTAPE 1 : ENREGISTREMENT DES ÉDITEURS ===\n");

        Editeur konami = new Editeur("Konami Digital Entertainment", false);
        Editeur indieStudio = new Editeur("Pixel Dreams Studio", true);
        Editeur epicGames = new Editeur("Epic Games", false);

        polysteam.ajouterEditeur(konami);
        polysteam.ajouterEditeur(indieStudio);
        polysteam.ajouterEditeur(epicGames);

        // ====================================
        // 2. PUBLICATION DE JEUX
        // ====================================
        System.out.println("\n📝 === ÉTAPE 2 : PUBLICATION DE JEUX AU CATALOGUE ===\n");

        JeuCatalogue proEvolution = new JeuCatalogue(
            "Pro Evolution Soccer 2026",
            "Konami Digital Entertainment",
            "PC",
            Arrays.asList("Sports", "Simulation"),
            49.99
        );
        proEvolution.setId("1"); // ID temporaire pour test Kafka

        JeuCatalogue captainTsubasa = new JeuCatalogue(
            "Captain Tsubasa: New Kick Off",
            "Konami Digital Entertainment",
            "PC",
            Arrays.asList("Sports", "Action"),
            39.99
        );
        captainTsubasa.setId("2"); // ID temporaire pour test Kafka

        JeuCatalogue pixelQuest = new JeuCatalogue(
            "Pixel Quest Adventures",
            "Pixel Dreams Studio",
            "PC",
            Arrays.asList("Aventure", "Plateforme"),
            19.99
        );
        pixelQuest.setVersionActuelle("0.9.5"); // Accès anticipé
        pixelQuest.setId("3"); // ID temporaire pour test Kafka

        JeuCatalogue fortnite = new JeuCatalogue(
            "Fortnite Battle Royale",
            "Epic Games",
            "PC",
            Arrays.asList("Action", "Battle Royale"),
            0.0
        );
        fortnite.setId("4"); // ID temporaire pour test Kafka

        polysteam.publierJeu(proEvolution);
        polysteam.publierJeu(captainTsubasa);
        polysteam.publierJeu(pixelQuest);
        polysteam.publierJeu(fortnite);

        // ====================================
        // 3. INSCRIPTION DES JOUEURS
        // ====================================
        System.out.println("\n📝 === ÉTAPE 3 : INSCRIPTION DES JOUEURS ===\n");

        Joueur alice = new Joueur(
            "AliceGamer",
            "Dupont",
            "Alice",
            LocalDate.of(2000, 5, 15)
        );

        Joueur bob = new Joueur(
            "BobThePlayer",
            "Martin",
            "Bob",
            LocalDate.of(1998, 8, 22)
        );

        Joueur charlie = new Joueur(
            "CharliePro",
            "Durand",
            "Charlie",
            LocalDate.of(2002, 3, 10)
        );

        polysteam.inscrireJoueur(alice);
        polysteam.inscrireJoueur(bob);
        polysteam.inscrireJoueur(charlie);

        // ====================================
        // 3.5. AJOUT D'AMIS
        // ====================================
        System.out.println("\n📝 === ÉTAPE 3.5 : AJOUT D'AMIS ===\n");

        polysteam.ajouterAmi("AliceGamer", "BobThePlayer");
        polysteam.ajouterAmi("BobThePlayer", "CharliePro");
        polysteam.ajouterAmi("AliceGamer", "CharliePro");

        // ====================================
        // 4. ACHAT DE JEUX PAR LES JOUEURS
        // ====================================
        System.out.println("\n📝 === ÉTAPE 4 : ACHATS DE JEUX ===\n");

        polysteam.acheterJeu("AliceGamer", "Pro Evolution Soccer 2026");
        polysteam.acheterJeu("AliceGamer", "Pixel Quest Adventures");
        polysteam.acheterJeu("BobThePlayer", "Pro Evolution Soccer 2026");
        polysteam.acheterJeu("BobThePlayer", "Captain Tsubasa: New Kick Off");
        polysteam.acheterJeu("BobThePlayer", "Fortnite Battle Royale");
        polysteam.acheterJeu("CharliePro", "Pixel Quest Adventures");
        polysteam.acheterJeu("CharliePro", "Fortnite Battle Royale");

        // ====================================
        // 5. SESSIONS DE JEU
        // ====================================
        System.out.println("\n📝 === ÉTAPE 5 : SESSIONS DE JEU ===\n");

        polysteam.jouer("AliceGamer", "Pro Evolution Soccer 2026", 120); // 2h
        polysteam.jouer("AliceGamer", "Pixel Quest Adventures", 45);     // 45min
        polysteam.jouer("BobThePlayer", "Pro Evolution Soccer 2026", 300); // 5h
        polysteam.jouer("BobThePlayer", "Captain Tsubasa: New Kick Off", 90); // 1h30
        polysteam.jouer("BobThePlayer", "Fortnite Battle Royale", 180); // 3h
        polysteam.jouer("CharliePro", "Pixel Quest Adventures", 60); // 1h
        polysteam.jouer("CharliePro", "Fortnite Battle Royale", 240); // 4h

        // ====================================
        // 6. RAPPORTS D'INCIDENTS
        // ====================================
        System.out.println("\n📝 === ÉTAPE 6 : RAPPORTS D'INCIDENTS ===\n");

        RapportIncident incident1 = new RapportIncident(
            "Pro Evolution Soccer 2026",
            "1.0.0",
            "PC",
            "AliceGamer",
            "NullPointerException lors du chargement d'une partie multijoueur"
        );

        RapportIncident incident2 = new RapportIncident(
            "Pixel Quest Adventures",
            "0.9.5",
            "PC",
            "CharliePro",
            "Crash au niveau 3 après avoir récupéré la clé d'or"
        );

        RapportIncident incident3 = new RapportIncident(
            "Pro Evolution Soccer 2026",
            "1.0.0",
            "PC",
            "BobThePlayer",
            "Freeze de 5 secondes lors des célébrations de but"
        );

        polysteam.rapporterIncident(incident1);
        polysteam.rapporterIncident(incident2);
        polysteam.rapporterIncident(incident3);

        // ====================================
        // 7. ÉVALUATIONS PAR LES JOUEURS
        // ====================================
        System.out.println("\n📝 === ÉTAPE 7 : ÉVALUATIONS DES JEUX ===\n");

        polysteam.evaluerJeu("AliceGamer", "Pro Evolution Soccer 2026", 7,
            "Bon jeu de foot, mais quelques bugs en multijoueur.");

        polysteam.evaluerJeu("BobThePlayer", "Pro Evolution Soccer 2026", 9,
            "Excellent gameplay ! Le meilleur jeu de foot depuis des années.");

        polysteam.evaluerJeu("BobThePlayer", "Captain Tsubasa: New Kick Off", 8,
            "Très fun, fidèle au manga. Quelques soucis de caméra.");

        polysteam.evaluerJeu("AliceGamer", "Pixel Quest Adventures", 6,
            "Bon concept mais beaucoup de bugs en accès anticipé.");

        polysteam.evaluerJeu("CharliePro", "Pixel Quest Adventures", 7,
            "Prometteur mais instable. J'attends la version 1.0.");

        polysteam.evaluerJeu("BobThePlayer", "Fortnite Battle Royale", 9,
            "Addictif ! Très bon Battle Royale gratuit.");

        polysteam.evaluerJeu("CharliePro", "Fortnite Battle Royale", 8,
            "Super jeu, mais devient répétitif après plusieurs heures.");

        // ====================================
        // 8. VOTES SUR L'UTILITÉ DES ÉVALUATIONS
        // ====================================
        System.out.println("\n📝 === ÉTAPE 8 : VOTES SUR LES ÉVALUATIONS ===\n");

        polysteam.voterUtiliteEvaluation("Pro Evolution Soccer 2026", "BobThePlayer", true);
        polysteam.voterUtiliteEvaluation("Pro Evolution Soccer 2026", "BobThePlayer", true);
        polysteam.voterUtiliteEvaluation("Pro Evolution Soccer 2026", "AliceGamer", true);
        polysteam.voterUtiliteEvaluation("Pro Evolution Soccer 2026", "AliceGamer", false);
        polysteam.voterUtiliteEvaluation("Fortnite Battle Royale", "BobThePlayer", true);

        // ====================================
        // 9. PUBLICATION DE PATCHES
        // ====================================
        System.out.println("\n📝 === ÉTAPE 9 : PUBLICATION DE CORRECTIFS (PATCHES) ===\n");

        Patch patch1 = new Patch(
            "PATCH-PES-001",
            "Pro Evolution Soccer 2026",
            "PC",
            "1.1.0",
            "1.0.0",
            "Correction des bugs multijoueurs et optimisation"
        );
        patch1.ajouterModification(new Patch.Modification(
            Patch.TypeModification.CORRECTION,
            "Fix du NullPointerException en multijoueur"
        ));
        patch1.ajouterModification(new Patch.Modification(
            Patch.TypeModification.CORRECTION,
            "Résolution du freeze lors des célébrations"
        ));
        patch1.ajouterModification(new Patch.Modification(
            Patch.TypeModification.OPTIMISATION,
            "Amélioration des performances de 15%"
        ));

        polysteam.publierPatch(patch1);

        Patch patch2 = new Patch(
            "PATCH-PXQ-001",
            "Pixel Quest Adventures",
            "PC",
            "1.0.0",
            "0.9.5",
            "Sortie officielle ! Corrections majeures"
        );
        patch2.ajouterModification(new Patch.Modification(
            Patch.TypeModification.CORRECTION,
            "Fix du crash au niveau 3"
        ));
        patch2.ajouterModification(new Patch.Modification(
            Patch.TypeModification.AJOUT,
            "Ajout de 5 nouveaux niveaux"
        ));
        patch2.ajouterModification(new Patch.Modification(
            Patch.TypeModification.OPTIMISATION,
            "Optimisation de la consommation mémoire"
        ));

        polysteam.publierPatch(patch2);

        // ====================================
        // 10. PUBLICATION D'EXTENSIONS (DLC)
        // ====================================
        System.out.println("\n📝 === ÉTAPE 10 : PUBLICATION D'EXTENSIONS ===\n");

        Extension dlc1 = new Extension(
            "Master League Deluxe",
            19.99,
            "1.0.0",
            "Pro Evolution Soccer 2026"
        );

        Extension dlc2 = new Extension(
            "Desert Kingdom Chapter",
            9.99,
            "1.0.0",
            "Pixel Quest Adventures"
        );

        Extension dlc3 = new Extension(
            "Battle Pass Season 5",
            14.99,
            "1.0.0",
            "Fortnite Battle Royale"
        );

        polysteam.publierExtension(dlc1);
        polysteam.publierExtension(dlc2);
        polysteam.publierExtension(dlc3);

        // ====================================
        // 11. ACHAT D'EXTENSIONS
        // ====================================
        System.out.println("\n📝 === ÉTAPE 11 : ACHATS D'EXTENSIONS ===\n");

        polysteam.acheterExtension("BobThePlayer", "Pro Evolution Soccer 2026", "Master League Deluxe");
        polysteam.acheterExtension("CharliePro", "Pixel Quest Adventures", "Desert Kingdom Chapter");
        polysteam.acheterExtension("BobThePlayer", "Fortnite Battle Royale", "Battle Pass Season 5");

        // ====================================
        // 11.5. ÉVALUATION D'EXTENSIONS (DLC)
        // ====================================
        System.out.println("\n📝 === ÉTAPE 11.5 : ÉVALUATIONS D'EXTENSIONS ===\n");

        polysteam.evaluerExtension("BobThePlayer", "Pro Evolution Soccer 2026", "Master League Deluxe", 8,
            "Excellent mode de jeu, beaucoup de profondeur !");

        polysteam.evaluerExtension("CharliePro", "Pixel Quest Adventures", "Desert Kingdom Chapter", 7,
            "Bons nouveaux niveaux, mais un peu court.");

        // ====================================
        // 12. AFFICHAGE DU CATALOGUE
        // ====================================
        System.out.println("\n📝 === AFFICHAGE DU CATALOGUE COMPLET ===\n");
        polysteam.afficherCatalogue();

        // ====================================
        // 13. PAGES DESCRIPTIVES
        // ====================================
        System.out.println("\n📝 === PAGES DESCRIPTIVES ===\n");

        // Page d'un éditeur
        polysteam.afficherPageEditeur("Konami Digital Entertainment");

        // Page d'un jeu
        polysteam.afficherPageJeu("Pro Evolution Soccer 2026");

        // Profils de joueurs avec différentes vues
        System.out.println("\n--- Vue propriétaire ---");
        polysteam.afficherProfilJoueur("AliceGamer", "AliceGamer");
        
        System.out.println("\n--- Vue ami ---");
        polysteam.afficherProfilJoueur("BobThePlayer", "AliceGamer");
        
        System.out.println("\n--- Vue publique ---");
        polysteam.afficherProfilJoueur("CharliePro", null);

        // ====================================
        // 14. RAPPORTS D'INCIDENTS PAR JEU
        // ====================================
        System.out.println("\n📝 === CONSULTATION DES RAPPORTS D'INCIDENTS ===\n");
        polysteam.afficherRapportsIncidentsPourJeu("Pro Evolution Soccer 2026");

        // ====================================
        // TEST KAFKA - PUBLICATION D'ÉVÉNEMENTS
        // ====================================
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║      📤 TEST KAFKA - ÉVÉNEMENTS 📤         ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        try {
            System.out.println("🔌 Initialisation du Kafka Producer...");
            IncidentEventProducer kafkaProducer = new IncidentEventProducer();

            // Test 1 : Publier un incident pour Pro Evolution Soccer
            System.out.println("\n📤 Test 1 : Publication d'un incident critique...");
            kafkaProducer.publierIncident(
                proEvolution.getId(),                    // jeuId
                proEvolution.getTitre(),                 // titreJeu
                "1",                                     // editeurId (Konami)
                "1.0.5",                                 // versionJeu
                "PC",                                    // plateforme
                "MaximilienTest",                        // pseudoJoueur
                "CRASH",                                 // typeIncident
                "Crash au démarrage après la mise à jour 1.0.5"
            );


            // Test 2 : Publier un incident pour Captain Tsubasa
            System.out.println("\n📤 Test 2 : Publication d'un second incident...");
            kafkaProducer.publierIncident(
                captainTsubasa.getId(),                  // jeuId
                captainTsubasa.getTitre(),               // titreJeu
                "1",                                     // editeurId (Konami)
                "2.1.0",                                 // versionJeu
                "PS5",                                   // plateforme
                "AlexTest",                              // pseudoJoueur
                "ERREUR_GRAPHIQUE",                      // typeIncident
                "Bug graphique pendant les animations de tir"
            );


            // Test 3 : Publier un incident pour un autre éditeur
            System.out.println("\n📤 Test 3 : Publication d'un incident pour un jeu indie...");
            kafkaProducer.publierIncident(
                pixelQuest.getId(),                 // jeuId
                pixelQuest.getTitre(),              // titreJeu
                "2",                                     // editeurId (Pixel Dreams Studio)
                "0.9.2",                                 // versionJeu
                "PC",                                    // plateforme
                "JulieTest",                             // pseudoJoueur
                "ERREUR_SAUVEGARDE",                     // typeIncident
                "Sauvegarde corrompue après 10h de jeu"
            );


            System.out.println("\n✅ Tous les événements ont été publiés sur Kafka !");
            System.out.println("💡 Vérifiez le topic 'plateforme.incidents' sur Kafka UI");

            // Fermer proprement le producer
            kafkaProducer.close();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test Kafka: " + e.getMessage());
            System.err.println("💡 Assurez-vous que Kafka est démarré avec: docker-compose -f docker-compose-kafka.yml up -d");
            e.printStackTrace();
        }

        // ====================================
        // RÉSUMÉ FINAL
        // ====================================
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║           📊 RÉSUMÉ POLYSTEAM 📊           ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println("✅ Joueurs inscrits : " + polysteam.getJoueurs().size());
        System.out.println("✅ Jeux au catalogue : " + polysteam.getCatalogue().size());
        System.out.println("✅ Démonstration complète terminée !");
        System.out.println("\n🎮 Merci d'avoir testé PolySteam ! 🎮\n");
    }
}

