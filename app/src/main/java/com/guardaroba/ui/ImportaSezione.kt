package com.guardaroba.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

/**
 * "Importa" in Impostazioni: sceglie uno ZIP di «Esporta» (SAF) e, dopo una conferma, sostituisce il Guardaroba salvandone prima una copia;
 * "Ripristina" rimette la copia (ADR-0015). Dopo ogni sostituzione chiama [onImportato].
 */
@Composable
fun ImportaSezione(onImportato: () -> Unit, avvisi: SnackbarHostState, bloccato: Boolean, vm: ImportaViewModel) {
    // Alcuni file manager danno agli ZIP un tipo diverso da application/zip: un file sbagliato lo ferma la validazione.
    val selettore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::scelto)
    }
    var daRipristinare by rememberSaveable { mutableStateOf(false) }
    MostraAvviso(avvisi, vm.avviso, vm::avvisoVisto) { vm.ripristina(onImportato) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Titolo("Importa")
        Text("Sostituisce Capi, Preferiti e foto con quelli di un file creato con «Esporta».")
        OutlinedButton(
            onClick = { selettore.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) },
            enabled = !vm.inCorso && !bloccato,
        ) {
            Text(if (vm.inCorso) "Sostituzione…" else "Importa")
        }
        vm.copiaDel?.let {
            Text(
                "C'è una copia del Guardaroba com'era prima dell'ultima sostituzione, del ${data(it)}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { daRipristinare = true }, enabled = !vm.inCorso && !bloccato) { Text("Ripristina il Guardaroba di prima") }
        }
    }
    if (vm.daConfermare != null) {
        AlertDialog(
            onDismissRequest = vm::annulla,
            title = { Text("Sostituire il Guardaroba?") },
            text = {
                Text(
                    "Tutti i Capi, i Preferiti e le foto di adesso verranno sostituiti da quelli del file. " +
                        "Prima ne salvo una copia: potrai tornare indietro con «Ripristina il Guardaroba di prima».",
                )
            },
            confirmButton = { TextButton(onClick = { vm.conferma(onImportato) }) { Text("Sostituisci") } },
            dismissButton = { TextButton(onClick = vm::annulla) { Text("Annulla") } },
        )
    }
    val copia = vm.copiaDel
    if (daRipristinare && copia != null) {
        AlertDialog(
            onDismissRequest = { daRipristinare = false },
            title = { Text("Tornare al Guardaroba di prima?") },
            text = { Text("Il Guardaroba di adesso viene sostituito dalla copia del ${data(copia)}, e diventa a sua volta la copia da ripristinare.") },
            confirmButton = {
                TextButton(onClick = { daRipristinare = false; vm.ripristina(onImportato) }) { Text("Ripristina") }
            },
            dismissButton = { TextButton(onClick = { daRipristinare = false }) { Text("Annulla") } },
        )
    }
}
