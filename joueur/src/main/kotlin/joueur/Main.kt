import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)

    // 1. Initialisation avec ton joueur de test
    val joueurTest = Joueur("jamaljamal", "jamal", "jamal", "1980-05-07")
    val engine = Evenement(joueurTest)

    println("--- 🚀 TEST DU MODULE DE JEU ET CRASH KAFKA ---")
    println("Utilisateur : ${joueurTest.pseudo}")

    // 2. Choix du jeu à lancer
    // Note : Assure-toi d'avoir déjà acheté ce jeu avec la fonction précédente
    print("\nEntrez le titre du jeu à lancer : ")
    val titre = sc.nextLine()

    print("Entrez la plateforme (ex: PC, PS5) : ")
    val plateforme = sc.nextLine()

    if (titre.isNotBlank() && plateforme.isNotBlank()) {
        println("\n--- DÉMARRAGE DE LA SIMULATION ---")
        println("Le programme va ajouter 1h de jeu toutes les 5 secondes.")
        println("Il y a 20% de chance qu'un crash survienne à chaque cycle.")
        println("----------------------------------------------------------")

        // 3. Appel de la fonction avec Kafka
        engine.jouerAvecRisqueDeCrash(titre, plateforme)

        println("\n--- FIN DE LA SIMULATION ---")
    } else {
        println("Saisie invalide.")
    }
}