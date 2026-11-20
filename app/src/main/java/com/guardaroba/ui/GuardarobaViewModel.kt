package com.guardaroba.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.CapoDao
import com.guardaroba.archivio.GuardarobaDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GuardarobaViewModel(private val dao: CapoDao) : ViewModel() {
    val capi = dao.tutti().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Aggiunge un Capo nuovo o aggiorna quello esistente con lo stesso id. */
    fun salva(capo: Capo) {
        viewModelScope.launch { dao.salva(capo) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { GuardarobaViewModel(GuardarobaDatabase.get(this[APPLICATION_KEY]!!).capoDao()) }
        }
    }
}
