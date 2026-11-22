package com.guardaroba.ui

import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.Preferito
import com.guardaroba.archivio.PreferitoConCapi
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Stagione
import org.junit.Assert.assertEquals
import org.junit.Test

class PerStagioneTest {
    private val camiciaEstiva = Capo(1, Categoria.SOPRA, "camicia", "bianco", stagione = Stagione.ESTIVO)
    private val jeans = Capo(2, Categoria.SOTTO, "jeans", "denim") // tutto l'anno
    private val cappottoInvernale = Capo(3, Categoria.CAPOSPALLA, "cappotto", "cammello", stagione = Stagione.INVERNALE)
    private fun preferito(id: Long, vararg capi: Capo) = PreferitoConCapi(Preferito(id, spiegazione = ""), capi.toList())

    private val estivo = preferito(1, camiciaEstiva, jeans)
    private val invernale = preferito(2, jeans, cappottoInvernale)
    private val tuttoLAnno = preferito(3, jeans)
    private val misto = preferito(4, camiciaEstiva, cappottoInvernale)
    private val preferiti = listOf(estivo, invernale, tuttoLAnno, misto)

    private fun ids(filtro: Stagione?) = preferiti.perStagione(filtro).map { it.preferito.id }

    @Test
    fun `senza Stagione restano tutti i Preferiti`() {
        assertEquals(listOf(1L, 2L, 3L, 4L), ids(null))
    }

    @Test
    fun `con una Stagione restano i Preferiti i cui Capi la ammettono tutti`() {
        assertEquals(listOf(1L, 3L), ids(Stagione.ESTIVO))
        assertEquals(listOf(2L, 3L), ids(Stagione.INVERNALE))
        assertEquals(listOf(3L), ids(Stagione.MEZZA_STAGIONE))
    }
}
