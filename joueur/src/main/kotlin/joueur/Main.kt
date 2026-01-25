import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val sc = Scanner(System.`in`)
    var engine: Evenement? = null
    var utilisateurLogge: Joueur? = null

    println("--- 🎮 BIENVENUE SUR POLYSTEAM ---")
    println("1. Se connecter")
    println("2. S'inscrire")
    print("Votre choix : ")

    val choix = sc.nextLine()

    if (choix == "1") {
        print("Pseudo : ")
        val p = sc.nextLine()
        print("Mot de passe : ")
        val m = sc.nextLine()

        // On utilise un engine temporaire pour appeler la connexion
        val tempEngine = Evenement(Joueur("guest", "", "", ""))
        utilisateurLogge = tempEngine.seConnecter(p, m)
    } else if (choix == "2") {
        println("\n--- ✨ CRÉATION DE COMPTE ---")
        print("Pseudo souhaité : ")
        val pseudo = sc.nextLine()

        print("Mot de passe (8 caractères min) : ")
        val mdp = sc.nextLine()

        print("Nom : ")
        val nom = sc.nextLine()

        print("Prénom : ")
        val prenom = sc.nextLine()

        print("Date de naissance (AAAA-MM-JJ) : ")
        val dateNais = sc.nextLine()

        // On utilise un engine temporaire pour l'inscription
        val tempEngine = Evenement(Joueur(pseudo, nom, prenom, dateNais))

        // Appelle ta fonction sécurisée avec les 5 paramètres
        val succes = tempEngine.inscrireJoueur(pseudo, mdp, nom, prenom, dateNais)

        if (succes) {
            // Si l'inscription réussit, on connecte l'utilisateur automatiquement
            utilisateurLogge = Joueur(pseudo, nom, prenom, dateNais)
        } else {
            println("❌ Impossible de créer le compte. Retour au menu.")
        }
    }

    // Si la connexion a réussi, on lance le menu principal
    if (utilisateurLogge != null) {
        engine = Evenement(utilisateurLogge)
        // Lancer la boucle du menu principal ici...
        println("\nChargement de votre bibliothèque...")
        engine.afficherJeuxPossedes()
    } else {
        println("Fin du programme.")
    }
}