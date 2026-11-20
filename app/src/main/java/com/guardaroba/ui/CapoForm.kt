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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
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
 * [onElimina] chiede di eliminare [iniziale]: la conferma la chiede chi chiama.
 */
@Composable
fun CapoForm(onSalva: (Capo) -> Unit, onAnnulla: () -> Unit, iniziale: Capo? = null, onElimina: () -> Unit = {}) {
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

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(if (iniziale == null) "Nuovo capo" else "Modifica capo", style = MaterialTheme.typography.headlineSmall)

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
        // Toccare di nuovo il Colore secondario scelto lo toglie.
        SceltaColore("Colore secondario (opzionale)", coloreSecondario) {
            coloreSecondario = it.takeIf { it != coloreSecondario }
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
        Text("Foto (opzionale)", style = MaterialTheme.typography.titleMedium)
        percorsoFoto?.let { FotoCapo(it, Modifier.size(160.dp).clip(RoundedCornerShape(12.dp))) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = !inCompressione, onClick = {
                try {
                    fotocamera.launch(FileProvider.getUriForFile(context, "${context.packageName}.scatti", scatto))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(context, "Nessuna app fotocamera", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Scatta") }
            OutlinedButton(enabled = !inCompressione, onClick = {
                galleria.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }) { Text("Dalla galleria") }
            if (percorsoFoto != null) {
                TextButton(enabled = !inCompressione, onClick = { scarta(percorsoFoto); percorsoFoto = null }) { Text("Rimuovi") }
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

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            if (iniziale != null) {
                TextButton(
                    onClick = onElimina,
                    enabled = !inCompressione,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Elimina") }
                Spacer(Modifier.weight(1f))
            }
            OutlinedButton(onClick = onAnnulla) { Text("Annulla") }
            val c = categoria
            val t = tipo
            val p = colorePrincipale
            Button(
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
        }
    }
}

@Composable
private fun Sezione(
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
private fun SceltaColore(titolo: String, selezionato: String?, onScelta: (String) -> Unit) {
    Text(titolo, style = MaterialTheme.typography.titleMedium)
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
                .border(
                    if (selezionato) 3.dp else 1.dp,
                    if (selezionato) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selezionato) Icon(Icons.Filled.Check, contentDescription = null, tint = colore.testoSopra())
        }
        Text(colore.nome, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}
