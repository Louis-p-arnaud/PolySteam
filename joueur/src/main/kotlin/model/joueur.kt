package model

import java.time.LocalDate

data class Joueur(
    private val _pseudo: String, // Unique et immuable après création
    private var _nom: String,
    private var _prenom: String,
    private var _dateDeNaissance: String
) {
    private var _estInscrit: Boolean = false
    private val _possedeJeux = mutableMapOf<String, Triple<Jeux, String, String>>() // [cite: 62]
    private val _wishList = mutableListOf<Jeux>() // [cite: 65, 68]
    private val _mapTempsDeJeux = mutableMapOf<String, Float>() // [cite: 50]

    // Getters et setters
    val pseudo: String get() = _pseudo
    var nom: String get() = _nom; set(value) { _nom = value }
    var prenom: String get() = _prenom; set(value) { _prenom = value }
    var dateDeNaissance: String get() = _dateDeNaissance; set(value) { _dateDeNaissance = value }
    var estInscrit: Boolean get() = _estInscrit; set(value) { _estInscrit = value }

    val possedeJeux get() = _possedeJeux
    val wishList get() = _wishList
    val mapTempsDeJeux get() = _mapTempsDeJeux
}