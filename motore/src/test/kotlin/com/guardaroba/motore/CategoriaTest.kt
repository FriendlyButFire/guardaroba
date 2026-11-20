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
}
