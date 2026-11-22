package com.guardaroba.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guardaroba.archivio.Capo
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Motivo
import com.guardaroba.motore.Perché
import com.guardaroba.motore.colore

// La composizione di un Outfit condivisa da Outfit e Preferiti (ADR-0012, ADR-0014).

/**
 * L'Outfit in piedi come un corpo: Sopra, Sotto (o Intero) e Scarpe in colonna; Capospalla e Accessori accanto, dall'altezza delle spalle.
 * Con [onApri] toccare un riquadro apre quel Capo; [cambiato] è il Capo da evidenziare (vedi [RiquadroOutfit]).
 */
@Composable
fun OutfitVerticale(capi: List<Capo>, onApri: ((Capo) -> Unit)? = null, cambiato: Capo? = null) {
    val (accanto, corpo) = capi.sortedBy { it.categoria }
        .partition { it.categoria == Categoria.CAPOSPALLA || it.categoria == Categoria.ACCESSORI }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column { corpo.forEach { RiquadroOutfit(it, Modifier.width(144.dp), it.id == cambiato?.id, onApri) } }
        if (accanto.isNotEmpty()) {
            Column { accanto.forEach { RiquadroOutfit(it, Modifier.width(96.dp), it.id == cambiato?.id, onApri) } }
        }
    }
}

/**
 * Il riquadro di un Capo dentro un Outfit, detto a voce con [descrizione]. [cambiato] lo cerchia nel colore d'errore: è il Capo
 * che ha reso un Preferito non più valido.
 */
@Composable
fun RiquadroOutfit(capo: Capo, modifier: Modifier, cambiato: Boolean = false, onApri: ((Capo) -> Unit)? = null) {
    val anello = if (cambiato) Modifier.border(3.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(16.dp)) else Modifier
    val tocco = onApri?.let { Modifier.clickable(onClickLabel = "Apri il capo") { it(capo) } } ?: Modifier
    RiquadroCapo(
        capo,
        modifier.then(anello).then(tocco)
            .clearAndSetSemantics { contentDescription = (if (cambiato) "Capo cambiato. " else "") + descrizione(capo) },
    )
}

/** Il riquadro detto a voce, Categoria compresa: "Sopra: t-shirt, rosso e bianco, a righe". */
fun descrizione(capo: Capo): String =
    "${capo.categoria.nome}: ${capo.tipo}, " + listOfNotNull(capo.colorePrincipale, capo.coloreSecondario).joinToString(" e ") { colore(it).nome } +
        (if (capo.motivo != Motivo.TINTA_UNITA) ", a ${capo.motivo.nome}" else "")

/** La frase dice perché funziona; la regola resta come etichetta per chi vuole impararla. Senza [perché] resta il [ripiego]. */
@Composable
fun Spiegazione(perché: Perché?, ripiego: String, onComeFunziona: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(perché?.frase ?: ripiego, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        perché?.let {
            Text(it.regola.maiuscola(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // La regola ha un nome tecnico: la pagina delle regole lo spiega (prima ci si arrivava solo dalle Impostazioni).
        TextButton(onClick = onComeFunziona) { Text("Come funziona") }
    }
}
