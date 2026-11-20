package com.guardaroba.motore

import org.junit.Assert.assertEquals
import org.junit.Test

/** Coppie attese da build-notes/color-harmony.md (ADR-0004): 5 + 35 + 16 valide, le altre 49 rifiutate. */
class ArmoniaTest {
    private fun coppie(vararg nomi: String) = nomi.map { coppia ->
        val (a, b) = coppia.split(" + ")
        setOf(a, b)
    }

    private val monocromatiche = coppie(
        "rosso + bordeaux", "rosso + rosa", "bordeaux + rosa", "azzurro + blu royal", "viola + lilla",
    )
    private val analoghe = coppie(
        "rosso + corallo", "rosso + arancione", "bordeaux + corallo", "bordeaux + arancione", "bordeaux + viola",
        "bordeaux + lilla", "rosa + corallo", "rosa + arancione", "rosa + viola", "rosa + lilla",
        "corallo + arancione", "corallo + senape", "arancione + giallo", "arancione + senape", "giallo + senape",
        "giallo + verde oliva", "giallo + verde salvia", "senape + verde oliva", "senape + verde salvia",
        "verde oliva + verde bosco", "verde oliva + verde salvia", "verde bosco + verde salvia",
        "verde bosco + azzurro", "verde bosco + blu royal", "verde bosco + turchese", "verde salvia + azzurro",
        "verde salvia + turchese", "azzurro + turchese", "azzurro + viola", "azzurro + lilla",
        "blu royal + turchese", "blu royal + viola", "blu royal + lilla", "turchese + viola", "turchese + lilla",
    )
    private val complementari = coppie(
        "rosso + verde bosco", "rosso + verde salvia", "bordeaux + verde bosco", "bordeaux + verde salvia",
        "rosa + verde bosco", "rosa + verde salvia", "corallo + verde bosco", "corallo + azzurro",
        "corallo + turchese", "arancione + azzurro", "arancione + blu royal", "arancione + turchese",
        "giallo + viola", "giallo + lilla", "senape + viola", "senape + lilla",
    )

    private fun armoniaDi(nomeA: String, nomeB: String) =
        armonia(PALETTE.single { it.nome == nomeA }, PALETTE.single { it.nome == nomeB })

    @Test
    fun `ogni coppia di colori non neutri ha l'Armonia attesa, o nessuna`() {
        val colori = PALETTE.filterNot { it.neutro }
        val attese = monocromatiche.associateWith { "monocromatico" } +
            analoghe.associateWith { "analoghi" } +
            complementari.associateWith { "complementari" }
        var rifiutate = 0

        for ((i, a) in colori.withIndex()) for (b in colori.drop(i + 1)) {
            val attesa = attese[setOf(a.nome, b.nome)]
            if (attesa == null) rifiutate++
            assertEquals("${a.nome} + ${b.nome}", attesa, armonia(a, b))
            assertEquals("${b.nome} + ${a.nome}", attesa, armonia(b, a))
        }
        assertEquals(49, rifiutate)
    }

    @Test
    fun `i casi al confine delle soglie`() {
        assertEquals("analoghi", armoniaDi("azzurro", "turchese")) // 15°: oltre i 12° del monocromatico
        assertEquals("analoghi", armoniaDi("corallo", "senape")) // 70°: entro i 75°
        assertEquals("complementari", armoniaDi("corallo", "azzurro")) // 153,5°: entro 180° ± 30°
        assertEquals(null, armoniaDi("rosso", "turchese")) // 147,5°: appena fuori dai complementari
    }

    @Test
    fun `la RYB dà le coppie della moda, non quelle dell'HSL`() {
        assertEquals("complementari", armoniaDi("rosso", "verde bosco")) // HSL 135°
        assertEquals("complementari", armoniaDi("giallo", "viola")) // HSL 135°
        assertEquals(null, armoniaDi("rosso", "giallo"))
    }
}
