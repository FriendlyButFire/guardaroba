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
import com.guardaroba.archivio.Foto
import com.guardaroba.archivio.GuardarobaDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GuardarobaViewModel(private val dao: CapoDao, private val foto: Foto) : ViewModel() {
    val capi = dao.tutti().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    var filtro by mutableStateOf(Filtro()) // shortcut: si perde se Android chiude il processo; SavedStateHandle se serve

    /** Aggiunge un Capo nuovo o aggiorna quello esistente con lo stesso id, cancellando la foto che non usa più. */
    fun salva(capo: Capo) {
        viewModelScope.launch { dao.salva(capo, foto) }
    }

    /** Quanti Preferiti spariscono eliminando il Capo [id]. */
    suspend fun quantiPreferiti(id: Long) = dao.quantiPreferiti(id)

    /** Elimina il Capo [id], la sua foto e i Preferiti che lo contengono. */
    fun elimina(id: Long) {
        viewModelScope.launch { dao.elimina(id, foto) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                GuardarobaViewModel(GuardarobaDatabase.get(app).capoDao(), Foto(app))
            }
        }
    }
}
