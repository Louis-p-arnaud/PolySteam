import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)

    // 1. Initialisation du joueur (on simule une connexion)
    val monPseudo = "jamaljamal"
    val joueurConnecte = Joueur(monPseudo, "Jamal", "Ben", "1990-01-01")
    val engine = Evenement(joueurConnecte)

    var continuer = true

    while (continuer) {
        println("\n--- 🎮 POLYSTEAM : MENU JOUEUR ---")
        println("Connecté en tant que : $monPseudo")
        println("1. Voir mon profil (Jeux, temps, avis)")
        println("2. Rechercher un autre profil")
        println("3. Lancer un jeu (Simulation + Crash Avro)")
        println("4. Voter pour une évaluation (Like/Dislike)")
        println("5. Quitter")
        print("\nVotre choix : ")

        when (sc.nextLine()) {
            "1" -> {
                engine.afficherProfilUtilisateur(monPseudo)
            }
            "2" -> {
                print("Entrez le pseudo à rechercher : ")
                val cible = sc.nextLine()
                engine.afficherProfilUtilisateur(cible)
            }
            "3" -> {
                print("Titre du jeu : ")
                val titre = sc.nextLine()
                print("Plateforme (PC, PS5, Switch) : ")
                val plateforme = sc.nextLine()

                if (titre.isNotBlank() && plateforme.isNotBlank()) {
                    println("\nLancement de la session...")
                    engine.jouerAvecCrashAvro(titre, plateforme)
                } else {
                    println("⚠️ Saisie invalide.")
                }
            }
            "4" -> {
                println("\n--- 👍 SYSTÈME DE VOTE ---")
                print("Titre du jeu concerné : ")
                val titre = sc.nextLine()
                print("Pseudo de l'auteur du commentaire : ")
                val auteur = sc.nextLine()

                print("Votre vote (1: Like 👍 / 2: Dislike 👎) : ")
                val choix = sc.nextLine() // On utilise nextLine pour éviter les bugs de buffer

                if (titre.isNotBlank() && auteur.isNotBlank()) {
                    engine.voterEvaluationParCible(titre, auteur, choix == "1")
                } else {
                    println("⚠️ Saisie incomplète.")
                }
            }
            "5" -> {
                println("Fermeture de PolySteam. Au revoir !")
                continuer = false
            }
            else -> println("❌ Option invalide, réessayez.")
        }
    }
}