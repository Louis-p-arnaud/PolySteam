package service

import model.Jeu
import model.Joueur
import com.projet.joueur.RapportIncidentEvent
import infrastructure.KafkaClientFactory
import org.apache.kafka.clients.producer.ProducerRecord
import java.sql.DriverManager
import java.sql.SQLException

class Evenement(private val joueur: Joueur) {

    /**
     * Simule le lancement d'un jeu avec une probabilité de crash.
     * En cas de crash, un rapport est envoyé à Kafka pour les éditeurs.
     */
    fun jouer(jeu: Jeu) {
        println("\n🎮 Tentative de lancement de : ${jeu.titre} (v${jeu.versionActuelle})")

        if (jeu.lancerJeu()) {
            println("💥 CRASH DÉTECTÉ sur ${jeu.titre} !")

            // 1. Création du rapport d'incident (Avro) conforme à ton nouveau besoin
            val rapport = RapportIncidentEvent.newBuilder()
                .setId(java.util.UUID.randomUUID().toString())
                .setJeuId(jeu.id)
                .setJoueurPseudo(joueur.pseudo)
                .setVersionJeu(jeu.versionActuelle)
                .setPlateforme(jeu.plateforme)
                .setDescriptionErreur("Crash critique lors de l'exécution (Simulation probabilité)")
                .setTimestamp(System.currentTimeMillis())
                .build()

            // 2. Envoi immédiat à Kafka via le nouveau Producer
            try {
                val producer = KafkaClientFactory.createRapportIncidentProducer()
                producer.send(ProducerRecord("rapports-incidents", jeu.id, rapport))
                println("📡 Rapport d'incident envoyé au topic 'rapports-incidents'.")
            } catch (e: Exception) {
                println("⚠️ Échec de l'envoi Kafka : ${e.message}")
            }
        } else {
            println("✅ Le jeu ${jeu.titre} s'est lancé correctement.")
            // Ici, tu pourras ajouter l'appel JDBC pour incrémenter le temps de jeu en BD
        }
    }


    fun inscriptionLocale() {
        println("📝 Préparation de l'inscription pour ${joueur.pseudo} dans la base commune.")
    }


    fun achatJeu(jeu: Jeu) {
        println("💰 Achat de ${jeu.titre} enregistré pour ${joueur.pseudo}.")
    }

    /**
     * Création d'un commentaire : vérification du temps de jeu (minimum 1h / 60 min).
     * Se base sur les données de l'ERD (temps_jeu_minutes).
     */
    fun creerCommentaire(jeuId: String, tempsJeuMinutes: Long) {
        if (tempsJeuMinutes >= 60) {
            println("✍️ Autorisation d'évaluer le jeu $jeuId (Temps: ${tempsJeuMinutes}min).")
        } else {
            println("❌ Évaluation refusée : Il faut au moins 60 minutes de jeu.")
        }
    }

    fun inscrireJoueur(pseudo: String, mdp: String, nom: String, prenom: String, dateN: String): Boolean {
        // 1. Vérification locale du mot de passe
        if (mdp.length < 8) {
            println("❌ Erreur : Le mot de passe doit contenir au moins 8 caractères.")
            return false
        }

        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val password = "PolySteam2026!"

        try {
            DriverManager.getConnection(url, user, password).use { conn ->
                // 2. Vérification de l'unicité du pseudo (SELECT)
                val checkSql = "SELECT COUNT(*) FROM joueur WHERE pseudo = ?"
                val checkStmt = conn.prepareStatement(checkSql)
                checkStmt.setString(1, pseudo)
                val rs = checkStmt.executeQuery()

                if (rs.next() && rs.getInt(1) > 0) {
                    println("❌ Erreur : Le pseudo '$pseudo' est déjà utilisé.")
                    return false
                }

                // 3. Insertion du nouveau compte (INSERT)
                // Note : L'ERD contient pseudo, nom, prenom, date_naissance
                // ✅ LA BONNE SYNTAXE :
                val insertSql = "INSERT INTO joueur (pseudo, nom, prenom, date_naissance) VALUES (?, ?, ?, ?::date)"
                val insertStmt = conn.prepareStatement(insertSql)
                insertStmt.setString(1, pseudo)
                insertStmt.setString(2, nom)
                insertStmt.setString(3, prenom)
                insertStmt.setString(4, dateN)

                insertStmt.executeUpdate()
                println("✅ Compte créé avec succès pour $pseudo !")
                return true
            }
        } catch (e: SQLException) {
            println("⚠️ Erreur base de données : ${e.message}")
            return false
        }
    }
}