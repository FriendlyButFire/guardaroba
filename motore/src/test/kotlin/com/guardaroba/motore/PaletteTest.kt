package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Test

class PaletteTest {
    @Test
    fun `la Palette ha 25 Colori con id unici`() {
        assertEquals(25, PALETTE.size)
        assertEquals(25, PALETTE.map { it.id }.toSet().size)
    }

    @Test
    fun `i Neutri sono i 10 della spec`() {
        assertEquals(
            listOf(
                "nero", "bianco", "panna", "grigio chiaro", "grigio antracite",
                "beige", "cammello", "marrone", "blu navy", "denim",
            ),
            PALETTE.filter { it.neutro }.map { it.nome },
        )
    }

    @Test
    fun `colore trova un Colore per id`() {
        val navy = colore("blu_navy")
        assertEquals("blu navy", navy.nome)
        assertEquals(Triple(220, 60, 20), Triple(navy.h, navy.s, navy.l))
    }
}
