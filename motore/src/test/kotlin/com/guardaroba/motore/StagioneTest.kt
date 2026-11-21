package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StagioneTest {
    @Test
    fun `il filtro Stagione ammette la sua Stagione e tutto l'anno`() {
        assertEquals(
            listOf(Stagione.INVERNALE, Stagione.TUTTO_L_ANNO),
            Stagione.entries.filter(Stagione.INVERNALE::ammette),
        )
    }

    @Test
    fun `un Capo è fuori stagione solo se il filtro c'è e non lo ammette`() {
        assertTrue(fuoriStagione(Stagione.INVERNALE, Stagione.ESTIVO))
        assertTrue(fuoriStagione(Stagione.MEZZA_STAGIONE, Stagione.INVERNALE))
        assertFalse(fuoriStagione(Stagione.ESTIVO, Stagione.ESTIVO))
        assertFalse(fuoriStagione(Stagione.TUTTO_L_ANNO, Stagione.ESTIVO))
        assertFalse(fuoriStagione(Stagione.INVERNALE, null))
    }
}
