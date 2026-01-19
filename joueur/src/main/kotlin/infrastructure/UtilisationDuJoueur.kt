package infrastructure

import model.Jeux
import model.Joueur
import service.Evenement
import java.time.LocalDate
import java.util.Scanner
import com.projet.joueur.InscriptionEvent
import org.apache.kafka.clients.producer.ProducerRecord

class UtilisationDuJoueur {
    companion object {
        // Simulation d'une base de données : Pseudo -> Paire(MotDePasse, Objet Joueur)
        private val baseDeDonneesUtilisateurs = mutableMapOf<String, Pair<String, Joueur>>()

        @JvmStatic
        fun run() {
            val sc = Scanner(System.`in`)
            var utilisateurConnecte: Joueur? = null

            println("\n--- 🎮 Bienvenue sur PolySteam ---")

            // ÉTAPE 1 : AUTHENTIFICATION
            while (utilisateurConnecte == null) {
                println("\n1. S'inscrire")
                println("2. Se connecter")
                println("3. Quitter")
                print("> ")

                when (sc.nextLine()) {
                    "1" -> {
                        println("--- Inscription ---")
                        print("Pseudo : "); val pseudo = sc.nextLine()
                        print("Mot de passe : "); val mdp = sc.nextLine()
                        print("Nom : "); val nom = sc.nextLine()
                        print("Prénom : "); val prenom = sc.nextLine()
                        print("Date de naissance (AAAA-MM-JJ) : "); val dateN = sc.nextLine()

                        val event = InscriptionEvent.newBuilder()
                            .setPseudo(pseudo)
                            .setPassword(mdp)
                            .setNom(nom)
                            .setPrenom(prenom)
                            .setDateNaissance(dateN)
                            .setTypeAction("INSCRIPTION")
                            .build()

                        // Envoi vers un topic "demandes-inscription"
                        val producer = KafkaClientFactory.createInscriptionProducer()
                        producer.send(ProducerRecord("demandes-inscription", pseudo, event))
                        println("⏳ Demande envoyée à la plateforme...")
                    }
                    "2" -> {
                        println("--- Connexion ---")
                        print("Pseudo : "); val pseudo = sc.nextLine()
                        print("Mot de passe : "); val mdp = sc.nextLine()

                        // On réutilise le schéma InscriptionEvent en changeant le typeAction
                        val event = InscriptionEvent.newBuilder()
                            .setPseudo(pseudo)
                            .setPassword(mdp)
                            .setNom("") // On peut mettre vide car inutile pour la connexion
                            .setPrenom("")
                            .setDateNaissance("")
                            .setTypeAction("CONNEXION") // <-- C'est ce champ qui guide la Plateforme
                            .build()

                        val producer = KafkaClientFactory.createInscriptionProducer()
                        producer.send(ProducerRecord("demandes-inscription", pseudo, event))

                        println("⏳ Vérification de vos identifiants auprès de la plateforme...")
                        // Ici, il faudra ajouter le code pour attendre la réponse (Consumer)
                    }
                    "3" -> return
                }
            }

            // ÉTAPE 2 : MENU PRINCIPAL (Une fois connecté)
            menuPrincipal(utilisateurConnecte, sc)
        }

        private fun menuPrincipal(joueur: Joueur, sc: Scanner) {
            val engine = Evenement(joueur)
            var continuer = true

            while (continuer) {
                println("\n--- MENU PRINCIPAL [${joueur.pseudo}] ---")
                println("1. Acquérir un jeu")
                println("2. Consulter les pages / Catalogue")
                println("3. Afficher mon flux d'informations")
                println("4. Évaluer/Commenter un jeu")
                println("5. Liker/Disliker un commentaire")
                println("6. Se déconnecter")
                print("> ")

                when (sc.nextLine()) {
                    "1" -> {
                        print("Nom du jeu : ")
                        val nom = sc.nextLine()
                        print("Support (PC, PS5, etc.) : ")
                        val support = sc.nextLine()
                        engine.achatJeu(Jeux(nom, 60, listOf("Action")), support) //
                    }
                    "2" -> engine.consulterPageJeux() //
                    "3" -> engine.affichageFluxInformation() //
                    "4" -> {
                        print("Nom du jeu à évaluer : ")
                        val nomJeu = sc.nextLine()
                        engine.creerCommentaireJeu(Jeux(nomJeu, 0, emptyList())) //
                    }
                    "5" -> {
                        println("1. Liker | 2. Disliker")
                        if(sc.nextLine() == "1") engine.LikerCommentaireJeu() else engine.DislikerCommentaireJeu() //
                    }
                    "6" -> continuer = false
                }
            }
            run() // Retour à l'écran de connexion
        }
    }
}