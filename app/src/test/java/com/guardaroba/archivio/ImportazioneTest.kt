package com.guardaroba.archivio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Stagione
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class ImportazioneTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val db = GuardarobaDatabase.apri(context)
    private val foto = Foto(context)
    private val cartellaFoto = File(context.filesDir, Foto.CARTELLA)

    @After fun chiudi() = db.close()

    private fun salvaFoto(nome: String, bytes: ByteArray) = foto.file(nome).apply { parentFile!!.mkdirs() }.writeBytes(bytes)

    private fun esporta(): ByteArray = ByteArrayOutputStream().also { runBlocking { Esportazione(db, foto).esporta(it) } }.toByteArray()

    private fun importa(zip: ByteArray) = runBlocking { Importazione(db, foto).importa(zip.inputStream()) }

    /** Il Guardaroba com'è ora: Capi con i byte della loro foto al posto del nome del file, e Preferiti con gli id dei Capi. */
    private fun istantanea() = runBlocking {
        val capi = db.capoDao().tutti().first().map { it.copy(percorsoFoto = null) to it.percorsoFoto?.let { n -> foto.file(n).readBytes().toList() } }
        val preferiti = db.preferitoDao().tutti().first().map { it.preferito to it.capi.map { c -> c.id }.toSet() }
        Triple(capi, preferiti, cartellaFoto.list().orEmpty().toSet())
    }

    /** Un Guardaroba con due Capi (uno con foto) e un Preferito. */
    private fun riempi(): Pair<Long, Long> = runBlocking {
        salvaFoto("a.webp", byteArrayOf(1, 2, 3, 4))
        val camicia = db.capoDao().salva(
            Capo(
                categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "blu_navy", coloreSecondario = "bianco",
                motivo = Motivo.RIGHE, stagione = Stagione.ESTIVO, taglia = "M", percorsoFoto = "a.webp", creatoIl = 10,
            ),
        )
        val jeans = db.capoDao().salva(Capo(categoria = Categoria.SOTTO, tipo = "jeans", colorePrincipale = "denim", creatoIl = 20))
        db.preferitoDao().salva("blu navy + denim: neutri", setOf(camicia, jeans), nome = "Ufficio")
        camicia to jeans
    }

    /** Sostituisce il Guardaroba con un altro, diverso in tutto: Capi, Preferiti e foto. */
    private fun cambiaTutto() = runBlocking {
        db.capoDao().tutti().first().forEach { db.capoDao().elimina(it.id, foto) }
        salvaFoto("b.webp", byteArrayOf(9, 9))
        val scarpe = db.capoDao().salva(Capo(categoria = Categoria.SCARPE, tipo = "sneakers", colorePrincipale = "nero", percorsoFoto = "b.webp", creatoIl = 5))
        val gonna = db.capoDao().salva(Capo(categoria = Categoria.SOTTO, tipo = "gonna", colorePrincipale = "rosso", creatoIl = 6))
        db.preferitoDao().salva("altro", setOf(scarpe, gonna))
    }

    /** `scripts/dati-di-prova.zip` (docs/agents/prove-su-dispositivo.md) è lo zip del JSON accanto, e si importa con Preferiti validi. */
    @Test
    fun `i dati di prova si importano`() {
        val zip = File("../scripts/dati-di-prova.zip").readBytes()
        val json = ZipInputStream(zip.inputStream()).use { it.nextEntry; it.readBytes().decodeToString() }
        assertEquals(File("../scripts/dati-di-prova/guardaroba.json").readText(), json)

        importa(zip)

        assertEquals(14, runBlocking { db.capoDao().tutti().first() }.size)
        val preferiti = runBlocking { db.preferitoDao().tutti().first() }
        assertEquals(2, preferiti.size)
        assertTrue(preferiti.all { it.giudizio() is com.guardaroba.motore.Giudizio.Valido })
    }

    @Test
    fun `il giro export, import ripristina Capi, Preferiti e foto identici`() {
        riempi()
        val prima = istantanea()
        val zip = esporta()
        cambiaTutto()

        importa(zip)

        val dopo = istantanea()
        assertEquals(prima.first, dopo.first)
        assertEquals(prima.second, dopo.second)
        // Le foto del Guardaroba sostituito spariscono: nella cartella resta solo quella importata.
        assertEquals(1, dopo.third.size)
        assertEquals(runBlocking { db.capoDao().tutti().first() }.mapNotNull { it.percorsoFoto }.toSet(), dopo.third)
    }

    @Test
    fun `importare lo stesso export sul Guardaroba da cui viene lo lascia identico`() {
        riempi()
        val prima = istantanea()

        importa(esporta())

        assertEquals(prima.first, istantanea().first)
        assertEquals(prima.second, istantanea().second)
    }

    @Test
    fun `un Preferito importato si ritrova anche salvando di nuovo lo stesso Outfit`() = runBlocking {
        val (camicia, jeans) = riempi()
        val zip = esporta()
        cambiaTutto()
        importa(zip)

        val id = db.preferitoDao().salva("di nuovo", setOf(camicia, jeans))

        assertEquals(1, db.preferitoDao().tutti().first().count { it.preferito.id == id })
        assertNotNull(db.preferitoDao().tutti().first().single { it.preferito.id == id }.preferito.nome)
    }

    // --- file non validi: il Guardaroba resta intatto ---

    private fun zip(vararg voci: Pair<String, ByteArray>): ByteArray = ByteArrayOutputStream().also { out ->
        ZipOutputStream(out).use { z -> voci.forEach { (nome, bytes) -> z.putNextEntry(ZipEntry(nome)); z.write(bytes) } }
    }.toByteArray()

    /** Le voci dell'export di [riempi], con il JSON passato da [modifica]. */
    private fun exportModificato(extra: List<Pair<String, ByteArray>> = emptyList(), modifica: (JSONObject) -> Unit = {}): ByteArray {
        val voci = ZipInputStream(esporta().inputStream()).use { z -> generateSequence { z.nextEntry }.map { it.name to z.readBytes() }.toList() }
        val modificate = voci.map { (nome, bytes) ->
            if (nome == Esportazione.NOME_JSON) nome to JSONObject(bytes.decodeToString()).also(modifica).toString().encodeToByteArray() else nome to bytes
        }
        return zip(*(modificate + extra).toTypedArray())
    }

    private fun JSONObject.capo(i: Int) = getJSONArray("capi").getJSONObject(i)
    private fun JSONObject.preferito(i: Int) = getJSONArray("preferiti").getJSONObject(i)

    @Test
    fun `un file non valido lascia Capi, Preferiti e foto intatti`() {
        riempi()
        val validi = esporta()
        val nonValidi = mapOf(
            "non è uno ZIP" to "ciao".encodeToByteArray(),
            "ZIP troncato" to validi.copyOf(validi.size / 2),
            "ZIP senza JSON" to zip("foto/a.webp" to byteArrayOf(1)),
            "JSON rovinato" to zip(Esportazione.NOME_JSON to "{capi:".encodeToByteArray()),
            "versione futura" to exportModificato { it.put("versione", 2) },
            "senza versione" to exportModificato { it.remove("versione") },
            "categoria sconosciuta" to exportModificato { it.capo(0).put("categoria", "CALZINI") },
            "colore fuori Palette" to exportModificato { it.capo(0).put("colorePrincipale", "fucsia_neon") },
            "colore secondario fuori Palette" to exportModificato { it.capo(0).put("coloreSecondario", "fucsia_neon") },
            "motivo mancante" to exportModificato { it.capo(0).remove("motivo") },
            "stagione sconosciuta" to exportModificato { it.capo(0).put("stagione", "PRIMAVERA") },
            "id come testo" to exportModificato { it.capo(0).put("id", "1") },
            "id ripetuto" to exportModificato { it.capo(1).put("id", it.capo(0).getLong("id")) },
            "tipo non testo" to exportModificato { it.capo(0).put("tipo", 3) },
            "foto mancante nello ZIP" to exportModificato { it.capo(1).put("percorsoFoto", "manca.webp") },
            "Preferito con un Capo che non c'è" to exportModificato { it.preferito(0).getJSONArray("capi").put(999) },
            "Preferito senza Capi" to exportModificato { it.preferito(0).put("capi", org.json.JSONArray()) },
            "Preferito senza la lista dei Capi" to exportModificato { it.preferito(0).remove("capi") },
            "due Preferiti con gli stessi Capi" to exportModificato {
                it.getJSONArray("preferiti").put(JSONObject(it.preferito(0).toString()).put("id", it.preferito(0).getLong("id") + 1))
            },
            "Preferito senza spiegazione" to exportModificato { it.preferito(0).remove("spiegazione") },
            "voce estranea" to exportModificato(listOf("altro.txt" to byteArrayOf(1))),
            "voce che esce dalla cartella" to exportModificato(listOf("foto/../guardaroba.db" to byteArrayOf(1))),
            "JSON vuoto" to zip(Esportazione.NOME_JSON to "{}".encodeToByteArray()),
        )
        val prima = istantanea()

        nonValidi.forEach { (caso, file) ->
            assertThrows(caso, ArchivioNonValido::class.java) { importa(file) }
            assertEquals(caso, prima, istantanea())
        }
    }

    @Test
    fun `se la transazione fallisce le foto copiate dallo ZIP spariscono e il Guardaroba resta intatto`() {
        riempi()
        val zip = esporta()
        cambiaTutto()
        val prima = istantanea()
        // I Capi sono già stati sostituiti quando l'inserimento dei Preferiti fallisce: tutto deve tornare indietro.
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER blocca BEFORE INSERT ON preferito BEGIN SELECT RAISE(ABORT, 'prova'); END")

        assertThrows(Exception::class.java) { importa(zip) }

        assertEquals(prima, istantanea())
    }

    @Test
    fun `l'export valido usato per costruire i casi non validi si importa davvero`() {
        riempi()
        val prima = istantanea()

        importa(exportModificato())

        assertEquals(prima.first, istantanea().first)
        assertTrue(istantanea().third.size == 1)
    }
}
