package com.guardaroba.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guardaroba.motore.Categoria
import com.guardaroba.motore.Consiglio
import com.guardaroba.motore.Esito
import com.guardaroba.motore.Stagione
import com.guardaroba.motore.perché

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitScreen(vm: OutfitViewModel) {
    val stato = vm.stato
    // Sopra l'if, come nelle Impostazioni: tornando dalle regole Proposta e scorrimento restano dov'erano.
    val scorrimento = rememberScrollState()
    var regole by rememberSaveable { mutableStateOf(false) }
    if (regole) {
        RegoleScreen(onIndietro = { regole = false })
        return
    }
    val preferiti by vm.outfitPreferiti.collectAsStateWithLifecycle()
    vm.avvisoFuoriStagione?.let { AvvisoFuoriStagione(it, vm::chiudiAvviso) }
    // In alto i comandi che cambiano la Richiesta, in basso l'azione principale sempre nello stesso posto: scorre solo il contenuto.
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Outfit") }, windowInsets = WindowInsets(0)) // insets already applied by the outer Scaffold
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            vm.partenza?.let { id ->
                // Il chip è l'uscita da "Parti da": toccarlo (o Indietro) torna alle Proposte da tutto il Guardaroba.
                BackHandler { vm.partiDa(null) }
                InputChip(
                    selected = true,
                    onClick = { vm.partiDa(null) },
                    label = { Text("Parti da: " + (stato?.capi?.get(id)?.tipo?.maiuscola() ?: "…")) },
                    trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Togli", Modifier.size(InputChipDefaults.IconSize)) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            // "tutto l'anno" entra già in ogni filtro, quindi non è una voce. Vale dalla prossima Proposta.
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (listOf(null) + (Stagione.entries - Stagione.TUTTO_L_ANNO)).forEach {
                    FilterChip(
                        selected = it == vm.filtroStagione,
                        onClick = { vm.scegliStagione(it) },
                        label = { Text((it?.nome ?: "qualsiasi stagione").maiuscola()) },
                    )
                }
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(scorrimento).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Material fade through tra una Proposta e l'altra; con "Rimuovi animazioni" Compose azzera le durate (animator_duration_scale).
            AnimatedContent(
                targetState = stato?.esito,
                contentKey = { (it as? Esito.Proposta)?.outfit?.idCapi ?: it },
                transitionSpec = {
                    (fadeIn(tween(210, delayMillis = 90)) + scaleIn(tween(210, delayMillis = 90), initialScale = 0.92f)) togetherWith
                        fadeOut(tween(90))
                },
                label = "Proposta",
            ) { esito ->
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (esito) {
                        null -> Messaggio("Premi Genera per ricevere un Outfit fatto con i tuoi Capi.")
                        is Esito.Proposta -> {
                            // mapNotNull: un Capo eliminato dopo la Proposta sparisce dalla miniatura
                            OutfitVerticale(esito.outfit.capi.mapNotNull { stato?.capi?.get(it.id) })
                            Spiegazione(perché(esito.outfit), esito.spiegazione, onComeFunziona = { regole = true })
                            // Il nome si chiede solo dopo Salva; una Proposta nuova chiude la finestra.
                            var chiediNome by rememberSaveable(esito.outfit.idCapi) { mutableStateOf(false) }
                            val traIPreferiti = esito.outfit.idCapi in preferiti
                            FilledTonalButton(onClick = { chiediNome = true }, enabled = !traIPreferiti) {
                                Icon(
                                    if (traIPreferiti) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                )
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text(if (traIPreferiti) "Tra i Preferiti" else "Salva")
                            }
                            if (chiediNome && !traIPreferiti) {
                                FinestraNome("Salva tra i Preferiti", "", onChiudi = { chiediNome = false }) { vm.salva(it) }
                            }
                        }
                        is Esito.MancaCategoria ->
                            Messaggio(mancano(esito.categorie, vm.partenza?.let { stato?.capi?.get(it)?.categoria }, vm.filtroStagione != null))
                        // Cosa non va e cosa fare, con le ragioni del Motore: niente vicoli ciechi.
                        is Esito.NessunAbbinamento -> Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                if (vm.partenza != null) "Nessun abbinamento per questo Capo" else "Nessun abbinamento",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                            )
                            Messaggio(esito.ragione.maiuscola() + ".")
                            Messaggio(esito.rimedio)
                        }
                        Esito.Esaurite -> Messaggio("Hai visto tutte le Proposte possibili.\nPremi Ricomincia per rivederle.")
                    }
                }
            }
            stato?.consigliati?.takeIf { it.isNotEmpty() }?.let { ColoriConsigliati(it, vm.partenza?.let { id -> stato.capi[id]?.tipo }) }
        }
        // Composto dopo quello di "Parti da", vince lui: Indietro riprende prima le Proposte di prima, poi esce da "Parti da".
        BackHandler(enabled = vm.cèPrecedente, onBack = vm::precedente)
        HorizontalDivider() // separa il contenuto che scorre dai comandi fissi
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val alto = Modifier.heightIn(min = 56.dp)
            if (vm.cèPrecedente) {
                OutlinedButton(onClick = vm::precedente, modifier = alto) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Precedente")
                }
            }
            val principale = alto.weight(1f)
            if (stato?.esito == Esito.Esaurite) {
                Button(onClick = vm::ricomincia, modifier = principale) { Text("Ricomincia") }
            } else {
                Button(onClick = vm::genera, modifier = principale) { Text(if (stato?.esito is Esito.Proposta) "Un altro" else "Genera") }
            }
        }
    }
}

/** I Colori della Palette che starebbero bene col Capo di partenza ([tipo]), raggruppati per regola; chiusi finché non si aprono. */
@Composable
private fun ColoriConsigliati(consigliati: List<Consiglio>, tipo: String?) {
    var aperti by rememberSaveable { mutableStateOf(false) }
    // Aperti finiscono sotto il pulsante principale: si portano in vista.
    val inVista = remember { BringIntoViewRequester() }
    LaunchedEffect(aperti) { if (aperti) inVista.bringIntoView() }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClickLabel = if (aperti) "Chiudi" else "Apri") { aperti = !aperti },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(tipo?.let { "Stanno bene con: ${it.maiuscola()}" } ?: "Colori consigliati", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Icon(if (aperti) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
        }
        if (aperti) {
            Column(Modifier.bringIntoViewRequester(inVista), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                consigliati.groupBy { it.regola }.forEach { (regola, gruppo) ->
                    Text(regola.maiuscola(), style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        gruppo.forEach { (colore) ->
                            Row(
                                Modifier.clearAndSetSemantics { contentDescription = colore.nome },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Box(Modifier.size(20.dp).background(colore.campione(), CircleShape).border(colore.bordo(), CircleShape))
                                Text(colore.nome.maiuscola(), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Il messaggio di MancaCategoria; l'Intero è un'alternativa solo se il Capo di partenza ([partenza]) non lo esclude (ADR-0008). */
private fun mancano(categorie: List<Categoria>, partenza: Categoria?, conFiltro: Boolean): String {
    val intero = (Categoria.SOPRA in categorie || Categoria.SOTTO in categorie) && Categoria.INTERO !in partenza?.escluse.orEmpty()
    return (if (categorie.size == 1) "Ti manca: " else "Ti mancano: ") + categorie.joinToString(", ") { it.nome } + "." +
        (if (intero) "\nAl posto di Sopra e Sotto va bene anche un Intero." else "") +
        "\nAggiungi i Capi che mancano nel Guardaroba" + (if (conFiltro) " o cambia filtro Stagione." else ".")
}

/** ADR-0008: il Capo di partenza resta nelle Proposte anche se il filtro non lo ammette; gli altri Capi seguono il filtro. */
@Composable
private fun AvvisoFuoriStagione(avviso: FuoriStagione, onOk: () -> Unit) {
    val (stagione, filtro) = avviso
    // Il plurale non si ricava da Stagione.nome; "tutto l'anno" non è mai un filtro, ma il when deve coprirlo.
    val capi = when (filtro) {
        Stagione.ESTIVO -> "Capi estivi"
        Stagione.INVERNALE -> "Capi invernali"
        Stagione.MEZZA_STAGIONE -> "Capi di mezza stagione"
        Stagione.TUTTO_L_ANNO -> "Capi per tutto l'anno"
    }
    AlertDialog(
        onDismissRequest = onOk,
        text = {
            Text(
                "Hai scelto un Capo fuori stagione (${stagione.nome}) con il filtro " +
                    "${filtro.nome.maiuscola()}: ti propongo comunque abbinamenti con $capi.",
            )
        },
        confirmButton = { TextButton(onClick = onOk) { Text("Ok") } },
    )
}

@Composable
private fun Messaggio(testo: String) {
    Text(testo, textAlign = TextAlign.Center)
}
