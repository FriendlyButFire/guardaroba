package com.guardaroba.archivio

import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferitoConCapiTest {
    private val tshirt = Capo(1, Categoria.SOPRA, "t-shirt", "rosso")
    private val gonna = Capo(2, Categoria.SOTTO, "gonna", "nero")
    private val sandali = Capo(3, Categoria.SCARPE, "sandali", "nero")
    private fun preferito(vararg capi: Capo) = PreferitoConCapi(Preferito(1, spiegazione = "rosso + nero: un colore + neutri"), capi.toList())

    @Test
    fun `il Capo responsabile è l'unico con un Colore che al salvataggio non c'era`() {
        val gialla = gonna.copy(colorePrincipale = "giallo")

        assertEquals(gialla, preferito(tshirt, gialla, sandali).responsabile())
    }

    @Test
    fun `senza un solo responsabile certo non si indica nessun Capo`() {
        assertEquals(null, preferito(tshirt, gonna, sandali).responsabile()) // ancora valido
        assertEquals(null, preferito(tshirt.copy(colorePrincipale = "verde_bosco"), gonna.copy(colorePrincipale = "giallo"), sandali).responsabile())
        assertEquals(null, preferito(tshirt, gonna.copy(motivo = Motivo.QUADRI), sandali.copy(motivo = Motivo.RIGHE)).responsabile())
        assertEquals(null, preferito(tshirt, gonna.copy(categoria = Categoria.SOPRA), sandali).responsabile())
    }
}
