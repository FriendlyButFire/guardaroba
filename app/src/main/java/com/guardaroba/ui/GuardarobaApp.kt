package com.guardaroba.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardaroba.archivio.Capo

enum class Sezione(val titolo: String, val icona: ImageVector) {
    GUARDAROBA("Guardaroba", Icons.AutoMirrored.Filled.List),
    OUTFIT("Outfit", Icons.Filled.Face),
    PREFERITI("Preferiti", Icons.Filled.Favorite),
    IMPOSTAZIONI("Impostazioni", Icons.Filled.Settings),
}

// shortcut: sections switch via saveable state, no navigation library; add one when a section needs sub-screens with a back stack.
@Composable
fun GuardarobaApp() {
    var sezione by rememberSaveable { mutableStateOf(Sezione.GUARDAROBA) }
    val outfit: OutfitViewModel = viewModel(factory = OutfitViewModel.Factory)
    val partiDa = { capo: Capo -> outfit.partiDa(capo.id); sezione = Sezione.OUTFIT }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Sezione.entries.forEach {
                    NavigationBarItem(
                        selected = it == sezione,
                        onClick = { sezione = it },
                        icon = { Icon(it.icona, contentDescription = null) },
                        label = { Text(it.titolo) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (sezione) {
                Sezione.GUARDAROBA -> GuardarobaScreen(onPartiDa = partiDa)
                Sezione.OUTFIT -> OutfitScreen(outfit)
                // La scheda di un Capo aperta da un Preferito: chiusa, si torna al Preferito (ADR-0014).
                Sezione.PREFERITI -> PreferitiScreen { id, chiudi -> GuardarobaScreen(onPartiDa = partiDa, capoAperto = id, onChiusa = chiudi) }
                // Dopo un Importa gli id indicano altri Capi: la sessione Outfit riparte da zero (ADR-0010).
                Sezione.IMPOSTAZIONI -> ImpostazioniScreen(onImportato = { outfit.partiDa(null) })
            }
        }
    }
}

@Preview
@Composable
private fun GuardarobaAppPreview() {
    GuardarobaTheme { GuardarobaApp() }
}
