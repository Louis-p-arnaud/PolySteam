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
        // Vérification locale du mot de passe
        if (mdp.length < 8) {
            println("❌ Erreur : Le mot de passe doit contenir au moins 8 caractères.")
            return false
        }

        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val password = "PolySteam2026!"

        try {
            DriverManager.getConnection(url, user, password).use { conn ->
                // Vérification de l'unicité du pseudo
                val checkSql = "SELECT COUNT(*) FROM joueur WHERE pseudo = ?"
                val checkStmt = conn.prepareStatement(checkSql)
                checkStmt.setString(1, pseudo)
                val rs = checkStmt.executeQuery()

                if (rs.next() && rs.getInt(1) > 0) {
                    println("❌ Erreur : Le pseudo '$pseudo' est déjà utilisé.")
                    return false
                }

                // Insertion du nouveau compte
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

    fun acheterJeuParTitre(titreJeu: String): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                // 1. Trouver l'ID et la VERSION ACTUELLE à partir du TITRE (Correction 'nom' -> 'titre')
                val findIdSql = "SELECT id, version_actuelle FROM jeu_catalogue WHERE titre = ?"
                val findIdStmt = conn.prepareStatement(findIdSql)
                findIdStmt.setString(1, titreJeu)
                val rsId = findIdStmt.executeQuery()

                if (!rsId.next()) {
                    println("❌ Le jeu '$titreJeu' n'existe pas dans le catalogue.")
                    return false
                }

                val jeuId = rsId.getString("id")
                val versionDuCatalogue = rsId.getString("version_actuelle") // On récupère la vraie version

                // 2. Vérifier si le joueur possède déjà ce jeu
                val checkSql = "SELECT COUNT(*) FROM jeu_possede WHERE joueur_pseudo = ? AND jeu_id = ?"
                val checkStmt = conn.prepareStatement(checkSql)
                checkStmt.setString(1, joueur.pseudo)
                checkStmt.setString(2, jeuId)
                val rsCheck = checkStmt.executeQuery()

                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    println("❌ Vous possédez déjà '$titreJeu' !")
                    return false
                }

                // 3. Insérer l'achat avec la version récupérée dynamiquement
                val insertSql = """
                INSERT INTO jeu_possede (joueur_pseudo, jeu_id, temps_jeu_minutes, version_installee) 
                VALUES (?, ?, 0, ?)
            """.trimIndent()

                val insertStmt = conn.prepareStatement(insertSql)
                insertStmt.setString(1, joueur.pseudo)
                insertStmt.setString(2, jeuId)
                insertStmt.setString(3, versionDuCatalogue) // Utilisation de la version du catalogue

                insertStmt.executeUpdate()
                println("💰 Achat réussi ! '$titreJeu' (v$versionDuCatalogue) est ajouté à votre bibliothèque.")
                true
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'achat : ${e.message}")
            false
        }
    }

/*
    fun acheterJeu(jeuId: String): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                // Vérifier si le joueur possède déjà ce jeu
                val checkSql = "SELECT COUNT(*) FROM jeu_possede WHERE joueur_pseudo = ? AND jeu_id = ?"
                val checkStmt = conn.prepareStatement(checkSql)
                checkStmt.setString(1, joueur.pseudo)
                checkStmt.setString(2, jeuId)
                val rs = checkStmt.executeQuery()

                if (rs.next() && rs.getInt(1) > 0) {
                    println("❌ Vous possédez déjà ce jeu !")
                    return false
                }

                // Insérer l'achat dans la table de liaison
                val insertSql = """
                INSERT INTO jeu_possede (joueur_pseudo, jeu_id, temps_jeu_minutes, version_installee) 
                VALUES (?, ?, 0, '1.0.0')
            """.trimIndent()

                val insertStmt = conn.prepareStatement(insertSql)
                insertStmt.setString(1, joueur.pseudo)
                insertStmt.setString(2, jeuId)

                insertStmt.executeUpdate()
                println("💰 Achat réussi ! Le jeu (ID: $jeuId) est maintenant dans votre bibliothèque.")
                true
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'achat : ${e.message}")
            false
        }
    }*/
}