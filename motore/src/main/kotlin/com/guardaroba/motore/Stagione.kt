package com.guardaroba.motore

/** Il periodo in cui si indossa un Capo (vedi GLOSSARY). */
enum class Stagione(val nome: String) {
    ESTIVO("estivo"),
    INVERNALE("invernale"),
    MEZZA_STAGIONE("mezza stagione"),
    TUTTO_L_ANNO("tutto l'anno"),
}

/** Il filtro Stagione: un Capo entra se ha la Stagione del filtro oppure "tutto l'anno". */
fun Stagione.ammette(stagioneCapo: Stagione) = stagioneCapo == this || stagioneCapo == Stagione.TUTTO_L_ANNO
