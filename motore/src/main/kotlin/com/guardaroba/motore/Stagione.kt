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

/** Un Capo con [stagioneCapo] è fuori stagione se c'è un [filtro] e non lo ammette (es. un Capo di partenza invernale con filtro estivo). */
fun fuoriStagione(stagioneCapo: Stagione, filtro: Stagione?) = filtro != null && !filtro.ammette(stagioneCapo)
