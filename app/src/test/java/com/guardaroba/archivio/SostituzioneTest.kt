package com.guardaroba.archivio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.motore.Categoria
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(AndroidJUnit4::class)
class SostituzioneTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val db = GuardarobaDatabase.apri(context)
    private val foto = Foto(context)
    private val copia = File(context.filesDir, "prova-copia.zip")
    private val sostituzione = Sostituzione(Esportazione(db, foto), Importazione(db, foto), copia)

    @After fun chiudi() {
        db.close()
        copia.delete()
    }

    private fun tipi() = runBlocking { db.capoDao().tutti().first().map { it.tipo }.toSet() }

    /** Lo ZIP di un Guardaroba con la sola camicia; poi nel database resta solo la gonna. */
    private fun zipConCamiciaPoiGonna(): ByteArray = runBlocking {
        db.capoDao().salva(Capo(categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "bianco", creatoIl = 1))
        val zip = ByteArrayOutputStream().also { Esportazione(db, foto).esporta(it) }.toByteArray()
        db.capoDao().eliminaTutti()
        db.capoDao().salva(Capo(categoria = Categoria.SOTTO, tipo = "gonna", colorePrincipale = "nero", creatoIl = 1))
        zip
    }

    @Test fun `prima di sostituire salva il Guardaroba di adesso, e ripristina torna indietro`() = runBlocking {
        val zip = zipConCamiciaPoiGonna()
        assertNull(sostituzione.copiaDel())

        sostituzione.sostituisci(zip.inputStream())
        assertEquals(setOf("camicia"), tipi())
        assertNotNull(sostituzione.copiaDel())

        sostituzione.ripristina()
        assertEquals(setOf("gonna"), tipi())

        // Anche il ripristino ha messo da parte quello che c'era: si torna alla camicia.
        sostituzione.ripristina()
        assertEquals(setOf("camicia"), tipi())
    }

    @Test fun `un file non valido non tocca il Guardaroba, e la copia è uguale a quello di adesso`() = runBlocking {
        zipConCamiciaPoiGonna()

        assertThrows(ArchivioNonValido::class.java) { runBlocking { sostituzione.sostituisci("non è uno zip".byteInputStream()) } }
        assertEquals(setOf("gonna"), tipi())
        assertFalse(File(copia.path + ".tmp").exists())

        sostituzione.ripristina()
        assertEquals(setOf("gonna"), tipi())
    }
}
