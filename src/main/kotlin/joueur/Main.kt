import model.Jeux
import model.Joueur
import service.Evenement
import java.time.LocalDate

fun main() {
    val j = Joueur("Sniper99", "Dupont", "Jean", LocalDate.of(2000, 5, 15))
    val engine = Evenement(j)
    val elden = Jeux("Elden Ring", 60, listOf("RPG"))

    engine.inscriptionUtilisateurPlateforme()
    engine.achatJeu(elden, "PS5")

    j.mapTempsDeJeux[elden.nomJeux] = 2.5f // Simule le temps passé [cite: 50]
    engine.creerCommentaireJeu(elden)
}