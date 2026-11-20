package com.guardaroba.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Material 3, follows the system light/dark setting; dynamic colours on Android 12+. */
@Composable
fun GuardarobaTheme(content: @Composable () -> Unit) {
    val scuro = isSystemInDarkTheme()
    val colori = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (scuro) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        scuro -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colori, content = content)
}
