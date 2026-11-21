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
import com.guardaroba.archivio.Esportazione
import com.guardaroba.archivio.Foto
import com.guardaroba.archivio.GuardarobaDatabase
import com.guardaroba.archivio.Importazione
import com.guardaroba.archivio.Sostituzione
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Un esito da mostrare nello snackbar; con [annullabile] lo snackbar offre Annulla, cioè [ImportaViewModel.ripristina]. */
data class Avviso(val testo: String, val annullabile: Boolean = false)

class ImportaViewModel(private val sostituzione: Sostituzione, private val resolver: ContentResolver) : ViewModel() {
    /** Il file scelto, in attesa che l'utente confermi la sostituzione del Guardaroba. */
    var daConfermare by mutableStateOf<Uri?>(null)
        private set
    var inCorso by mutableStateOf(false)
        private set
    var avviso by mutableStateOf<Avviso?>(null)
        private set

    /** Quando è stata fatta la copia del Guardaroba di prima dell'ultimo Importa, se c'è (ADR-0015). */
    var copiaDel by mutableStateOf(sostituzione.copiaDel()) // solo la data di un file: niente IO pesante
        private set

    fun scelto(uri: Uri) {
        daConfermare = uri
    }

    fun annulla() {
        daConfermare = null
    }

    fun avvisoVisto() {
        avviso = null
    }

    /** Sostituisce il Guardaroba con il file confermato, dopo averne salvato una copia, e poi chiama [importato]. */
    fun conferma(importato: () -> Unit) {
        val uri = daConfermare ?: return
        daConfermare = null
        sostituisci(importato, "Guardaroba importato.", "Il file non è un export valido del Guardaroba: non è cambiato niente.") {
            val input = resolver.openInputStream(uri) ?: throw IOException("File non apribile")
            input.use { sostituzione.sostituisci(it) }
        }
    }

    /** Rimette il Guardaroba di prima; quello di adesso diventa la nuova copia, quindi anche questo si annulla. */
    fun ripristina(ripristinato: () -> Unit) = sostituisci(
        ripristinato,
        "Guardaroba di prima ripristinato.",
        "La copia di prima non si legge più: il Guardaroba è rimasto com'era.",
    ) { sostituzione.ripristina() }

    private fun sostituisci(fatto: () -> Unit, riuscito: String, nonValido: String, azione: suspend () -> Unit) {
        // Anche l'Annulla di uno snackbar vecchio arriva qui: mai due sostituzioni insieme sulla stessa copia.
        if (inCorso) return
        inCorso = true
        viewModelScope.launch {
            avviso = try {
                withContext(Dispatchers.IO) { azione() }
                fatto()
                Avviso(riuscito, annullabile = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ArchivioNonValido) {
                Avviso(nonValido)
            } catch (e: Exception) {
                Avviso("Operazione non riuscita: il Guardaroba è rimasto com'era. Riprova.")
            } finally {
                copiaDel = sostituzione.copiaDel()
                inCorso = false
            }
        }
    }

    companion object {
        /** La copia del Guardaroba di prima: in filesDir ma fuori da `foto/`, quindi fuori dal backup automatico (ADR-0015). */
        const val COPIA = "guardaroba-prima.zip"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                val db = GuardarobaDatabase.get(app)
                val foto = Foto(app)
                ImportaViewModel(Sostituzione(Esportazione(db, foto), Importazione(db, foto), File(app.filesDir, COPIA)), app.contentResolver)
            }
        }
    }
}
