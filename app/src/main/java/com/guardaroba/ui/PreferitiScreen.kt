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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardaroba.archivio.PreferitoConCapi

@Composable
fun PreferitiScreen(vm: PreferitiViewModel = viewModel(factory = PreferitiViewModel.Factory)) {
    val preferiti by vm.preferiti.collectAsStateWithLifecycle()
    when {
        preferiti == null -> Unit
        preferiti!!.isEmpty() -> Text(
            "Nessun Preferito.\nSalva una Proposta dalla sezione Outfit.",
            Modifier.padding(24.dp),
            textAlign = TextAlign.Center,
        )
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(preferiti!!, key = { it.preferito.id }) { SchedaPreferito(it) }
        }
    }
}

@Composable
private fun SchedaPreferito(preferito: PreferitoConCapi) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth()) {
                // Al massimo 5 Capi per Outfit: entrano tutti in una riga.
                preferito.capi.sortedBy { it.categoria }.forEach { RiquadroCapo(it, Modifier.weight(1f)) }
            }
            Text(preferito.preferito.spiegazione, Modifier.padding(8.dp), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
