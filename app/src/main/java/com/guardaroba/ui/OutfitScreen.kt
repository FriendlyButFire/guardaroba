package com.guardaroba.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
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
import com.guardaroba.motore.Consiglio
import com.guardaroba.motore.Esito
import com.guardaroba.motore.Stagione

@Composable
fun OutfitScreen(vm: OutfitViewModel) {
    val stato = vm.stato
    val salvati by vm.salvati.collectAsStateWithLifecycle()
    // heightIn: il contenuto corto resta centrato, quello lungo (Colori consigliati) scorre.
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight).fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            vm.partenza?.let { id ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Parti da: " + (stato?.capi?.get(id)?.tipo ?: "…"), style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { vm.partiDa(null) }) { Text("Genera a caso") }
                }
            }
            when (val esito = stato?.esito) {
                null -> Messaggio("Premi Genera per ricevere un Outfit fatto con i tuoi Capi.")
                is Esito.Proposta -> {
                    Row(Modifier.fillMaxWidth()) {
                        // mapNotNull: un Capo eliminato dopo la Proposta sparisce dalla miniatura
                        esito.outfit.capi.mapNotNull { stato.capi[it.id] }.forEach { RiquadroCapo(it, Modifier.weight(1f)) }
                    }
                    Text(esito.spiegazione, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    // Una Proposta nuova riparte con il nome vuoto.
                    var nome by rememberSaveable(esito.outfit.idCapi) { mutableStateOf("") }
                    val salvata = esito.outfit.idCapi in salvati
                    if (!salvata) {
                        OutlinedTextField(nome, { nome = it }, label = { Text("Nome (opzionale)") }, singleLine = true)
                    }
                    OutlinedButton(onClick = { vm.salva(nome) }, enabled = !salvata) {
                        Text(if (salvata) "Tra i Preferiti" else "Salva")
                    }
                }
                is Esito.MancaCategoria -> Messaggio(
                    (if (esito.categorie.size == 1) "Ti manca: " else "Ti mancano: ") +
                        esito.categorie.joinToString(", ") { it.nome } + ".\nAggiungi questi Capi nel Guardaroba.",
                )
                Esito.NessunAbbinamento -> Messaggio(
                    if (vm.partenza != null) "Nessun abbinamento per questo capo." else "Nessun abbinamento possibile con i colori dei tuoi Capi.",
                )
                Esito.Esaurite -> Messaggio("Hai visto tutte le Proposte possibili.")
            }
            stato?.consigliati?.takeIf { it.isNotEmpty() }?.let { ColoriConsigliati(it) }
            // "tutto l'anno" entra già in ogni filtro, quindi non è una voce. Vale dalla prossima Proposta.
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf(null) + (Stagione.entries - Stagione.TUTTO_L_ANNO)).forEach {
                    FilterChip(
                        selected = it == vm.filtroStagione,
                        onClick = { vm.scegliStagione(it) },
                        label = { Text(it?.nome ?: "nessuno") },
                    )
                }
            }
            if (stato?.esito == Esito.Esaurite) {
                Button(onClick = vm::ricomincia) { Text("Ricomincia") }
            } else {
                Button(onClick = vm::genera) { Text(if (stato?.esito is Esito.Proposta) "Un altro" else "Genera") }
            }
        }
    }
}

/** I Colori della Palette che starebbero bene col Capo di partenza, raggruppati per regola. */
@Composable
private fun ColoriConsigliati(consigliati: List<Consiglio>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Colori consigliati", style = MaterialTheme.typography.titleSmall)
        consigliati.groupBy { it.regola }.forEach { (regola, gruppo) ->
            Text(regola, style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                gruppo.forEach { (colore) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            Modifier.size(20.dp).background(colore.campione(), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        )
                        Text(colore.nome, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun Messaggio(testo: String) {
    Text(testo, textAlign = TextAlign.Center)
}
