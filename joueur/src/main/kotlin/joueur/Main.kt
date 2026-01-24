import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)

    val engine = Evenement(Joueur("jamaljamal", "jamal", "jamal", "1980-05-07"))

    // Affiche la liste des jeux possédés par jamaljamal
    engine.afficherJeuxPossedes()
}