package com.guardaroba.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.guardaroba.motore.PALETTE
import org.junit.Assert.assertTrue
import org.junit.Test

class TestoSopraTest {
    private fun contrasto(a: Color, b: Color): Float {
        val (scura, chiara) = listOf(a.luminance(), b.luminance()).sorted()
        return (chiara + 0.05f) / (scura + 0.05f)
    }

    @Test
    fun `il testo su ogni Colore della Palette arriva a 4,5 a 1, ed è il migliore tra nero e bianco`() {
        PALETTE.forEach {
            val sfondo = it.campione()
            val migliore = maxOf(contrasto(Color.Black, sfondo), contrasto(Color.White, sfondo))
            val scelto = contrasto(it.testoSopra(), sfondo)
            assertTrue("${it.nome}: $scelto", scelto >= 4.5f && scelto == migliore)
        }
    }
}
