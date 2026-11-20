package com.guardaroba.archivio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.motore.Categoria
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferitoDaoTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val sopra = Capo(categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "blu_navy", creatoIl = 1)
    private val sotto = Capo(categoria = Categoria.SOTTO, tipo = "jeans", colorePrincipale = "denim", creatoIl = 2)
    private val scarpe = Capo(categoria = Categoria.SCARPE, tipo = "sneakers", colorePrincipale = "bianco", creatoIl = 3)
    private val altreScarpe = Capo(categoria = Categoria.SCARPE, tipo = "eleganti", colorePrincipale = "nero", creatoIl = 4)

    private fun GuardarobaDatabase.inserisci(vararg capi: Capo) = runBlocking {
        capi.map { it.copy(id = capoDao().salva(it)) }
    }

    @Test
    fun `un Preferito salvato si ritrova riaprendo il database, con i suoi Capi e la Spiegazione`() = runBlocking {
        val capi = GuardarobaDatabase.apri(context).run {
            inserisci(sopra, sotto, scarpe).also {
                preferitoDao().salva("blu navy + denim + bianco: neutri", it.map { c -> c.id }.toSet())
                close()
            }
        }

        val riaperto = GuardarobaDatabase.apri(context)
        val preferiti = riaperto.preferitoDao().tutti().first()
        riaperto.close()

        assertEquals(1, preferiti.size)
        assertEquals("blu navy + denim + bianco: neutri", preferiti[0].preferito.spiegazione)
        assertEquals(capi.toSet(), preferiti[0].capi.toSet())
    }

    @Test
    fun `salvare due volte lo stesso Outfit non crea doppioni, un Outfit diverso sì`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val (a, b, c, d) = db.inserisci(sopra, sotto, scarpe, altreScarpe).map { it.id }

        val primo = db.preferitoDao().salva("neutri", setOf(a, b, c))
        val ripetuto = db.preferitoDao().salva("neutri", setOf(c, b, a))
        db.preferitoDao().salva("neutri", setOf(a, b)) // sottoinsieme: è un altro Outfit
        db.preferitoDao().salva("neutri", setOf(a, b, d))
        val preferiti = db.preferitoDao().tutti().first()
        db.close()

        assertEquals(primo, ripetuto)
        assertEquals(setOf(setOf(a, b, c), setOf(a, b), setOf(a, b, d)), preferiti.map { p -> p.capi.map { it.id }.toSet() }.toSet())
    }

    @Test
    fun `il nome è opzionale al salvataggio e si può cambiare o togliere`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val (a, b, c, d) = db.inserisci(sopra, sotto, scarpe, altreScarpe).map { it.id }
        val dao = db.preferitoDao()

        val colloquio = dao.salva("neutri", setOf(a, b, d), nome = "  colloquio ")
        val senzaNome = dao.salva("neutri", setOf(a, b, c), nome = "   ")
        suspend fun nomi() = dao.tutti().first().associate { it.preferito.id to it.preferito.nome }
        assertEquals(mapOf(colloquio to "colloquio", senzaNome to null), nomi())

        dao.rinomina(senzaNome, " weekend")
        dao.rinomina(colloquio, "")
        assertEquals(mapOf(colloquio to null, senzaNome to "weekend"), nomi())
        db.close()
    }

    @Test
    fun `eliminare un Preferito lascia intatti i suoi Capi e gli altri Preferiti`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val capi = db.inserisci(sopra, sotto, scarpe, altreScarpe)
        val (a, b, c, d) = capi.map { it.id }
        val dao = db.preferitoDao()
        val eliminato = dao.salva("neutri", setOf(a, b, c))
        val resta = dao.salva("neutri", setOf(a, b, d))

        dao.elimina(eliminato)
        val righeRimaste = dao.conICapi(setOf(a, b, c), 3)
        val preferiti = dao.tutti().first()
        val guardaroba = db.capoDao().tutti().first()
        db.close()

        assertEquals(null, righeRimaste) // la cascata ha tolto le sue righe di preferito_capo
        assertEquals(listOf(resta), preferiti.map { it.preferito.id })
        assertEquals(capi.toSet(), guardaroba.toSet())
    }
}
