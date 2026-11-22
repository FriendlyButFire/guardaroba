package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SpiegazioneTest {
    private var prossimoId = 1L
    private fun capo(categoria: Categoria, colorePrincipale: String, motivo: Motivo = Motivo.TINTA_UNITA) =
        Capo(prossimoId++, categoria, colore(colorePrincipale), motivo)

    private fun outfit(sopra: String, sotto: String, scarpe: String) =
        Outfit(listOf(capo(Categoria.SOPRA, sopra), capo(Categoria.SOTTO, sotto), capo(Categoria.SCARPE, scarpe)))

    @Test
    fun `un colore con un Neutro dice che il Neutro lo fa risaltare`() {
        assertEquals(
            Perché("un colore + neutri", "Il nero è neutro: fa risaltare il rosso senza competere."),
            perché(outfit("rosso", "nero", "nero")),
        )
    }

    @Test
    fun `con più Neutri la frase va al plurale, nell'ordine dei Capi`() {
        assertEquals(
            "Il denim e il nero sono neutri: fanno risaltare il rosso senza competere.",
            perché(outfit("rosso", "denim", "nero"))?.frase,
        )
    }

    @Test
    fun `due colori complementari si fanno risaltare, e il Neutro li accompagna`() {
        assertEquals(
            Perché(
                "complementari",
                "Il blu royal e l'arancione sono opposti sulla ruota dei colori: si fanno risaltare a vicenda. " +
                    "Il nero, neutro, li accompagna senza competere.",
            ),
            perché(outfit("blu_royal", "arancione", "nero")),
        )
    }

    @Test
    fun `due colori analoghi sono vicini sulla ruota, senza Neutri non c'è seconda frase`() {
        assertEquals(
            Perché("analoghi", "Il rosso e l'arancione sono vicini sulla ruota dei colori: insieme sono armoniosi."),
            perché(outfit("rosso", "arancione", "rosso")),
        )
    }

    @Test
    fun `due colori monocromatici sono due toni della stessa tinta`() {
        val outfit = Outfit(outfit("rosso", "bordeaux", "nero").capi + capo(Categoria.CAPOSPALLA, "bianco"))

        assertEquals(
            Perché(
                "monocromatico",
                "Il rosso e il bordeaux sono due toni della stessa tinta: stanno insieme senza stonare. " +
                    "Il bianco e il nero, neutri, li accompagnano senza competere.",
            ),
            perché(outfit),
        )
    }

    @Test
    fun `solo Neutri, o un solo Colore, hanno la loro frase`() {
        assertEquals(
            Perché("neutri", "Il bianco, il denim e il nero sono neutri: stanno bene tra loro e con qualsiasi colore."),
            perché(outfit("bianco", "denim", "nero")),
        )
        assertEquals(Perché("neutri", "Tutto in nero: un neutro da solo non sbaglia mai."), perché(outfit("nero", "nero", "nero")))
        assertEquals(
            Perché("un colore + neutri", "Tutto in rosso: un colore solo non ha niente con cui stonare."),
            perché(outfit("rosso", "rosso", "rosso")),
        )
    }

    @Test
    fun `un Outfit non valido non ha un perché`() {
        assertEquals(null, perché(outfit("rosso", "giallo", "nero")))
    }

    @Test
    fun `ogni Outfit valido fatto con la Palette ha la sua frase`() {
        PALETTE.forEach { a ->
            PALETTE.forEach { b ->
                val o = outfit(a.id, b.id, "nero")
                if (giudica(o) is Giudizio.Valido) assertNotNull("${a.nome} + ${b.nome}", perché(o))
            }
        }
    }
}
