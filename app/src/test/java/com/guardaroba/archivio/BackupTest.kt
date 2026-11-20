package com.guardaroba.archivio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BackupTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun scrivi(file: File, byte: Int) {
        file.parentFile!!.mkdirs()
        file.writeBytes(ByteArray(byte))
    }

    @Test
    fun `senza database né foto il backup è vuoto`() {
        assertEquals(StatoBackup(byte = 0, foto = 0), Backup(context).stato())
    }

    @Test
    fun `il backup conta database e foto, non la cache né altri file`() {
        scrivi(context.getDatabasePath(GuardarobaDatabase.NOME), 4096)
        scrivi(context.getDatabasePath("${GuardarobaDatabase.NOME}-wal"), 1000)
        scrivi(File(context.filesDir, "foto/a.webp"), 50_000)
        scrivi(File(context.filesDir, "foto/b.webp"), 40_000)
        scrivi(File(context.cacheDir, "scatti/scatto.jpg"), 3_000_000)
        scrivi(File(context.filesDir, "altro.txt"), 7)

        assertEquals(StatoBackup(byte = 95_096, foto = 2), Backup(context).stato())
    }

    @Test
    fun `l'avviso parte dall'80 per cento dei 25 MB e il backup si ferma al 100`() {
        val mb = 1024L * 1024
        assertEquals(LivelloBackup.OK, StatoBackup(20 * mb - 1, 0).livello)
        assertEquals(LivelloBackup.AVVISO, StatoBackup(20 * mb, 0).livello)
        assertEquals(LivelloBackup.AVVISO, StatoBackup(25 * mb - 1, 0).livello)
        assertEquals(LivelloBackup.FERMO, StatoBackup(25 * mb, 0).livello)
    }
}
