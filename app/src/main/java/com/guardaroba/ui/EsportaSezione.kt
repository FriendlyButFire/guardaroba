package com.guardaroba.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/** "Esporta" in Impostazioni: salva Capi, Preferiti e foto in un unico ZIP dove sceglie l'utente (SAF, nessun permesso). */
@Composable
fun EsportaSezione(avvisi: SnackbarHostState, bloccato: Boolean, vm: EsportaViewModel) {
    val selettore = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let(vm::esporta)
    }
    MostraAvviso(avvisi, vm.avviso, vm::avvisoVisto)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Titolo("Esporta")
        Text("Salva Capi, Preferiti e foto in un unico file, da tenere da parte o da portare su un altro telefono.")
        Text(
            vm.ultimaCopia?.let { "Ultima copia: ${data(it)}." } ?: "Non hai ancora salvato una copia.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Il pulsante pieno è l'azione sicura; Importa, che sostituisce tutto, è contornato (ADR-0015).
        Button(onClick = { selettore.launch("guardaroba-${LocalDate.now()}.zip") }, enabled = !vm.inCorso && !bloccato) {
            Text(if (vm.inCorso) "Esportazione…" else "Esporta")
        }
    }
}
