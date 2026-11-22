package com.guardaroba.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.CapoDao
import com.guardaroba.archivio.GuardarobaDatabase
import com.guardaroba.archivio.PreferitoDao
import com.guardaroba.archivio.perMotore
import com.guardaroba.motore.Consiglio
import com.guardaroba.motore.Esito
import com.guardaroba.motore.Richiesta
import com.guardaroba.motore.Stagione
import com.guardaroba.motore.coloriConsigliati
import com.guardaroba.motore.fuoriStagione
import com.guardaroba.motore.proponi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.guardaroba.motore.Capo as CapoMotore

/** L'ultimo Esito del Motore, con i Capi dell'Archivio per mostrarne le miniature e i Colori consigliati per il Capo di partenza. */
data class StatoOutfit(val esito: Esito, val capi: Map<Long, Capo>, val consigliati: List<Consiglio>)

/** Il Capo di partenza ha [stagioneCapo], che il [filtro] attivo non ammette (ADR-0008). */
data class FuoriStagione(val stagioneCapo: Stagione, val filtro: Stagione)

class OutfitViewModel(private val dao: CapoDao, private val preferiti: PreferitoDao) : ViewModel() {
    var stato by mutableStateOf<StatoOutfit?>(null)
        private set

    /** Il filtro Stagione scelto prima di generare; null = nessun filtro. */
    var filtroStagione by mutableStateOf<Stagione?>(null)
        private set

    /** L'id del Capo di "Parti da questo capo"; null = Genera. */
    var partenza by mutableStateOf<Long?>(null)
        private set

    /** L'avviso da mostrare quando il filtro non ammette il Capo di partenza (ADR-0008); null = nessun avviso. */
    var avvisoFuoriStagione by mutableStateOf<FuoriStagione?>(null)
        private set

    /** Gli Outfit tra i Preferiti, come insiemi di id dei Capi: segue anche le eliminazioni dalla lista. */
    val outfitPreferiti = preferiti.tutti()
        .map { lista -> lista.map { p -> p.capi.map { it.id }.toSet() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Le Proposte mostrate prima di quella attuale, l'ultima in fondo: "Precedente" le riprende a ritroso. */
    private val precedenti = mutableStateListOf<Esito.Proposta>()

    /** C'è una Proposta a cui tornare con "Precedente". */
    val cèPrecedente get() = precedenti.isNotEmpty()

    /** Gli Outfit già proposti in questa sessione, come insiemi di id dei Capi. */
    private val giàViste = mutableSetOf<Set<Long>>()

    /** Il Guardaroba dell'ultimo aggiornamento, come lo vede il Motore: dice quali Capi sono cambiati. */
    private var ultimi = emptyMap<Long, CapoMotore>()

    init {
        // ADR-0010: se un Capo della Proposta mostrata cambia (per il Motore) o sparisce, la Proposta (e la sua Spiegazione) non vale più.
        viewModelScope.launch {
            dao.tutti().collect { capi ->
                val attuali = capi.associateBy { it.id }
                val perMotore = capi.associate { it.id to it.perMotore() }
                // Un Outfit con un Capo cambiato o sparito è un Outfit nuovo: esce dalle già viste, qualunque Esito sia mostrato.
                val cambiati = ultimi.keys.filter { perMotore[it] != ultimi[it] }.toSet()
                ultimi = perMotore
                giàViste.removeAll { visto -> visto.any(cambiati::contains) }
                // ADR-0010 anche per le Proposte di prima: con un Capo cambiato o sparito la loro Spiegazione non vale più.
                precedenti.removeAll { p -> p.outfit.capi.any { perMotore[it.id] != it } }
                val daCapo = partenza
                if (daCapo != null && daCapo !in attuali) {
                    partiDa(null) // il Capo di partenza è sparito: si torna a Genera
                    return@collect
                }
                val s = stato ?: return@collect
                val proposta = s.esito as? Esito.Proposta
                if (proposta != null && proposta.outfit.capi.any { perMotore[it.id] != it }) {
                    stato = null
                    if (daCapo != null) genera()
                    return@collect
                }
                // Il resto (foto, Tipo, Taglia, i Colori del Capo di partenza) si aggiorna senza toccare l'Esito.
                val aggiornato = s.copy(capi = attuali, consigliati = daCapo?.let { coloriConsigliati(perMotore.getValue(it)) }.orEmpty())
                if (aggiornato != s) stato = aggiornato
            }
        }
    }

    /** Chiede una nuova Proposta al Motore ("Genera" e "un altro"), partendo dal Capo di [partenza] se c'è. */
    fun genera() {
        viewModelScope.launch {
            val capi = dao.tutti().first()
            val perMotore = capi.map { it.perMotore() }
            // Se il Capo di partenza è stato eliminato nel frattempo, lo toglie da [partenza] il collector in init.
            val capoDiPartenza = perMotore.find { it.id == partenza }
            val richiesta = capoDiPartenza?.let { Richiesta.PartiDa(it, filtroStagione) } ?: Richiesta.Genera(filtroStagione)
            val esito = proponi(perMotore, richiesta, giàViste)
            if (esito is Esito.Proposta) giàViste += esito.outfit.idCapi
            (stato?.esito as? Esito.Proposta)?.let {
                precedenti += it
                if (precedenti.size > MAX_PRECEDENTI) precedenti.removeAt(0)
            }
            stato = StatoOutfit(esito, capi.associateBy { it.id }, capoDiPartenza?.let(::coloriConsigliati).orEmpty())
        }
    }

    /** "Precedente": torna alla Proposta mostrata prima di quella attuale (o prima di Esaurite / Nessun abbinamento). */
    fun precedente() {
        val s = stato ?: return
        val prima = precedenti.removeLastOrNull() ?: return
        stato = s.copy(esito = prima)
    }

    /** Salva la Proposta mostrata tra i Preferiti ("Salva"), col [nome] opzionale; vale per qualsiasi Richiesta. */
    fun salva(nome: String) {
        val proposta = stato?.esito as? Esito.Proposta ?: return
        viewModelScope.launch { preferiti.salva(proposta.spiegazione, proposta.outfit.idCapi, nome) }
    }

    /**
     * "Parti da questo capo" ([id]) o, con null, ritorno a Genera: le Proposte già viste ripartono da zero.
     * Con null serve anche dopo un Importa, che riusa gli id per Capi diversi (ADR-0010).
     */
    fun partiDa(id: Long?) {
        partenza = id
        giàViste.clear()
        precedenti.clear()
        stato = null
        avvisoFuoriStagione = null
        if (id != null) {
            avvisaSeFuoriStagione()
            genera()
        }
    }

    /** Cambia il filtro Stagione; le Proposte già viste ripartono da zero (ADR-0006). */
    fun scegliStagione(stagione: Stagione?) {
        if (stagione == filtroStagione) return // ritoccare il filtro scelto non è cambiarlo
        filtroStagione = stagione
        giàViste.clear()
        if (partenza != null) avvisaSeFuoriStagione()
    }

    /**
     * Avvisa una volta, all'avvio di "Parti da questo capo" o al cambio di filtro, se il Capo di partenza è fuori stagione (ADR-0008).
     * Cambiare la Stagione del Capo di partenza nel Guardaroba non riavvisa: rigenera soltanto.
     */
    private fun avvisaSeFuoriStagione() {
        viewModelScope.launch {
            val capo = dao.tutti().first().find { it.id == partenza } ?: return@launch
            val filtro = filtroStagione
            avvisoFuoriStagione = if (filtro != null && fuoriStagione(capo.stagione, filtro)) FuoriStagione(capo.stagione, filtro) else null
        }
    }

    /** "Ok" sull'avviso fuori stagione. */
    fun chiudiAvviso() {
        avvisoFuoriStagione = null
    }

    /** Azzera le Proposte già viste e ne chiede una nuova. */
    fun ricomincia() {
        giàViste.clear()
        genera()
    }

    companion object {
        // shortcut: tetto fisso alla cronologia, basta per una mattina davanti all'armadio; da rivedere se serve tornare più indietro
        private const val MAX_PRECEDENTI = 20

        val Factory = viewModelFactory {
            initializer {
                val db = GuardarobaDatabase.get(this[APPLICATION_KEY]!!)
                OutfitViewModel(db.capoDao(), db.preferitoDao())
            }
        }
    }
}
