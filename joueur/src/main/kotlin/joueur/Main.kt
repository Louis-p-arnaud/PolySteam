import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)
    val joueur = Joueur("jamaljamal", "jamal", "jamal", "1980-05-07")
    val engine = Evenement(joueur)

    println("--- 🎮 BOUTIQUE POLYSTEAM (Vérification Support) ---")

    print("Titre du jeu : ")
    val titre = sc.nextLine()

    print("Support (PC, PS5, Switch, etc.) : ")
    val support = sc.nextLine()

    if (titre.isNotBlank() && support.isNotBlank()) {
        engine.acheterJeuParTitreEtSupport(titre, support)
    } else {
        println("Saisie incomplète.")
    }
}