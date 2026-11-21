package com.guardaroba.ui

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
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

class EsportaViewModel(
    private val esportazione: Esportazione,
    private val resolver: ContentResolver,
    private val preferenze: SharedPreferences,
) : ViewModel() {
    var inCorso by mutableStateOf(false)
        private set
    var avviso by mutableStateOf<Avviso?>(null)
        private set

    /** Quando è riuscito l'ultimo Esporta, se c'è stato (ADR-0015). */
    var ultimaCopia by mutableStateOf(preferenze.getLong(ULTIMA_COPIA, 0).takeIf { it > 0 })
        private set

    fun avvisoVisto() {
        avviso = null
    }

    /** Scrive l'export nel file scelto con il selettore di sistema; se fallisce o viene interrotto, toglie il file a metà. */
    fun esporta(uri: Uri) {
        inCorso = true
        viewModelScope.launch {
            try {
                val nome = withContext(Dispatchers.IO) {
                    val out = resolver.openOutputStream(uri, "wt") ?: throw IOException("File non apribile")
                    out.use { esportazione.esporta(it) }
                    nome(uri)
                }
                ultimaCopia = System.currentTimeMillis().also { preferenze.edit().putLong(ULTIMA_COPIA, it).apply() }
                avviso = Avviso(nome?.let { "Copia salvata: $it" } ?: "Copia salvata.")
            } catch (e: Exception) {
                withContext(NonCancellable + Dispatchers.IO) { runCatching { DocumentsContract.deleteDocument(resolver, uri) } }
                if (e is CancellationException) throw e
                avviso = Avviso("Esportazione non riuscita: il file non è stato salvato. Riprova o scegli un'altra posizione.")
            } finally {
                inCorso = false
            }
        }
    }

    /** Il nome del file scelto, per dire all'utente cosa cercare; null se il selettore non lo dà. */
    private fun nome(uri: Uri): String? = runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { if (it.moveToFirst()) it.getString(0) else null }
    }.getOrNull()

    companion object {
        private const val ULTIMA_COPIA = "ultimaCopia"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                EsportaViewModel(
                    Esportazione(GuardarobaDatabase.get(app), Foto(app)),
                    app.contentResolver,
                    app.getSharedPreferences("impostazioni", Context.MODE_PRIVATE),
                )
            }
        }
    }
}
