package com.guardaroba.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Perché
import com.guardaroba.motore.colore

// La composizione di un Outfit condivisa da Outfit e Preferiti (ADR-0012, ADR-0014).

/**
 * L'Outfit in piedi come un corpo: Sopra, Sotto (o Intero) e Scarpe in colonna; Capospalla e Accessori accanto, dall'altezza delle spalle.
 * Con [onApri] toccare un riquadro apre quel Capo.
 */
@Composable
fun OutfitVerticale(capi: List<Capo>, onApri: ((Capo) -> Unit)? = null) {
    val (accanto, corpo) = capi.sortedBy { it.categoria }
        .partition { it.categoria == Categoria.CAPOSPALLA || it.categoria == Categoria.ACCESSORI }
    @Composable
    fun Riquadro(capo: Capo, larghezza: Dp) {
        val tocco = onApri?.let { Modifier.clickable(onClickLabel = "Apri il capo") { it(capo) } } ?: Modifier
        RiquadroCapo(capo, tocco.width(larghezza).clearAndSetSemantics { contentDescription = descrizione(capo) })
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column { corpo.forEach { Riquadro(it, 144.dp) } }
        if (accanto.isNotEmpty()) {
            Column { accanto.forEach { Riquadro(it, 96.dp) } }
        }
    }
}

/** Il riquadro detto a voce, Categoria compresa: "Sopra: t-shirt, rosso e bianco, a righe". */
fun descrizione(capo: Capo): String =
    "${capo.categoria.nome}: ${capo.tipo}, " + listOfNotNull(capo.colorePrincipale, capo.coloreSecondario).joinToString(" e ") { colore(it).nome } +
        (if (capo.motivo != Motivo.TINTA_UNITA) ", a ${capo.motivo.nome}" else "")

/** La frase dice perché funziona; la regola resta come etichetta per chi vuole impararla. Senza [perché] resta il [ripiego]. */
@Composable
fun Spiegazione(perché: Perché?, ripiego: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(perché?.frase ?: ripiego, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        perché?.let {
            Text(it.regola.maiuscola(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
