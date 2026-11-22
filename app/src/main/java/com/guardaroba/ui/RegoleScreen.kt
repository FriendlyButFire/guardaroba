package com.guardaroba.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.PALETTE
import com.guardaroba.motore.colore

/** Una regola di armonia (ADR-0004) con un esempio di Colori della Palette; `RegoleTest` controlla che il Motore la giudichi così. */
internal data class Regola(val nome: String, val testo: String, val esempio: List<String>)

internal val REGOLE = listOf(
    Regola(
        "neutri",
        PALETTE.filter { it.neutro }.map { it.nome }.let { it.dropLast(1).joinToString() + " e " + it.last() }.maiuscola() +
            " stanno bene con tutto, anche tra loro.",
        listOf("nero", "beige", "denim"),
    ),
    Regola("un colore + neutri", "Un colore solo, con quanti Neutri vuoi: i Neutri lo fanno risaltare.", listOf("rosso", "nero", "bianco")),
    Regola("monocromatico", "Due toni dello stesso colore, uno più chiaro e uno più scuro.", listOf("azzurro", "blu_royal", "bianco")),
    Regola("analoghi", "Due colori vicini sulla ruota dei colori: insieme sono armoniosi.", listOf("giallo", "arancione", "beige")),
    Regola("complementari", "Due colori opposti sulla ruota dei colori: si fanno risaltare a vicenda.", listOf("rosso", "verde_bosco", "nero")),
)

/** "Come scegliamo gli abbinamenti", aperta da Impostazioni: le regole del Motore in parole semplici, con un esempio a colori (ADR-0015). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegoleScreen(onIndietro: () -> Unit) {
    BackHandler(onBack = onIndietro)
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Come scegliamo gli abbinamenti") },
            navigationIcon = { IconButton(onClick = onIndietro) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro") } },
            windowInsets = WindowInsets(0),
        )
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "Ogni Proposta usa solo i tuoi Capi e segue una di queste regole. Contano i Colori principali; " +
                    "il Colore secondario e gli Accessori sono liberi.",
            )
            REGOLE.forEach { regola ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Titolo(regola.nome.maiuscola())
                    Text(regola.testo)
                    Esempio(regola.esempio)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Titolo("In più")
                Text("Al massimo due colori oltre ai Neutri: tre colori insieme sono troppi.")
                val motivi = Motivo.entries.filter { it != Motivo.TINTA_UNITA }.joinToString { it.nome }
                Text("Al massimo un Capo con un motivo ($motivi): gli altri a tinta unita.")
            }
        }
    }
}

/** I Colori dell'esempio in fila, letti a voce come "Esempio: rosso, nero, bianco". */
@Composable
private fun Esempio(ids: List<String>) {
    val colori = ids.map(::colore)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { contentDescription = "Esempio: " + colori.joinToString { it.nome } },
    ) {
        colori.forEach { c ->
            Box(Modifier.size(24.dp).background(c.campione(), CircleShape).border(c.bordo(), CircleShape))
        }
        Text(colori.joinToString(" + ") { it.nome }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
