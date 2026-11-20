package com.guardaroba.motore

import kotlin.random.Random

/** Un Capo come lo vede il Motore: solo ciò che conta per gli abbinamenti. */
data class Capo(val id: Long, val categoria: Categoria, val colorePrincipale: Colore)

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

// shortcut: niente Intero, Outfit solo Sopra + Sotto + Scarpe; l'alternativa Intero arriva col ticket 10.
private val NECESSARIE = listOf(Categoria.SOPRA, Categoria.SOTTO, Categoria.SCARPE)

/**
 * Propone a caso un Outfit valido fatto con i Capi del [guardaroba], escludendo quelli in [giàViste]
 * (insiemi di id dei Capi, vedi [Outfit.idCapi]). [caso] è iniettabile per avere test deterministici.
 */
fun proponi(guardaroba: List<Capo>, richiesta: Richiesta, giàViste: Set<Set<Long>>, caso: Random = Random.Default): Esito {
    val perCategoria = guardaroba.groupBy { it.categoria }
    val mancanti = NECESSARIE.filter { it !in perCategoria }
    if (mancanti.isNotEmpty()) return Esito.MancaCategoria(mancanti)
    // shortcut: enumerazione completa delle combinazioni, adeguata fino a qualche centinaio di Capi (spec).
    val valide = perCategoria.getValue(Categoria.SOPRA).flatMap { sopra ->
        perCategoria.getValue(Categoria.SOTTO).flatMap { sotto ->
            perCategoria.getValue(Categoria.SCARPE).map { scarpe -> Outfit(listOf(sopra, sotto, scarpe)) }
        }
    }.mapNotNull { outfit -> spiegazione(outfit)?.let { Esito.Proposta(outfit, it) } }
    if (valide.isEmpty()) return Esito.NessunAbbinamento
    return valide.filter { it.outfit.idCapi !in giàViste }.randomOrNull(caso) ?: Esito.Esaurite
}

/** La Spiegazione dell'Outfit, o null se l'Outfit non è valido. */
private fun spiegazione(outfit: Outfit): String? {
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
