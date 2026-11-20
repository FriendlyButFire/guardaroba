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
import com.guardaroba.motore.colore
import com.guardaroba.motore.proponi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.guardaroba.motore.Capo as CapoMotore

/** L'ultimo Esito del Motore, con i Capi dell'Archivio per mostrarne le miniature; [salvata] se la Proposta è tra i Preferiti. */
data class StatoOutfit(val esito: Esito, val capi: Map<Long, Capo>, val salvata: Boolean = false)

class OutfitViewModel(private val dao: CapoDao, private val preferiti: PreferitoDao) : ViewModel() {
    var stato by mutableStateOf<StatoOutfit?>(null)
        private set

    /** Gli Outfit già proposti in questa sessione, come insiemi di id dei Capi. */
    private val giàViste = mutableSetOf<Set<Long>>()

    /** Chiede una nuova Proposta al Motore ("Genera" e "un altro"). */
    fun genera() {
        viewModelScope.launch {
            val capi = dao.tutti().first()
            val esito = proponi(capi.map { it.perMotore() }, Richiesta.Genera, giàViste)
            if (esito is Esito.Proposta) giàViste += esito.outfit.idCapi
            stato = StatoOutfit(esito, capi.associateBy { it.id })
        }
    }

    /** Salva la Proposta mostrata tra i Preferiti ("Salva"); vale per qualsiasi Richiesta. */
    fun salva() {
        val attuale = stato ?: return
        val proposta = attuale.esito as? Esito.Proposta ?: return
        viewModelScope.launch {
            preferiti.salva(proposta.spiegazione, proposta.outfit.idCapi)
            // Solo se nel frattempo non è arrivata un'altra Proposta.
            if (stato === attuale) stato = attuale.copy(salvata = true)
        }
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

private fun Capo.perMotore() = CapoMotore(id, categoria, colore(colorePrincipale))
