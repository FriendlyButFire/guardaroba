package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Test

class StagioneTest {
    @Test
    fun `il filtro Stagione ammette la sua Stagione e tutto l'anno`() {
        assertEquals(
            listOf(Stagione.INVERNALE, Stagione.TUTTO_L_ANNO),
            Stagione.entries.filter(Stagione.INVERNALE::ammette),
        )
    }
}
