package com.guardaroba.ui

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.Foto
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Colore
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.PALETTE
import com.guardaroba.motore.Stagione
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Come si esce dalla scheda: con modifiche non salvate prima si chiede se scartarle. */
private enum class Uscita { CHIUDI, PARTI_DA, DUPLICA }

/**
 * Scheda di un Capo: nuovo se [iniziale] è null, altrimenti la modifica di [iniziale] (stesso id). Parte da [partenza]:
 * vuota, il Capo da modificare o quello da duplicare. Nulla si salva finché non si preme Salva.
 * Una foto scelta qui è già compressa in [Foto]; se non arriva al salvataggio (sostituita, o form chiuso senza Salva) il form la cancella,
 * mentre quella di [iniziale] la cancella [com.guardaroba.archivio.CapoDao.salva] solo a Salva.
 * [onSalvaEAltro] (solo per un Capo nuovo) salva e chiede una scheda vuota. Su [iniziale]: [onElimina] chiede di eliminarlo
 * (la conferma la chiede chi chiama), [onPartiDa] è "Parti da questo capo", [onDuplica] apre un Capo nuovo uguale.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapoForm(
    partenza: Bozza,
    iniziale: Capo?,
    avvisi: SnackbarHostState,
    onSalva: (Capo) -> Unit,
    onSalvaEAltro: (Capo) -> Unit,
    onChiudi: () -> Unit,
    onElimina: () -> Unit,
    onPartiDa: () -> Unit,
    onDuplica: () -> Unit,
) {
    var bozza by rememberSaveable { mutableStateOf(partenza) }
    var inCompressione by remember { mutableStateOf(false) }
    var uscita by rememberSaveable { mutableStateOf<Uscita?>(null) } // l'uscita in attesa di "Scartare le modifiche?"

    val context = LocalContext.current
    val foto = remember { Foto(context) }
    val scope = rememberCoroutineScope()
    fun scarta(nome: String?) { if (nome != iniziale?.percorsoFoto) foto.elimina(nome) }
    // Il form si chiude con ✕, Indietro, cambio di sezione o chiusura dell'app: la foto non salvata va via in ogni caso.
    // Non a una rotazione, dove rememberSaveable la ripresenta.
    // shortcut: se Android uccide il processo e l'utente non torna al form, o se il form si chiude a metà compressione,
    // il file resta orfano in foto/; aggiungere una pulizia dei file non usati da nessun Capo se capita davvero.
    val activity = LocalActivity.current
    var salvato by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose { if (!salvato && activity?.isChangingConfigurations != true) scarta(bozza.percorsoFoto) }
    }
    fun usa(uri: Uri, poi: () -> Unit = {}) {
        inCompressione = true
        scope.launch {
            val esito = withContext(Dispatchers.IO) {
                runCatching { foto.comprimi { context.contentResolver.openInputStream(uri) ?: throw IOException("Foto non trovata") } }
                    .also { poi() }
            }
            inCompressione = false
            esito
                .onSuccess { scarta(bozza.percorsoFoto); bozza = bozza.copy(percorsoFoto = it) }
                .onFailure { Toast.makeText(context, "Impossibile leggere la foto", Toast.LENGTH_SHORT).show() }
        }
    }

    // La fotocamera scrive l'originale in cache; se ne tiene solo la versione compressa.
    val scatto = remember { File(context.cacheDir, "scatti/scatto.jpg").apply { parentFile!!.mkdirs() } }
    val fotocamera = rememberLauncherForActivityResult(TakePicture()) { ok ->
        if (ok) usa(Uri.fromFile(scatto)) { scatto.delete() } else scatto.delete()
    }
    val galleria = rememberLauncherForActivityResult(PickVisualMedia()) { uri -> uri?.let { usa(it) } }

    fun esegui(u: Uscita) = when (u) {
        Uscita.CHIUDI -> onChiudi()
        Uscita.PARTI_DA -> onPartiDa() // parte dal Capo salvato
        Uscita.DUPLICA -> onDuplica() // duplica il Capo salvato
    }
    fun esci(u: Uscita) { if (bozza != partenza) uscita = u else esegui(u) }
    BackHandler { esci(Uscita.CHIUDI) }

    val capo = bozza.capo(iniziale).takeIf { !inCompressione }
    fun salva(poi: (Capo) -> Unit) { capo?.let { salvato = true; poi(it) } }

    Scaffold(
        contentWindowInsets = WindowInsets(0), // insets already applied by the outer Scaffold
        snackbarHost = { SnackbarHost(avvisi) },
        topBar = {
            TopAppBar(
                title = { Text(if (iniziale == null) "Nuovo capo" else "Modifica capo") },
                navigationIcon = {
                    IconButton(onClick = { esci(Uscita.CHIUDI) }) { Icon(Icons.Filled.Close, contentDescription = "Chiudi") }
                },
                actions = {
                    TextButton(enabled = capo != null, onClick = { salva(onSalva) }) { Text("Salva") }
                    AltreAzioni {
                        if (iniziale == null) {
                            DropdownMenuItem(
                                text = { Text("Salva e aggiungi un altro") },
                                enabled = capo != null,
                                onClick = { it(); salva(onSalvaEAltro) },
                            )
                        } else {
                            DropdownMenuItem(text = { Text("Duplica") }, onClick = { it(); esci(Uscita.DUPLICA) })
                            DropdownMenuItem(text = { Text("Parti da questo capo") }, onClick = { it(); esci(Uscita.PARTI_DA) })
                            DropdownMenuItem(
                                text = { Text("Elimina", color = MaterialTheme.colorScheme.error) },
                                enabled = !inCompressione,
                                onClick = { it(); onElimina() },
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AreaFoto(
                bozza.percorsoFoto,
                inCompressione,
                onScatta = {
                    try {
                        fotocamera.launch(FileProvider.getUriForFile(context, "${context.packageName}.scatti", scatto))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "Nessuna app fotocamera", Toast.LENGTH_SHORT).show()
                    }
                },
                onGalleria = { galleria.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                onRimuovi = { scarta(bozza.percorsoFoto); bozza = bozza.copy(percorsoFoto = null) },
            )

            Sezione("Categoria") {
                Categoria.entries.forEach {
                    FilterChip(
                        selected = it == bozza.categoria,
                        onClick = { if (it != bozza.categoria) bozza = bozza.copy(categoria = it, tipo = null) },
                        label = { Text(it.nome) },
                    )
                }
            }
            bozza.categoria?.let { scelta ->
                Sezione("Tipo") {
                    scelta.tipi.forEach {
                        FilterChip(selected = it == bozza.tipo, onClick = { bozza = bozza.copy(tipo = it) }, label = { Text(it) })
                    }
                }
            }

            SceltaColore("Colore principale", bozza.colorePrincipale) { bozza = bozza.copy(colorePrincipale = it) }
            // Il Colore secondario resta chiuso finché non serve; è già aperto se il Capo ne ha uno.
            var conSecondario by rememberSaveable { mutableStateOf(bozza.coloreSecondario != null) }
            if (conSecondario) {
                // Toccare di nuovo il Colore scelto lo toglie; Rimuovi lo toglie e richiude la scelta.
                SceltaColore(
                    "Colore secondario",
                    bozza.coloreSecondario,
                    onRimuovi = { bozza = bozza.copy(coloreSecondario = null); conSecondario = false },
                ) { bozza = bozza.copy(coloreSecondario = it.takeIf { it != bozza.coloreSecondario }) }
            } else {
                TextButton(onClick = { conSecondario = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Aggiungi colore secondario")
                }
            }

            Sezione("Motivo") {
                Motivo.entries.forEach {
                    FilterChip(selected = it == bozza.motivo, onClick = { bozza = bozza.copy(motivo = it) }, label = { Text(it.nome) })
                }
            }
            Sezione("Stagione") {
                Stagione.entries.forEach {
                    FilterChip(selected = it == bozza.stagione, onClick = { bozza = bozza.copy(stagione = it) }, label = { Text(it.nome) })
                }
            }

            OutlinedTextField(
                value = bozza.taglia,
                onValueChange = { bozza = bozza.copy(taglia = it) },
                label = { Text("Taglia (opzionale)") },
                placeholder = { Text("es. M, 42, W32 L34") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    uscita?.let { u ->
        AlertDialog(
            onDismissRequest = { uscita = null },
            title = { Text("Scartare le modifiche?") },
            text = { Text("Le modifiche a questo capo non verranno salvate.") },
            confirmButton = { TextButton(onClick = { uscita = null; esegui(u) }) { Text("Scarta") } },
            dismissButton = { TextButton(onClick = { uscita = null }) { Text("Continua a modificare") } },
        )
    }
}

/** Il menu ⋮ della barra in alto; ogni voce riceve la funzione che chiude il menu. */
@Composable
private fun AltreAzioni(voci: @Composable (chiudi: () -> Unit) -> Unit) {
    var aperto by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { aperto = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Altre azioni") }
        DropdownMenu(expanded = aperto, onDismissRequest = { aperto = false }) { voci { aperto = false } }
    }
}

/** La foto in cima alla scheda, grande e toccabile: apre Scatta / Dalla galleria / Rimuovi foto. */
@Composable
private fun AreaFoto(
    percorso: String?,
    inCompressione: Boolean,
    onScatta: () -> Unit,
    onGalleria: () -> Unit,
    onRimuovi: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(16.dp)
    Box {
        Box(
            Modifier
                .fillMaxWidth()
                .then(if (percorso != null) Modifier.aspectRatio(4f / 3f) else Modifier.height(160.dp))
                .clip(forma)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(enabled = !inCompressione, onClickLabel = if (percorso != null) "Cambia foto" else "Aggiungi foto") { menu = true }
                .semantics { if (percorso != null) contentDescription = "Foto del capo" },
            contentAlignment = Alignment.Center,
        ) {
            when {
                inCompressione -> CircularProgressIndicator()
                percorso != null -> FotoCapo(percorso, Modifier.matchParentSize())
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Aggiungi foto", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Opzionale", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text("Scatta") }, onClick = { menu = false; onScatta() })
            DropdownMenuItem(text = { Text("Dalla galleria") }, onClick = { menu = false; onGalleria() })
            if (percorso != null) DropdownMenuItem(text = { Text("Rimuovi foto") }, onClick = { menu = false; onRimuovi() })
        }
    }
}

@Composable
internal fun Sezione(
    titolo: String,
    stile: TextStyle = MaterialTheme.typography.titleMedium,
    voci: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titolo, style = stile)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { voci() }
    }
}

/** Un Colore dalla Palette, con i Neutri raggruppati a parte. */
@Composable
private fun SceltaColore(titolo: String, selezionato: String?, onRimuovi: (() -> Unit)? = null, onScelta: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(titolo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        onRimuovi?.let { TextButton(onClick = it) { Text("Rimuovi") } }
    }
    PALETTE.partition { it.neutro }.toList().zip(listOf("Neutri", "Colori")).forEach { (gruppo, nomeGruppo) ->
        Sezione(nomeGruppo, MaterialTheme.typography.labelLarge) {
            gruppo.forEach { CampioneColore(it, it.id == selezionato) { onScelta(it.id) } }
        }
    }
}

@Composable
private fun CampioneColore(colore: Colore, selezionato: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.width(72.dp).selectable(selected = selezionato, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .background(colore.campione(), CircleShape)
                .border(if (selezionato) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else colore.bordo(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selezionato) Icon(Icons.Filled.Check, contentDescription = null, tint = colore.testoSopra())
        }
        Text(colore.nome, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}
