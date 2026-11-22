package com.guardaroba.ui

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.GuardarobaDatabase
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Esito
import com.guardaroba.motore.Stagione
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class OutfitViewModelTest {
    private val db = GuardarobaDatabase.apri(ApplicationProvider.getApplicationContext<Context>())
    private val capi = runBlocking {
        listOf(
            Capo(categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "bianco"),
            Capo(categoria = Categoria.SOTTO, tipo = "jeans", colorePrincipale = "denim"),
            Capo(categoria = Categoria.SCARPE, tipo = "sneakers", colorePrincipale = "nero"),
            Capo(categoria = Categoria.ACCESSORI, tipo = "cintura", colorePrincipale = "marrone"),
        ).map { it.copy(id = db.capoDao().salva(it)) }
    }
    private val vm = OutfitViewModel(db.capoDao(), db.preferitoDao())

    @After
    fun chiudi() = db.close()

    /** Lascia girare il main looper (viewModelScope) finché [condizione] vale, mentre Room lavora sui suoi thread. */
    private fun aspetta(condizione: () -> Boolean) {
        repeat(200) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condizione()) return
            Thread.sleep(10)
        }
        throw AssertionError("condizione mai vera")
    }

    @Test
    fun `eliminare un Capo della Proposta mostrata la toglie, e Salva non salva un Capo che non c'è più`() {
        vm.genera()
        aspetta { vm.stato?.esito is Esito.Proposta }

        runBlocking { db.capoDao().eliminaConPreferiti(capi[0].id) }
        aspetta { vm.stato == null }
        vm.salva("vecchia")
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(runBlocking { db.preferitoDao().tutti().first() }.isEmpty())
    }

    @Test
    fun `cambiare un Capo che non è nella Proposta la lascia com'è`() {
        vm.genera()
        aspetta { vm.stato?.esito is Esito.Proposta }
        val prima = vm.stato

        runBlocking { db.capoDao().salva(capi[3].copy(colorePrincipale = "nero")) } // l'Accessorio: Genera non lo usa mai
        repeat(20) { shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10) }

        assertTrue(vm.stato?.esito === prima?.esito)
    }

    @Test
    fun `eliminare il Capo di partenza torna a Genera, cambiarne un altro rigenera attorno allo stesso Capo`() {
        vm.partiDa(capi[0].id)
        aspetta { vm.stato?.esito is Esito.Proposta }

        runBlocking { db.capoDao().salva(capi[1].copy(colorePrincipale = "beige")) }
        aspetta { (vm.stato?.esito as? Esito.Proposta)?.spiegazione?.contains("beige") == true }
        assertEquals(capi[0].id, vm.partenza)

        runBlocking { db.capoDao().eliminaConPreferiti(capi[0].id) }
        aspetta { vm.partenza == null }
        assertNull(vm.stato)
    }

    @Test
    fun `dopo Esaurite, cambiare un Capo già visto ridà una Proposta`() {
        vm.genera()
        aspetta { vm.stato?.esito is Esito.Proposta }
        vm.genera()
        aspetta { vm.stato?.esito == Esito.Esaurite }

        runBlocking { db.capoDao().salva(capi[1].copy(colorePrincipale = "beige")) }
        repeat(20) { shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10) }
        vm.genera()

        aspetta { (vm.stato?.esito as? Esito.Proposta)?.spiegazione?.contains("beige") == true }
    }

    @Test
    fun `tornare a Genera, come dopo un Importa, azzera Partenza, Proposta e già viste`() {
        vm.partiDa(capi[0].id)
        aspetta { vm.stato?.esito is Esito.Proposta }
        vm.genera()
        aspetta { vm.stato?.esito == Esito.Esaurite }

        vm.partiDa(null)
        assertNull(vm.partenza)
        assertNull(vm.stato)
        vm.genera()

        aspetta { vm.stato?.esito is Esito.Proposta }
    }

    @Test
    fun `cambiare foto, Tipo o Taglia di un Capo della Proposta la lascia valida, con la foto nuova`() {
        vm.genera()
        aspetta { vm.stato?.esito is Esito.Proposta }
        val prima = vm.stato!!.esito

        runBlocking { db.capoDao().salva(capi[0].copy(percorsoFoto = "nuova.webp", tipo = "polo", taglia = "M")) }

        aspetta { vm.stato?.capi?.get(capi[0].id)?.percorsoFoto == "nuova.webp" }
        assertEquals(prima, vm.stato!!.esito)
    }

    @Test
    fun `cambiare il Colore del Capo di partenza aggiorna i Colori consigliati anche se non c'è una Proposta`() {
        vm.partiDa(capi[0].id)
        aspetta { vm.stato?.esito is Esito.Proposta }
        vm.genera()
        aspetta { vm.stato?.esito == Esito.Esaurite }
        assertTrue(vm.stato!!.consigliati.any { it.colore.id == "rosso" })

        runBlocking { db.capoDao().salva(capi[0].copy(colorePrincipale = "rosso")) }

        aspetta { vm.stato?.consigliati?.none { it.colore.id == "rosso" } == true }
        assertTrue(vm.stato!!.consigliati.any { it.colore.id == "bianco" })
    }

    @Test
    fun `partire da un Capo fuori stagione, o cambiare filtro mentre ci si parte, avvisa una volta sola`() {
        val maglione = Capo(categoria = Categoria.SOPRA, tipo = "maglione", colorePrincipale = "bianco", stagione = Stagione.INVERNALE)
        val id = runBlocking { db.capoDao().salva(maglione) }
        vm.scegliStagione(Stagione.ESTIVO)

        vm.partiDa(capi[0].id) // tutto l'anno: mai fuori stagione
        aspetta { vm.stato?.esito is Esito.Proposta }
        assertNull(vm.avvisoFuoriStagione)

        vm.partiDa(id)
        aspetta { vm.avvisoFuoriStagione == FuoriStagione(Stagione.INVERNALE, Stagione.ESTIVO) && vm.stato?.esito is Esito.Proposta }
        vm.chiudiAvviso()
        vm.genera() // "Un altro"
        aspetta { vm.stato?.esito == Esito.Esaurite }
        assertNull(vm.avvisoFuoriStagione)

        vm.scegliStagione(Stagione.MEZZA_STAGIONE)
        aspetta { vm.avvisoFuoriStagione == FuoriStagione(Stagione.INVERNALE, Stagione.MEZZA_STAGIONE) }
        vm.chiudiAvviso()
        vm.scegliStagione(Stagione.INVERNALE)
        repeat(20) { shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(10) }
        assertNull(vm.avvisoFuoriStagione)

        vm.scegliStagione(Stagione.ESTIVO)
        aspetta { vm.avvisoFuoriStagione != null }
        vm.partiDa(null) // "Genera a caso", Importa o Capo eliminato: l'avviso non vale più
        assertNull(vm.avvisoFuoriStagione)
    }

    /** Un secondo Sopra neutro: con lui Genera ha due Proposte. */
    private fun dueProposte() = runBlocking {
        db.capoDao().salva(Capo(categoria = Categoria.SOPRA, tipo = "maglia", colorePrincipale = "grigio_chiaro"))
    }

    @Test
    fun `Precedente torna alla Proposta di prima, anche da Esaurite, e poi non c'è altro`() {
        dueProposte()
        vm.genera()
        aspetta { vm.stato?.esito is Esito.Proposta }
        val prima = vm.stato?.esito
        assertTrue(!vm.cèPrecedente)
        vm.genera()
        aspetta { vm.stato?.esito != prima }
        vm.genera()
        aspetta { vm.stato?.esito == Esito.Esaurite }

        vm.precedente()
        assertTrue(vm.stato?.esito != prima && vm.stato?.esito is Esito.Proposta)
        vm.precedente()
        assertTrue(vm.stato?.esito === prima)
        assertTrue(!vm.cèPrecedente)
    }

    @Test
    fun `una Proposta precedente con un Capo cambiato esce dalla cronologia, e tornare a Genera la svuota`() {
        dueProposte()
        vm.genera()
        aspetta { vm.stato?.esito is Esito.Proposta }
        val prima = vm.stato?.esito as Esito.Proposta
        vm.genera()
        aspetta { vm.stato?.esito != prima }
        assertTrue(vm.cèPrecedente)

        val sotto = capi.first { c -> prima.outfit.capi.any { it.id == c.id } && c.categoria == Categoria.SOTTO }
        runBlocking { db.capoDao().salva(sotto.copy(colorePrincipale = "nero")) } // il Sotto è in tutte e due: rigenera anche la mostrata
        aspetta { !vm.cèPrecedente }

        vm.genera()
        aspetta { vm.stato?.esito is Esito.Proposta }
        vm.genera()
        aspetta { vm.cèPrecedente }
        vm.partiDa(null)
        assertTrue(!vm.cèPrecedente)
    }
}
