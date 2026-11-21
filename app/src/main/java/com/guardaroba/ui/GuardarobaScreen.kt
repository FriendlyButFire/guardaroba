package com.guardaroba.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
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

/**
 * Il bordo di un campione o di un riquadro: marcato quando il Colore si confonde con lo sfondo (un Capo nero nel tema scuro,
 * uno bianco in quello chiaro), cioè sotto il contrasto 3:1 che WCAG chiede agli elementi non testuali.
 */
@Composable
fun Colore.bordo(): BorderStroke =
    if (contrasto(campione(), MaterialTheme.colorScheme.surface) < 3f) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

/** Testo leggibile sopra il campione: tra nero e bianco quello col contrasto WCAG più alto. */
fun Colore.testoSopra(): Color = campione().let { if (contrasto(Color.Black, it) >= contrasto(Color.White, it)) Color.Black else Color.White }

/** Il rapporto di contrasto WCAG tra due colori, da 1 a 21. */
private fun contrasto(a: Color, b: Color): Float {
    val (scura, chiara) = listOf(a.luminance(), b.luminance()).sorted()
    return (chiara + 0.05f) / (scura + 0.05f)
}

/** Le etichette cominciano con la maiuscola; i nomi del Motore ("estivo", "rosso", "t-shirt") sono minuscoli e restano così nei dati. */
fun String.maiuscola() = replaceFirstChar { it.uppercase() }

private const val NUOVO = 0L // l'id di un Capo non ancora salvato

@Composable
fun GuardarobaScreen(
    onPartiDa: (Capo) -> Unit,
    vm: GuardarobaViewModel = viewModel(factory = GuardarobaViewModel.Factory),
    capoAperto: Long? = null,
    onChiusa: (() -> Unit)? = null,
) {
    val capi by vm.capi.collectAsStateWithLifecycle()
    // Id del Capo aperto nella scheda: null = chiusa, NUOVO = Capo nuovo.
    // Con [onChiusa] (scheda aperta da Preferiti, ADR-0014) chiudere la scheda torna da chi l'ha aperta invece che alla griglia.
    var aperto by rememberSaveable { mutableStateOf(capoAperto) }
    // Quanti Preferiti contengono il Capo aperto, mentre è aperta la conferma di eliminazione; null = nessuna conferma.
    var preferitiDaEliminare by rememberSaveable { mutableStateOf<Int?>(null) }
    var filtriAperti by rememberSaveable { mutableStateOf(false) }
    // Con un Capo nuovo: l'id del Capo da cui parte con Duplica, null = scheda vuota.
    var modello by rememberSaveable { mutableStateOf<Long?>(null) }
    // Cambia a ogni scheda aperta, anche da Nuovo a Nuovo ("Salva e aggiungi un altro"), così riparte da zero.
    var scheda by rememberSaveable { mutableIntStateOf(0) }
    val avvisi = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun apri(id: Long?, da: Long? = null) {
        if (id == null && onChiusa != null) onChiusa() else { aperto = id; modello = da; scheda++ }
    }

    aperto?.let { id ->
        val iniziale = capi?.find { it.id == id }
        val partenza = when {
            id != NUOVO -> iniziale?.bozza()
            modello != null -> capi?.find { it.id == modello }?.duplicato()
            else -> Bozza()
        }
        // Un Capo esistente si apre solo quando la lista è caricata (es. dopo che il processo è stato ricreato).
        if (partenza != null) {
            key(scheda) {
                CapoForm(
                    partenza = partenza,
                    iniziale = iniziale,
                    avvisi = avvisi,
                    onSalva = { vm.salva(it); apri(null) },
                    onSalvaEAltro = {
                        vm.salva(it)
                        apri(NUOVO)
                        scope.launch { avvisi.showSnackbar("Capo salvato") }
                    },
                    onChiudi = { apri(null) },
                    onElimina = { scope.launch { preferitiDaEliminare = vm.quantiPreferiti(id) } },
                    onPartiDa = { apri(null); iniziale?.let(onPartiDa) },
                    onDuplica = { apri(NUOVO, da = id) },
                )
            }
            preferitiDaEliminare?.let { quanti ->
                ConfermaEliminazione(
                    quanti,
                    onConferma = { vm.elimina(id); preferitiDaEliminare = null; apri(null) },
                    onAnnulla = { preferitiDaEliminare = null },
                )
            }
            return
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0), // insets already applied by the outer Scaffold
        floatingActionButton = {
            FloatingActionButton(onClick = { apri(NUOVO) }) {
                Icon(Icons.Filled.Add, contentDescription = "Aggiungi capo")
            }
        },
    ) { padding ->
        val lista = capi ?: run {
            // Ancora in caricamento: un indicatore, non lo stato vuoto a sproposito né uno schermo bianco.
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        if (lista.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Il Guardaroba è vuoto.\nPremi + per aggiungere un capo.", textAlign = TextAlign.Center)
            }
        } else {
            val filtrati = lista.filter(vm.filtro::ammette)
            Column(Modifier.padding(padding)) {
                FiltriAttivi(vm.filtro, onApri = { filtriAperti = true }) { vm.filtro = it }
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
                    GrigliaCapi(filtrati, onApri = { apri(it.id) }, Modifier.weight(1f))
                }
            }
            if (filtriAperti) {
                PannelloFiltri(vm.filtro, filtrati.size, onCambia = { vm.filtro = it }, onChiudi = { filtriAperti = false })
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
                    0 -> "Il capo sparirà dal Guardaroba."
                    1 -> "Il capo compare in 1 Preferito: verrà eliminato anche quello."
                    else -> "Il capo compare in $quanti Preferiti: verranno eliminati anche quelli."
                },
            )
        },
        confirmButton = { TextButton(onClick = onConferma) { Text("Elimina") } },
        dismissButton = { TextButton(onClick = onAnnulla) { Text("Annulla") } },
    )
}

/** Il pulsante Filtri, poi solo i filtri attivi: toccarne uno lo toglie. */
@Composable
private fun FiltriAttivi(filtro: Filtro, onApri: () -> Unit, onCambia: (Filtro) -> Unit) {
    FlowRow(
        Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = onApri,
            label = { Text("Filtri") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        )
        filtro.categoria?.let { FiltroAttivo(it.nome) { onCambia(filtro.copy(categoria = null)) } }
        filtro.stagione?.let { FiltroAttivo(it.nome.maiuscola()) { onCambia(filtro.copy(stagione = null)) } }
        filtro.colore?.let(::colore)?.let { FiltroAttivo(it.nome.maiuscola(), it) { onCambia(filtro.copy(colore = null)) } }
        if (listOfNotNull(filtro.categoria, filtro.stagione, filtro.colore).size > 1) {
            TextButton(onClick = { onCambia(Filtro()) }) { Text("Azzera") }
        }
    }
}

@Composable
private fun FiltroAttivo(etichetta: String, colore: Colore? = null, onTogli: () -> Unit) {
    InputChip(
        selected = true,
        onClick = onTogli,
        label = { Text(etichetta) },
        leadingIcon = colore?.let { { PuntoColore(it) } },
        trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Togli", Modifier.size(InputChipDefaults.IconSize)) },
    )
}

@Composable
private fun PuntoColore(colore: Colore) {
    Box(Modifier.size(16.dp).background(colore.campione(), CircleShape).border(colore.bordo(), CircleShape))
}

/** Il pannello dal basso con tutti i filtri: si applicano subito; toccare di nuovo la voce scelta la toglie. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PannelloFiltri(filtro: Filtro, quanti: Int, onCambia: (Filtro) -> Unit, onChiudi: () -> Unit) {
    val stato = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // shortcut: Material3 1.4.0 pads the sheet content by the status-bar inset minus the sheet offset, so the sheet's height
    // follows its own position: a fast fling up overshoots under the status bar, the Expanded anchor moves, the settle
    // restarts, and the sheet bounces forever. The sheet stops below the status bar instead (consumed here, so the content
    // padding no longer depends on the offset); re-check without this when upgrading Material3.
    ModalBottomSheet(
        onDismissRequest = onChiudi,
        sheetState = stato,
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Filtri", style = MaterialTheme.typography.titleLarge)
            Sezione("Categoria") {
                Categoria.entries.forEach {
                    FilterChip(
                        selected = it == filtro.categoria,
                        onClick = { onCambia(filtro.copy(categoria = it.takeIf { it != filtro.categoria })) },
                        label = { Text(it.nome) },
                    )
                }
            }
            Sezione("Stagione") {
                (Stagione.entries - Stagione.TUTTO_L_ANNO).forEach { // "tutto l'anno" entra già in ogni filtro
                    FilterChip(
                        selected = it == filtro.stagione,
                        onClick = { onCambia(filtro.copy(stagione = it.takeIf { it != filtro.stagione })) },
                        label = { Text(it.nome.maiuscola()) },
                    )
                }
            }
            Sezione("Colore") {
                PALETTE.forEach {
                    FilterChip(
                        selected = it.id == filtro.colore,
                        onClick = { onCambia(filtro.copy(colore = it.id.takeIf { it != filtro.colore })) },
                        label = { Text(it.nome.maiuscola()) },
                        leadingIcon = { PuntoColore(it) },
                    )
                }
            }
            Button(
                onClick = { scope.launch { stato.hide() }.invokeOnCompletion { onChiudi() } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when (quanti) {
                        0 -> "Nessun capo: chiudi"
                        1 -> "Mostra 1 capo"
                        else -> "Mostra $quanti capi"
                    },
                )
            }
        }
    }
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
            .border(principale.bordo(), forma),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(capo.tipo.maiuscola(), color = principale.testoSopra(), style = MaterialTheme.typography.labelLarge)
            }
            // Il Colore secondario come fascia in basso nel riquadro.
            capo.coloreSecondario?.let { Box(Modifier.fillMaxWidth().weight(0.3f).background(colore(it).campione())) }
        }
        capo.percorsoFoto?.let { FotoCapo(it, Modifier.matchParentSize()) }
    }
}

/** La foto [nome] di [Foto], ritagliata a riempire [modifier]; niente finché non è caricata o se il file manca. [intera] per la foto grande della scheda. */
@Composable
fun FotoCapo(nome: String, modifier: Modifier = Modifier, intera: Boolean = false) {
    val context = LocalContext.current
    val immagine by produceState<ImageBitmap?>(null, nome) {
        // Nelle miniature basta metà risoluzione (360 px): dimezza la memoria della griglia.
        val opzioni = BitmapFactory.Options().apply { inSampleSize = if (intera) 1 else 2 }
        value = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(Foto(context).file(nome).path, opzioni)?.asImageBitmap() }
    }
    immagine?.let { Image(it, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop) }
}
