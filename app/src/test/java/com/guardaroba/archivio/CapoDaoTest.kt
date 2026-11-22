package com.guardaroba.archivio

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Stagione
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CapoDaoTest {
    @Test
    fun `un Capo salvato si ritrova riaprendo il database, con tutti i campi`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val capo = Capo(
            categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "blu_navy", coloreSecondario = "bianco",
            motivo = Motivo.RIGHE, stagione = Stagione.MEZZA_STAGIONE, taglia = "W32 L34", creatoIl = 1,
        )
        GuardarobaDatabase.apri(context).run {
            capoDao().salva(capo)
            close()
        }

        val riaperto = GuardarobaDatabase.apri(context)
        val capi = riaperto.capoDao().tutti().first()
        riaperto.close()

        assertEquals(listOf(capo.copy(id = 1)), capi)
    }

    @Test
    fun `salvare un Capo esistente lo aggiorna, stesso id e nessun duplicato`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = GuardarobaDatabase.apri(context)
        val dao = db.capoDao()
        val id = dao.salva(Capo(categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "bianco", creatoIl = 1))
        val salvato = dao.tutti().first().single()

        val modificato = salvato.copy(tipo = "polo", colorePrincipale = "rosso", taglia = "L")
        dao.salva(modificato)
        val capi = dao.tutti().first()
        db.close()

        assertEquals(1L, id)
        assertEquals(listOf(modificato), capi)
    }

    @Test
    fun `sostituire o rimuovere la foto di un Capo cancella il file precedente`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val foto = Foto(context)
        val db = GuardarobaDatabase.apri(context)
        val dao = db.capoDao()
        fun nuovaFoto(nome: String) = nome.also { foto.file(it).apply { parentFile!!.mkdirs() }.writeText("foto") }
        val id = dao.salva(Capo(categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "bianco", percorsoFoto = nuovaFoto("a.webp")), foto)
        val capo = dao.tutti().first().single()

        dao.salva(capo.copy(percorsoFoto = nuovaFoto("b.webp")), foto)
        val dopoSostituzione = listOf("a.webp", "b.webp").map { foto.file(it).exists() }
        dao.salva(capo.copy(percorsoFoto = null), foto)
        val dopoRimozione = foto.file("b.webp").exists()
        val capi = dao.tutti().first()
        db.close()

        assertEquals(1L, id)
        assertEquals(listOf(false, true), dopoSostituzione)
        assertEquals(false, dopoRimozione)
        assertEquals(listOf(capo.copy(percorsoFoto = null)), capi)
    }

    @Test
    fun `salvare un Capo senza cambiare la foto la tiene`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val foto = Foto(context)
        foto.file("a.webp").apply { parentFile!!.mkdirs() }.writeText("foto")
        val db = GuardarobaDatabase.apri(context)
        val dao = db.capoDao()
        dao.salva(Capo(categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "bianco", percorsoFoto = "a.webp"), foto)

        dao.salva(dao.tutti().first().single().copy(tipo = "polo"), foto)
        db.close()

        assertEquals(true, foto.file("a.webp").exists())
    }
}
