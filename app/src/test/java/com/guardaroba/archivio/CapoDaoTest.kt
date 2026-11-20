package com.guardaroba.archivio

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guardaroba.motore.Categoria
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CapoDaoTest {
    @Test
    fun `un Capo salvato si ritrova riaprendo il database`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        GuardarobaDatabase.apri(context).run {
            capoDao().inserisci(Capo(categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "blu_navy"))
            close()
        }

        val riaperto = GuardarobaDatabase.apri(context)
        val capi = riaperto.capoDao().tutti().first()
        riaperto.close()

        assertEquals(listOf(Triple(Categoria.SOPRA, "camicia", "blu_navy")), capi.map { Triple(it.categoria, it.tipo, it.colorePrincipale) })
    }
}
