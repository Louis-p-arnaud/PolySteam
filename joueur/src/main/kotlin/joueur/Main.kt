import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val engine = Evenement(Joueur("jamaljamal", "jamal", "jamal", "1980-05-07"))

    println("Quel profil voulez-vous consulter ?")
    val scanner = java.util.Scanner(System.`in`)
    val pseudo = scanner.nextLine()

    engine.afficherProfilUtilisateur(pseudo)
}