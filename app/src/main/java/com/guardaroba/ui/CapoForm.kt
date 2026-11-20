package com.guardaroba.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Colore
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.PALETTE
import com.guardaroba.motore.Stagione

/**
 * Scheda di un Capo: nuovo se [iniziale] è null, altrimenti la modifica di [iniziale] (stesso id).
 * Un campo = uno stato + una [Sezione]; nulla si salva finché non si preme Salva.
 */
@Composable
fun CapoForm(onSalva: (Capo) -> Unit, onAnnulla: () -> Unit, iniziale: Capo? = null) {
    var categoria by rememberSaveable { mutableStateOf(iniziale?.categoria) }
    var tipo by rememberSaveable { mutableStateOf(iniziale?.tipo) }
    var colorePrincipale by rememberSaveable { mutableStateOf(iniziale?.colorePrincipale) }
    var coloreSecondario by rememberSaveable { mutableStateOf(iniziale?.coloreSecondario) }
    var motivo by rememberSaveable { mutableStateOf(iniziale?.motivo ?: Motivo.TINTA_UNITA) }
    var stagione by rememberSaveable { mutableStateOf(iniziale?.stagione ?: Stagione.TUTTO_L_ANNO) }
    var taglia by rememberSaveable { mutableStateOf(iniziale?.taglia.orEmpty()) }

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
        OutlinedTextField(
            value = taglia,
            onValueChange = { taglia = it },
            label = { Text("Taglia (opzionale)") },
            placeholder = { Text("es. M, 42, W32 L34") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            OutlinedButton(onClick = onAnnulla) { Text("Annulla") }
            val c = categoria
            val t = tipo
            val p = colorePrincipale
            Button(
                enabled = c != null && t != null && p != null,
                onClick = {
                    // copy() del Capo modificato tiene id, creatoIl e ogni campo che il form non mostra.
                    onSalva(
                        (iniziale ?: Capo(categoria = c!!, tipo = t!!, colorePrincipale = p!!)).copy(
                            categoria = c!!, tipo = t!!, colorePrincipale = p!!, coloreSecondario = coloreSecondario,
                            motivo = motivo, stagione = stagione, taglia = taglia.trim().ifEmpty { null },
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
