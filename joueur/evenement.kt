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
}