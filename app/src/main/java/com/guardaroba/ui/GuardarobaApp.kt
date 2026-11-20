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
import androidx.compose.material3.MaterialTheme
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
        // Placeholder per section until its ticket lands.
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text(sezione.titolo, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Preview
@Composable
private fun GuardarobaAppPreview() {
    GuardarobaTheme { GuardarobaApp() }
}
