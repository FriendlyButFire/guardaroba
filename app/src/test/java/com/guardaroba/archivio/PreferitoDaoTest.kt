package com.guardaroba.archivio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Giudizio
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
    fun `modificare un Capo di un Preferito ne aggiorna il Giudizio, e un Outfit non più valido resta tra i Preferiti`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val (camicia, jeans, sneakers) = db.inserisci(sopra, sotto, scarpe)
        db.preferitoDao().salva("blu navy + denim + bianco: neutri", setOf(camicia.id, jeans.id, sneakers.id))
        suspend fun giudizio() = db.preferitoDao().tutti().first().single().giudizio()
        assertEquals(Giudizio.Valido("blu navy + denim + bianco: neutri"), giudizio())

        db.capoDao().salva(camicia.copy(colorePrincipale = "rosso"))
        assertEquals(Giudizio.Valido("rosso + denim + bianco: un colore + neutri"), giudizio())

        db.capoDao().salva(jeans.copy(colorePrincipale = "giallo"))
        assertEquals(Giudizio.NonValido("rosso + giallo + bianco non si abbinano"), giudizio())
        db.close()
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

        val colloquio = dao.salva("neutri", setOf(a, b, d), nome = "  colloquio ")!!
        val senzaNome = dao.salva("neutri", setOf(a, b, c), nome = "   ")!!
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
        val eliminato = dao.salva("neutri", setOf(a, b, c))!!
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

    @Test
    fun `eliminare un Capo elimina in cascata i suoi Preferiti e la sua foto, e lascia intatti gli altri`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val foto = Foto(context)
        foto.file("scarpe.webp").apply { parentFile!!.mkdirs() }.writeText("foto")
        val capi = db.inserisci(sopra, sotto, scarpe.copy(percorsoFoto = "scarpe.webp"), altreScarpe)
        val (a, b, c, d) = capi.map { it.id }
        val dao = db.preferitoDao()
        dao.salva("neutri", setOf(a, b, c))
        dao.salva("neutri", setOf(a, c))
        val resta = dao.salva("neutri", setOf(a, b, d))

        val quanti = db.capoDao().quantiPreferiti(c)
        db.capoDao().elimina(c, foto)
        val preferiti = dao.tutti().first()
        val guardaroba = db.capoDao().tutti().first()
        db.close()

        assertEquals(2, quanti)
        assertEquals(listOf(resta), preferiti.map { it.preferito.id })
        assertEquals(setOf(a, b, d), preferiti.single().capi.map { it.id }.toSet())
        assertEquals(capi.map { it.id } - c, guardaroba.map { it.id })
        assertEquals(false, foto.file("scarpe.webp").exists())
    }

    @Test
    fun `un Capo che non è in nessun Preferito ne conta zero`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val (a, b) = db.inserisci(sopra, sotto).map { it.id }
        db.preferitoDao().salva("neutri", setOf(a))

        val quanti = db.capoDao().quantiPreferiti(b)
        db.close()

        assertEquals(0, quanti)
    }

    @Test
    fun `un Outfit con un Capo eliminato nel frattempo non si salva, e non rompe niente`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val (a, b, c) = db.inserisci(sopra, sotto, scarpe).map { it.id }
        db.capoDao().eliminaConPreferiti(c)

        val id = db.preferitoDao().salva("neutri", setOf(a, b, c))
        val preferiti = db.preferitoDao().tutti().first()
        db.close()

        assertEquals(null, id)
        assertEquals(emptyList<PreferitoConCapi>(), preferiti)
    }

    @Test
    fun `un Preferito eliminato si ripristina con lo stesso id, nome e data`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val (a, b, c) = db.inserisci(sopra, sotto, scarpe).map { it.id }
        val dao = db.preferitoDao()
        dao.salva("neutri", setOf(a, b, c), nome = "colloquio")
        val prima = dao.tutti().first().single()

        dao.elimina(prima.preferito.id)
        dao.ripristina(prima)
        val dopo = dao.tutti().first().single()
        db.close()

        assertEquals(prima.preferito, dopo.preferito)
        assertEquals(prima.capi.toSet(), dopo.capi.toSet())
    }

    @Test
    fun `un Preferito non si ripristina se un suo Capo non c'è più o se l'Outfit è già tornato tra i Preferiti`() = runBlocking {
        val db = GuardarobaDatabase.apri(context)
        val (a, b, c, d) = db.inserisci(sopra, sotto, scarpe, altreScarpe).map { it.id }
        val dao = db.preferitoDao()
        dao.salva("neutri", setOf(a, b, c))
        dao.salva("neutri", setOf(a, b, d))
        val tutti = dao.tutti().first()
        val conC = tutti.single { p -> p.capi.any { it.id == c } }
        val conD = tutti.single { p -> p.capi.any { it.id == d } }

        dao.elimina(conC.preferito.id)
        db.capoDao().eliminaConPreferiti(c)
        dao.ripristina(conC)
        dao.elimina(conD.preferito.id)
        val risalvato = dao.salva("neutri", setOf(a, b, d))
        dao.ripristina(conD)
        val preferiti = dao.tutti().first()
        db.close()

        assertEquals(listOf(risalvato), preferiti.map { it.preferito.id })
    }
}
