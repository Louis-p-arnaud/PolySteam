package model

data class Jeux(
    private var _nomJeux: String,
    private var _prix: Int,
    private var _types: List<String>
) {
    // Getters et Setters
    var nomJeux: String
        get() = _nomJeux
        set(value) { _nomJeux = value }

    var prix: Int
        get() = _prix
        set(value) { _prix = value }

    var types: List<String>
        get() = _types
        set(value) { _types = value }
}
