package com.guardaroba.motore

import kotlin.random.Random

/** Un Capo come lo vede il Motore: solo ciò che conta per gli abbinamenti. Il [coloreSecondario] non vincola. */
data class Capo(
    val id: Long,
    val categoria: Categoria,
    val colorePrincipale: Colore,
    val motivo: Motivo = Motivo.TINTA_UNITA,
    val coloreSecondario: Colore? = null,
    val stagione: Stagione = Stagione.TUTTO_L_ANNO,
)

/** Un insieme di Capi indossabili insieme; si identifica con l'insieme degli id dei suoi Capi. */
data class Outfit(val capi: List<Capo>) {
    val idCapi: Set<Long> get() = capi.map { it.id }.toSet()
}

sealed interface Richiesta {
    /** null = nessun filtro, entrano tutti i Capi. */
    val filtroStagione: Stagione?

    data class Genera(override val filtroStagione: Stagione? = null) : Richiesta

    /** "Parti da questo capo": [capo] è in ogni Proposta, anche se è un Capospalla o un Accessorio, e il filtro non lo esclude. */
    data class PartiDa(val capo: Capo, override val filtroStagione: Stagione? = null) : Richiesta
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
    val filtro = richiesta.filtroStagione
    val partenza = (richiesta as? Richiesta.PartiDa)?.capo
    val perCategoria = guardaroba.filter { it.id != partenza?.id && (filtro == null || filtro.ammette(it.stagione)) }
        .groupBy { it.categoria }.toMutableMap()
    if (partenza != null) {
        // Il Capo di partenza è l'unico della sua Categoria, e le Categorie che esclude non entrano.
        perCategoria[partenza.categoria] = listOf(partenza)
        partenza.categoria.escluse.forEach(perCategoria::remove)
    }
    fun capi(categoria: Categoria) = perCategoria[categoria].orEmpty()
    val mancanti = mancanti(perCategoria.keys)
    if (mancanti.isNotEmpty()) return Esito.MancaCategoria(mancanti)
    // shortcut: enumerazione completa degli Outfit possibili, adeguata fino a qualche centinaio di Capi (spec).
    val basi = capi(Categoria.SOPRA).flatMap { sopra -> capi(Categoria.SOTTO).map { sotto -> listOf(sopra, sotto) } } +
        capi(Categoria.INTERO).map { intero -> listOf(intero) }
    fun proposte(candidati: List<Outfit>) = candidati.mapNotNull { o -> (giudica(o) as? Giudizio.Valido)?.let { Esito.Proposta(o, it.spiegazione) } }
    // Con invernale o mezza stagione ogni Capospalla compatibile dà una Proposta; senza nessuno, l'Outfit resta senza.
    // Un Capospalla di partenza invece entra sempre: senza di lui non c'è Proposta.
    val capospallaDiPartenza = partenza?.categoria == Categoria.CAPOSPALLA
    val capospalla = if (capospallaDiPartenza || filtro == Stagione.INVERNALE || filtro == Stagione.MEZZA_STAGIONE) {
        capi(Categoria.CAPOSPALLA)
    } else {
        emptyList()
    }
    val accessori = listOfNotNull(partenza?.takeIf { it.categoria == Categoria.ACCESSORI }) // solo come Capo di partenza
    val valide = basi.flatMap { base ->
        capi(Categoria.SCARPE).flatMap { scarpe ->
            val conCapospalla = proposte(capospalla.map { Outfit(base + it + scarpe + accessori) })
            if (capospallaDiPartenza) conCapospalla else conCapospalla.ifEmpty { proposte(listOf(Outfit(base + scarpe + accessori))) }
        }
    }
    if (valide.isEmpty()) return Esito.NessunAbbinamento
    return valide.filter { it.outfit.idCapi !in giàViste }.randomOrNull(caso) ?: Esito.Esaurite
}

/** Le Categorie obbligatorie assenti tra [presenti]: Sopra e Sotto mancano solo se manca anche l'alternativa, l'Intero. */
private fun mancanti(presenti: Set<Categoria>): List<Categoria> =
    listOf(Categoria.SOPRA, Categoria.SOTTO).filterNot { Categoria.INTERO in presenti || it in presenti } +
        listOf(Categoria.SCARPE).filterNot { it in presenti }

/** Il giudizio del Motore su un Outfit: valido con la sua Spiegazione, o non valido con il motivo. */
sealed interface Giudizio {
    data class Valido(val spiegazione: String) : Giudizio
    data class NonValido(val motivo: String) : Giudizio
}

/**
 * Giudica un Outfit qualsiasi, anche uno non fatto da [proponi] (un Preferito con Capi modificati): una sola Categoria
 * per Capo, nessuna Categoria esclusa, quelle obbligatorie presenti, poi Motivi e Colori. Gli Accessori non contano
 * per Motivi e Colori (ADR-0004). I Colori si elencano in ordine di Categoria.
 */
fun giudica(outfit: Outfit): Giudizio {
    val capi = outfit.capi.sortedBy { it.categoria }
    val categorie = capi.groupingBy { it.categoria }.eachCount()
    categorie.entries.find { it.value > 1 }?.let { return Giudizio.NonValido("più di un Capo ${it.key.nome}") }
    capi.firstNotNullOfOrNull { c -> c.categoria.escluse.find { it in categorie }?.let { c.categoria to it } }
        ?.let { (a, b) -> return Giudizio.NonValido("${a.nome} e ${b.nome} insieme") }
    mancanti(categorie.keys).takeIf { it.isNotEmpty() }
        ?.let { m -> return Giudizio.NonValido("manca " + m.joinToString(" e ") { it.nome }) }
    val colorati = capi.filter { it.categoria != Categoria.ACCESSORI }
    if (colorati.count { it.motivo != Motivo.TINTA_UNITA } > 1) return Giudizio.NonValido("più di un Capo non in tinta unita")
    val colori = colorati.map { it.colorePrincipale }.distinct()
    val elenco = colori.joinToString(" + ") { it.nome }
    return regola(colori)?.let { Giudizio.Valido("$elenco: $it") } ?: Giudizio.NonValido("$elenco non si abbinano")
}

/** La regola che rende validi insieme questi Colori distinti, o null se non stanno insieme. */
private fun regola(colori: List<Colore>): String? {
    val nonNeutri = colori.filterNot { it.neutro }
    return when (nonNeutri.size) {
        0 -> "neutri"
        1 -> "un colore + neutri"
        2 -> armonia(nonNeutri[0], nonNeutri[1])
        else -> null
    }
}

/** Un Colore della Palette che sta bene con un Capo, con la regola che li abbina (es. "complementari"). */
data class Consiglio(val colore: Colore, val regola: String)

/** I Colori della Palette, in ordine di Palette, che starebbero bene col Colore principale del [capo], tranne lo stesso. */
fun coloriConsigliati(capo: Capo): List<Consiglio> = (PALETTE - capo.colorePrincipale)
    .mapNotNull { c -> regola(listOf(capo.colorePrincipale, c))?.let { Consiglio(c, it) } }
