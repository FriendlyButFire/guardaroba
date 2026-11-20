package com.guardaroba.motore

import kotlin.random.Random

/** Un Capo come lo vede il Motore: solo ciò che conta per gli abbinamenti. Il [coloreSecondario] non vincola. */
data class Capo(
    val id: Long,
    val categoria: Categoria,
    val colorePrincipale: Colore,
    val motivo: Motivo = Motivo.TINTA_UNITA,
    val coloreSecondario: Colore? = null,
)

/** Un insieme di Capi indossabili insieme; si identifica con l'insieme degli id dei suoi Capi. */
data class Outfit(val capi: List<Capo>) {
    val idCapi: Set<Long> get() = capi.map { it.id }.toSet()
}

sealed interface Richiesta {
    data object Genera : Richiesta
}

sealed interface Esito {
    data class Proposta(val outfit: Outfit, val spiegazione: String) : Esito
    data class MancaCategoria(val categorie: List<Categoria>) : Esito
    data object NessunAbbinamento : Esito
    data object Esaurite : Esito
}

/**
 * Propone a caso un Outfit valido fatto con i Capi del [guardaroba], escludendo quelli in [giàViste]
 * (insiemi di id dei Capi, vedi [Outfit.idCapi]). [caso] è iniettabile per avere test deterministici.
 */
fun proponi(guardaroba: List<Capo>, richiesta: Richiesta, giàViste: Set<Set<Long>>, caso: Random = Random.Default): Esito {
    val perCategoria = guardaroba.groupBy { it.categoria }
    fun capi(categoria: Categoria) = perCategoria[categoria].orEmpty()
    // Sopra e Sotto mancano solo se manca anche l'alternativa, l'Intero.
    val mancanti = listOf(Categoria.SOPRA, Categoria.SOTTO).filter { it !in perCategoria && Categoria.INTERO !in perCategoria } +
        listOf(Categoria.SCARPE).filter { it !in perCategoria }
    if (mancanti.isNotEmpty()) return Esito.MancaCategoria(mancanti)
    // shortcut: enumerazione completa delle combinazioni, adeguata fino a qualche centinaio di Capi (spec).
    val basi = capi(Categoria.SOPRA).flatMap { sopra -> capi(Categoria.SOTTO).map { sotto -> listOf(sopra, sotto) } } +
        capi(Categoria.INTERO).map { intero -> listOf(intero) }
    val valide = basi.flatMap { base ->
        capi(Categoria.SCARPE).map { scarpe -> Outfit(base + scarpe) }
    }.mapNotNull { outfit -> spiegazione(outfit)?.let { Esito.Proposta(outfit, it) } }
    if (valide.isEmpty()) return Esito.NessunAbbinamento
    return valide.filter { it.outfit.idCapi !in giàViste }.randomOrNull(caso) ?: Esito.Esaurite
}

/** La Spiegazione dell'Outfit, o null se l'Outfit non è valido. */
private fun spiegazione(outfit: Outfit): String? {
    if (outfit.capi.count { it.motivo != Motivo.TINTA_UNITA } > 1) return null
    val colori = outfit.capi.map { it.colorePrincipale }.distinct()
    val nonNeutri = colori.filterNot { it.neutro }
    val regola = when (nonNeutri.size) {
        0 -> "neutri"
        1 -> "un colore + neutri"
        2 -> armonia(nonNeutri[0], nonNeutri[1]) ?: return null
        else -> return null
    }
    return colori.joinToString(" + ") { it.nome } + ": " + regola
}
