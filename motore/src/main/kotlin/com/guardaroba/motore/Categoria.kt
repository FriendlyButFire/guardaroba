package com.guardaroba.motore

/** Il ruolo del Capo nell'Outfit, con i suoi Tipi (vedi GLOSSARY). */
enum class Categoria(val nome: String, val tipi: List<String>) {
    SOPRA("Sopra", listOf("t-shirt", "camicia", "polo", "maglione", "felpa", "top")),
    SOTTO("Sotto", listOf("pantaloni", "jeans", "shorts", "gonna")),
    INTERO("Intero", listOf("vestito", "tuta")),
    CAPOSPALLA("Capospalla", listOf("giacca", "cappotto", "giubbotto", "blazer")),
    SCARPE("Scarpe", listOf("sneakers", "eleganti", "stivali", "sandali")),
    ACCESSORI("Accessori", listOf("cintura", "borsa", "cappello", "sciarpa"));

    /** Le Categorie che non stanno in un Outfit con questa: Sopra e Sotto escludono l'Intero, e viceversa. */
    val escluse: Set<Categoria>
        get() = when (this) {
            SOPRA, SOTTO -> setOf(INTERO)
            INTERO -> setOf(SOPRA, SOTTO)
            else -> emptySet()
        }
}
