package com.guardaroba.ui

import com.guardaroba.motore.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Outfit
import com.guardaroba.motore.colore
import com.guardaroba.motore.perché
import org.junit.Assert.assertEquals
import org.junit.Test

class RegoleTest {
    /** L'esempio di ogni regola, vestito come Sopra, Sotto e Scarpe, è valido proprio per quella regola. */
    @Test fun `gli esempi della pagina delle regole sono giudicati dal Motore con la loro regola`() {
        val categorie = listOf(Categoria.SOPRA, Categoria.SOTTO, Categoria.SCARPE)
        REGOLE.forEach { regola ->
            val outfit = Outfit(regola.esempio.mapIndexed { i, id -> Capo(i.toLong(), categorie[i], colore(id)) })
            assertEquals(regola.esempio.toString(), regola.nome, perché(outfit)?.regola)
        }
    }
}
