import model.Jeux
import model.Joueur
import service.Evenement
import java.time.LocalDate
import com.projet.joueur.AchatJeuEvent


fun main() {
    val j = Joueur("Sniper99", "Dupont", "Jean", LocalDate.of(2000, 5, 15))
    val engine = Evenement(j)
    val elden = Jeux("Elden Ring", 60, listOf("RPG"))

    engine.inscriptionUtilisateurPlateforme()
    engine.achatJeu(elden, "PS5")

    j.mapTempsDeJeux[elden.nomJeux] = 2.5f // Simule le temps passé [cite: 50]
    engine.creerCommentaireJeu(elden)

    try {
        val event = AchatJeuEvent.newBuilder()
            .setPseudo("German")           // Pos 0
            .setNomJeu("Elden Ring")       // Pos 1
            .setSupport("PS5")          // Pos 2 (C'est probablement celui-ci qui manque !)
            .setPrixPaye(50)
            .setTimestamp(System.currentTimeMillis())
            .build()

        println("✅ Succès Avro : Objet créé pour le joueur ${event.getPseudo()}")
    } catch (e: Exception) {
        println("❌ Erreur Avro : ${e.message}")
    }
}