package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Test

class GiudicaTest {
    private var prossimoId = 1L
    private fun capo(categoria: Categoria, colorePrincipale: String, motivo: Motivo = Motivo.TINTA_UNITA) =
        Capo(prossimoId++, categoria, colore(colorePrincipale), motivo)

    @Test
    fun `un Outfit valido ha la stessa Spiegazione di una Proposta, in qualunque ordine siano i Capi`() {
        val outfit = Outfit(listOf(capo(Categoria.SCARPE, "nero"), capo(Categoria.SOTTO, "arancione"), capo(Categoria.SOPRA, "blu_royal")))

        assertEquals(Giudizio.Valido("blu royal + arancione + nero: complementari"), giudica(outfit))
    }

    @Test
    fun `due colori non armonici non sono validi e il motivo li nomina`() {
        val outfit = Outfit(listOf(capo(Categoria.SOPRA, "rosso"), capo(Categoria.SOTTO, "giallo"), capo(Categoria.SCARPE, "nero")))

        assertEquals(Giudizio.NonValido("rosso + giallo + nero non si abbinano"), giudica(outfit))
    }

    @Test
    fun `due Capi con un Motivo non sono validi, ma un Accessorio con un Motivo non conta`() {
        val sopra = capo(Categoria.SOPRA, "bianco", Motivo.RIGHE)
        val sotto = capo(Categoria.SOTTO, "denim")
        val scarpe = capo(Categoria.SCARPE, "nero")

        assertEquals(
            Giudizio.NonValido("più di un Capo non in tinta unita"),
            giudica(Outfit(listOf(sopra, sotto.copy(motivo = Motivo.QUADRI), scarpe))),
        )
        assertEquals(
            Giudizio.Valido("bianco + denim + nero: neutri"),
            giudica(Outfit(listOf(sopra, sotto, scarpe, capo(Categoria.ACCESSORI, "rosso", Motivo.FANTASIA)))),
        )
    }

    @Test
    fun `Capospalla e Accessori sono opzionali, e gli Accessori possono essere più di uno`() {
        val outfit = Outfit(
            listOf(
                capo(Categoria.INTERO, "nero"), capo(Categoria.CAPOSPALLA, "cammello"), capo(Categoria.SCARPE, "nero"),
                capo(Categoria.ACCESSORI, "rosso"), capo(Categoria.ACCESSORI, "giallo"),
            ),
        )

        assertEquals(Giudizio.Valido("nero + cammello: neutri"), giudica(outfit))
    }

    @Test
    fun `un Capo passato a un'altra Categoria può lasciare l'Outfit incompleto, doppio o con Categorie escluse`() {
        val sopra = capo(Categoria.SOPRA, "bianco")
        val sotto = capo(Categoria.SOTTO, "denim")
        val scarpe = capo(Categoria.SCARPE, "nero")

        assertEquals(Giudizio.NonValido("manca Sotto"), giudica(Outfit(listOf(sopra, sotto.copy(categoria = Categoria.CAPOSPALLA), scarpe))))
        assertEquals(Giudizio.NonValido("più di un Capo Sopra"), giudica(Outfit(listOf(sopra, sotto.copy(categoria = Categoria.SOPRA), scarpe))))
        assertEquals(Giudizio.NonValido("Sotto e Intero insieme"), giudica(Outfit(listOf(sopra.copy(categoria = Categoria.INTERO), sotto, scarpe))))
        assertEquals(Giudizio.NonValido("manca Scarpe"), giudica(Outfit(listOf(sopra, sotto, scarpe.copy(categoria = Categoria.ACCESSORI)))))
    }
}
