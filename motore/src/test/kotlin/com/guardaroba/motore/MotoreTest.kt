package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MotoreTest {
    private var prossimoId = 1L
    private fun capo(categoria: Categoria, colorePrincipale: String) =
        Capo(prossimoId++, categoria, colore(colorePrincipale))

    private fun genera(guardaroba: List<Capo>, giàViste: Set<Set<Long>> = emptySet()) =
        proponi(guardaroba, Richiesta.Genera, giàViste, Random(42))

    @Test
    fun `un Outfit di soli Neutri è una Proposta con Spiegazione neutri`() {
        val sopra = capo(Categoria.SOPRA, "blu_navy")
        val sotto = capo(Categoria.SOTTO, "beige")
        val scarpe = capo(Categoria.SCARPE, "beige")

        val esito = genera(listOf(sopra, sotto, scarpe))

        assertEquals(Esito.Proposta(Outfit(listOf(sopra, sotto, scarpe)), "blu navy + beige: neutri"), esito)
    }

    @Test
    fun `Neutri più un colore non neutro è una Proposta con Spiegazione un colore + neutri`() {
        val sopra = capo(Categoria.SOPRA, "rosso")
        val sotto = capo(Categoria.SOTTO, "denim")
        val scarpe = capo(Categoria.SCARPE, "bianco")

        val esito = genera(listOf(sopra, sotto, scarpe))

        assertEquals(
            Esito.Proposta(Outfit(listOf(sopra, sotto, scarpe)), "rosso + denim + bianco: un colore + neutri"),
            esito,
        )
    }

    @Test
    fun `due colori non neutri non sono validi, quindi si propone l'unico Outfit valido`() {
        val sopraRosso = capo(Categoria.SOPRA, "rosso")
        val sopraBeige = capo(Categoria.SOPRA, "beige")
        val sottoVerde = capo(Categoria.SOTTO, "verde_bosco")
        val scarpe = capo(Categoria.SCARPE, "nero")
        val guardaroba = listOf(sopraRosso, sopraBeige, sottoVerde, scarpe)

        repeat(10) { seed ->
            val esito = proponi(guardaroba, Richiesta.Genera, emptySet(), Random(seed))
            assertEquals(Outfit(listOf(sopraBeige, sottoVerde, scarpe)), (esito as Esito.Proposta).outfit)
        }
    }

    @Test
    fun `senza Outfit validi l'Esito è NessunAbbinamento`() {
        val guardaroba = listOf(
            capo(Categoria.SOPRA, "rosso"),
            capo(Categoria.SOTTO, "giallo"),
            capo(Categoria.SCARPE, "nero"),
        )

        assertEquals(Esito.NessunAbbinamento, genera(guardaroba))
    }

    @Test
    fun `lo stesso colore non neutro su due Capi conta come un colore`() {
        val guardaroba = listOf(
            capo(Categoria.SOPRA, "rosso"),
            capo(Categoria.SOTTO, "rosso"),
            capo(Categoria.SCARPE, "nero"),
        )

        assertEquals("rosso + nero: un colore + neutri", (genera(guardaroba) as Esito.Proposta).spiegazione)
    }

    @Test
    fun `con il Guardaroba vuoto mancano Sopra, Sotto e Scarpe`() {
        assertEquals(
            Esito.MancaCategoria(listOf(Categoria.SOPRA, Categoria.SOTTO, Categoria.SCARPE)),
            genera(emptyList()),
        )
    }

    @Test
    fun `MancaCategoria elenca solo le Categorie necessarie assenti`() {
        val guardaroba = listOf(capo(Categoria.SOPRA, "nero"), capo(Categoria.ACCESSORI, "nero"))

        assertEquals(Esito.MancaCategoria(listOf(Categoria.SOTTO, Categoria.SCARPE)), genera(guardaroba))
    }

    @Test
    fun `Genera non aggiunge mai Accessori né Capospalla, e i loro colori non limitano`() {
        val sopra = capo(Categoria.SOPRA, "rosso")
        val sotto = capo(Categoria.SOTTO, "denim")
        val scarpe = capo(Categoria.SCARPE, "bianco")
        val guardaroba = listOf(
            sopra, sotto, scarpe,
            capo(Categoria.ACCESSORI, "giallo"),
            capo(Categoria.CAPOSPALLA, "nero"),
        )

        assertEquals(Outfit(listOf(sopra, sotto, scarpe)), (genera(guardaroba) as Esito.Proposta).outfit)
    }

    @Test
    fun `stesso seed, stessa Proposta`() {
        val guardaroba = listOf(
            capo(Categoria.SOPRA, "nero"), capo(Categoria.SOPRA, "bianco"), capo(Categoria.SOPRA, "beige"),
            capo(Categoria.SOTTO, "denim"), capo(Categoria.SOTTO, "grigio_chiaro"),
            capo(Categoria.SCARPE, "nero"), capo(Categoria.SCARPE, "marrone"),
        )

        assertEquals(genera(guardaroba), genera(guardaroba))
    }

    @Test
    fun `un altro non ripete le Proposte già viste e poi l'Esito è Esaurite`() {
        val guardaroba = listOf(
            capo(Categoria.SOPRA, "nero"), capo(Categoria.SOPRA, "bianco"),
            capo(Categoria.SOTTO, "denim"),
            capo(Categoria.SCARPE, "nero"),
        )
        val giàViste = mutableSetOf<Set<Long>>()

        repeat(2) {
            val proposta = genera(guardaroba, giàViste) as Esito.Proposta
            assertTrue(giàViste.add(proposta.outfit.id))
        }
        assertEquals(Esito.Esaurite, genera(guardaroba, giàViste))
    }
}
