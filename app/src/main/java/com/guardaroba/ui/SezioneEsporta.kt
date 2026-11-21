package com.guardaroba.ui

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guardaroba.archivio.Esportazione
import com.guardaroba.archivio.Foto
import com.guardaroba.archivio.GuardarobaDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate

class EsportaViewModel(private val esportazione: Esportazione, private val resolver: ContentResolver) : ViewModel() {
    var inCorso by mutableStateOf(false)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    /** Scrive l'export nel file scelto con il selettore di sistema; se fallisce, toglie il file a metà. */
    fun esporta(uri: Uri) {
        inCorso = true
        messaggio = null
        viewModelScope.launch {
            messaggio = try {
                withContext(Dispatchers.IO) {
                    val out = resolver.openOutputStream(uri, "wt") ?: throw IOException("File non apribile")
                    out.use { esportazione.esporta(it) }
                }
                "Guardaroba esportato."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.IO) { runCatching { DocumentsContract.deleteDocument(resolver, uri) } }
                "Esportazione non riuscita: il file non è stato salvato. Riprova o scegli un'altra posizione."
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

/** "Esporta" in Impostazioni: salva Capi, Preferiti e foto in un unico ZIP dove sceglie l'utente (SAF, nessun permesso). */
@Composable
fun SezioneEsporta(vm: EsportaViewModel = viewModel(factory = EsportaViewModel.Factory)) {
    val selettore = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let(vm::esporta)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Esporta", style = MaterialTheme.typography.titleMedium)
        Text("Salva Capi, Preferiti e foto in un unico file ZIP, dove vuoi tu.", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { selettore.launch("guardaroba-${LocalDate.now()}.zip") }, enabled = !vm.inCorso) {
            Text(if (vm.inCorso) "Esportazione…" else "Esporta")
        }
        vm.messaggio?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}
