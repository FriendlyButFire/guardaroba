package com.guardaroba.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** "Importa" in Impostazioni: sceglie uno ZIP di «Esporta» (SAF) e, dopo una conferma, sostituisce il Guardaroba. */
@Composable
fun ImportaSezione(vm: ImportaViewModel = viewModel(factory = ImportaViewModel.Factory)) {
    // Alcuni file manager danno agli ZIP un tipo diverso da application/zip: un file sbagliato lo ferma la validazione.
    val selettore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::scelto)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Importa", style = MaterialTheme.typography.titleMedium)
        Text("Ripristina Capi, Preferiti e foto da un file ZIP creato con «Esporta».", style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = { selettore.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) },
            enabled = !vm.inCorso,
        ) {
            Text(if (vm.inCorso) "Importazione…" else "Importa")
        }
        vm.messaggio?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
    if (vm.daConfermare != null) {
        AlertDialog(
            onDismissRequest = vm::annulla,
            title = { Text("Sostituire il Guardaroba?") },
            text = { Text("Tutti i Capi, i Preferiti e le foto di adesso verranno sostituiti da quelli del file. Non si può annullare.") },
            confirmButton = { TextButton(onClick = vm::conferma) { Text("Sostituisci") } },
            dismissButton = { TextButton(onClick = vm::annulla) { Text("Annulla") } },
        )
    }
}
