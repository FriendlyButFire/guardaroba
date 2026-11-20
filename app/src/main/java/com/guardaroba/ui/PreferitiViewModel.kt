package com.guardaroba.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardaroba.archivio.GuardarobaDatabase
import com.guardaroba.archivio.PreferitoDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class PreferitiViewModel(dao: PreferitoDao) : ViewModel() {
    val preferiti = dao.tutti().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer { PreferitiViewModel(GuardarobaDatabase.get(this[APPLICATION_KEY]!!).preferitoDao()) }
        }
    }
}
