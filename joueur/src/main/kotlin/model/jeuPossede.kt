package model

data class JeuPossede(
    val id: Int,
    val joueurPseudo: String,
    val jeuId: String,
    var tempsJeuMinutes: Long = 0, // Stocké en minutes selon l'ERD
    val versionInstallee: String
)