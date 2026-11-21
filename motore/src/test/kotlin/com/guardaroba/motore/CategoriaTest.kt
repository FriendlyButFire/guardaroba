package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoriaTest {
    @Test
    fun `le Categorie sono le sei della spec, in ordine`() {
        assertEquals(
            listOf("Sopra", "Sotto", "Intero", "Capospalla", "Scarpe", "Accessori"),
            Categoria.entries.map { it.nome },
        )
    }

    @Test
    fun `ogni Categoria ha i Tipi della spec`() {
        assertEquals(listOf("t-shirt", "camicia", "polo", "maglione", "felpa", "top"), Categoria.SOPRA.tipi)
        assertEquals(listOf("pantaloni", "jeans", "shorts", "gonna"), Categoria.SOTTO.tipi)
        assertEquals(listOf("vestito", "tuta"), Categoria.INTERO.tipi)
        assertEquals(listOf("giacca", "cappotto", "giubbotto", "blazer"), Categoria.CAPOSPALLA.tipi)
        assertEquals(listOf("sneakers", "eleganti", "stivali", "sandali"), Categoria.SCARPE.tipi)
        assertEquals(listOf("cintura", "borsa", "cappello", "sciarpa"), Categoria.ACCESSORI.tipi)
    }

    @Test
    fun `Sopra e Sotto escludono l'Intero, e l'Intero esclude Sopra e Sotto`() {
        assertEquals(setOf(Categoria.INTERO), Categoria.SOPRA.escluse)
        assertEquals(setOf(Categoria.INTERO), Categoria.SOTTO.escluse)
        assertEquals(setOf(Categoria.SOPRA, Categoria.SOTTO), Categoria.INTERO.escluse)
        assertEquals(emptySet<Categoria>(), Categoria.SCARPE.escluse)
    }
}
