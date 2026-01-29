import model.Joueur
import service.Evenement
import java.util.Scanner

fun main() {
    val scanner = Scanner(System.`in`)

    afficherBanniere()

    // Authentification
    val utilisateurLogge = menuAuthentification(scanner) ?: return

    // Menu principal
    menuPrincipal(utilisateurLogge, scanner)

    println("\n✨ Merci d'avoir utilisé PolySteam ! À bientôt ! ✨")
}

// ═══════════════════════════════════════════════════════════════════
// AUTHENTIFICATION
// ═══════════════════════════════════════════════════════════════════

fun afficherBanniere() {
    println("""
        ╔════════════════════════════════════════════════════╗
        ║                                                    ║
        ║          🎮  BIENVENUE SUR POLYSTEAM  🎮          ║
        ║                                                    ║
        ║        Votre plateforme de jeux vidéo !           ║
        ║                                                    ║
        ╚════════════════════════════════════════════════════╝
    """.trimIndent())
}

fun menuAuthentification(scanner: Scanner): Joueur? {
    while (true) {
        println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("           AUTHENTIFICATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("1. 🔑 Se connecter")
        println("2. ✨ Créer un compte")
        println("3. 🚪 Quitter")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        print("👉 Votre choix : ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                val joueur = seConnecter(scanner)
                if (joueur != null) return joueur
            }
            "2" -> {
                val joueur = creerCompte(scanner)
                if (joueur != null) return joueur
            }
            "3" -> {
                println("\n👋 Au revoir !")
                return null
            }
            else -> println("❌ Choix invalide. Veuillez saisir 1, 2 ou 3.")
        }
    }
}

fun seConnecter(scanner: Scanner): Joueur? {
    println("\n┌─────────────────────────────────────┐")
    println("│       🔑 CONNEXION                  │")
    println("└─────────────────────────────────────┘")

    print("Pseudo : ")
    val pseudo = scanner.nextLine().trim()

    if (pseudo.isEmpty()) {
        println("❌ Le pseudo ne peut pas être vide.")
        return null
    }

    print("Mot de passe : ")
    val mdp = scanner.nextLine()

    val joueur = Evenement(Joueur("guest", "", "", "")).seConnecter(pseudo, mdp)

    if (joueur != null) {
        println("✅ Connexion réussie ! Bienvenue ${joueur.pseudo} !")
    }

    return joueur
}

fun creerCompte(scanner: Scanner): Joueur? {
    println("\n┌─────────────────────────────────────┐")
    println("│       ✨ INSCRIPTION                │")
    println("└─────────────────────────────────────┘")
    println("ℹ️  Veuillez remplir tous les champs.")
    println()

    print("Pseudo (unique) : ")
    val pseudo = scanner.nextLine().trim()

    if (pseudo.isEmpty()) {
        println("❌ Le pseudo ne peut pas être vide.")
        return null
    }

    print("Mot de passe (min. 8 caractères) : ")
    val mdp = scanner.nextLine()

    if (mdp.length < 8) {
        println("❌ Le mot de passe doit contenir au moins 8 caractères.")
        return null
    }

    print("Nom : ")
    val nom = scanner.nextLine().trim()

    print("Prénom : ")
    val prenom = scanner.nextLine().trim()

    print("Date de naissance (AAAA-MM-JJ) : ")
    val date = scanner.nextLine().trim()

    val tempEngine = Evenement(Joueur(pseudo, nom, prenom, date))

    return if (tempEngine.inscrireJoueur(pseudo, mdp, nom, prenom, date)) {
        println("✅ Inscription réussie ! Bienvenue ${pseudo} !")
        Joueur(pseudo, nom, prenom, date)
    } else {
        null
    }
}

// ═══════════════════════════════════════════════════════════════════
// MENU PRINCIPAL
// ═══════════════════════════════════════════════════════════════════

fun menuPrincipal(utilisateur: Joueur, scanner: Scanner) {
    val service = Evenement(utilisateur)
    var continuer = true

    while (continuer) {
        println("\n╔════════════════════════════════════════════════════╗")
        println("║       🏠 MENU PRINCIPAL - ${utilisateur.pseudo.uppercase().padEnd(19)}║")
        println("╚════════════════════════════════════════════════════╝")
        println()
        println("1. 🎮 Ma Bibliothèque")
        println("      └─ Gérer mes jeux, jouer, mettre à jour")
        println()
        println("2. 🛒 Boutique")
        println("      └─ Découvrir et acheter des jeux, gérer ma wishlist")
        println()
        println("3. 👥 Réseau Social")
        println("      └─ Gérer mes amis et consulter des profils")
        println()
        println("4. 👤 Mon Profil")
        println("      └─ Voir mes informations et évaluations")
        println()
        println("5. 📱 Voir mon flux d'actualité")
        println()
        println("6. 🚪 Se déconnecter")
        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        print("👉 Choisissez une option (1-6) : ")

        when (scanner.nextLine().trim()) {
            "1" -> menuBibliotheque(service, scanner)
            "2" -> menuBoutique(service, scanner)
            "3" -> menuSocial(service, scanner)
            "4" -> menuProfil(service, utilisateur, scanner)
            "5" -> {
                service.afficherFluxActualite()
                attendreUtilisateur(scanner)
            }
            "6" -> {
                println("\n👋 Déconnexion en cours...")
                continuer = false
            }
            else -> println("❌ Option invalide. Veuillez choisir entre 1 et 5.")
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MENU : BIBLIOTHÈQUE
// ═══════════════════════════════════════════════════════════════════

fun menuBibliotheque(service: Evenement, scanner: Scanner) {
    var continuer = true

    while (continuer) {
        println("\n┌────────────────────────────────────────────────┐")
        println("│          🎮 MA BIBLIOTHÈQUE                    │")
        println("└────────────────────────────────────────────────┘")

        service.afficherJeuxPossedes()

        println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Que souhaitez-vous faire ?")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("1. 🎯 Lancer un jeu")
        println("2. ⬆️  Mettre à jour un jeu")
        println("0. ↩️  Retour au menu principal")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        print("👉 Votre choix : ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                println("\n🎯 LANCER UN JEU")
                print("Titre du jeu : ")
                val titre = scanner.nextLine().trim()

                if (titre.isEmpty()) {
                    println("❌ Le titre ne peut pas être vide.")
                } else {
                    print("Plateforme (PC/PS5/Xbox/Switch) : ")
                    val plateforme = scanner.nextLine().trim()

                    if (plateforme.isEmpty()) {
                        println("❌ La plateforme ne peut pas être vide.")
                    } else {
                        service.jouerAvecCrashAvro(titre, plateforme)
                    }
                }

                attendreUtilisateur(scanner)
            }
            "2" -> {
                println("\n⬆️  MISE À JOUR DE JEU")
                print("Titre du jeu à mettre à jour : ")
                val titre = scanner.nextLine().trim()

                if (titre.isEmpty()) {
                    println("❌ Le titre ne peut pas être vide.")
                } else {
                    service.mettreAJourJeu(titre)
                }

                attendreUtilisateur(scanner)
            }
            "0" -> {
                continuer = false
                println("↩️  Retour au menu principal...")
            }
            else -> println("❌ Option invalide. Veuillez choisir 1, 2 ou 0.")
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MENU : BOUTIQUE
// ═══════════════════════════════════════════════════════════════════

fun menuBoutique(service: Evenement, scanner: Scanner) {
    var continuer = true
    while (continuer) {
        println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("             🛒 BOUTIQUE POLYSTEAM")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("1. 📚 Voir tous les jeux disponibles") // Nouvelle option
        println("2. 🔍 Voir la fiche détaillée d'un jeu")
        println("3. 💳 Acheter un jeu")
        println("4. 💖 Gérer ma Wishlist")
        println("5. 🏢 En savoir plus sur un Éditeur")
        println("0. ↩️  Retour")
        print("\n👉 Choix : ")

        when (scanner.nextLine()) {
            "1" -> {
                service.afficherCatalogueTitres()
                attendreUtilisateur(scanner)
            }
            "2" -> {
                print("Titre du jeu : ")
                val t = scanner.nextLine()
                service.afficherFicheJeuParTitre(t)
                attendreUtilisateur(scanner)
            }
            "3" -> {
                print("Titre du jeu : ")
                val t = scanner.nextLine()
                print("Plateforme : ")
                val p = scanner.nextLine()
                service.acheterJeuParTitreEtSupport(t, p)
                attendreUtilisateur(scanner)
            }
            "4" -> menuWishlist(service, scanner)
            "5" -> {
                print("Nom de l'éditeur : ")
                val nom = scanner.nextLine()
                service.consulterEditeur(nom)
                attendreUtilisateur(scanner)
            }
            "0" -> continuer = false
            else -> println("❌ Option invalide.")
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MENU : RÉSEAU SOCIAL
// ═══════════════════════════════════════════════════════════════════

fun menuSocial(service: Evenement, scanner: Scanner) {
    var continuer = true

    while (continuer) {
        println("\n┌────────────────────────────────────────────────┐")
        println("│          👥 RÉSEAU SOCIAL                      │")
        println("└────────────────────────────────────────────────┘")
        println("\nGérez vos amis et consultez des profils !")
        println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Que souhaitez-vous faire ?")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("1. 👫 Voir ma liste d'amis")
        println("2. ➕ Envoyer une demande d'ami")
        println("3. ✅ Accepter une demande d'ami")
        println("4. 🔍 Rechercher et consulter un profil")
        println("0. ↩️  Retour au menu principal")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        print("👉 Votre choix : ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                println("\n👫 MA LISTE D'AMIS")
                service.afficherListeAmi()
                attendreUtilisateur(scanner)
            }
            "2" -> {
                println("\n➕ ENVOYER UNE DEMANDE D'AMI")
                print("Pseudo du joueur à ajouter : ")
                val pseudo = scanner.nextLine().trim()

                if (pseudo.isEmpty()) {
                    println("❌ Le pseudo ne peut pas être vide.")
                } else {
                    service.envoyerDemandeAmi(pseudo)
                }

                attendreUtilisateur(scanner)
            }
            "3" -> {
                val ilYADesDemandes = service.consulterDemandeAmi()

                println("\n✅ ACCEPTER UNE DEMANDE D'AMI")

                print("Pseudo de l'expéditeur : ")
                val pseudo = scanner.nextLine().trim()

                if (pseudo.isEmpty()) {
                    println("❌ Le pseudo ne peut pas être vide.")
                } else {
                    service.accepterDemandeAmi(pseudo)
                }

                attendreUtilisateur(scanner)
            }
            "4" -> {
                println("\n🔍 RECHERCHER UN PROFIL")
                print("Pseudo à rechercher : ")
                val pseudo = scanner.nextLine().trim()

                if (pseudo.isEmpty()) {
                    println("❌ Le pseudo ne peut pas être vide.")
                } else {
                    val existe = service.afficherProfilUtilisateur(pseudo)

                    if (existe) {
                        menuActionsProfilJoueur(service, pseudo, scanner)
                    } else {
                        println("❌ Profil introuvable.")
                    }
                }

                attendreUtilisateur(scanner)
            }
            "0" -> {
                continuer = false
                println("↩️  Retour au menu principal...")
            }
            else -> println("❌ Option invalide. Veuillez choisir entre 0 et 4.")
        }
    }
}

fun menuActionsProfilJoueur(service: Evenement, pseudoCible: String, scanner: Scanner) {
    println("\n┌────────────────────────────────────────────────┐")
    println("│     ACTIONS SUR LE PROFIL DE $pseudoCible")
    println("└────────────────────────────────────────────────┘")
    println("1. 👍 Voter pour une évaluation de ce joueur")
    println("0. ↩️  Retour")
    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("👉 Votre choix : ")

    when (scanner.nextLine().trim()) {
        "1" -> {
            println("\n👍 VOTER POUR UNE ÉVALUATION")
            print("Titre du jeu concerné : ")
            val titre = scanner.nextLine().trim()

            if (titre.isEmpty()) {
                println("❌ Le titre ne peut pas être vide.")
            } else {
                print("Cette évaluation est-elle utile ? (O/N) : ")
                val reponse = scanner.nextLine().trim().uppercase()

                if (reponse == "O" || reponse == "N") {
                    val vote = reponse == "O"
                    service.voterEvaluationParCible(titre, pseudoCible, vote)
                } else {
                    println("❌ Réponse invalide. Veuillez saisir O ou N.")
                }
            }
        }
        "0" -> println("↩️  Retour...")
        else -> println("❌ Option invalide.")
    }
}

// ═══════════════════════════════════════════════════════════════════
// MENU : MON PROFIL
// ═══════════════════════════════════════════════════════════════════

fun menuProfil(service: Evenement, utilisateur: Joueur, scanner: Scanner) {
    var continuer = true

    while (continuer) {
        println("\n┌────────────────────────────────────────────────┐")
        println("│          👤 MON PROFIL                         │")
        println("└────────────────────────────────────────────────┘")

        service.afficherProfilUtilisateur(utilisateur.pseudo)

        println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Que souhaitez-vous faire ?")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("1. ✍️  Évaluer un jeu")
        println("0. ↩️  Retour au menu principal")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        print("👉 Votre choix : ")

        when (scanner.nextLine().trim()) {
            "1" -> {
                println("\n✍️  ÉVALUER UN JEU")
                println("ℹ️  Vous pouvez noter et commenter un jeu que vous possédez.")
                println()
                print("Titre du jeu à évaluer : ")
                val titre = scanner.nextLine().trim()

                if (titre.isEmpty()) {
                    println("❌ Le titre ne peut pas être vide.")
                } else {
                    print("Plateforme (PC/PS5/Xbox/Switch) : ")
                    val plateforme = scanner.nextLine().trim()

                    if (plateforme.isEmpty()) {
                        println("❌ La plateforme ne peut pas être vide.")
                    } else {
                        print("Note (0-5) : ")
                        val noteStr = scanner.nextLine().trim()

                        val note = noteStr.toIntOrNull()
                        if (note == null || note !in 0..5) {
                            println("❌ La note doit être entre 0 et 5.")
                        } else {
                            print("Commentaire : ")
                            val commentaire = scanner.nextLine().trim()

                            // Multipliez la note par 2 pour la convertir sur 10 avant l'envoi
                            service.evaluerJeu(titre, plateforme, note * 2, commentaire)
                        }
                    }
                }

                attendreUtilisateur(scanner)
            }
            "0" -> {
                continuer = false
                println("↩️  Retour au menu principal...")
            }
            else -> println("❌ Option invalide. Veuillez choisir 1 ou 0.")
        }
    }
}


fun menuWishlist(service: Evenement, scanner: Scanner) {
    var enWishlist = true
    while (enWishlist) {
        service.afficherWishlist()

        println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("1. ✨ Ajouter un jeu à ma liste")
        println("2. 🗑️  Retirer un jeu de ma liste") // Nouvelle option
        println("0. ↩️  Retour")
        print("\n👉 Choix : ")

        when (scanner.nextLine()) {
            "1" -> {
                print("Titre du jeu à ajouter : ")
                val titre = scanner.nextLine()
                service.ajouterALaWishlist(titre)
                attendreUtilisateur(scanner)
            }
            "2" -> {
                print("Titre du jeu à retirer : ")
                val titre = scanner.nextLine()
                service.retirerDeLaWishlist(titre)
                attendreUtilisateur(scanner)
            }
            "0" -> enWishlist = false
            else -> println("❌ Option invalide.")
        }
    }
}



// ═══════════════════════════════════════════════════════════════════
// FONCTIONS UTILITAIRES
// ═══════════════════════════════════════════════════════════════════

fun attendreUtilisateur(scanner: Scanner) {
    println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("Appuyez sur Entrée pour continuer...")
    scanner.nextLine()
}
