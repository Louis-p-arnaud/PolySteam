package service

import com.projet.joueur.ConsulterProfilEvent
import com.projet.joueur.EvaluationEvent
import com.projet.joueur.ReactionEvent
import model.Jeux
import model.Joueur
import com.projet.joueur.TempsJeuEvent

class Evenement(private val joueur: Joueur) {

    fun inscriptionUtilisateurPlateforme() {
        joueur.estInscrit = true
        println("${joueur.pseudo} est inscrit.")
    }

    fun achatJeu(jeu: Jeux, support: String) {
        joueur.possedeJeux[jeu.nomJeux] = Triple(jeu, support, "1.0")
        println("Achat de ${jeu.nomJeux} sur $support.")
    }

    fun creerCommentaireJeu(jeu: Jeux) {
        val temps = joueur.mapTempsDeJeux[jeu.nomJeux] ?: 0f
        // Vérification possession et temps de jeu suffisant [cite: 70]
        if (joueur.possedeJeux.containsKey(jeu.nomJeux) && temps >= 1.0f) {
            println("Commentaire autorisé pour ${jeu.nomJeux}.")
        } else {
            println("Action refusée : conditions non remplies.")
        }
    }

    fun simulerSessionDeJeu(jeu: Jeux, heures: Float): TempsJeuEvent {
        // Mise à jour locale du temps de jeu (Logique Métier)
        val tempsActuel = joueur.mapTempsDeJeux[jeu.nomJeux] ?: 0f
        joueur.mapTempsDeJeux[jeu.nomJeux] = tempsActuel + heures

        println("🎮 ${joueur.pseudo} a joué à ${jeu.nomJeux} pendant $heures h.")
        println("⏳ Temps total sur ce jeu : ${joueur.mapTempsDeJeux[jeu.nomJeux]} h.")

        // Création de l'objet Avro pour informer la plateforme
        return TempsJeuEvent.newBuilder()
            .setPseudo(joueur.pseudo)
            .setNomJeu(jeu.nomJeux)
            .setHeuresAjoutees(heures)
            .setTimestamp(System.currentTimeMillis())
            .build()
    }

    /**
     * Crée une évaluation pour un jeu.
     * Vérifie la règle métier : temps de jeu > 1h.
     */
    fun creerEvaluation(nomJeu: String, note: Int, commentaire: String): EvaluationEvent? {
        val tempsDeJeu = joueur.mapTempsDeJeux[nomJeu] ?: 0f

        if (tempsDeJeu < 1.0f) {
            println("❌ Action impossible : Vous n'avez que ${tempsDeJeu}h sur $nomJeu (minimum 1h requis).")
            return null
        }

        return EvaluationEvent.newBuilder()
            .setPseudo(joueur.pseudo)
            .setNomJeu(nomJeu)
            .setNote(note)
            .setCommentaire(commentaire)
            .setTimestamp(System.currentTimeMillis())
            .build()
    }

    /**
     * Crée une réaction (Like/Dislike) sur le commentaire d'un autre joueur.
     */
    fun reagirAUnCommentaire(pseudoAuteur: String, utile: Boolean): ReactionEvent {
        return ReactionEvent.newBuilder()
            .setPseudoAuteurReaction(joueur.pseudo)
            .setPseudoCible(pseudoAuteur)
            .setEstUtile(utile)
            .setTimestamp(System.currentTimeMillis())
            .build()
    }

    /**
     * Enregistre l'intention de consulter le profil d'un autre joueur.
     */
    fun consulterProfil(pseudoCible: String): ConsulterProfilEvent {
        return ConsulterProfilEvent.newBuilder()
            .setPseudoVisiteur(joueur.pseudo)
            .setPseudoConsulte(pseudoCible)
            .setTimestamp(System.currentTimeMillis())
            .build()
    }






    fun affichageFluxInformation() {
        println("📡 Affichage du flux d'actualités pour ${joueur.pseudo}...")
    }

    fun LikerCommentaireJeu() {
        println("👍 Vous avez aimé un commentaire.")
    }

    fun DislikerCommentaireJeu() {
        println("👎 Vous avez disliké un commentaire.")
    }

    fun consulterJoueur(autreJoueur: Joueur) {
        println("👤 Consultation du profil de ${autreJoueur.pseudo} par ${joueur.pseudo}.")
    }

    fun consulterPageJeux() {
        println("📖 Consultation de la boutique/catalogue des jeux.")
    }

    fun consulterFluxInformation() {
        println("🔍 Consultation détaillée du flux d'information.")
    }
}