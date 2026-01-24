package infrastructure

import model.Jeu
import model.Joueur
import service.Evenement
import java.util.Scanner

class UtilisationDuJoueur {
    companion object {

        @JvmStatic
        fun run() {
            val sc = Scanner(System.`in`)
            var utilisateurConnecte: Joueur? = null

            println("\n--- 🎮 Bienvenue sur PolySteam ---")

            // ÉTAPE 1 : AUTHENTIFICATION (Via Base de Données)
            while (utilisateurConnecte == null) {
                println("\n1. S'inscrire")
                println("2. Se connecter")
                println("3. Quitter")
                print("> ")

                when (sc.nextLine()) {
                    "1" -> {
                        println("--- Inscription (BD Commune) ---")
                        print("Pseudo : "); val pseudo = sc.nextLine()
                        print("Mot de passe (8 car. min) : "); val mdp = sc.nextLine()
                        print("Nom : "); val nom = sc.nextLine()
                        print("Prénom : "); val prenom = sc.nextLine()
                        print("Date de naissance (AAAA-MM-JJ) : "); val dateN = sc.nextLine()

                        // On utilise un moteur temporaire pour l'inscription
                        val engine = Evenement(Joueur(pseudo, nom, prenom, dateN))
                        val succes = engine.inscrireJoueur(pseudo, mdp, nom, prenom, dateN)

                        if (succes) {
                            println("✅ Inscription réussie. Veuillez vous connecter.")
                        }
                    }
                    "2" -> {
                        println("--- Connexion ---")
                        print("Pseudo : "); val pseudo = sc.nextLine()
                        print("Mot de passe : "); val mdp = sc.nextLine()

                        // Simulation de connexion via BD
                        // Idéalement : vérifier le pseudo et mdp avec un SELECT en BD
                        println("🔍 Vérification en base de données...")
                        utilisateurConnecte = Joueur(pseudo, "Nom", "Prenom", "2000-01-01")
                        println("✅ Connecté en tant que $pseudo")
                    }
                    "3" -> return
                }
            }

            // ÉTAPE 2 : MENU PRINCIPAL
            menuPrincipal(utilisateurConnecte!!, sc)
        }

        private fun menuPrincipal(joueur: Joueur, sc: Scanner) {
            val engine = Evenement(joueur)
            var continuer = true

            while (continuer) {
                println("\n--- MENU PRINCIPAL [${joueur.pseudo}] ---")
                println("1. Acquérir un jeu (BD)")
                println("2. Jouer à un jeu (Test Probabilité Crash / Kafka)")
                println("3. Évaluer un jeu (Condition 60 min)")
                println("4. Se déconnecter")
                print("> ")

                when (sc.nextLine()) {
                    "1" -> {
                        print("Nom du jeu à acheter : ")
                        val nom = sc.nextLine()
                        // Simulation d'un objet Jeu issu du catalogue
                        val jeuAchete = Jeu(java.util.UUID.randomUUID().toString(), nom, "EditeurID", "PC", "1.0")
                        engine.acheterJeuParTitreEtSupport(jeuAchete.titre,"PC")
                    }
                    "2" -> {
                        print("Quel jeu voulez-vous lancer ? ")
                        val nom = sc.nextLine()
                        // Simulation d'un jeu possédé avec 10% de chance de crash
                        val jeu = Jeu("uuid-123", nom, "Editeur-X", "PC", "1.2", 0.10)

                        // Cette fonction déclenchera un message Kafka uniquement en cas de crash
                        engine.jouer(jeu)
                    }
                    "3" -> {
                        print("ID du jeu à évaluer : ")
                        val id = sc.nextLine()
                        // On simule une récupération du temps de jeu depuis la table 'jeu_possede'
                        val tempsMinutes: Long = 75 // Exemple : le joueur a 75 min
                        engine.creerCommentaire(id, tempsMinutes)
                    }
                    "4" -> {
                        continuer = false
                        println("👋 Déconnexion...")
                    }
                }
            }
            run() // Retour à l'accueil
        }
    }
}