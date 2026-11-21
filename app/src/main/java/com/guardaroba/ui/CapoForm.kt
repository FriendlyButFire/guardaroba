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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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

/**
 * Scheda di un Capo: nuovo se [iniziale] è null, altrimenti la modifica di [iniziale] (stesso id).
 * Un campo = uno stato + una [Sezione]; nulla si salva finché non si preme Salva.
 * Una foto scelta qui è già compressa in [Foto]; se non arriva al salvataggio (sostituita, o form chiuso senza Salva) il form la cancella,
 * mentre quella di [iniziale] la cancella [com.guardaroba.archivio.CapoDao.salva] solo a Salva.
 * [onElimina] chiede di eliminare [iniziale]: la conferma la chiede chi chiama. [onPartiDa] è "Parti da questo capo" su [iniziale].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapoForm(
    onSalva: (Capo) -> Unit,
    onAnnulla: () -> Unit,
    iniziale: Capo? = null,
    onElimina: () -> Unit,
    onPartiDa: () -> Unit,
) {
    var categoria by rememberSaveable { mutableStateOf(iniziale?.categoria) }
    var tipo by rememberSaveable { mutableStateOf(iniziale?.tipo) }
    var colorePrincipale by rememberSaveable { mutableStateOf(iniziale?.colorePrincipale) }
    var coloreSecondario by rememberSaveable { mutableStateOf(iniziale?.coloreSecondario) }
    var motivo by rememberSaveable { mutableStateOf(iniziale?.motivo ?: Motivo.TINTA_UNITA) }
    var stagione by rememberSaveable { mutableStateOf(iniziale?.stagione ?: Stagione.TUTTO_L_ANNO) }
    var taglia by rememberSaveable { mutableStateOf(iniziale?.taglia.orEmpty()) }
    var percorsoFoto by rememberSaveable { mutableStateOf(iniziale?.percorsoFoto) }
    var inCompressione by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val foto = remember { Foto(context) }
    val scope = rememberCoroutineScope()
    fun scarta(nome: String?) { if (nome != iniziale?.percorsoFoto) foto.elimina(nome) }
    // Il form si chiude con Annulla, Indietro, cambio di sezione o chiusura dell'app: la foto non salvata va via in ogni caso.
    // Non a una rotazione, dove rememberSaveable la ripresenta.
    // shortcut: se Android uccide il processo e l'utente non torna al form, o se il form si chiude a metà compressione,
    // il file resta orfano in foto/; aggiungere una pulizia dei file non usati da nessun Capo se capita davvero.
    val activity = LocalActivity.current
    var salvato by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose { if (!salvato && activity?.isChangingConfigurations != true) scarta(percorsoFoto) }
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
                .onSuccess { scarta(percorsoFoto); percorsoFoto = it }
                .onFailure { Toast.makeText(context, "Impossibile leggere la foto", Toast.LENGTH_SHORT).show() }
        }
    }

    // La fotocamera scrive l'originale in cache; se ne tiene solo la versione compressa.
    val scatto = remember { File(context.cacheDir, "scatti/scatto.jpg").apply { parentFile!!.mkdirs() } }
    val fotocamera = rememberLauncherForActivityResult(TakePicture()) { ok ->
        if (ok) usa(Uri.fromFile(scatto)) { scatto.delete() } else scatto.delete()
    }
    val galleria = rememberLauncherForActivityResult(PickVisualMedia()) { uri -> uri?.let { usa(it) } }


    BackHandler(onBack = onAnnulla)

    val c = categoria
    val t = tipo
    val p = colorePrincipale
    Scaffold(
        contentWindowInsets = WindowInsets(0), // insets already applied by the outer Scaffold
        topBar = {
            TopAppBar(
                title = { Text(if (iniziale == null) "Nuovo capo" else "Modifica capo") },
                navigationIcon = {
                    IconButton(onClick = onAnnulla) { Icon(Icons.Filled.Close, contentDescription = "Chiudi") }
                },
                actions = {
                    TextButton(
                        enabled = c != null && t != null && p != null && !inCompressione,
                        onClick = {
                            // copy() del Capo modificato tiene id, creatoIl e ogni campo che il form non mostra.
                            salvato = true
                            onSalva(
                                (iniziale ?: Capo(categoria = c!!, tipo = t!!, colorePrincipale = p!!)).copy(
                                    categoria = c!!, tipo = t!!, colorePrincipale = p!!, coloreSecondario = coloreSecondario,
                                    motivo = motivo, stagione = stagione, taglia = taglia.trim().ifEmpty { null },
                                    percorsoFoto = percorsoFoto,
                                ),
                            )
                        },
                    ) { Text("Salva") }
                    if (iniziale != null) {
                        AltreAzioni {
                            // Parte dal Capo salvato: le modifiche non salvate qui si perdono, come chiudendo.
                            DropdownMenuItem(text = { Text("Parti da questo capo") }, onClick = { it(); onPartiDa() })
                            DropdownMenuItem(
                                text = { Text("Elimina", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
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
                percorsoFoto,
                inCompressione,
                onScatta = {
                    try {
                        fotocamera.launch(FileProvider.getUriForFile(context, "${context.packageName}.scatti", scatto))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "Nessuna app fotocamera", Toast.LENGTH_SHORT).show()
                    }
                },
                onGalleria = { galleria.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                onRimuovi = { scarta(percorsoFoto); percorsoFoto = null },
            )

            Sezione("Categoria") {
                Categoria.entries.forEach {
                    FilterChip(
                        selected = it == categoria,
                        onClick = { if (it != categoria) { categoria = it; tipo = null } },
                        label = { Text(it.nome) },
                    )
                }
            }
            categoria?.let { scelta ->
                Sezione("Tipo") {
                    scelta.tipi.forEach {
                        FilterChip(selected = it == tipo, onClick = { tipo = it }, label = { Text(it) })
                    }
                }
            }

            SceltaColore("Colore principale", colorePrincipale) { colorePrincipale = it }
            // Il Colore secondario resta chiuso finché non serve; è già aperto se il Capo ne ha uno.
            var conSecondario by rememberSaveable { mutableStateOf(coloreSecondario != null) }
            if (conSecondario) {
                // Toccare di nuovo il Colore scelto lo toglie; Rimuovi lo toglie e richiude la scelta.
                SceltaColore("Colore secondario", coloreSecondario, onRimuovi = { coloreSecondario = null; conSecondario = false }) {
                    coloreSecondario = it.takeIf { it != coloreSecondario }
                }
            } else {
                TextButton(onClick = { conSecondario = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Aggiungi colore secondario")
                }
            }

            Sezione("Motivo") {
                Motivo.entries.forEach {
                    FilterChip(selected = it == motivo, onClick = { motivo = it }, label = { Text(it.nome) })
                }
            }
            Sezione("Stagione") {
                Stagione.entries.forEach {
                    FilterChip(selected = it == stagione, onClick = { stagione = it }, label = { Text(it.nome) })
                }
            }

            OutlinedTextField(
                value = taglia,
                onValueChange = { taglia = it },
                label = { Text("Taglia (opzionale)") },
                placeholder = { Text("es. M, 42, W32 L34") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
