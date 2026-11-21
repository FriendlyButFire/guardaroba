package com.guardaroba.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.PreferitoConCapi
import com.guardaroba.motore.Giudizio
import com.guardaroba.motore.rimedio

/** I Preferiti; [schedaCapo] mostra la scheda di un Capo aperta da un Preferito e chiama la sua funzione quando si chiude. */
@Composable
fun PreferitiScreen(
    vm: PreferitiViewModel = viewModel(factory = PreferitiViewModel.Factory),
    schedaCapo: @Composable (id: Long, chiudi: () -> Unit) -> Unit,
) {
    val preferiti by vm.preferiti.collectAsStateWithLifecycle()
    val lista = preferiti ?: return
    // Il Preferito aperto in grande e il Capo aperto da lì; il Preferito resta aperto mentre la scheda del Capo è in vista.
    var aperto by rememberSaveable { mutableStateOf<Long?>(null) }
    var capoAperto by rememberSaveable { mutableStateOf<Long?>(null) }
    capoAperto?.let { id ->
        schedaCapo(id) { capoAperto = null }
        return
    }
    lista.find { it.preferito.id == aperto }?.let { p ->
        VistaPreferito(p, onIndietro = { aperto = null }, onApriCapo = { capoAperto = it.id })
        return
    }
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
            items(lista, key = { it.preferito.id }) { p ->
                SchedaPreferito(
                    p,
                    onApri = { aperto = p.preferito.id },
                    onRinomina = { daRinominare = p.preferito.id },
                    onElimina = { daEliminare = p.preferito.id },
                )
            }
        }
    }
    lista.find { it.preferito.id == daRinominare }?.let { p ->
        FinestraNome("Rinomina", p.preferito.nome.orEmpty(), onChiudi = { daRinominare = null }) { vm.rinomina(p.preferito.id, it) }
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
private fun SchedaPreferito(preferito: PreferitoConCapi, onApri: () -> Unit, onRinomina: () -> Unit, onElimina: () -> Unit) {
    Card(onApri, Modifier.fillMaxWidth()) {
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
            val cambiato = preferito.responsabile()
            Row(Modifier.fillMaxWidth()) {
                // Al massimo 5 Capi per Outfit: entrano tutti in una riga.
                preferito.capi.sortedBy { it.categoria }.forEach { RiquadroOutfit(it, Modifier.weight(1f), it.id == cambiato?.id) }
            }
            // Spiegazione e validità seguono i Capi di adesso; un Preferito non più valido resta, segnalato (ADR-0011).
            Box(Modifier.padding(8.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                when (val giudizio = preferito.giudizio()) {
                    // La frase come in Outfit, la regola come etichetta (ADR-0012); più piccole, perché qui sono tante.
                    is Giudizio.Valido -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val perché = preferito.perché()
                        Text(perché?.frase ?: giudizio.spiegazione, style = MaterialTheme.typography.bodyLarge)
                        perché?.let {
                            Text(it.regola.maiuscola(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    is Giudizio.NonValido -> NonPiùValido(giudizio.ragione, inGrande = false)
                }
            }
        }
    }
}

/** Il Preferito in grande, composto come in Outfit e giudicato con i Capi di adesso (ADR-0011, ADR-0014); toccare un riquadro apre il Capo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VistaPreferito(preferito: PreferitoConCapi, onIndietro: () -> Unit, onApriCapo: (Capo) -> Unit) {
    BackHandler(onBack = onIndietro)
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(preferito.preferito.nome ?: "Senza nome", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = onIndietro) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro") }
            },
            windowInsets = WindowInsets(0),
        )
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val cambiato = preferito.responsabile()
            OutfitVerticale(preferito.capi, onApri = onApriCapo, cambiato = cambiato)
            // Tornando dalla scheda del Capo il giudizio può cambiare: TalkBack lo annuncia.
            Box(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                when (val giudizio = preferito.giudizio()) {
                    is Giudizio.Valido -> Spiegazione(preferito.perché(), giudizio.spiegazione)
                    is Giudizio.NonValido -> NonPiùValido(giudizio.ragione, inGrande = true, cambiato)
                }
            }
        }
    }
}

/**
 * "Non più valido" come stato da sistemare, non come errore: l'etichetta, la ragione del Motore e il rimedio. [inGrande] nella vista
 * grande, dove il Capo [cambiato] (se si sa) è cerchiato e toccarlo apre la sua scheda.
 */
@Composable
private fun NonPiùValido(ragione: String, inGrande: Boolean, cambiato: Capo? = null) {
    val allineamento = if (inGrande) TextAlign.Center else TextAlign.Start
    Column(
        horizontalAlignment = if (inGrande) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, Modifier.size(18.dp))
                Text("Non più valido", style = MaterialTheme.typography.labelLarge)
            }
        }
        Text(ragione.maiuscola() + ".", style = MaterialTheme.typography.bodyLarge, textAlign = allineamento)
        Text(
            rimedio(ragione) + (if (inGrande && cambiato != null) " Il Capo cambiato è cerchiato: toccalo per correggerlo." else ""),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = allineamento,
        )
    }
}

/** Chiede il nome di un Preferito (nuovo o da rinominare); lasciarlo vuoto vuol dire senza nome. */
@Composable
fun FinestraNome(titolo: String, attuale: String, onChiudi: () -> Unit, onConferma: (String) -> Unit) {
    var nome by rememberSaveable { mutableStateOf(attuale) }
    AlertDialog(
        onDismissRequest = onChiudi,
        title = { Text(titolo) },
        text = { OutlinedTextField(nome, { nome = it }, label = { Text("Nome (opzionale)") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConferma(nome); onChiudi() }) { Text("Salva") } },
        dismissButton = { TextButton(onClick = onChiudi) { Text("Annulla") } },
    )
}
