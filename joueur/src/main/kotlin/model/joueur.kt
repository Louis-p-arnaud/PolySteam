package model

import java.util.Date

data class Joueur(
    val pseudo: String, // Clé primaire
    val nom: String,
    val prenom: String,
    val dateNaissance: String,
    val dateInscription: Date = Date()
)