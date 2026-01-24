import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)
    val j = Joueur("jamaljamal", "jamal", "jamal", "1980-05-07")
    val engine = Evenement(j)

    println("--- 🔍 RECHERCHE CATALOGUE ---")
    print("Entrez le titre du jeu : ")
    val titre = sc.nextLine()

    if (titre.isNotBlank()) {
        // Affiche toutes les versions (PC, PS5, etc.)
        engine.afficherFicheJeuParTitre(titre)

        println("\nSouhaitez-vous acheter une de ces versions ? (o/n)")
        if (sc.nextLine().lowercase() == "o") {
            print("Précisez le support souhaité : ")
            val support = sc.nextLine()
            engine.acheterJeuParTitreEtSupport(titre, support)
        }
    }
}