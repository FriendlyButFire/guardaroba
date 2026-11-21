package com.guardaroba.ui

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardaroba.archivio.Esportazione
import com.guardaroba.archivio.Foto
import com.guardaroba.archivio.GuardarobaDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class EsportaViewModel(private val esportazione: Esportazione, private val resolver: ContentResolver) : ViewModel() {
    var inCorso by mutableStateOf(false)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    /** Scrive l'export nel file scelto con il selettore di sistema; se fallisce o viene interrotto, toglie il file a metà. */
    fun esporta(uri: Uri) {
        inCorso = true
        messaggio = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val out = resolver.openOutputStream(uri, "wt") ?: throw IOException("File non apribile")
                    out.use { esportazione.esporta(it) }
                }
                messaggio = "Guardaroba esportato."
            } catch (e: Exception) {
                withContext(NonCancellable + Dispatchers.IO) { runCatching { DocumentsContract.deleteDocument(resolver, uri) } }
                if (e is CancellationException) throw e
                messaggio = "Esportazione non riuscita: il file non è stato salvato. Riprova o scegli un'altra posizione."
            } finally {
                inCorso = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                EsportaViewModel(Esportazione(GuardarobaDatabase.get(app), Foto(app)), app.contentResolver)
            }
        }
    }
}
