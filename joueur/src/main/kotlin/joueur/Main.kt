import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)

    // 1. Initialisation avec votre compte de test
    // (Assurez-vous que ce joueur existe dans la table 'joueur')
    val joueurTest = Joueur("jamaljamal", "jamal", "jamal", "1980-05-07")
    val engine = Evenement(joueurTest)

    println("--- 🛡️ TEST KAFKA AVRO & SCHEMA REGISTRY ---")
    println("Joueur connecté : ${joueurTest.pseudo}")

    // 2. Sélection du jeu
    // Note : Le joueur doit POSSÉDER ce jeu dans 'jeu_possede'
    print("\nEntrez le titre du jeu (ex: FIFA 24) : ")
    val titre = sc.nextLine()

    print("Entrez la plateforme (ex: PC, PS5) : ")
    val plateforme = sc.nextLine()

    if (titre.isNotBlank() && plateforme.isNotBlank()) {
        println("\n🚀 Démarrage de la simulation...")
        println("📍 Serveur Kafka : 86.252.172.215:9092")
        println("📍 Schema Registry : 86.252.172.215:8081")
        println("--------------------------------------------------")

        // 3. Lancement de la boucle de jeu avec crash Avro
        engine.jouerAvecCrashAvro(titre, plateforme)

        println("\n--- FIN DU TEST ---")
    } else {
        println("❌ Erreur : Saisie incomplète.")
    }
}