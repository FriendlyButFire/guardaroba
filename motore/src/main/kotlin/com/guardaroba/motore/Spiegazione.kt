package com.guardaroba.motore

/** La Spiegazione di un Outfit valido in parole semplici: la [regola] che lo rende valido (es. "complementari") e la [frase] che dice perché funziona. */
data class Perché(val regola: String, val frase: String)

/** Perché i Colori dell'Outfit stanno bene insieme, ricavato dalle stesse regole di [giudica]; null se [giudica] non lo trova valido. */
fun perché(outfit: Outfit): Perché? {
    if (giudica(outfit) !is Giudizio.Valido) return null
    val colori = coloriPrincipali(outfit)
    val regola = regola(colori) ?: return null
    val (nonNeutri, neutri) = colori.partition { !it.neutro }
    val frase = when (nonNeutri.size) {
        0 -> if (neutri.size == 1) {
            "Tutto in ${neutri[0].nome}: un neutro da solo non sbaglia mai."
        } else {
            "${conArticolo(neutri)} sono neutri: stanno bene tra loro e con qualsiasi colore."
        }
        1 -> if (neutri.isEmpty()) {
            "Tutto in ${nonNeutri[0].nome}: un colore solo non ha niente con cui stonare."
        } else if (neutri.size == 1) {
            "${conArticolo(neutri)} è neutro: fa risaltare ${conArticolo(nonNeutri)} senza competere."
        } else {
            "${conArticolo(neutri)} sono neutri: fanno risaltare ${conArticolo(nonNeutri)} senza competere."
        }
        else -> "${conArticolo(nonNeutri)} " + when (regola) {
            "monocromatico" -> "sono due toni della stessa tinta: stanno insieme senza stonare."
            "analoghi" -> "sono vicini sulla ruota dei colori: insieme sono armoniosi."
            "complementari" -> "sono opposti sulla ruota dei colori: si fanno risaltare a vicenda."
            else -> error("Armonia senza frase: $regola")
        } + accompagnano(neutri)
    }
    return Perché(regola, frase.replaceFirstChar { it.uppercase() })
}

/** La seconda frase per i Neutri accanto a due colori in Armonia; vuota senza Neutri. */
private fun accompagnano(neutri: List<Colore>): String = when (neutri.size) {
    0 -> ""
    1 -> " ${conArticolo(neutri).replaceFirstChar { it.uppercase() }}, neutro, li accompagna senza competere."
    else -> " ${conArticolo(neutri).replaceFirstChar { it.uppercase() }}, neutri, li accompagnano senza competere."
}

/** "il nero", "l'arancione", "il nero e il bianco", "il nero, il bianco e il denim". */
private fun conArticolo(colori: List<Colore>): String {
    val nomi = colori.map { if (it.nome.first() in "aeiou") "l'${it.nome}" else "il ${it.nome}" }
    return if (nomi.size == 1) nomi[0] else nomi.dropLast(1).joinToString(", ") + " e " + nomi.last()
}
