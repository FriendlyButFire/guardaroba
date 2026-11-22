package com.guardaroba.ui

import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Stagione
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltroTest {
    private val camiciaEstiva = Capo(1, Categoria.SOPRA, "camicia", "bianco", stagione = Stagione.ESTIVO)
    private val jeansTuttoLAnno = Capo(2, Categoria.SOTTO, "jeans", "denim", coloreSecondario = "bianco")
    private val cappottoInvernale = Capo(3, Categoria.CAPOSPALLA, "cappotto", "cammello", stagione = Stagione.INVERNALE)
    private val guardaroba = listOf(camiciaEstiva, jeansTuttoLAnno, cappottoInvernale)

    private fun ids(filtro: Filtro) = guardaroba.filter(filtro::ammette).map { it.id }

    @Test
    fun `senza filtri entrano tutti i Capi`() {
        assertEquals(listOf(1L, 2L, 3L), ids(Filtro()))
        assertFalse(Filtro().attivo)
    }

    @Test
    fun `filtro Categoria`() {
        assertEquals(listOf(2L), ids(Filtro(categoria = Categoria.SOTTO)))
    }

    @Test
    fun `filtro Colore guarda il principale e il secondario`() {
        assertEquals(listOf(1L, 2L), ids(Filtro(colore = "bianco")))
        assertEquals(listOf(3L), ids(Filtro(colore = "cammello")))
    }

    @Test
    fun `filtro Stagione include i Capi tutto l'anno`() {
        assertEquals(listOf(1L, 2L), ids(Filtro(stagione = Stagione.ESTIVO)))
        assertEquals(listOf(2L, 3L), ids(Filtro(stagione = Stagione.INVERNALE)))
        assertEquals(listOf(2L), ids(Filtro(stagione = Stagione.MEZZA_STAGIONE)))
        assertEquals(listOf(2L), ids(Filtro(stagione = Stagione.TUTTO_L_ANNO)))
    }

    @Test
    fun `i filtri si combinano`() {
        val filtro = Filtro(colore = "bianco", stagione = Stagione.INVERNALE)
        assertTrue(filtro.attivo)
        assertEquals(listOf(2L), ids(filtro))
        assertEquals(emptyList<Long>(), ids(filtro.copy(categoria = Categoria.SOPRA)))
    }

    @Test
    fun `le opzioni sono solo le Categorie e i Colori principali dei Capi, in ordine`() {
        assertEquals(listOf(Categoria.SOPRA, Categoria.SOTTO, Categoria.CAPOSPALLA), Filtro().categorie(guardaroba))
        // Nell'ordine della Palette; un Colore solo secondario non diventa un'opzione.
        val conSecondarioRosso = jeansTuttoLAnno.copy(id = 4, coloreSecondario = "rosso")
        assertEquals(listOf("bianco", "cammello", "denim"), Filtro().colori(guardaroba + conSecondarioRosso).map { it.id })
    }

    @Test
    fun `un filtro attivo resta tra le opzioni anche senza Capi, per poterlo togliere`() {
        val filtro = Filtro(categoria = Categoria.SCARPE, colore = "rosso")
        assertTrue(Categoria.SCARPE in filtro.categorie(guardaroba))
        assertTrue("rosso" in filtro.colori(guardaroba).map { it.id })
        assertEquals(listOf(Categoria.SCARPE), filtro.categorie(emptyList()))
    }
}
