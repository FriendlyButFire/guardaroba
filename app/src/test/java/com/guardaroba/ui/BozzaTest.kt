package com.guardaroba.ui

import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Stagione
import org.junit.Assert.assertEquals
import org.junit.Test

class BozzaTest {
    private val camicia = Capo(
        id = 7, categoria = Categoria.SOPRA, tipo = "camicia", colorePrincipale = "azzurro", coloreSecondario = "bianco",
        motivo = Motivo.RIGHE, stagione = Stagione.ESTIVO, taglia = "M", percorsoFoto = "camicia.jpg", creatoIl = 1_000,
    )

    @Test
    fun `Duplica riempie tutto tranne la Foto`() {
        assertEquals(
            Bozza(Categoria.SOPRA, "camicia", "azzurro", "bianco", Motivo.RIGHE, Stagione.ESTIVO, "M", percorsoFoto = null),
            camicia.duplicato(),
        )
    }

    @Test
    fun `il duplicato si salva come Capo nuovo`() {
        val nuovo = camicia.duplicato().capo(iniziale = null)!!
        assertEquals(0L, nuovo.id)
        assertEquals(null, nuovo.percorsoFoto)
        assertEquals(Triple("camicia", Motivo.RIGHE, "M"), Triple(nuovo.tipo, nuovo.motivo, nuovo.taglia))
    }

    @Test
    fun `salvare una modifica tiene id e data di creazione, e la Taglia vuota diventa null`() {
        val salvato = camicia.bozza().copy(colorePrincipale = "rosa", taglia = "  ").capo(camicia)!!
        assertEquals(camicia.copy(colorePrincipale = "rosa", taglia = null), salvato)
    }

    @Test
    fun `una Bozza incompleta non diventa un Capo`() {
        assertEquals(null, Bozza(Categoria.SOPRA, colorePrincipale = "rosa").capo(iniziale = null))
    }
}
