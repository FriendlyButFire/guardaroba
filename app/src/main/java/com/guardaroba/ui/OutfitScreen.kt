package com.guardaroba.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardaroba.motore.Esito

@Composable
fun OutfitScreen(vm: OutfitViewModel = viewModel(factory = OutfitViewModel.Factory)) {
    val stato = vm.stato
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (val esito = stato?.esito) {
            null -> Messaggio("Premi Genera per ricevere un Outfit fatto con i tuoi Capi.")
            is Esito.Proposta -> {
                Row(Modifier.fillMaxWidth()) {
                    // mapNotNull: un Capo eliminato dopo la Proposta sparisce dalla miniatura
                    esito.outfit.capi.mapNotNull { stato.capi[it.id] }.forEach { RiquadroCapo(it, Modifier.weight(1f)) }
                }
                Text(esito.spiegazione, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
            is Esito.MancaCategoria -> Messaggio(
                (if (esito.categorie.size == 1) "Ti manca: " else "Ti mancano: ") +
                    esito.categorie.joinToString(", ") { it.nome } + ".\nAggiungi questi Capi nel Guardaroba.",
            )
            Esito.NessunAbbinamento -> Messaggio("Nessun abbinamento possibile con i colori dei tuoi Capi.")
            Esito.Esaurite -> Messaggio("Hai visto tutte le Proposte possibili.")
        }
        if (stato?.esito == Esito.Esaurite) {
            Button(onClick = vm::ricomincia) { Text("Ricomincia") }
        } else {
            Button(onClick = vm::genera) { Text(if (stato?.esito is Esito.Proposta) "Un altro" else "Genera") }
        }
    }
}

@Composable
private fun Messaggio(testo: String) {
    Text(testo, textAlign = TextAlign.Center)
}
