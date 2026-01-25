import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)
    var utilisateurLogge: Joueur? = null

    println("--- 🎮 BIENVENUE SUR POLYSTEAM ---")

    // --- ÉTAPE 1 : AUTHENTIFICATION ---
    while (utilisateurLogge == null) {
        println("\n1. Se connecter")
        println("2. Créer un compte")
        println("3. Quitter")
        print("👉 Choix : ")

        when (scanner.nextLine()) {
            "1" -> {
                print("Pseudo : ")
                val p = scanner.nextLine()
                print("Mot de passe : ")
                val m = scanner.nextLine()
                utilisateurLogge = Evenement(Joueur("guest", "", "", "")).seConnecter(p, m)
            }
            "2" -> {
                println("\n--- ✨ INSCRIPTION ---")
                print("Pseudo : "); val pseudo = scanner.nextLine()
                print("MDP (8 car. min) : "); val mdp = scanner.nextLine()
                print("Nom : "); val nom = scanner.nextLine()
                print("Prénom : "); val prenom = scanner.nextLine()
                print("Date Naissance (AAAA-MM-JJ) : "); val date = scanner.nextLine()

                val tempEngine = Evenement(Joueur(pseudo, nom, prenom, date))
                if (tempEngine.inscrireJoueur(pseudo, mdp, nom, prenom, date)) {
                    utilisateurLogge = Joueur(pseudo, nom, prenom, date)
                }
            }
            "3" -> return
            else -> println("❌ Choix invalide.")
        }
    }

    // --- ÉTAPE 2 : NAVIGATION PRINCIPALE ---
    val service = Evenement(utilisateurLogge!!)
    var enCours = true

    while (enCours) {
        println("\n========= 🏠 MENU PRINCIPAL - ${utilisateurLogge.pseudo.uppercase()} =========")
        println("1. 🎮 Ma Bibliothèque (Jouer, Mettre à jour)")
        println("2. 🛒 Boutique (Acheter, Voir fiches jeux)")
        println("3. 👥 Réseau Social (Amis, Profils, Demandes)")
        println("4. 👤 Mon Profil (Infos, Mes évaluations)")
        println("5. 🚪 Se déconnecter")
        print("👉 Action : ")

        when (scanner.nextLine()) {
            "1" -> menuBibliotheque(service, scanner)
            "2" -> menuBoutique(service, scanner)
            "3" -> menuSocial(service, scanner)
            "4" -> service.afficherProfilUtilisateur(utilisateurLogge.pseudo)
            "5" -> {
                println("👋 Déconnexion..."); enCours = false
            }
            else -> println("❌ Option inconnue.")
        }
    }
}

// --- SOUS-MENU : BIBLIOTHÈQUE ---
fun menuBibliotheque(service: Evenement, scanner: Scanner) {
    service.afficherJeuxPossedes()
    println("\n[1] Lancer un jeu | [2] Mettre à jour un jeu | [0] Retour")
    print("👉 Choix : ")
    when (scanner.nextLine()) {
        "1" -> {
            print("Titre du jeu : "); val t = scanner.nextLine()
            print("Plateforme : "); val p = scanner.nextLine()
            service.jouerAvecCrashAvro(t, p)
        }
        "2" -> {
            print("Titre du jeu : "); val t = scanner.nextLine()
            service.mettreAJourJeu(t)
        }
    }
}

// --- SOUS-MENU : BOUTIQUE ---
fun menuBoutique(service: Evenement, scanner: Scanner) {
    println("\n--- 🛒 BOUTIQUE POLYSTEAM ---")
    println("[1] Voir la fiche d'un jeu | [2] Acheter un jeu | [0] Retour")
    print("👉 Choix : ")
    when (scanner.nextLine()) {
        "1" -> {
            print("Titre du jeu : "); val t = scanner.nextLine()
            service.afficherFicheJeuParTitre(t)
        }
        "2" -> {
            print("Titre du jeu : "); val t = scanner.nextLine()
            print("Plateforme : "); val p = scanner.nextLine()
            service.acheterJeuParTitreEtSupport(t, p)
        }
    }
}

// --- SOUS-MENU : RÉSEAU SOCIAL ---
fun menuSocial(service: Evenement, scanner: Scanner) {
    println("\n--- 👥 ESPACE SOCIAL ---")
    println("1. Voir ma liste d'amis")
    println("2. Envoyer une demande d'ami")
    println("3. Accepter une demande")
    println("4. Rechercher et voir un profil")
    println("0. Retour")
    print("👉 Choix : ")

    when (scanner.nextLine()) {
        "1" -> service.afficherListeAmi()
        "2" -> {
            print("Pseudo du destinataire : "); val p = scanner.nextLine()
            service.envoyerDemandeAmi(p)
        }
        "3" -> {
            print("Pseudo de l'expéditeur : "); val p = scanner.nextLine()
            service.accepterDemandeAmi(p)
        }
        "4" -> {
            print("Pseudo à rechercher : "); val p = scanner.nextLine()
            service.afficherProfilUtilisateur(p)

            // Logique contextuelle : après avoir vu le profil, on propose d'évaluer une de ses évals
            println("\n[1] Voter pour une évaluation de ce joueur | [0] Retour")
            if (scanner.nextLine() == "1") {
                print("Titre du jeu concerné : "); val t = scanner.nextLine()
                print("Est-ce utile ? (O/N) : ")
                val vote = scanner.nextLine().uppercase() == "O"
                service.voterEvaluationParCible(t, p, vote)
            }
        }
    }
}