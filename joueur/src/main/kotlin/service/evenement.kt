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

            // Création du rapport d'incident (Avro) conforme à ton nouveau besoin
            val rapport = RapportIncidentEvent.newBuilder()
                .setId(java.util.UUID.randomUUID().toString())
                .setJeuId(jeu.id)
                .setJoueurPseudo(joueur.pseudo)
                .setVersionJeu(jeu.versionActuelle)
                .setPlateforme(jeu.plateforme)
                .setDescriptionErreur("Crash critique lors de l'exécution (Simulation probabilité)")
                .setTimestamp(System.currentTimeMillis())
                .build()

            // Envoi immédiat à Kafka via le nouveau Producer
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
}