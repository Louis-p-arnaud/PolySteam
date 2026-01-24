package model

import kotlin.random.Random

data class Jeu(
    val id: String, // UUID
    val titre: String,
    val editeurId: String,
    val plateforme: String,
    val versionActuelle: String,
    val probabiliteCrash: Double = 0.05 // 5% de chance par défaut
) {
    /**
     * Simule le lancement du jeu.
     * Retourne 'true' si le jeu crash, 'false' s'il fonctionne.
     */
    fun lancerJeu(): Boolean {
        return Random.nextDouble() < probabiliteCrash
    }
}