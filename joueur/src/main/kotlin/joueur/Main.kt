import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)

    // On initialise l'engine (le joueur temporaire sera remplacé après l'inscription)
    var engine = Evenement(Joueur("temp", "", "", ""))

    println("--- ✨ FORMULAIRE D'INSCRIPTION POLYSTEAM ---")

    print("Pseudo souhaité : ")
    val pseudo = sc.nextLine()

    print("Mot de passe : ")
    val motDePasse = sc.nextLine()

    print("Nom : ")
    val nom = sc.nextLine()

    print("Prénom : ")
    val prenom = sc.nextLine()

    print("Date de naissance (AAAA-MM-JJ) : ")
    val dateNais = sc.nextLine()

    // 1. Appel de ta fonction existante
    // (J'assume qu'elle renvoie un Boolean ou qu'elle lance une exception en cas d'erreur)
    try {
        engine.inscrireJoueur(pseudo,motDePasse , nom, prenom, dateNais)

        // 2. Mise à jour de l'objet Joueur pour la suite de la session
        val nouveauJoueur = Joueur(pseudo, nom, prenom, dateNais)
        engine = Evenement(nouveauJoueur)

        println("\n--- 👥 TROUVER DES AMIS ---")
        print("Souhaitez-vous envoyer une demande d'ami ? (o/n) : ")

        if (sc.nextLine().lowercase() == "o") {
            print("Pseudo du joueur à ajouter : ")
            val pseudoAmi = sc.nextLine()

            if (pseudoAmi.isNotBlank() && pseudoAmi != pseudo) {
                engine.envoyerDemandeAmi(pseudoAmi)
            } else {
                println("⚠️ Action impossible (pseudo vide ou identique au vôtre).")
            }
        }
    } catch (e: Exception) {
        println("❌ Échec de l'inscription : ${e.message}")
    }

    println("\nFin de la procédure.")
}