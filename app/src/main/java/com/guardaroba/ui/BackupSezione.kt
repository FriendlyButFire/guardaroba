package com.guardaroba.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guardaroba.archivio.Backup
import com.guardaroba.archivio.LivelloBackup
import com.guardaroba.archivio.StatoBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/** Quanto occupa il backup automatico rispetto ai 25 MB di Android, con gli avvisi dell'ADR-0003. */
@Composable
fun BackupSezione() {
    val context = LocalContext.current
    // Ricalcolato ogni volta che si apre Impostazioni: la sezione esce dalla composizione cambiando scheda.
    val stato by produceState<StatoBackup?>(null) { value = withContext(Dispatchers.IO) { Backup(context).stato() } }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Backup automatico", style = MaterialTheme.typography.titleMedium)
        val s = stato ?: return@Column
        Text("${mb(s.byte)} MB usati su ${mb(StatoBackup.LIMITE)} MB · ${s.foto} foto")
        LinearProgressIndicator(
            progress = { (s.byte.toFloat() / StatoBackup.LIMITE).coerceAtMost(1f) },
            Modifier.fillMaxWidth(),
            color = if (s.livello == LivelloBackup.OK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        when (s.livello) {
            LivelloBackup.OK -> Text("Guardaroba, Preferiti e foto sono inclusi nel backup di Android.")
            LivelloBackup.AVVISO -> Text(
                "Il backup ha superato l'80% del limite. Elimina qualche foto o esporta il Guardaroba in ZIP.",
                color = MaterialTheme.colorScheme.error,
            )
            LivelloBackup.FERMO -> Text(
                "Limite di 25 MB raggiunto: il backup automatico è fermo e i nuovi dati non vengono salvati. " +
                    "Esporta il Guardaroba in ZIP per averne una copia.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun mb(byte: Long) = String.format(Locale.ITALIAN, "%.1f", byte / (1024.0 * 1024))
