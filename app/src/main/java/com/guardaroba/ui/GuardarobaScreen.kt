package com.guardaroba.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Colore
import com.guardaroba.motore.colore

/** Il campione a schermo di un Colore della Palette. */
fun Colore.campione(): Color = Color.hsl(h.toFloat(), s / 100f, l / 100f)

/** Testo leggibile sopra il campione. */
fun Colore.testoSopra(): Color = if (l > 55) Color.Black else Color.White

@Composable
fun GuardarobaScreen(vm: GuardarobaViewModel = viewModel(factory = GuardarobaViewModel.Factory)) {
    val capi by vm.capi.collectAsStateWithLifecycle()
    var formAperto by rememberSaveable { mutableStateOf(false) }

    if (formAperto) {
        BackHandler { formAperto = false }
        CapoForm(
            onSalva = { vm.aggiungi(it); formAperto = false },
            onAnnulla = { formAperto = false },
        )
        return
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0), // insets already applied by the outer Scaffold
        floatingActionButton = {
            FloatingActionButton(onClick = { formAperto = true }) {
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
            GrigliaCapi(lista, Modifier.padding(padding))
        }
    }
}

@Composable
private fun GrigliaCapi(capi: List<Capo>, modifier: Modifier = Modifier) {
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
            items(gruppo, key = { it.id }) { RiquadroCapo(it) }
        }
    }
}

@Composable
fun RiquadroCapo(capo: Capo, modifier: Modifier = Modifier) {
    val principale = colore(capo.colorePrincipale)
    val forma = RoundedCornerShape(12.dp)
    Column(
        modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(forma)
            .background(principale.campione())
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma),
    ) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Text(capo.tipo, color = principale.testoSopra(), style = MaterialTheme.typography.labelLarge)
        }
        // Il Colore secondario come fascia in basso nel riquadro.
        capo.coloreSecondario?.let { Box(Modifier.fillMaxWidth().weight(0.3f).background(colore(it).campione())) }
    }
}
