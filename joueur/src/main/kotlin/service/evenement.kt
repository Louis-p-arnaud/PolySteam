package service

import model.Jeu
import model.Joueur
import com.projet.joueur.RapportIncidentEvent
import infrastructure.KafkaClientFactory
import org.apache.kafka.clients.producer.ProducerRecord
import java.sql.DriverManager
import java.sql.SQLException
import org.apache.kafka.clients.producer.KafkaProducer
import java.util.Properties
import java.util.Random
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord



class Evenement(private val joueur: Joueur) {

    private fun creerConfigurationKafkaAvro(): Properties {
        val props = Properties()
        props["bootstrap.servers"] = "86.252.172.215:9092"

        // URL du Schema Registry de ton ami (port par défaut 8081)
        props["schema.registry.url"] = "http://86.252.172.215:8081"

        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        // On utilise le sérialiseur Avro pour la valeur
        props["value.serializer"] = KafkaAvroSerializer::class.java.name

        return props
    }


    /**
     * Simule le lancement d'un jeu avec une probabilité de crash.
     * En cas de crash, un rapport est envoyé à Kafka pour les éditeurs.
     */
    fun jouerAvecCrashAvro(titre: String, plateforme: String) {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"
        val random = Random()

        // 1. Définition du Schéma Avro
        val schemaString = """
    {
      "type": "record",
      "name": "RapportIncident",
      "namespace": "com.polysteam.avro",
      "fields": [
        {"name": "joueur_pseudo", "type": "string"},
        {"name": "jeu_id", "type": "string"},
        {"name": "titre", "type": "string"},
        {"name": "plateforme", "type": "string"},
        {"name": "type_erreur", "type": "string"},
        {"name": "timestamp", "type": "long"}
      ]
    }
    """.trimIndent()
        val schema = Schema.Parser().parse(schemaString)

        // 2. Configuration Kafka avec Avro et Schema Registry
        val props = Properties()
        props["bootstrap.servers"] = "86.252.172.215:9092"
        props["schema.registry.url"] = "http://86.252.172.215:8081"
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = KafkaAvroSerializer::class.java.name

        val producer = KafkaProducer<String, GenericRecord>(props)

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                // 3. Vérification de la possession du jeu
                val checkSql = """
                SELECT jc.id FROM jeu_catalogue jc
                JOIN jeu_possede jp ON jc.id = jp.jeu_id
                WHERE jc.titre = ? AND UPPER(jc.plateforme) = UPPER(?) AND jp.joueur_pseudo = ?
            """.trimIndent()

                val stmtCheck = conn.prepareStatement(checkSql)
                stmtCheck.setString(1, titre)
                stmtCheck.setString(2, plateforme)
                stmtCheck.setString(3, joueur.pseudo)
                val rs = stmtCheck.executeQuery()

                if (!rs.next()) {
                    println("❌ Erreur : Vous ne possédez pas ce jeu.")
                    return
                }
                val jeuId = rs.getString("id")

                println("\n🎮 Session lancée : $titre ($plateforme)")
                println("⏳ Simulation : 1h ajoutée toutes les 5s. Risque de crash Avro activé.")

                while (true) {
                    Thread.sleep(5000)

                    // 4. Simulation du crash (20% de chance)
                    if (random.nextInt(5) == 0) {
                        println("\n💥 CRASH DÉTECTÉ !")

                        // Création de l'objet Avro
                        val avroRecord = GenericData.Record(schema).apply {
                            put("joueur_pseudo", joueur.pseudo)
                            put("jeu_id", jeuId)
                            put("titre", titre)
                            put("plateforme", plateforme)
                            put("type_erreur", "AVRO_SERIALIZED_CRASH")
                            put("timestamp", System.currentTimeMillis())
                        }

                        // Envoi vers Kafka
                        val kafkaRecord = ProducerRecord<String, GenericRecord>("rapports-incidents", joueur.pseudo, avroRecord)
                        producer.send(kafkaRecord) { meta, ex ->
                            if (ex == null) {
                                println("📤 Rapport Avro envoyé avec succès (Offset: ${meta.offset()})")
                            } else {
                                println("❌ Échec Kafka/Avro : ${ex.message}")
                            }
                        }
                        break
                    }

                    // 5. Si pas de crash, mise à jour du temps en base
                    val updateSql = "UPDATE jeu_possede SET temps_jeu_minutes = temps_jeu_minutes + 60 WHERE joueur_pseudo = ? AND jeu_id = ?"
                    val upStmt = conn.prepareStatement(updateSql)
                    upStmt.setString(1, joueur.pseudo)
                    upStmt.setString(2, jeuId)
                    upStmt.executeUpdate()
                    println("📈 +1h de jeu enregistrée...")
                }
            }
        } catch (e: Exception) {
            println("⚠️ Interruption : ${e.message}")
        } finally {
            producer.close()
        }
    }

    fun inscriptionLocale() {
        println("📝 Préparation de l'inscription pour ${joueur.pseudo} dans la base commune.")
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

    fun acheterJeuParTitreEtSupport(titreJeu: String, supportSaisi: String): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                val findIdSql = "SELECT id, version_actuelle FROM jeu_catalogue WHERE titre = ? AND plateforme = ?"
                val findIdStmt = conn.prepareStatement(findIdSql)
                findIdStmt.setString(1, titreJeu)
                findIdStmt.setString(2, supportSaisi)

                val rsId = findIdStmt.executeQuery()

                if (!rsId.next()) {
                    println("❌ Erreur : Le jeu '$titreJeu' n'est pas disponible sur le support '$supportSaisi'.")
                    return false
                }

                val jeuId = rsId.getString("id")
                val versionCatalogue = rsId.getString("version_actuelle")

                val checkSql = "SELECT COUNT(*) FROM jeu_possede WHERE joueur_pseudo = ? AND jeu_id = ?"
                val checkStmt = conn.prepareStatement(checkSql)
                checkStmt.setString(1, joueur.pseudo)
                checkStmt.setString(2, jeuId)

                if (checkStmt.executeQuery().let { it.next() && it.getInt(1) > 0 }) {
                    println("❌ Vous possédez déjà '$titreJeu' sur ce support.")
                    return false
                }

                val insertSql = "INSERT INTO jeu_possede (joueur_pseudo, jeu_id, temps_jeu_minutes, version_installee) VALUES (?, ?, 0, ?)"
                val insertStmt = conn.prepareStatement(insertSql)
                insertStmt.setString(1, joueur.pseudo)
                insertStmt.setString(2, jeuId)
                insertStmt.setString(3, versionCatalogue)

                insertStmt.executeUpdate()
                println("💰 Achat réussi ! '$titreJeu' ajouté sur $supportSaisi.")
                true
            }
        } catch (e: Exception) {
            println("⚠️ Erreur : ${e.message}")
            false
        }
    }

    fun mettreAJourJeu(titreJeu: String): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                // 1. Chercher si une mise à jour est disponible
                val querySql = """
                SELECT jp.jeu_id, jc.version_actuelle, jp.version_installee 
                FROM jeu_possede jp
                JOIN jeu_catalogue jc ON jp.jeu_id = jc.id
                WHERE jp.joueur_pseudo = ? AND jc.titre = ?
            """.trimIndent()

                val stmt = conn.prepareStatement(querySql)
                stmt.setString(1, joueur.pseudo)
                stmt.setString(2, titreJeu)
                val rs = stmt.executeQuery()

                if (rs.next()) {
                    val vCatalogue = rs.getString("version_actuelle")
                    val vInstallee = rs.getString("version_installee")
                    val jeuId = rs.getString("jeu_id")

                    if (vCatalogue == vInstallee) {
                        println("✅ Le jeu '$titreJeu' est déjà à jour (v$vInstallee).")
                        return false
                    }

                    // 2. Mettre à jour la version installée
                    println("📥 Mise à jour trouvée : v$vInstallee -> v$vCatalogue. Téléchargement...")

                    val updateSql = "UPDATE jeu_possede SET version_installee = ? WHERE joueur_pseudo = ? AND jeu_id = ?"
                    val updateStmt = conn.prepareStatement(updateSql)
                    updateStmt.setString(1, vCatalogue)
                    updateStmt.setString(2, joueur.pseudo)
                    updateStmt.setString(3, jeuId)

                    updateStmt.executeUpdate()
                    println("✨ Mise à jour terminée ! '$titreJeu' est maintenant en version $vCatalogue.")
                    true
                } else {
                    println("❌ Vous ne possédez pas le jeu '$titreJeu'.")
                    false
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de la mise à jour : ${e.message}")
            false
        }
    }

    fun afficherFicheJeuParTitre(titreRecherche: String) {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                // On ajoute une jointure sur jeu_genre pour récupérer l'attribut 'genre'
                val sql = """
                SELECT jc.titre, jc.date_publication, e.nom AS nom_editeur, jc.version_actuelle, 
                       jc.est_version_anticipee, jc.prix_actuel, jg.genre, jc.plateforme
                FROM jeu_catalogue jc
                JOIN editeur e ON jc.editeur_id = e.id
                LEFT JOIN jeu_genre jg ON jc.id = jg.jeu_id
                WHERE jc.titre = ?
            """.trimIndent()

                val stmt = conn.prepareStatement(sql)
                stmt.setString(1, titreRecherche)

                val rs = stmt.executeQuery()
                var jeuTrouve = false

                while (rs.next()) {
                    if (!jeuTrouve) {
                        println("\n--- 📄 FICHE INFORMATION : ${rs.getString("titre")} ---")
                        println("📅 Date Publication : ${rs.getDate("date_publication")}")
                        println("🏢 Éditeur          : ${rs.getString("nom_editeur")}")
                        // On récupère 'genre' depuis la table jeu_genre
                        println("🏷️ Genre           : ${rs.getString("genre") ?: "Non spécifié"}")
                        println("\nDisponibilité par plateforme :")
                        jeuTrouve = true
                    }

                    val plateforme = rs.getString("plateforme")
                    val prix = rs.getDouble("prix_actuel")
                    val version = rs.getString("version_actuelle")
                    val anticipe = if (rs.getBoolean("est_version_anticipee")) "[ACCÈS ANTICIPÉ]" else ""

                    println("  • [$plateforme] : $prix€ | Version : $version $anticipe")
                }

                if (!jeuTrouve) {
                    println("❌ Aucun jeu trouvé pour le titre '$titreRecherche'.")
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur SQL : ${e.message}")
        }
    }

    fun afficherFicheEditeur(nomEditeur: String) {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                // Requête pour obtenir les infos de l'éditeur ET la liste de ses jeux
                val sql = """
                SELECT e.nom, e.est_independant, e.date_creation, jc.titre, jc.date_publication
                FROM editeur e
                LEFT JOIN jeu_catalogue jc ON e.id = jc.editeur_id
                WHERE UPPER(e.nom) = UPPER(?)
            """.trimIndent()

                val stmt = conn.prepareStatement(sql)
                stmt.setString(1, nomEditeur)

                val rs = stmt.executeQuery()
                var editeurAffiche = false

                while (rs.next()) {
                    if (!editeurAffiche) {
                        println("\n--- 🏢 FICHE ÉDITEUR : ${rs.getString("nom")} ---")
                        println("🛠️ Type : ${if (rs.getBoolean("est_independant")) "Indépendant" else "Studio Majeur"}")
                        println("📅 Création : ${rs.getTimestamp("date_creation")}")
                        println("\n📚 Catalogue des jeux proposés :")
                        editeurAffiche = true
                    }

                    val titreJeu = rs.getString("titre")
                    if (titreJeu != null) {
                        val datePub = rs.getDate("date_publication")
                        println("  • $titreJeu (Sorti le : $datePub)")
                    }
                }

                if (!editeurAffiche) {
                    println("❌ Aucun éditeur trouvé au nom de '$nomEditeur'.")
                } else {
                    println("------------------------------------------")
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de la récupération de l'éditeur : ${e.message}")
        }
    }

    fun afficherJeuxPossedes() {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                val sql = """
                SELECT jc.titre, jc.plateforme, jp.temps_jeu_minutes, jp.version_installee 
                FROM jeu_possede jp
                JOIN jeu_catalogue jc ON jp.jeu_id = jc.id
                WHERE jp.joueur_pseudo = ?
            """.trimIndent()

                val stmt = conn.prepareStatement(sql)
                stmt.setString(1, joueur.pseudo)

                val rs = stmt.executeQuery()
                var aDesJeux = false

                println("\n--- 📚 BIBLIOTHÈQUE DE ${joueur.pseudo} ---")

                while (rs.next()) {
                    aDesJeux = true
                    val titre = rs.getString("titre")
                    val plateforme = rs.getString("plateforme")
                    val temps = rs.getInt("temps_jeu_minutes")
                    val version = rs.getString("version_installee")

                    println("🎮 $titre [$plateforme]")
                    println("   • Temps de jeu : ${temps / 60}h ${temps % 60}min")
                    println("   • Version installée : $version")
                    println("   -----------------------")
                }

                if (!aDesJeux) {
                    println("Votre bibliothèque est vide. Visitez la boutique pour acquérir des jeux !")
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'affichage de la bibliothèque : ${e.message}")
        }
    }

    fun evaluerJeu(titre: String, plateforme: String, note: Int, commentaire: String): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                // 1. Récupérer l'ID du jeu et vérifier possession + temps de jeu (min 60 min)
                val checkSql = """
                SELECT jc.id, jp.temps_jeu_minutes 
                FROM jeu_catalogue jc
                JOIN jeu_possede jp ON jc.id = jp.jeu_id
                WHERE jc.titre = ? AND UPPER(jc.plateforme) = UPPER(?) AND jp.joueur_pseudo = ?
            """.trimIndent()

                val checkStmt = conn.prepareStatement(checkSql)
                checkStmt.setString(1, titre)
                checkStmt.setString(2, plateforme)
                checkStmt.setString(3, joueur.pseudo)
                val rs = checkStmt.executeQuery()

                if (!rs.next()) {
                    println("❌ Erreur : Vous ne possédez pas ce jeu sur cette plateforme.")
                    return false
                }

                val jeuId = rs.getString("id")
                val tempsJeu = rs.getInt("temps_jeu_minutes")

                if (tempsJeu < 60) {
                    println("❌ Erreur : Vous devez avoir joué au moins 1 heure (actuellement : ${tempsJeu}min).")
                    return false
                }

                // 2. Insérer l'évaluation
                val insertSql = """
                INSERT INTO evaluation (joueur_pseudo, jeu_id, note, commentaire) 
                VALUES (?, ?, ?, ?)
            """.trimIndent()

                val insertStmt = conn.prepareStatement(insertSql)
                insertStmt.setString(1, joueur.pseudo)
                insertStmt.setString(2, jeuId)
                insertStmt.setInt(3, note)
                insertStmt.setString(4, commentaire)

                insertStmt.executeUpdate()
                println("⭐ Évaluation publiée avec succès pour '$titre' !")
                true
            }
        } catch (e: Exception) {
            println("⚠️ Erreur : ${e.message}")
            false
        }
    }


    fun afficherProfilUtilisateur(pseudoRecherche: String) {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                // 1. Récupérer les informations personnelles du joueur
                val sqlJoueur = "SELECT pseudo, nom, prenom, date_naissance FROM joueur WHERE pseudo = ?"
                val stmtJ = conn.prepareStatement(sqlJoueur)
                stmtJ.setString(1, pseudoRecherche)
                val rsJ = stmtJ.executeQuery()

                if (!rsJ.next()) {
                    println("❌ L'utilisateur '$pseudoRecherche' n'existe pas.")
                    return
                }

                println("\n============================================")
                println("👤 PROFIL DE : ${rsJ.getString("pseudo").uppercase()}")
                println("============================================")
                println("Nom         : ${rsJ.getString("nom")}")
                println("Prénom      : ${rsJ.getString("prenom")}")
                println("Né(e) le    : ${rsJ.getDate("date_naissance")}")
                println("--------------------------------------------")

                // 2. Récupérer la bibliothèque et le temps de jeu
                println("\n🎮 BIBLIOTHÈQUE ET TEMPS DE JEU :")
                val sqlJeux = """
                SELECT jc.titre, jc.plateforme, jp.temps_jeu_minutes 
                FROM jeu_possede jp
                JOIN jeu_catalogue jc ON jp.jeu_id = jc.id
                WHERE jp.joueur_pseudo = ?
                ORDER BY jp.temps_jeu_minutes DESC
            """.trimIndent()

                val stmtG = conn.prepareStatement(sqlJeux)
                stmtG.setString(1, pseudoRecherche)
                val rsG = stmtG.executeQuery()

                var aDesJeux = false
                while (rsG.next()) {
                    aDesJeux = true
                    val t = rsG.getInt("temps_jeu_minutes")
                    println("• ${rsG.getString("titre")} [${rsG.getString("plateforme")}] : ${t / 60}h ${t % 60}min")
                }
                if (!aDesJeux) println("Aucun jeu dans la bibliothèque.")

                // 3. Récupérer les évaluations laissées par le joueur
                println("\n⭐ ÉVALUATIONS LAISSÉES :")
                val sqlEval = """
                SELECT jc.titre, e.note, e.commentaire, e.date_publication
                FROM evaluation e
                JOIN jeu_catalogue jc ON e.jeu_id = jc.id
                WHERE e.joueur_pseudo = ?
                ORDER BY e.date_publication DESC
            """.trimIndent()

                val stmtE = conn.prepareStatement(sqlEval)
                stmtE.setString(1, pseudoRecherche)
                val rsE = stmtE.executeQuery()

                var aDesEvals = false
                while (rsE.next()) {
                    aDesEvals = true
                    println("--------------------------------------------")
                    println("Jeu : ${rsE.getString("titre")}")
                    println("Note : ${rsE.getInt("note")}/10")
                    println("Commentaire : \"${rsE.getString("commentaire")}\"")
                    println("Le : ${rsE.getTimestamp("date_publication")}")
                }
                if (!aDesEvals) println("Aucune évaluation rédigée.")

                println("============================================\n")
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'affichage du profil : ${e.message}")
        }
    }

    fun voterEvaluationParCible(titreJeu: String, pseudoAuteur: String, estUnLike: Boolean): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->
                conn.autoCommit = false // Début d'une transaction pour la sécurité

                // 1. Trouver l'ID de l'évaluation
                val findIdSql = """
                SELECT e.id FROM evaluation e
                JOIN jeu_catalogue jc ON e.jeu_id = jc.id
                WHERE jc.titre = ? AND e.joueur_pseudo = ?
            """.trimIndent()

                val stmtId = conn.prepareStatement(findIdSql)
                stmtId.setString(1, titreJeu)
                stmtId.setString(2, pseudoAuteur)
                val rsId = stmtId.executeQuery()

                if (!rsId.next()) {
                    println("❌ Aucune évaluation trouvée.")
                    return false
                }
                val evaluationId = rsId.getInt("id")

                // 2. Vérifier si un vote existe déjà
                val checkSql = "SELECT est_utile FROM votes_evaluation WHERE evaluation_id = ? AND votant_pseudo = ?"
                val checkStmt = conn.prepareStatement(checkSql)
                checkStmt.setInt(1, evaluationId)
                checkStmt.setString(2, joueur.pseudo)
                val rsVote = checkStmt.executeQuery()

                if (rsVote.next()) {
                    val ancienVoteEstLike = rsVote.getBoolean("est_utile")

                    if (ancienVoteEstLike == estUnLike) {
                        println("⚠️ Vous avez déjà voté ainsi.")
                        return false
                    } else {
                        // CHANGEMENT DE VOTE (ex: Like -> Dislike)
                        // Mise à jour du vote individuel
                        val upVoteSql = "UPDATE votes_evaluation SET est_utile = ? WHERE evaluation_id = ? AND votant_pseudo = ?"
                        val upVoteStmt = conn.prepareStatement(upVoteSql)
                        upVoteStmt.setBoolean(1, estUnLike)
                        upVoteStmt.setInt(2, evaluationId)
                        upVoteStmt.setString(3, joueur.pseudo)
                        upVoteStmt.executeUpdate()

                        // Mise à jour des compteurs globaux dans la table evaluation
                        val sqlCompteurs = if (estUnLike) {
                            "UPDATE evaluation SET nombre_votes_utile = nombre_votes_utile + 1, nombre_votes_pas_utile = nombre_votes_pas_utile - 1 WHERE id = ?"
                        } else {
                            "UPDATE evaluation SET nombre_votes_utile = nombre_votes_utile - 1, nombre_votes_pas_utile = nombre_votes_pas_utile + 1 WHERE id = ?"
                        }
                        val upCounters = conn.prepareStatement(sqlCompteurs)
                        upCounters.setInt(1, evaluationId)
                        upCounters.executeUpdate()

                        println("🔄 Votre vote a été modifié et les compteurs mis à jour.")
                    }
                } else {
                    // NOUVEAU VOTE
                    // Insertion du vote individuel
                    val insertVoteSql = "INSERT INTO votes_evaluation (evaluation_id, votant_pseudo, est_utile) VALUES (?, ?, ?)"
                    val insertStmt = conn.prepareStatement(insertVoteSql)
                    insertStmt.setInt(1, evaluationId)
                    insertStmt.setString(2, joueur.pseudo)
                    insertStmt.setBoolean(3, estUnLike)
                    insertStmt.executeUpdate()

                    // Incrémentation du compteur correspondant
                    val sqlIncr = if (estUnLike) {
                        "UPDATE evaluation SET nombre_votes_utile = nombre_votes_utile + 1 WHERE id = ?"
                    } else {
                        "UPDATE evaluation SET nombre_votes_pas_utile = nombre_votes_pas_utile + 1 WHERE id = ?"
                    }
                    val incrStmt = conn.prepareStatement(sqlIncr)
                    incrStmt.setInt(1, evaluationId)
                    incrStmt.executeUpdate()

                    println("✅ Nouveau vote enregistré et compteur incrémenté !")
                }

                conn.commit()
                true
            }
        } catch (e: Exception) {
            println("⚠️ Erreur : ${e.message}")
            false
        }
    }
}