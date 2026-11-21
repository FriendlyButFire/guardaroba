package com.guardaroba.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Le Impostazioni: una sezione per argomento. Esporta sta sotto il Backup, a cui rimandano i suoi avvisi; Importa la segue (ADR-0015). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpostazioniScreen(
    onImportato: () -> Unit,
    esporta: EsportaViewModel = viewModel(factory = EsportaViewModel.Factory),
    importa: ImportaViewModel = viewModel(factory = ImportaViewModel.Factory),
) {
    var regole by rememberSaveable { mutableStateOf(false) }
    val scorrimento = rememberScrollState() // sopra l'if: tornando dalle regole si riparte da dove si era
    if (regole) {
        RegoleScreen(onIndietro = { regole = false })
        return
    }
    // Come nei Preferiti, lo snackbar vive con questa schermata: aprendo le regole o cambiando sezione si perde (vedi MostraAvviso).
    val avvisi = remember { SnackbarHostState() }
    Scaffold(
        contentWindowInsets = WindowInsets(0), // insets already applied by the outer Scaffold
        topBar = { TopAppBar(title = { Text("Impostazioni") }, windowInsets = WindowInsets(0)) },
        snackbarHost = { SnackbarHost(avvisi) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(scorrimento).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            BackupSezione()
            // Un Esporta mentre Importa cancella le foto vecchie (o il contrario) farebbe uno ZIP incoerente: uno alla volta.
            EsportaSezione(avvisi, bloccato = importa.inCorso, vm = esporta)
            ImportaSezione(onImportato, avvisi, bloccato = esporta.inCorso, vm = importa)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Titolo("Abbinamenti")
                Text("Le regole sui colori che l'app usa per ogni Proposta, con un esempio per ciascuna.")
                OutlinedButton(onClick = { regole = true }) { Text("Come scegliamo gli abbinamenti") }
            }
            val context = LocalContext.current
            val versione = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
            Text("Guardaroba $versione", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Il titolo di una sezione delle Impostazioni, detto da TalkBack come intestazione. */
@Composable
fun Titolo(testo: String) {
    Text(testo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
}

/**
 * Mostra [avviso] nello snackbar, con Annulla se [onAnnulla] c'è e l'avviso è annullabile; poi lo segna come visto, anche se la
 * schermata esce prima: un Annulla che ricompare minuti dopo sostituirebbe il Guardaroba a sorpresa.
 */
@Composable
fun MostraAvviso(avvisi: SnackbarHostState, avviso: Avviso?, onVisto: () -> Unit, onAnnulla: (() -> Unit)? = null) {
    LaunchedEffect(avviso) {
        val a = avviso ?: return@LaunchedEffect
        avvisi.currentSnackbarData?.dismiss()
        try {
            val azione = "Annulla".takeIf { a.annullabile && onAnnulla != null }
            val esito = avvisi.showSnackbar(a.testo, actionLabel = azione, duration = SnackbarDuration.Long)
            if (esito == SnackbarResult.ActionPerformed) onAnnulla?.invoke()
        } finally {
            onVisto()
        }
    }
}

/** "21 novembre 2026". */
fun data(millis: Long): String =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN).format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
