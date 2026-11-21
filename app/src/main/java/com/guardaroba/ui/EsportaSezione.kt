package com.guardaroba.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate

/** "Esporta" in Impostazioni: salva Capi, Preferiti e foto in un unico ZIP dove sceglie l'utente (SAF, nessun permesso). */
@Composable
fun EsportaSezione(vm: EsportaViewModel = viewModel(factory = EsportaViewModel.Factory)) {
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
