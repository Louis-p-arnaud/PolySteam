import model.Joueur
import service.Evenement
import java.util.Scanner
import infrastructure.UtilisationDuJoueur

fun main() {
    val sc = Scanner(System.`in`)

    println("--- 🧪 TEST DU SYSTÈME D'INSCRIPTION ---")

    // On crée un joueur "vide" ou temporaire pour accéder aux fonctions de l'engine
    val joueurTemp = Joueur("Invite", "", "", "2000-01-01")
    val engine = Evenement(joueurTemp)

    // 1. TEST DE L'INSCRIPTION
    println("\n📝 Création d'un nouveau compte :")
    print("Pseudo souhaité : ")
    val pseudo = sc.nextLine()
    print("Mot de passe (min 8 caractères) : ")
    val mdp = sc.nextLine()
    print("Nom : ")
    val nom = sc.nextLine()
    print("Prénom : ")
    val prenom = sc.nextLine()
    print("Date de naissance (AAAA-MM-JJ) : ")
    val dateN = sc.nextLine()

    // Appel de la fonction avec la logique JDBC (Unicité + Longueur MDP)
    val succes = engine.inscrireJoueur(pseudo, mdp, nom, prenom, dateN)

    if (succes) {
        println("\n✅ Test réussi : Le compte a été validé et inséré en base.")
        println("Vous pouvez maintenant lancer l'application complète.")

     /*   // Optionnel : Lancer l'interface utilisateur réelle
        println("\nSouhaitez-vous lancer l'interface PolySteam ? (o/n)")
        if (sc.nextLine().lowercase() == "o") {
            UtilisationDuJoueur.run()
        }*/
    } else {
        println("\n❌ Test échoué : Les conditions n'ont pas été remplies ou erreur SQL.")
        println("Vérifiez la console pour le détail de l'erreur.")
    }
}