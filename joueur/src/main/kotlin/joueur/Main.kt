import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)
    val j = Joueur("jamaljamal", "jamal", "jamal", "1980-05-07")
    val engine = Evenement(j)

    println("--- 🏢 RECHERCHE ÉDITEUR ---")
    print("Entrez le nom de l'éditeur (ex: Ubisoft, EA, etc.) : ")
    val nomE = sc.nextLine()

    if (nomE.isNotBlank()) {
        engine.afficherFicheEditeur(nomE)
    }
}