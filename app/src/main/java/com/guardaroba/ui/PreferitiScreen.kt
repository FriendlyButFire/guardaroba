package com.guardaroba.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardaroba.archivio.PreferitoConCapi

@Composable
fun PreferitiScreen(vm: PreferitiViewModel = viewModel(factory = PreferitiViewModel.Factory)) {
    val preferiti by vm.preferiti.collectAsStateWithLifecycle()
    val lista = preferiti ?: return
    // Il Preferito di cui è aperta la finestra di rinomina o di eliminazione.
    var daRinominare by rememberSaveable { mutableStateOf<Long?>(null) }
    var daEliminare by rememberSaveable { mutableStateOf<Long?>(null) }
    when {
        lista.isEmpty() -> Text(
            "Nessun Preferito.\nSalva una Proposta dalla sezione Outfit.",
            Modifier.padding(24.dp),
            textAlign = TextAlign.Center,
        )
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(lista, key = { it.preferito.id }) {
                SchedaPreferito(it, onRinomina = { daRinominare = it.preferito.id }, onElimina = { daEliminare = it.preferito.id })
            }
        }
    }
    lista.find { it.preferito.id == daRinominare }?.let { p ->
        FinestraRinomina(p.preferito.nome.orEmpty(), onChiudi = { daRinominare = null }) { vm.rinomina(p.preferito.id, it) }
    }
    lista.find { it.preferito.id == daEliminare }?.let { p ->
        AlertDialog(
            onDismissRequest = { daEliminare = null },
            title = { Text("Eliminare il Preferito?") },
            text = { Text("${p.preferito.nome ?: "L'Outfit"} sparirà dai Preferiti. I Capi restano nel Guardaroba.") },
            confirmButton = {
                TextButton(onClick = { vm.elimina(p.preferito.id); daEliminare = null }) { Text("Elimina") }
            },
            dismissButton = { TextButton(onClick = { daEliminare = null }) { Text("Annulla") } },
        )
    }
}

@Composable
private fun SchedaPreferito(preferito: PreferitoConCapi, onRinomina: () -> Unit, onElimina: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    preferito.preferito.nome ?: "Senza nome",
                    Modifier.weight(1f).padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (preferito.preferito.nome == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = onRinomina) { Icon(Icons.Filled.Edit, contentDescription = "Rinomina") }
                IconButton(onClick = onElimina) { Icon(Icons.Filled.Delete, contentDescription = "Elimina") }
            }
            Row(Modifier.fillMaxWidth()) {
                // Al massimo 5 Capi per Outfit: entrano tutti in una riga.
                preferito.capi.sortedBy { it.categoria }.forEach { RiquadroCapo(it, Modifier.weight(1f)) }
            }
            Text(preferito.preferito.spiegazione, Modifier.padding(8.dp), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Chiede il nuovo nome; lasciarlo vuoto toglie il nome. */
@Composable
private fun FinestraRinomina(attuale: String, onChiudi: () -> Unit, onConferma: (String) -> Unit) {
    var nome by rememberSaveable { mutableStateOf(attuale) }
    AlertDialog(
        onDismissRequest = onChiudi,
        title = { Text("Rinomina") },
        text = { OutlinedTextField(nome, { nome = it }, label = { Text("Nome (opzionale)") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConferma(nome); onChiudi() }) { Text("Salva") } },
        dismissButton = { TextButton(onClick = onChiudi) { Text("Annulla") } },
    )
}
