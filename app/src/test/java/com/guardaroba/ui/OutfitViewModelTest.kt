package com.guardaroba.ui

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.GuardarobaDatabase
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Esito
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

        assertTrue(vm.stato === prima)
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
}
