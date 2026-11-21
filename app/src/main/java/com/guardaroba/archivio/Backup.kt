package com.guardaroba.archivio

import android.content.Context
import java.io.File

/**
 * Quanto pesa il backup automatico di Android: gli stessi file inclusi da `res/xml/backup_*.xml`,
 * cioè la cartella dei database e quella delle [Foto] (ADR-0003).
 */
class Backup(private val context: Context) {
    // shortcut: somma i byte dei file, senza l'overhead dell'archivio che crea Android; il margine dell'80% lo copre.
    fun stato(): StatoBackup {
        val database = context.getDatabasePath(GuardarobaDatabase.NOME).parentFile!!.walk().filter { it.isFile }.sumOf { it.length() }
        val foto = File(context.filesDir, Foto.CARTELLA).walk().filter { it.isFile }.toList()
        return StatoBackup(database + foto.sumOf { it.length() }, foto.size)
    }
}

data class StatoBackup(val byte: Long, val foto: Int) {
    val livello
        get() = when {
            byte >= LIMITE -> LivelloBackup.FERMO
            byte >= SOGLIA_AVVISO -> LivelloBackup.AVVISO
            else -> LivelloBackup.OK
        }

    companion object {
        /** La quota del backup automatico per app: oltre, Android smette di farlo. */
        const val LIMITE = 25L * 1024 * 1024

        /** Da qui l'app avvisa: l'80% del [LIMITE] (ADR-0003). */
        const val SOGLIA_AVVISO = LIMITE * 8 / 10
    }
}

enum class LivelloBackup { OK, AVVISO, FERMO }
