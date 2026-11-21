package com.guardaroba.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
        Titolo("Backup automatico")
        val s = stato ?: return@Column
        Text("${mb(s.byte)} MB usati su ${mb(StatoBackup.LIMITE)} MB · ${s.foto} foto")
        LinearProgressIndicator(
            progress = { (s.byte.toFloat() / StatoBackup.LIMITE).coerceAtMost(1f) },
            Modifier.fillMaxWidth(),
            color = if (s.livello == LivelloBackup.OK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        when (s.livello) {
            // L'app non può sapere se il backup di Google è acceso: lo dice come condizione e porta dove si controlla (ADR-0015).
            LivelloBackup.OK -> Text("Se il backup di Google è attivo sul telefono, Guardaroba, Preferiti e foto sono inclusi.")
            LivelloBackup.AVVISO -> Text(
                "Il backup ha superato l'${StatoBackup.SOGLIA_AVVISO * 100 / StatoBackup.LIMITE}% del limite. Elimina qualche foto o salva una copia con «Esporta» qui sotto.",
                color = MaterialTheme.colorScheme.error,
            )
            LivelloBackup.FERMO -> Text(
                "Limite di ${mb(StatoBackup.LIMITE)} MB raggiunto: il backup automatico è fermo e i nuovi dati non vengono salvati. " +
                    "Salva una copia con «Esporta» qui sotto.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        TextButton(onClick = { apriBackupDiSistema(context) }) { Text("Controlla il backup del telefono") }
    }
}

/** La pagina Backup delle impostazioni di sistema. L'azione non è pubblica: dove manca si aprono le Impostazioni. */
private fun apriBackupDiSistema(context: Context) {
    try {
        context.startActivity(Intent("com.android.settings.BACKUP_SETTINGS"))
    } catch (e: RuntimeException) { // ActivityNotFoundException, o SecurityException se un produttore la tiene privata
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}

private fun mb(byte: Long) = String.format(Locale.ITALIAN, "%.1f", byte / (1024.0 * 1024))
