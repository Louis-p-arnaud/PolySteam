import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)

    // Initialisation avec ton compte qui fonctionne
    val joueur = Joueur("jamaljamal", "jamal", "jamal", "1980-05-07")
    val engine = Evenement(joueur)

    println("--- 🎮 BOUTIQUE POLYSTEAM ---")
    println("Connecté en tant que : ${joueur.pseudo}")

    print("\nEntrez le TITRE du jeu que vous voulez acheter : ")
    val titreSaisi = sc.nextLine()

    if (titreSaisi.isNotBlank()) {
        val succes = engine.acheterJeuParTitre(titreSaisi)

        if (succes) {
            println("✅ Félicitations ! Allez voir sur pgAdmin (table jeu_possede).")
        }
    } else {
        println("Titre vide, abandon.")
    }
}