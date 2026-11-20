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
            capoDao().inserisci(capo)
            close()
        }

        val riaperto = GuardarobaDatabase.apri(context)
        val capi = riaperto.capoDao().tutti().first()
        riaperto.close()

        assertEquals(listOf(capo.copy(id = 1)), capi)
    }
}
