package com.guardaroba.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardaroba.archivio.ArchivioNonValido
import com.guardaroba.archivio.Foto
import com.guardaroba.archivio.GuardarobaDatabase
import com.guardaroba.archivio.Importazione
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ImportaViewModel(private val importazione: Importazione, private val resolver: ContentResolver) : ViewModel() {
    /** Il file scelto, in attesa che l'utente confermi la sostituzione del Guardaroba. */
    var daConfermare by mutableStateOf<Uri?>(null)
        private set
    var inCorso by mutableStateOf(false)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    fun scelto(uri: Uri) {
        daConfermare = uri
        messaggio = null
    }

    fun annulla() {
        daConfermare = null
    }

    /** Sostituisce il Guardaroba con il file confermato; se il file non è valido, i dati restano com'erano. */
    fun conferma() {
        val uri = daConfermare ?: return
        daConfermare = null
        inCorso = true
        viewModelScope.launch {
            messaggio = try {
                withContext(Dispatchers.IO) {
                    val input = resolver.openInputStream(uri) ?: throw IOException("File non apribile")
                    input.use { importazione.importa(it) }
                }
                "Guardaroba importato."
            } catch (e: CancellationException) {
                throw e
            } catch (e: ArchivioNonValido) {
                "Il file non è un export valido del Guardaroba: non è cambiato niente."
            } catch (e: Exception) {
                "Importazione non riuscita: il Guardaroba è rimasto com'era. Riprova."
            } finally {
                inCorso = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                ImportaViewModel(Importazione(GuardarobaDatabase.get(app), Foto(app)), app.contentResolver)
            }
        }
    }
}
