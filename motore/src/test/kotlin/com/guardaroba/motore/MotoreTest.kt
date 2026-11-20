package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MotoreTest {
    private var prossimoId = 1L
    private fun capo(
        categoria: Categoria,
        colorePrincipale: String,
        motivo: Motivo = Motivo.TINTA_UNITA,
        coloreSecondario: String? = null,
    ) = Capo(prossimoId++, categoria, colore(colorePrincipale), motivo, coloreSecondario?.let(::colore))

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
    fun `due colori complementari sono una Proposta con Spiegazione complementari`() {
        val sopra = capo(Categoria.SOPRA, "blu_royal")
        val sotto = capo(Categoria.SOTTO, "arancione")
        val scarpe = capo(Categoria.SCARPE, "nero")

        val esito = genera(listOf(sopra, sotto, scarpe))

        assertEquals(
            Esito.Proposta(Outfit(listOf(sopra, sotto, scarpe)), "blu royal + arancione + nero: complementari"),
            esito,
        )
    }

    @Test
    fun `due colori non armonici non sono validi, quindi si propone l'unico Outfit valido`() {
        val sopraRosso = capo(Categoria.SOPRA, "rosso")
        val sopraBeige = capo(Categoria.SOPRA, "beige")
        val sottoGiallo = capo(Categoria.SOTTO, "giallo")
        val scarpe = capo(Categoria.SCARPE, "nero")
        val guardaroba = listOf(sopraRosso, sopraBeige, sottoGiallo, scarpe)

        repeat(10) { seed ->
            val esito = proponi(guardaroba, Richiesta.Genera, emptySet(), Random(seed))
            assertEquals(Outfit(listOf(sopraBeige, sottoGiallo, scarpe)), (esito as Esito.Proposta).outfit)
        }
    }

    @Test
    fun `la Spiegazione nomina ogni Armonia`() {
        fun spiegazione(sopra: String, sotto: String) =
            (genera(listOf(capo(Categoria.SOPRA, sopra), capo(Categoria.SOTTO, sotto), capo(Categoria.SCARPE, "bianco")))
                as Esito.Proposta).spiegazione

        assertEquals("azzurro + blu royal + bianco: monocromatico", spiegazione("azzurro", "blu_royal"))
        assertEquals("verde oliva + senape + bianco: analoghi", spiegazione("verde_oliva", "senape"))
        assertEquals("blu royal + arancione + bianco: complementari", spiegazione("blu_royal", "arancione"))
    }

    @Test
    fun `tre colori non neutri non sono mai validi, anche se a due a due armonizzano`() {
        val guardaroba = listOf(
            capo(Categoria.SOPRA, "rosso"),
            capo(Categoria.SOTTO, "bordeaux"),
            capo(Categoria.SCARPE, "rosa"),
        )

        assertEquals(Esito.NessunAbbinamento, genera(guardaroba))
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
    fun `Genera non aggiunge Accessori né Capospalla, e il colore di un Accessorio non limita`() {
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
            assertTrue(giàViste.add(proposta.outfit.idCapi))
        }
        assertEquals(Esito.Esaurite, genera(guardaroba, giàViste))
    }

    @Test
    fun `due Capi a righe non compaiono mai nella stessa Proposta`() {
        val sopraRighe = capo(Categoria.SOPRA, "bianco", Motivo.RIGHE)
        val sopraTinta = capo(Categoria.SOPRA, "nero")
        val sottoRighe = capo(Categoria.SOTTO, "blu_navy", Motivo.RIGHE)
        val sottoTinta = capo(Categoria.SOTTO, "beige")
        val scarpe = capo(Categoria.SCARPE, "nero")
        val guardaroba = listOf(sopraRighe, sopraTinta, sottoRighe, sottoTinta, scarpe)
        val giàViste = mutableSetOf<Set<Long>>()

        repeat(3) {
            val outfit = (genera(guardaroba, giàViste) as Esito.Proposta).outfit
            assertTrue(outfit.capi.count { it.motivo != Motivo.TINTA_UNITA } <= 1)
            giàViste += outfit.idCapi
        }
        assertEquals(Esito.Esaurite, genera(guardaroba, giàViste))
    }

    @Test
    fun `scarpe a quadri con un Sopra a fantasia non sono valide`() {
        val guardaroba = listOf(
            capo(Categoria.SOPRA, "nero", Motivo.FANTASIA),
            capo(Categoria.SOTTO, "denim"),
            capo(Categoria.SCARPE, "marrone", Motivo.QUADRI),
        )

        assertEquals(Esito.NessunAbbinamento, genera(guardaroba))
    }

    @Test
    fun `un Colore secondario non neutro non rende invalido un Outfit`() {
        val guardaroba = listOf(
            capo(Categoria.SOPRA, "rosso", coloreSecondario = "giallo"),
            capo(Categoria.SOTTO, "denim", coloreSecondario = "verde_oliva"),
            capo(Categoria.SCARPE, "bianco"),
        )

        assertEquals("rosso + denim + bianco: un colore + neutri", (genera(guardaroba) as Esito.Proposta).spiegazione)
    }

    @Test
    fun `con solo Interi e Scarpe si propone Intero + Scarpe, senza MancaCategoria per Sopra e Sotto`() {
        val intero = capo(Categoria.INTERO, "rosso")
        val scarpe = capo(Categoria.SCARPE, "nero")

        assertEquals(Esito.Proposta(Outfit(listOf(intero, scarpe)), "rosso + nero: un colore + neutri"), genera(listOf(intero, scarpe)))
    }

    @Test
    fun `con Sopra, Sotto e Intero si propongono entrambe le composizioni, mai Intero insieme a Sopra o Sotto`() {
        val sopra = capo(Categoria.SOPRA, "bianco")
        val sotto = capo(Categoria.SOTTO, "denim")
        val intero = capo(Categoria.INTERO, "nero")
        val scarpe = capo(Categoria.SCARPE, "nero")
        val guardaroba = listOf(sopra, sotto, intero, scarpe)
        val giàViste = mutableSetOf<Set<Long>>()

        repeat(2) { giàViste += (genera(guardaroba, giàViste) as Esito.Proposta).outfit.idCapi }

        assertEquals(setOf(setOf(sopra.id, sotto.id, scarpe.id), setOf(intero.id, scarpe.id)), giàViste)
        assertEquals(Esito.Esaurite, genera(guardaroba, giàViste))
    }

    @Test
    fun `con un Intero ma senza Scarpe manca solo Scarpe`() {
        assertEquals(Esito.MancaCategoria(listOf(Categoria.SCARPE)), genera(listOf(capo(Categoria.INTERO, "nero"))))
    }

    @Test
    fun `con un Intero e un Sopra senza Sotto si propone l'Intero`() {
        val intero = capo(Categoria.INTERO, "nero")
        val scarpe = capo(Categoria.SCARPE, "bianco")

        assertEquals(
            Outfit(listOf(intero, scarpe)),
            (genera(listOf(capo(Categoria.SOPRA, "bianco"), intero, scarpe)) as Esito.Proposta).outfit,
        )
    }

    @Test
    fun `il colore dell'Intero conta per l'Armonia`() {
        val intero = capo(Categoria.INTERO, "blu_royal")
        val scarpe = capo(Categoria.SCARPE, "arancione")

        assertEquals("blu royal + arancione: complementari", (genera(listOf(intero, scarpe)) as Esito.Proposta).spiegazione)
        assertEquals(Esito.NessunAbbinamento, genera(listOf(capo(Categoria.INTERO, "rosso"), capo(Categoria.SCARPE, "giallo"))))
    }

    @Test
    fun `un Intero a fantasia conta nel limite di un Capo a fantasia`() {
        val guardaroba = listOf(capo(Categoria.INTERO, "nero", Motivo.FANTASIA), capo(Categoria.SCARPE, "bianco", Motivo.RIGHE))

        assertEquals(Esito.NessunAbbinamento, genera(guardaroba))
    }
}
