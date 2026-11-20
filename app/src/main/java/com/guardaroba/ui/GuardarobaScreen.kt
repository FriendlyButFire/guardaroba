package com.guardaroba.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.Foto
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Colore
import com.guardaroba.motore.PALETTE
import com.guardaroba.motore.Stagione
import com.guardaroba.motore.colore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Il campione a schermo di un Colore della Palette. */
fun Colore.campione(): Color = Color.hsl(h.toFloat(), s / 100f, l / 100f)

/** Testo leggibile sopra il campione. */
fun Colore.testoSopra(): Color = if (l > 55) Color.Black else Color.White

private const val NUOVO = 0L // l'id di un Capo non ancora salvato

@Composable
fun GuardarobaScreen(vm: GuardarobaViewModel = viewModel(factory = GuardarobaViewModel.Factory)) {
    val capi by vm.capi.collectAsStateWithLifecycle()
    // Id del Capo aperto nella scheda: null = chiusa, NUOVO = Capo nuovo.
    var aperto by rememberSaveable { mutableStateOf<Long?>(null) }
    // Quanti Preferiti contengono il Capo aperto, mentre è aperta la conferma di eliminazione; null = nessuna conferma.
    var preferitiDaEliminare by rememberSaveable { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    aperto?.let { id ->
        val iniziale = capi?.find { it.id == id }
        // Un Capo esistente si apre solo quando la lista è caricata (es. dopo che il processo è stato ricreato).
        if (id == NUOVO || iniziale != null) {
            CapoForm(
                onSalva = { vm.salva(it); aperto = null },
                onAnnulla = { aperto = null },
                iniziale = iniziale,
                onElimina = { scope.launch { preferitiDaEliminare = vm.quantiPreferiti(id) } },
            )
            preferitiDaEliminare?.let { quanti ->
                ConfermaEliminazione(
                    quanti,
                    onConferma = { vm.elimina(id); preferitiDaEliminare = null; aperto = null },
                    onAnnulla = { preferitiDaEliminare = null },
                )
            }
            return
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0), // insets already applied by the outer Scaffold
        floatingActionButton = {
            FloatingActionButton(onClick = { aperto = NUOVO }) {
                Icon(Icons.Filled.Add, contentDescription = "Aggiungi capo")
            }
        },
    ) { padding ->
        val lista = capi ?: return@Scaffold // ancora in caricamento: niente stato vuoto a sproposito
        if (lista.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Il Guardaroba è vuoto.\nPremi + per aggiungere un capo.", textAlign = TextAlign.Center)
            }
        } else {
            val filtrati = lista.filter(vm.filtro::ammette)
            Column(Modifier.padding(padding)) {
                BarraFiltri(vm.filtro) { vm.filtro = it }
                if (filtrati.isEmpty()) {
                    Column(
                        Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Nessun capo corrisponde ai filtri.", textAlign = TextAlign.Center)
                        TextButton(onClick = { vm.filtro = Filtro() }) { Text("Azzera filtri") }
                    }
                } else {
                    GrigliaCapi(filtrati, onApri = { aperto = it.id }, Modifier.weight(1f))
                }
            }
        }
    }
}

/** Chiede conferma prima di eliminare un Capo, avvisando dei [quanti] Preferiti che spariscono con lui. */
@Composable
private fun ConfermaEliminazione(quanti: Int, onConferma: () -> Unit, onAnnulla: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text("Eliminare il capo?") },
        text = {
            Text(
                when (quanti) {
                    0 -> "Il capo e la sua foto spariranno dal Guardaroba."
                    1 -> "Il capo compare in 1 Preferito: verrà eliminato anche quello."
                    else -> "Il capo compare in $quanti Preferiti: verranno eliminati anche quelli."
                },
            )
        },
        confirmButton = { TextButton(onClick = onConferma) { Text("Elimina") } },
        dismissButton = { TextButton(onClick = onAnnulla) { Text("Annulla") } },
    )
}

/** Una riga per filtro, scorrevole in orizzontale; toccare di nuovo la voce scelta la toglie. */
@Composable
private fun BarraFiltri(filtro: Filtro, onCambia: (Filtro) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp)) {
        RigaFiltri {
            if (filtro.attivo) {
                AssistChip(
                    onClick = { onCambia(Filtro()) },
                    label = { Text("Azzera") },
                    leadingIcon = { Icon(Icons.Filled.Close, contentDescription = null) },
                )
            }
            Categoria.entries.forEach {
                FilterChip(
                    selected = it == filtro.categoria,
                    onClick = { onCambia(filtro.copy(categoria = it.takeIf { it != filtro.categoria })) },
                    label = { Text(it.nome) },
                )
            }
        }
        RigaFiltri {
            (Stagione.entries - Stagione.TUTTO_L_ANNO).forEach { // "tutto l'anno" entra già in ogni filtro
                FilterChip(
                    selected = it == filtro.stagione,
                    onClick = { onCambia(filtro.copy(stagione = it.takeIf { it != filtro.stagione })) },
                    label = { Text(it.nome) },
                )
            }
        }
        RigaFiltri {
            PALETTE.forEach {
                FilterChip(
                    selected = it.id == filtro.colore,
                    onClick = { onCambia(filtro.copy(colore = it.id.takeIf { it != filtro.colore })) },
                    label = { Text(it.nome) },
                    leadingIcon = {
                        Box(
                            Modifier
                                .size(16.dp)
                                .background(it.campione(), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RigaFiltri(voci: @Composable () -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { voci() }
}

@Composable
private fun GrigliaCapi(capi: List<Capo>, onApri: (Capo) -> Unit, modifier: Modifier = Modifier) {
    val perCategoria = capi.groupBy { it.categoria }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(96.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
    ) {
        Categoria.entries.forEach { categoria ->
            val gruppo = perCategoria[categoria] ?: return@forEach
            item(key = categoria.name, span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    categoria.nome,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
                )
            }
            items(gruppo, key = { it.id }) { RiquadroCapo(it, Modifier.clickable { onApri(it) }) }
        }
    }
}

/** La foto del Capo se c'è, altrimenti (o finché si carica) il riquadro del suo Colore principale. */
@Composable
fun RiquadroCapo(capo: Capo, modifier: Modifier = Modifier) {
    val principale = colore(capo.colorePrincipale)
    val forma = RoundedCornerShape(12.dp)
    Box(
        modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(forma)
            .background(principale.campione())
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(capo.tipo, color = principale.testoSopra(), style = MaterialTheme.typography.labelLarge)
            }
            // Il Colore secondario come fascia in basso nel riquadro.
            capo.coloreSecondario?.let { Box(Modifier.fillMaxWidth().weight(0.3f).background(colore(it).campione())) }
        }
        capo.percorsoFoto?.let { FotoCapo(it, Modifier.matchParentSize()) }
    }
}

/** La foto [nome] di [Foto], ritagliata a riempire [modifier]; niente finché non è caricata o se il file manca. */
@Composable
fun FotoCapo(nome: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val immagine by produceState<ImageBitmap?>(null, nome) {
        // Metà risoluzione (360 px) basta per una miniatura e dimezza la memoria della griglia.
        val opzioni = BitmapFactory.Options().apply { inSampleSize = 2 }
        value = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(Foto(context).file(nome).path, opzioni)?.asImageBitmap() }
    }
    immagine?.let { Image(it, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop) }
}
