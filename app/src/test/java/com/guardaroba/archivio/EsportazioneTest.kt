package com.guardaroba.archivio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Stagione
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

@RunWith(AndroidJUnit4::class)
class EsportazioneTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val db = GuardarobaDatabase.apri(context)
    private val foto = Foto(context)

    @After fun chiudi() = db.close()

    /** Le voci dello ZIP esportato, per nome. */
    private fun esporta(): Map<String, ByteArray> {
        val zip = ByteArrayOutputStream().also { runBlocking { Esportazione(db, foto).esporta(it) } }
        return ZipInputStream(zip.toByteArray().inputStream()).use { z ->
            generateSequence { z.nextEntry }.associate { it.name to z.readBytes() }
        }
    }

    @Test
    fun `lo ZIP contiene il JSON con versione, tutti i campi di Capi e Preferiti, e le foto`() = runBlocking {
        val bytesFoto = byteArrayOf(1, 2, 3, 4)
        foto.file("a.webp").apply { parentFile!!.mkdirs() }.writeBytes(bytesFoto)
        val camicia = Capo(
            categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "blu_navy", coloreSecondario = "bianco",
            motivo = Motivo.RIGHE, stagione = Stagione.ESTIVO, taglia = "M", percorsoFoto = "a.webp", creatoIl = 10,
        )
        val jeans = Capo(categoria = Categoria.SOTTO, tipo = "jeans", colorePrincipale = "denim", creatoIl = 20)
        val idCamicia = db.capoDao().salva(camicia)
        val idJeans = db.capoDao().salva(jeans)
        db.preferitoDao().salva("blu navy + denim: neutri", setOf(idCamicia, idJeans), nome = "Ufficio")

        val voci = esporta()

        assertEquals(setOf(Esportazione.JSON, "foto/a.webp"), voci.keys)
        assertArrayEquals(bytesFoto, voci.getValue("foto/a.webp"))
        val json = JSONObject(voci.getValue(Esportazione.JSON).decodeToString())
        assertEquals(1, json.getInt("versione"))
        val capi = json.getJSONArray("capi")
        assertEquals(2, capi.length())
        capi.getJSONObject(0).run {
            assertEquals(idCamicia, getLong("id"))
            assertEquals("SOPRA", getString("categoria"))
            assertEquals("camicia", getString("tipo"))
            assertEquals("blu_navy", getString("colorePrincipale"))
            assertEquals("bianco", getString("coloreSecondario"))
            assertEquals("RIGHE", getString("motivo"))
            assertEquals("ESTIVO", getString("stagione"))
            assertEquals("M", getString("taglia"))
            assertEquals("a.webp", getString("percorsoFoto"))
            assertEquals(10L, getLong("creatoIl"))
        }
        capi.getJSONObject(1).run {
            assertTrue(isNull("coloreSecondario"))
            assertTrue(isNull("taglia"))
            assertTrue(isNull("percorsoFoto"))
        }
        val preferito = json.getJSONArray("preferiti").getJSONObject(0)
        assertEquals("Ufficio", preferito.getString("nome"))
        assertEquals("blu navy + denim: neutri", preferito.getString("spiegazione"))
        assertTrue(preferito.has("id") && preferito.has("creatoIl"))
        val idCapi = preferito.getJSONArray("capi").let { a -> (0 until a.length()).map { a.getLong(it) }.toSet() }
        assertEquals(setOf(idCamicia, idJeans), idCapi)
    }

    @Test
    fun `un Capo la cui foto non è più sul disco esce senza foto, e lo ZIP resta coerente`() = runBlocking {
        db.capoDao().salva(Capo(categoria = Categoria.SCARPE, tipo = "sneakers", colorePrincipale = "bianco", percorsoFoto = "sparita.webp"))

        val voci = esporta()

        assertEquals(setOf(Esportazione.JSON), voci.keys)
        val capo = JSONObject(voci.getValue(Esportazione.JSON).decodeToString()).getJSONArray("capi").getJSONObject(0)
        assertTrue(capo.isNull("percorsoFoto"))
    }

    @Test
    fun `un Guardaroba vuoto si esporta comunque`() {
        val json = JSONObject(esporta().getValue(Esportazione.JSON).decodeToString())

        assertEquals(0, json.getJSONArray("capi").length())
        assertEquals(0, json.getJSONArray("preferiti").length())
    }
}
