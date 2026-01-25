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

        // Schéma Avro & Kafka (Inchangé)
        // 1. Définition du Schéma Avro (CORRIGÉ)
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

        val schema = org.apache.avro.Schema.Parser().parse(schemaString)
        val props = Properties().apply {
            put("bootstrap.servers", "86.252.172.215:9092")
            put("schema.registry.url", "http://86.252.172.215:8081")
            put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer")
        }

        val producer = KafkaProducer<String, GenericRecord>(props)

        try {
            // Vérifier la possession (Ouverture/Fermeture immédiate)
            val jeuId = DriverManager.getConnection(url, user, pass).use { conn ->
                val checkSql = """
                SELECT jc.id FROM jeu_catalogue jc
                JOIN jeu_possede jp ON jc.id = jp.jeu_id
                WHERE jc.titre = ? AND UPPER(jc.plateforme) = UPPER(?) AND jp.joueur_pseudo = ?
            """.trimIndent()

                conn.prepareStatement(checkSql).use { stmt ->
                    stmt.setString(1, titre)
                    stmt.setString(2, plateforme)
                    stmt.setString(3, joueur.pseudo)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) rs.getString("id") else null
                    }
                }
            }

            if (jeuId == null) {
                println("❌ Erreur : Vous ne possédez pas ce jeu.")
                return
            }

            println("\n🎮 Session lancée : $titre ($plateforme)")

            // 3. BOUCLE DE JEU (La connexion est fermée pendant le Thread.sleep)
            while (true) {
                Thread.sleep(5000)

                if (random.nextInt(5) == 0) { // CRASH
                    println("\n💥 CRASH DÉTECTÉ !")
                    val avroRecord = GenericData.Record(schema).apply {
                        put("joueur_pseudo", joueur.pseudo); put("jeu_id", jeuId)
                        put("titre", titre); put("plateforme", plateforme)
                        put("type_erreur", "AVRO_SERIALIZED_CRASH"); put("timestamp", System.currentTimeMillis())
                    }
                    producer.send(ProducerRecord("rapports-incidents", joueur.pseudo, avroRecord))
                    break
                }

                // Mise à jour du temps (On ouvre, on update, on ferme direct)
                try {
                    DriverManager.getConnection(url, user, pass).use { conn ->
                        val updateSql = "UPDATE jeu_possede SET temps_jeu_minutes = temps_jeu_minutes + 60 WHERE joueur_pseudo = ? AND jeu_id = ?"
                        conn.prepareStatement(updateSql).use { upStmt ->
                            upStmt.setString(1, joueur.pseudo)
                            upStmt.setString(2, jeuId)
                            upStmt.executeUpdate()
                            println("📈 +1h de jeu enregistrée (Connexion libérée)")
                        }
                    }
                } catch (e: SQLException) {
                    println("⚠️ Alerte : Impossible de mettre à jour le temps (${e.message})")
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur : ${e.message}")
        } finally {
            producer.close()
        }
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
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val password = "PolySteam2026!"

        try {
            DriverManager.getConnection(url, user, password).use { conn ->
                // INSERT incluant maintenant la colonne mot_de_passe
                val insertSql = "INSERT INTO joueur (pseudo, nom, prenom, date_naissance, mot_de_passe) VALUES (?, ?, ?, ?::date, ?)"

                conn.prepareStatement(insertSql).use { insertStmt ->
                    insertStmt.setString(1, pseudo)
                    insertStmt.setString(2, nom)
                    insertStmt.setString(3, prenom)
                    insertStmt.setString(4, dateN)
                    insertStmt.setString(5, mdp) // Enregistrement du MDP

                    insertStmt.executeUpdate()
                }
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

                // Récupération de l'ID et de la version (avec .use pour stmt et rs)
                val findIdSql = "SELECT id, version_actuelle FROM jeu_catalogue WHERE titre = ? AND plateforme = ?"
                val infoJeu = conn.prepareStatement(findIdSql).use { stmt ->
                    stmt.setString(1, titreJeu)
                    stmt.setString(2, supportSaisi)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            Pair(rs.getString("id"), rs.getString("version_actuelle"))
                        } else null
                    }
                }

                if (infoJeu == null) {
                    println("❌ Erreur : Le jeu '$titreJeu' n'est pas disponible sur le support '$supportSaisi'.")
                    return false
                }
                val (jeuId, versionCatalogue) = infoJeu

                // Vérification de la possession (avec .use)
                val checkSql = "SELECT COUNT(*) FROM jeu_possede WHERE joueur_pseudo = ? AND jeu_id = ?"
                val dejaPossede = conn.prepareStatement(checkSql).use { stmt ->
                    stmt.setString(1, joueur.pseudo)
                    stmt.setString(2, jeuId)
                    stmt.executeQuery().use { rs ->
                        rs.next() && rs.getInt(1) > 0
                    }
                }

                if (dejaPossede) {
                    println("❌ Vous possédez déjà '$titreJeu' sur ce support.")
                    return false
                }

                // Insertion de l'achat (avec .use)
                val insertSql = "INSERT INTO jeu_possede (joueur_pseudo, jeu_id, temps_jeu_minutes, version_installee) VALUES (?, ?, 0, ?)"
                conn.prepareStatement(insertSql).use { stmt ->
                    stmt.setString(1, joueur.pseudo)
                    stmt.setString(2, jeuId)
                    stmt.setString(3, versionCatalogue)
                    stmt.executeUpdate()
                }

                println("💰 Achat réussi ! '$titreJeu' ajouté sur $supportSaisi.")
                true
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'achat : ${e.message}")
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

                // Chercher si une mise à jour est disponible
                val querySql = """
                SELECT jp.jeu_id, jc.version_actuelle, jp.version_installee 
                FROM jeu_possede jp
                JOIN jeu_catalogue jc ON jp.jeu_id = jc.id
                WHERE jp.joueur_pseudo = ? AND jc.titre = ?
            """.trimIndent()

                // On utilise .use pour le Statement et le ResultSet
                val updateInfo = conn.prepareStatement(querySql).use { stmt ->
                    stmt.setString(1, joueur.pseudo)
                    stmt.setString(2, titreJeu)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            Triple(
                                rs.getString("jeu_id"),
                                rs.getString("version_actuelle"),
                                rs.getString("version_installee")
                            )
                        } else null
                    }
                }

                if (updateInfo != null) {
                    val (jeuId, vCatalogue, vInstallee) = updateInfo

                    if (vCatalogue == vInstallee) {
                        println("✅ Le jeu '$titreJeu' est déjà à jour (v$vInstallee).")
                        return false
                    }

                    // Mettre à jour la version installée
                    println("📥 Mise à jour trouvée : v$vInstallee -> v$vCatalogue. Téléchargement...")

                    val updateSql = "UPDATE jeu_possede SET version_installee = ? WHERE joueur_pseudo = ? AND jeu_id = ?"
                    conn.prepareStatement(updateSql).use { updateStmt ->
                        updateStmt.setString(1, vCatalogue)
                        updateStmt.setString(2, joueur.pseudo)
                        updateStmt.setString(3, jeuId)
                        updateStmt.executeUpdate()
                    }

                    println("✨ Mise à jour terminée ! '$titreJeu' est maintenant en version $vCatalogue.")
                    return true
                } else {
                    println("❌ Vous ne possédez pas le jeu '$titreJeu'.")
                    return false
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
            DriverManager.getConnection(url, user, pass).use { conn ->
                // 1. On récupère d'abord les infos générales (avec STRING_AGG pour grouper les genres)
                val sqlInfos = """
                SELECT jc.titre, jc.date_publication, e.nom AS nom_editeur, 
                       STRING_AGG(jg.genre, ', ') AS genres
                FROM jeu_catalogue jc
                JOIN editeur e ON jc.editeur_id = e.id
                LEFT JOIN jeu_genre jg ON jc.id = jg.jeu_id
                WHERE jc.titre = ?
                GROUP BY jc.titre, jc.date_publication, e.nom
                LIMIT 1
            """.trimIndent()

                val aEteTrouve = conn.prepareStatement(sqlInfos).use { stmt ->
                    stmt.setString(1, titreRecherche)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            println("\n--- 📄 FICHE INFORMATION : ${rs.getString("titre")} ---")
                            println("📅 Date Publication : ${rs.getDate("date_publication")}")
                            println("🏢 Éditeur          : ${rs.getString("nom_editeur")}")
                            println("🏷️ Genre(s)         : ${rs.getString("genres") ?: "Non spécifié"}")
                            true
                        } else false
                    }
                }

                if (!aEteTrouve) {
                    println("❌ Aucun jeu trouvé pour le titre '$titreRecherche'.")
                    return
                }

                // 2. On récupère les plateformes SANS doublons avec DISTINCT
                val sqlPlateformes = """
                SELECT DISTINCT plateforme, prix_actuel, version_actuelle, est_version_anticipee
                FROM jeu_catalogue 
                WHERE titre = ?
            """.trimIndent()

                conn.prepareStatement(sqlPlateformes).use { stmtP ->
                    stmtP.setString(1, titreRecherche)
                    stmtP.executeQuery().use { rsP ->
                        println("\nDisponibilité par plateforme :")
                        while (rsP.next()) {
                            val plat = rsP.getString("plateforme")
                            val prix = rsP.getDouble("prix_actuel")
                            val vers = rsP.getString("version_actuelle")
                            val anticipe = if (rsP.getBoolean("est_version_anticipee")) "[ACCÈS ANTICIPÉ]" else ""
                            println("  • [$plat] : $prix€ | Version : $vers $anticipe")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur : ${e.message}")
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

                // Imbrication des .use pour une libération totale des ressources
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, nomEditeur)

                    stmt.executeQuery().use { rs ->
                        var editeurAffiche = false

                        while (rs.next()) {
                            // Affichage de l'en-tête (une seule fois)
                            if (!editeurAffiche) {
                                println("\n--- 🏢 FICHE ÉDITEUR : ${rs.getString("nom")} ---")
                                val type = if (rs.getBoolean("est_independant")) "Indépendant" else "Studio Majeur"
                                println("🛠️ Type : $type")
                                println("📅 Création : ${rs.getTimestamp("date_creation")}")
                                println("\n📚 Catalogue des jeux proposés :")
                                editeurAffiche = true
                            }

                            // Affichage de la liste des jeux (boucle sur les résultats du JOIN)
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
                    } // Fermeture automatique du ResultSet
                } // Fermeture automatique du PreparedStatement
            } // Fermeture automatique de la Connection
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

                // Utilisation de .use pour le Statement et le ResultSet
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, joueur.pseudo)

                    stmt.executeQuery().use { rs ->
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
                    } // Le ResultSet est fermé ici
                } // Le PreparedStatement est fermé ici
            } // La Connection est fermée ici
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

                // 1. Récupérer l'ID du jeu et vérifier possession + temps de jeu
                val checkSql = """
                SELECT jc.id, jp.temps_jeu_minutes 
                FROM jeu_catalogue jc
                JOIN jeu_possede jp ON jc.id = jp.jeu_id
                WHERE jc.titre = ? AND UPPER(jc.plateforme) = UPPER(?) AND jp.joueur_pseudo = ?
            """.trimIndent()

                val jeuId = conn.prepareStatement(checkSql).use { stmt ->
                    stmt.setString(1, titre)
                    stmt.setString(2, plateforme)
                    stmt.setString(3, joueur.pseudo)

                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            val tempsJeu = rs.getInt("temps_jeu_minutes")
                            if (tempsJeu < 60) {
                                println("❌ Erreur : Vous devez avoir joué au moins 1 heure (actuellement : ${tempsJeu}min).")
                                return false // Sortie propre
                            }
                            rs.getString("id")
                        } else {
                            println("❌ Erreur : Vous ne possédez pas ce jeu sur cette plateforme.")
                            return false // Sortie propre
                        }
                    }
                }

                // 2. Insérer l'évaluation
                val insertSql = """
                INSERT INTO evaluation (joueur_pseudo, jeu_id, note, commentaire) 
                VALUES (?, ?, ?, ?)
            """.trimIndent()

                conn.prepareStatement(insertSql).use { insertStmt ->
                    insertStmt.setString(1, joueur.pseudo)
                    insertStmt.setString(2, jeuId)
                    insertStmt.setInt(3, note)
                    insertStmt.setString(4, commentaire)

                    insertStmt.executeUpdate()
                }

                println("⭐ Évaluation publiée avec succès pour '$titre' !")
                true
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'évaluation : ${e.message}")
            false
        }
    }


    fun afficherProfilUtilisateur(pseudoRecherche: String): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            // On utilise 'return' ici pour renvoyer le résultat du bloc .use
            return DriverManager.getConnection(url, user, pass).use { conn ->

                // 1. Informations personnelles du joueur
                val sqlJoueur = "SELECT pseudo, nom, prenom, date_naissance FROM joueur WHERE pseudo = ?"
                val joueurExiste = conn.prepareStatement(sqlJoueur).use { stmtJ ->
                    stmtJ.setString(1, pseudoRecherche)
                    stmtJ.executeQuery().use { rsJ ->
                        if (rsJ.next()) {
                            println("\n============================================")
                            println("👤 PROFIL DE : ${rsJ.getString("pseudo").uppercase()}")
                            println("============================================")
                            println("Nom         : ${rsJ.getString("nom")}")
                            println("Prénom      : ${rsJ.getString("prenom")}")
                            println("Né(e) le    : ${rsJ.getDate("date_naissance")}")
                            println("--------------------------------------------")
                            true
                        } else {
                            println("❌ L'utilisateur '$pseudoRecherche' n'existe pas.")
                            false
                        }
                    }
                }

                // Si le joueur n'existe pas, on arrête et on retourne false
                if (!joueurExiste) return false

                // 2. Bibliothèque et temps de jeu
                println("\n🎮 BIBLIOTHÈQUE ET TEMPS DE JEU :")
                val sqlJeux = """
                    SELECT jc.titre, jc.plateforme, jp.temps_jeu_minutes 
                    FROM jeu_possede jp
                    JOIN jeu_catalogue jc ON jp.jeu_id = jc.id
                    WHERE jp.joueur_pseudo = ?
                    ORDER BY jp.temps_jeu_minutes DESC
                """.trimIndent()

                conn.prepareStatement(sqlJeux).use { stmtG ->
                    stmtG.setString(1, pseudoRecherche)
                    stmtG.executeQuery().use { rsG ->
                        var aDesJeux = false
                        while (rsG.next()) {
                            aDesJeux = true
                            val t = rsG.getInt("temps_jeu_minutes")
                            println("• ${rsG.getString("titre")} [${rsG.getString("plateforme")}] : ${t / 60}h ${t % 60}min")
                        }
                        if (!aDesJeux) println("Aucun jeu dans la bibliothèque.")
                    }
                }
                //3. Wishlist du joueur
                println("\n💖 LISTE DE SOUHAITS (WISHLIST) :")
                val sqlWish = """
                    SELECT jc.titre, jc.plateforme, jc.prix_actuel
                    FROM wishlist w
                    JOIN jeu_catalogue jc ON w.jeu_id = jc.id
                    WHERE w.joueur_pseudo = ?
                    ORDER BY w.date_ajout DESC
                """.trimIndent()

                conn.prepareStatement(sqlWish).use { stmtW ->
                    stmtW.setString(1, pseudoRecherche)
                    stmtW.executeQuery().use { rsW ->
                        var aDesSouhaits = false
                        while (rsW.next()) {
                            aDesSouhaits = true
                            // ATTENTION : Ici le nom doit être identique au SELECT du SQL
                            val prix = rsW.getDouble("prix_actuel")
                            val titre = rsW.getString("titre")
                            val plateforme = rsW.getString("plateforme")

                            println("• $titre [$plateforme] - $prix€")
                        }
                        if (!aDesSouhaits) println("Aucun jeu dans la liste de souhaits.")
                    }
                }

                // 3. Évaluations laissées par le joueur
                println("\n⭐ ÉVALUATIONS LAISSÉES :")
                val sqlEval = """
                    SELECT jc.titre, e.note, e.commentaire, e.date_publication, 
                           e.nombre_votes_utile, e.nombre_votes_pas_utile
                    FROM evaluation e
                    JOIN jeu_catalogue jc ON e.jeu_id = jc.id
                    WHERE e.joueur_pseudo = ?
                    ORDER BY e.date_publication DESC
                """.trimIndent()

                conn.prepareStatement(sqlEval).use { stmtE ->
                    stmtE.setString(1, pseudoRecherche)
                    stmtE.executeQuery().use { rsE ->
                        var aDesEvals = false
                        while (rsE.next()) {
                            aDesEvals = true
                            val likes = rsE.getInt("nombre_votes_utile")
                            val dislikes = rsE.getInt("nombre_votes_pas_utile")

                            println("--------------------------------------------")
                            println("Jeu         : ${rsE.getString("titre")}")
                            println("Note        : ${rsE.getInt("note")}/10")
                            println("Commentaire : \"${rsE.getString("commentaire")}\"")
                            println("Le          : ${rsE.getTimestamp("date_publication")}")
                            // Affichage des nouveaux compteurs
                            println("👍 Utile ($likes) | 👎 Pas utile ($dislikes)")
                        }
                        if (!aDesEvals) println("Aucune évaluation rédigée.")
                    }
                }

                println("============================================\n")
                true // Succès : on retourne true à la fin du bloc .use
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'affichage du profil : ${e.message}")
            return false // Erreur : on retourne false
        }
    }


    fun voterEvaluationParCible(titreJeu: String, pseudoAuteur: String, estUnLike: Boolean): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->
                conn.autoCommit = false // Début de la transaction

                // 1. Trouver l'ID de l'évaluation
                val findIdSql = """
                SELECT e.id FROM evaluation e
                JOIN jeu_catalogue jc ON e.jeu_id = jc.id
                WHERE jc.titre = ? AND e.joueur_pseudo = ?
            """.trimIndent()

                val evaluationId = conn.prepareStatement(findIdSql).use { stmt ->
                    stmt.setString(1, titreJeu)
                    stmt.setString(2, pseudoAuteur)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) rs.getInt("id") else null
                    }
                }

                if (evaluationId == null) {
                    println("❌ Aucune évaluation trouvée.")
                    return false
                }

                // Vérifier si un vote existe déjà
                val checkSql = "SELECT est_utile FROM votes_evaluation WHERE evaluation_id = ? AND votant_pseudo = ?"
                val ancienVote = conn.prepareStatement(checkSql).use { stmt ->
                    stmt.setInt(1, evaluationId)
                    stmt.setString(2, joueur.pseudo)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) rs.getBoolean("est_utile") else null
                    }
                }

                if (ancienVote != null) {
                    if (ancienVote == estUnLike) {
                        println("⚠️ Vous avez déjà voté ainsi.")
                        conn.rollback() // Annule la transaction par sécurité avant de quitter
                        return false
                    } else {
                        // Changement de vote
                        val upVoteSql = "UPDATE votes_evaluation SET est_utile = ? WHERE evaluation_id = ? AND votant_pseudo = ?"
                        conn.prepareStatement(upVoteSql).use { stmt ->
                            stmt.setBoolean(1, estUnLike)
                            stmt.setInt(2, evaluationId)
                            stmt.setString(3, joueur.pseudo)
                            stmt.executeUpdate()
                        }

                        val sqlCompteurs = if (estUnLike) {
                            "UPDATE evaluation SET nombre_votes_utile = nombre_votes_utile + 1, nombre_votes_pas_utile = nombre_votes_pas_utile - 1 WHERE id = ?"
                        } else {
                            "UPDATE evaluation SET nombre_votes_utile = nombre_votes_utile - 1, nombre_votes_pas_utile = nombre_votes_pas_utile + 1 WHERE id = ?"
                        }
                        conn.prepareStatement(sqlCompteurs).use { stmt ->
                            stmt.setInt(1, evaluationId)
                            stmt.executeUpdate()
                        }
                        println("🔄 Votre vote a été modifié et les compteurs mis à jour.")
                    }
                } else {
                    // Nouveau vote
                    val insertVoteSql = "INSERT INTO votes_evaluation (evaluation_id, votant_pseudo, est_utile) VALUES (?, ?, ?)"
                    conn.prepareStatement(insertVoteSql).use { stmt ->
                        stmt.setInt(1, evaluationId)
                        stmt.setString(2, joueur.pseudo)
                        stmt.setBoolean(3, estUnLike)
                        stmt.executeUpdate()
                    }

                    val sqlIncr = if (estUnLike) {
                        "UPDATE evaluation SET nombre_votes_utile = nombre_votes_utile + 1 WHERE id = ?"
                    } else {
                        "UPDATE evaluation SET nombre_votes_pas_utile = nombre_votes_pas_utile + 1 WHERE id = ?"
                    }
                    conn.prepareStatement(sqlIncr).use { stmt ->
                        stmt.setInt(1, evaluationId)
                        stmt.executeUpdate()
                    }
                    println("✅ Nouveau vote enregistré !")
                }

                conn.commit() // Valide définitivement toutes les opérations
                true
            }
        } catch (e: Exception) {
            println("⚠️ Erreur : ${e.message}")
            false
        }
    }


    fun envoyerDemandeAmi(pseudoDestinataire: String) {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            // 1. Ouverture de la connexion avec .use
            DriverManager.getConnection(url, user, pass).use { conn ->
                val sql = "INSERT INTO ami (joueur_pseudo, ami_pseudo, statut) VALUES (?, ?, 'EN_ATTENTE')"

                // 2. Préparation du statement avec .use pour une libération immédiate
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, joueur.pseudo) // Expéditeur (joueur connecté)
                    stmt.setString(2, pseudoDestinataire) // Destinataire

                    stmt.executeUpdate()
                    println("✉️ Demande d'ami envoyée à $pseudoDestinataire !")
                }
            } // La connexion est automatiquement fermée ici
        } catch (e: Exception) {
            // En cas de doublon (clé primaire violée), PostgreSQL lève une exception
            println("⚠️ Erreur : Impossible d'envoyer la demande. (Le joueur n'existe pas ou une demande est déjà en cours).")
        }
    }

    fun accepterDemandeAmi(pseudoExpediteur: String) {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->
                conn.autoCommit = false // Début de la transaction

                try {
                    // Mettre à jour la demande reçue (de 'EN_ATTENTE' à 'ACCEPTE')
                    val updateSql = "UPDATE ami SET statut = 'ACCEPTE' WHERE joueur_pseudo = ? AND ami_pseudo = ?"
                    val succesUpdate = conn.prepareStatement(updateSql).use { stmtUp ->
                        stmtUp.setString(1, pseudoExpediteur)
                        stmtUp.setString(2, joueur.pseudo)
                        stmtUp.executeUpdate() > 0
                    }

                    if (succesUpdate) {
                        // Créer la relation inverse pour que l'amitié soit réciproque
                        val insertSql = "INSERT INTO ami (joueur_pseudo, ami_pseudo, statut) VALUES (?, ?, 'ACCEPTE')"
                        conn.prepareStatement(insertSql).use { stmtIn ->
                            stmtIn.setString(1, joueur.pseudo)
                            stmtIn.setString(2, pseudoExpediteur)
                            stmtIn.executeUpdate()
                        }

                        conn.commit() // Valide les deux opérations
                        println("✅ Vous êtes maintenant ami avec $pseudoExpediteur !")
                    } else {
                        println("❌ Aucune demande en attente trouvée de la part de $pseudoExpediteur.")
                        conn.rollback()
                    }
                } catch (e: Exception) {
                    conn.rollback() // Annule tout en cas d'erreur durant le processus
                    throw e
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'acceptation : ${e.message}")
        }
    }

    fun afficherListeAmi() {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(url, user, pass).use { conn ->

                val sql = "SELECT ami_pseudo, date_ajout FROM ami WHERE joueur_pseudo = ? AND statut = 'ACCEPTE'"

                // Utilisation de .use pour le Statement et le ResultSet
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, joueur.pseudo)

                    stmt.executeQuery().use { rs ->
                        println("\n--- 👥 LISTE D'AMIS DE ${joueur.pseudo} ---")

                        var aDesAmis = false
                        while (rs.next()) {
                            aDesAmis = true
                            val ami = rs.getString("ami_pseudo")
                            val date = rs.getTimestamp("date_ajout")
                            println("• $ami (Amis depuis le : $date)")
                        }

                        if (!aDesAmis) {
                            println("Vous n'avez pas encore d'amis.")
                        }
                    } // Le ResultSet est fermé ici
                } // Le PreparedStatement est fermé ici
            } // La Connection est fermée ici
        } catch (e: Exception) {
            println("⚠️ Erreur d'affichage : ${e.message}")
        }
    }

    fun seConnecter(pseudoSaisi: String, mdpSaisi: String): Joueur? {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            return DriverManager.getConnection(url, user, pass).use { conn ->
                // On récupère les infos du joueur si le pseudo et le mot de passe correspondent
                val sql = "SELECT pseudo, nom, prenom, date_naissance FROM joueur WHERE pseudo = ? AND mot_de_passe = ?"

                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, pseudoSaisi)
                    stmt.setString(2, mdpSaisi)

                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            println("✅ Connexion réussie ! Ravie de vous revoir, ${rs.getString("prenom")}.")
                            // On retourne un objet Joueur complet pour mettre à jour la session
                            Joueur(
                                rs.getString("pseudo"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("date_naissance")
                            )
                        } else {
                            println("❌ Erreur : Pseudo ou mot de passe incorrect.")
                            null
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de la connexion : ${e.message}")
            return null
        }
    }


    fun consulterDemandeAmi(): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            Class.forName("org.postgresql.Driver")
            return DriverManager.getConnection(url, user, pass).use { conn ->
                // On cherche les demandes où l'utilisateur connecté est le destinataire (ami_pseudo)
                val sql = "SELECT joueur_pseudo, date_ajout FROM ami WHERE ami_pseudo = ? AND statut = 'EN_ATTENTE'"

                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, joueur.pseudo)

                    stmt.executeQuery().use { rs ->
                        println("\n--- ✉️ DEMANDES D'AMITIÉ REÇUES ---")
                        var aDesDemandes = false

                        while (rs.next()) {
                            aDesDemandes = true
                            val expediteur = rs.getString("joueur_pseudo")
                            val date = rs.getTimestamp("date_ajout")
                            println("• $expediteur (Reçue le : $date)")
                        }

                        if (!aDesDemandes) {
                            println("Aucune demande en attente.")
                            false
                        } else {
                            true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de la consultation des demandes : ${e.message}")
            return false
        }
    }


    fun ajouterALaWishlist(titreJeu: String): Boolean {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        return try {
            DriverManager.getConnection(url, user, pass).use { conn ->
                // 1. Trouver l'ID (VARCHAR) du jeu
                val sqlId = "SELECT id FROM jeu_catalogue WHERE titre = ? LIMIT 1"
                val jeuId = conn.prepareStatement(sqlId).use { stmt ->
                    stmt.setString(1, titreJeu)
                    stmt.executeQuery().use { rs -> if (rs.next()) rs.getString("id") else null }
                }

                if (jeuId == null) {
                    println("❌ Jeu non trouvé dans le catalogue.")
                    return false
                }

                // 2. VÉRIFICATION : Est-ce que le joueur possède déjà ce jeu ?
                val sqlCheckPossede = "SELECT 1 FROM jeu_possede WHERE joueur_pseudo = ? AND jeu_id = ?"
                val dejaPossede = conn.prepareStatement(sqlCheckPossede).use { stmt ->
                    stmt.setString(1, joueur.pseudo)
                    stmt.setString(2, jeuId)
                    stmt.executeQuery().use { rs -> rs.next() }
                }

                if (dejaPossede) {
                    println("❌ Impossible : Vous possédez déjà '$titreJeu' dans votre bibliothèque !")
                    return false
                }

                // 3. Insertion dans la wishlist
                // Le bloc catch gérera automatiquement si le jeu est déjà en wishlist (Doublon PK)
                val sqlInsert = "INSERT INTO wishlist (joueur_pseudo, jeu_id) VALUES (?, ?)"
                conn.prepareStatement(sqlInsert).use { stmt ->
                    stmt.setString(1, joueur.pseudo)
                    stmt.setString(2, jeuId)
                    stmt.executeUpdate()
                }

                println("💖 $titreJeu a été ajouté à votre liste de souhaits !")
                true
            }
        } catch (e: Exception) {
            // En PostgreSQL, l'erreur de duplication (23505) est levée si la PK existe déjà
            println("⚠️ Info : Ce jeu est probablement déjà dans votre wishlist.")
            false
        }
    }
    fun afficherWishlist() {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            DriverManager.getConnection(url, user, pass).use { conn ->
                val sql = """
                SELECT jc.titre, jc.prix_actuel, jc.plateforme 
                FROM wishlist w
                JOIN jeu_catalogue jc ON w.jeu_id = jc.id
                WHERE w.joueur_pseudo = ?
            """.trimIndent()

                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, joueur.pseudo)
                    stmt.executeQuery().use { rs ->
                        println("\n--- ✨ MA WISHLIST (${joueur.pseudo}) ---")
                        var empty = true
                        while (rs.next()) {
                            empty = false
                            println("• ${rs.getString("titre")} [${rs.getString("plateforme")}] - ${rs.getDouble("prix_actuel")}€")
                        }
                        if (empty) println("Votre wishlist est vide.")
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de l'affichage de la wishlist : ${e.message}")
        }
    }

    fun afficherCatalogueTitres() {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            DriverManager.getConnection(url, user, pass).use { conn ->
                // On récupère les titres distincts pour ne pas afficher 4 fois le même jeu s'il est sur 4 supports
                val sql = "SELECT DISTINCT titre, prix_actuel FROM jeu_catalogue ORDER BY titre ASC"

                conn.prepareStatement(sql).use { stmt ->
                    stmt.executeQuery().use { rs ->
                        println("\n--- 📚 CATALOGUE DES JEUX DISPONIBLES ---")
                        var count = 0
                        while (rs.next()) {
                            count++
                            val titre = rs.getString("titre")
                            val prix = rs.getDouble("prix_actuel")
                            println("$count. $titre (À partir de $prix€)")
                        }
                        if (count == 0) println("Le catalogue est actuellement vide.")
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de la récupération du catalogue : ${e.message}")
        }
    }

    fun consulterEditeur(nomEditeur: String) {
        val url = "jdbc:postgresql://86.252.172.215:5432/polysteam"
        val user = "polysteam_user"
        val pass = "PolySteam2026!"

        try {
            DriverManager.getConnection(url, user, pass).use { conn ->
                // Informations générales de l'éditeur
                val sqlEditeur = "SELECT id, nom, date_creation, est_independant FROM editeur WHERE nom ILIKE ?"

                val editeurId = conn.prepareStatement(sqlEditeur).use { stmt ->
                    stmt.setString(1, nomEditeur)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            val estIndep = if (rs.getBoolean("est_independant")) "Oui ✅" else "Non 🏢"
                            println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            println("🏢 ÉDITEUR : ${rs.getString("nom").uppercase()}")
                            println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                            println("📅 Création      : ${rs.getDate("date_creation")}")
                            println("🌿 Indépendant   : $estIndep")
                            rs.getString("id")
                        } else null
                    }
                }

                if (editeurId == null) {
                    println("❌ Aucun éditeur trouvé au nom de '$nomEditeur'.")
                    return
                }

                // Liste des jeux possédés dans le catalogue
                val sqlJeux = """
                SELECT DISTINCT titre, plateforme, prix_actuel 
                FROM jeu_catalogue 
                WHERE editeur_id = ? 
                ORDER BY titre ASC
            """.trimIndent()

                conn.prepareStatement(sqlJeux).use { stmtJ ->
                    stmtJ.setString(1, editeurId)
                    stmtJ.executeQuery().use { rsJ ->
                        println("\n📚 JEUX AU CATALOGUE :")
                        var aDesJeux = false
                        while (rsJ.next()) {
                            aDesJeux = true
                            println(" • ${rsJ.getString("titre")} [${rsJ.getString("plateforme")}] - ${rsJ.getDouble("prix_actuel")}€")
                        }
                        if (!aDesJeux) println(" Aucun jeu répertorié pour cet éditeur.")
                        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Erreur lors de la consultation : ${e.message}")
        }
    }





}