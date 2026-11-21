package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

/** "Parti da questo capo": Richiesta.PartiDa, un test per ogni Categoria di partenza. */
class PartiDaTest {
    private var prossimoId = 1L
    private fun capo(
        categoria: Categoria,
        colorePrincipale: String,
        motivo: Motivo = Motivo.TINTA_UNITA,
        stagione: Stagione = Stagione.TUTTO_L_ANNO,
    ) = Capo(prossimoId++, categoria, colore(colorePrincipale), motivo, stagione = stagione)

    private fun partiDa(guardaroba: List<Capo>, capo: Capo, giàViste: Set<Set<Long>> = emptySet(), filtroStagione: Stagione? = null) =
        proponi(guardaroba, Richiesta.PartiDa(capo, filtroStagione), giàViste, Random(42))

    /** Tutte le Proposte che partono da [capo], chiedendo "un altro" fino a Esaurite. */
    private fun tutte(guardaroba: List<Capo>, capo: Capo, filtroStagione: Stagione? = null): Set<Set<Long>> {
        val giàViste = mutableSetOf<Set<Long>>()
        while (true) {
            val esito = partiDa(guardaroba, capo, giàViste, filtroStagione) as? Esito.Proposta ?: return giàViste
            giàViste += esito.outfit.idCapi
        }
    }

    @Test
    fun `partendo da un Sopra ogni Proposta ha quel Sopra e mai un Intero`() {
        val sopra = capo(Categoria.SOPRA, "bianco")
        val altroSopra = capo(Categoria.SOPRA, "nero")
        val jeans = capo(Categoria.SOTTO, "denim")
        val pantaloni = capo(Categoria.SOTTO, "beige")
        val vestito = capo(Categoria.INTERO, "nero")
        val scarpe = capo(Categoria.SCARPE, "nero")

        assertEquals(
            setOf(setOf(sopra.id, jeans.id, scarpe.id), setOf(sopra.id, pantaloni.id, scarpe.id)),
            tutte(listOf(sopra, altroSopra, jeans, pantaloni, vestito, scarpe), sopra),
        )
    }

    @Test
    fun `partendo da un Sotto ogni Proposta ha quel Sotto e mai un Intero`() {
        val camicia = capo(Categoria.SOPRA, "bianco")
        val polo = capo(Categoria.SOPRA, "blu_navy")
        val sotto = capo(Categoria.SOTTO, "beige")
        val altroSotto = capo(Categoria.SOTTO, "denim")
        val vestito = capo(Categoria.INTERO, "nero")
        val scarpe = capo(Categoria.SCARPE, "nero")

        assertEquals(
            setOf(setOf(camicia.id, sotto.id, scarpe.id), setOf(polo.id, sotto.id, scarpe.id)),
            tutte(listOf(camicia, polo, sotto, altroSotto, vestito, scarpe), sotto),
        )
    }

    @Test
    fun `partendo da un Intero le Proposte sono Intero più Scarpe, senza Sopra né Sotto`() {
        val vestito = capo(Categoria.INTERO, "rosso")
        val altroVestito = capo(Categoria.INTERO, "nero")
        val sneakers = capo(Categoria.SCARPE, "bianco")
        val stivali = capo(Categoria.SCARPE, "nero")
        val guardaroba = listOf(vestito, altroVestito, capo(Categoria.SOPRA, "bianco"), capo(Categoria.SOTTO, "denim"), sneakers, stivali)

        assertEquals(setOf(setOf(vestito.id, sneakers.id), setOf(vestito.id, stivali.id)), tutte(guardaroba, vestito))
    }

    @Test
    fun `partendo dalle Scarpe ogni Proposta ha quelle Scarpe, con Sopra e Sotto o con un Intero`() {
        val sopra = capo(Categoria.SOPRA, "bianco")
        val sotto = capo(Categoria.SOTTO, "denim")
        val vestito = capo(Categoria.INTERO, "nero")
        val scarpe = capo(Categoria.SCARPE, "marrone")
        val altreScarpe = capo(Categoria.SCARPE, "nero")

        assertEquals(
            setOf(setOf(sopra.id, sotto.id, scarpe.id), setOf(vestito.id, scarpe.id)),
            tutte(listOf(sopra, sotto, vestito, scarpe, altreScarpe), scarpe),
        )
    }

    @Test
    fun `partendo da un Capospalla è in ogni Proposta, con qualsiasi filtro e qualsiasi sua Stagione`() {
        val sopra = capo(Categoria.SOPRA, "bianco")
        val sotto = capo(Categoria.SOTTO, "denim")
        val scarpe = capo(Categoria.SCARPE, "nero")
        val cappotto = capo(Categoria.CAPOSPALLA, "cammello", stagione = Stagione.INVERNALE)
        val guardaroba = listOf(sopra, sotto, scarpe, cappotto, capo(Categoria.CAPOSPALLA, "nero"))
        val atteso = setOf(setOf(sopra.id, sotto.id, cappotto.id, scarpe.id))

        assertEquals(atteso, tutte(guardaroba, cappotto))
        assertEquals(atteso, tutte(guardaroba, cappotto, Stagione.ESTIVO))
        assertEquals(atteso, tutte(guardaroba, cappotto, Stagione.INVERNALE))
    }

    @Test
    fun `partendo da un Accessorio è in ogni Proposta e il suo colore non limita né compare nella Spiegazione`() {
        val sopra = capo(Categoria.SOPRA, "rosso")
        val sotto = capo(Categoria.SOTTO, "denim")
        val scarpe = capo(Categoria.SCARPE, "bianco")
        val borsa = capo(Categoria.ACCESSORI, "giallo", Motivo.FANTASIA) // rosso + giallo non armonizzano
        val camicia = capo(Categoria.SOPRA, "bianco", Motivo.RIGHE)

        assertEquals(
            Esito.Proposta(Outfit(listOf(sopra, sotto, scarpe, borsa)), "rosso + denim + bianco: un colore + neutri"),
            partiDa(listOf(sopra, sotto, scarpe, borsa), borsa),
        )
        assertEquals(
            setOf(setOf(sopra.id, sotto.id, scarpe.id, borsa.id), setOf(camicia.id, sotto.id, scarpe.id, borsa.id)),
            tutte(listOf(sopra, camicia, sotto, scarpe, borsa), borsa),
        )
        val sciarpa = capo(Categoria.ACCESSORI, "nero", stagione = Stagione.INVERNALE)
        assertEquals(
            setOf(setOf(sopra.id, sotto.id, scarpe.id, sciarpa.id)),
            tutte(listOf(sopra, sotto, scarpe, sciarpa), sciarpa, Stagione.ESTIVO),
        )
    }

    @Test
    fun `il filtro Stagione vale per gli altri Capi, non per il Capo di partenza`() {
        val maglione = capo(Categoria.SOPRA, "grigio_chiaro", stagione = Stagione.INVERNALE)
        val shorts = capo(Categoria.SOTTO, "beige", stagione = Stagione.ESTIVO)
        val pantaloni = capo(Categoria.SOTTO, "blu_navy", stagione = Stagione.INVERNALE)
        val scarpe = capo(Categoria.SCARPE, "nero")

        assertEquals(
            setOf(setOf(maglione.id, shorts.id, scarpe.id)),
            tutte(listOf(maglione, shorts, pantaloni, scarpe), maglione, Stagione.ESTIVO),
        )
    }

    @Test
    fun `con filtro invernale si aggiunge un Capospalla compatibile come in Genera`() {
        val sopra = capo(Categoria.SOPRA, "bianco")
        val sotto = capo(Categoria.SOTTO, "denim")
        val scarpe = capo(Categoria.SCARPE, "nero")
        val giacca = capo(Categoria.CAPOSPALLA, "beige")

        assertEquals(setOf(setOf(sopra.id, sotto.id, giacca.id, scarpe.id)), tutte(listOf(sopra, sotto, scarpe, giacca), sopra, Stagione.INVERNALE))
    }

    @Test
    fun `se il Capo di partenza non si abbina a nulla l'Esito è NessunAbbinamento, anche se altri Outfit sono validi`() {
        val sopra = capo(Categoria.SOPRA, "rosso")
        val guardaroba = listOf(sopra, capo(Categoria.SOPRA, "bianco"), capo(Categoria.SOTTO, "giallo"), capo(Categoria.SCARPE, "nero"))

        assertEquals(Esito.NessunAbbinamento, partiDa(guardaroba, sopra))
    }

    @Test
    fun `partendo da un Sopra l'Intero non sostituisce il Sotto mancante`() {
        val sopra = capo(Categoria.SOPRA, "bianco")

        assertEquals(
            Esito.MancaCategoria(listOf(Categoria.SOTTO)),
            partiDa(listOf(sopra, capo(Categoria.INTERO, "nero"), capo(Categoria.SCARPE, "nero")), sopra),
        )
    }

    @Test
    fun `viste tutte le Proposte con il Capo di partenza l'Esito è Esaurite`() {
        val sopra = capo(Categoria.SOPRA, "bianco")
        val guardaroba = listOf(sopra, capo(Categoria.SOPRA, "nero"), capo(Categoria.SOTTO, "denim"), capo(Categoria.SCARPE, "nero"))

        assertEquals(Esito.Esaurite, partiDa(guardaroba, sopra, tutte(guardaroba, sopra)))
    }
}
