package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Test

class ColoriConsigliatiTest {
    private val neutri = PALETTE.filter { it.neutro }.map { it.nome }

    private fun consigliatiPer(id: String) =
        coloriConsigliati(Capo(1, Categoria.SOPRA, colore(id))).map { it.colore.nome to it.regola }

    @Test
    fun `per un Colore non neutro i consigliati sono i Neutri e i Colori in Armonia, in ordine di Palette`() {
        assertEquals(
            neutri.map { it to "un colore + neutri" } + listOf(
                "bordeaux" to "monocromatico",
                "rosa" to "monocromatico",
                "corallo" to "analoghi",
                "arancione" to "analoghi",
                "verde bosco" to "complementari",
                "verde salvia" to "complementari",
            ),
            consigliatiPer("rosso"),
        )
    }

    @Test
    fun `per un Colore Neutro sono consigliati tutti gli altri Colori della Palette`() {
        assertEquals(
            (neutri - "blu navy").map { it to "neutri" } +
                PALETTE.filterNot { it.neutro }.map { it.nome to "un colore + neutri" },
            consigliatiPer("blu_navy"),
        )
    }
}
