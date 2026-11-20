package com.guardaroba.ui

import androidx.compose.runtime.getValue
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
import com.guardaroba.motore.Esito
import com.guardaroba.motore.Richiesta
import com.guardaroba.motore.Stagione
import com.guardaroba.motore.colore
import com.guardaroba.motore.proponi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.guardaroba.motore.Capo as CapoMotore

/** L'ultimo Esito del Motore, con i Capi dell'Archivio per mostrarne le miniature. */
data class StatoOutfit(val esito: Esito, val capi: Map<Long, Capo>)

class OutfitViewModel(private val dao: CapoDao, private val preferiti: PreferitoDao) : ViewModel() {
    var stato by mutableStateOf<StatoOutfit?>(null)
        private set

    /** Il filtro Stagione scelto prima di generare; null = nessun filtro. */
    var filtroStagione by mutableStateOf<Stagione?>(null)

    /** Gli Outfit tra i Preferiti, come insiemi di id dei Capi: segue anche le eliminazioni dalla lista. */
    val salvati = preferiti.tutti()
        .map { lista -> lista.map { p -> p.capi.map { it.id }.toSet() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Gli Outfit già proposti in questa sessione, come insiemi di id dei Capi. */
    private val giàViste = mutableSetOf<Set<Long>>()

    /** Chiede una nuova Proposta al Motore ("Genera" e "un altro"). */
    fun genera() {
        viewModelScope.launch {
            val capi = dao.tutti().first()
            val esito = proponi(capi.map { it.perMotore() }, Richiesta.Genera(filtroStagione), giàViste)
            if (esito is Esito.Proposta) giàViste += esito.outfit.idCapi
            stato = StatoOutfit(esito, capi.associateBy { it.id })
        }
    }

    /** Salva la Proposta mostrata tra i Preferiti ("Salva"), col [nome] opzionale; vale per qualsiasi Richiesta. */
    fun salva(nome: String) {
        val proposta = stato?.esito as? Esito.Proposta ?: return
        viewModelScope.launch { preferiti.salva(proposta.spiegazione, proposta.outfit.idCapi, nome) }
    }

    /** Azzera le Proposte già viste e ne chiede una nuova. */
    fun ricomincia() {
        giàViste.clear()
        genera()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val db = GuardarobaDatabase.get(this[APPLICATION_KEY]!!)
                OutfitViewModel(db.capoDao(), db.preferitoDao())
            }
        }
    }
}

private fun Capo.perMotore() =
    CapoMotore(id, categoria, colore(colorePrincipale), motivo, coloreSecondario?.let(::colore), stagione)
