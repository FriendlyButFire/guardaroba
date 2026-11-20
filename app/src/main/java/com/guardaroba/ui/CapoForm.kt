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
import com.guardaroba.motore.PALETTE

/** Form del nuovo Capo: un campo = uno stato + una [Sezione]. */
@Composable
fun CapoForm(onSalva: (Capo) -> Unit, onAnnulla: () -> Unit) {
    var categoria by rememberSaveable { mutableStateOf<Categoria?>(null) }
    var tipo by rememberSaveable { mutableStateOf<String?>(null) }
    var colorePrincipale by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Nuovo capo", style = MaterialTheme.typography.headlineSmall)

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

        Text("Colore principale", style = MaterialTheme.typography.titleMedium)
        PALETTE.partition { it.neutro }.toList().zip(listOf("Neutri", "Colori")).forEach { (gruppo, titolo) ->
            Sezione(titolo, MaterialTheme.typography.labelLarge) {
                gruppo.forEach { CampioneColore(it, it.id == colorePrincipale) { colorePrincipale = it.id } }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            OutlinedButton(onClick = onAnnulla) { Text("Annulla") }
            val c = categoria
            val t = tipo
            val p = colorePrincipale
            Button(
                enabled = c != null && t != null && p != null,
                onClick = { onSalva(Capo(categoria = c!!, tipo = t!!, colorePrincipale = p!!)) },
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
