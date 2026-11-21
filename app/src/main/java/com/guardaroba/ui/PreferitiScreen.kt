package com.guardaroba.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardaroba.archivio.Capo
import com.guardaroba.archivio.PreferitoConCapi
import com.guardaroba.motore.Giudizio
import com.guardaroba.motore.rimedio

/**
 * I Preferiti; [onVaiAOutfit] porta alla sezione Outfit dallo stato vuoto, [schedaCapo] mostra la scheda di un Capo aperta da un
 * Preferito e chiama la sua funzione quando si chiude.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferitiScreen(
    onVaiAOutfit: () -> Unit,
    vm: PreferitiViewModel = viewModel(factory = PreferitiViewModel.Factory),
    schedaCapo: @Composable (id: Long, chiudi: () -> Unit) -> Unit,
) {
    val preferiti by vm.preferiti.collectAsStateWithLifecycle()
    // Il Preferito aperto in grande e il Capo aperto da lì; il Preferito resta aperto mentre la scheda del Capo è in vista.
    var aperto by rememberSaveable { mutableStateOf<Long?>(null) }
    var capoAperto by rememberSaveable { mutableStateOf<Long?>(null) }
    var daRinominare by rememberSaveable { mutableStateOf<Long?>(null) } // il Preferito con la finestra Rinomina aperta
    var validoVisto by rememberSaveable(aperto) { mutableStateOf<Boolean?>(null) } // il giudizio del Preferito aperto, l'ultima volta in vista
    val avvisi = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    capoAperto?.let { id ->
        schedaCapo(id) { capoAperto = null }
        return
    }
    val lista = preferiti
    lista?.find { it.preferito.id == aperto }?.let { p ->
        VistaPreferito(p, validoVisto, onVisto = { validoVisto = it }, onIndietro = { aperto = null }, onApriCapo = { capoAperto = it.id })
        return
    }
    // Eliminare si annulla dallo snackbar: niente finestra di conferma (ADR-0014).
    // shortcut: lo snackbar vive con questa schermata: aprendo un Preferito ricompare al ritorno, cambiando sezione si perde
    // (il Preferito resta eliminato); spostarlo nello Scaffold dell'app se capita di volerlo annullare da un'altra sezione.
    fun elimina(p: PreferitoConCapi) {
        vm.elimina(p)
        scope.launch {
            avvisi.currentSnackbarData?.dismiss()
            val esito = avvisi.showSnackbar("Preferito eliminato", actionLabel = "Annulla", duration = SnackbarDuration.Long)
            if (esito == SnackbarResult.ActionPerformed) vm.ripristina(p)
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0), // insets already applied by the outer Scaffold
        topBar = { TopAppBar(title = { Text("Preferiti") }, windowInsets = WindowInsets(0)) },
        snackbarHost = { SnackbarHost(avvisi) },
    ) { padding ->
        when {
            // Ancora in caricamento: un indicatore, non lo stato vuoto a sproposito (come nel Guardaroba, ADR-0013).
            lista == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            lista.isEmpty() -> Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Nessun Preferito.\nSalva da Outfit le Proposte che ti piacciono.", textAlign = TextAlign.Center)
                FilledTonalButton(onClick = onVaiAOutfit) { Text("Vai a Outfit") }
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(lista, key = { it.preferito.id }) { p ->
                    SchedaPreferito(
                        p,
                        onApri = { aperto = p.preferito.id },
                        onRinomina = { daRinominare = p.preferito.id },
                        onElimina = { elimina(p) },
                    )
                }
            }
        }
    }
    lista?.find { it.preferito.id == daRinominare }?.let { p ->
        FinestraNome("Rinomina", p.preferito.nome.orEmpty(), onChiudi = { daRinominare = null }) {
            vm.rinomina(p.preferito.id, it)
            scope.launch { avvisi.showSnackbar("Preferito rinominato") }
        }
    }
}

@Composable
private fun SchedaPreferito(preferito: PreferitoConCapi, onApri: () -> Unit, onRinomina: () -> Unit, onElimina: () -> Unit) {
    val nome = preferito.preferito.nome ?: "Senza nome"
    Card(onApri, Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    nome,
                    Modifier.weight(1f).padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (preferito.preferito.nome == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                // Nel menu ⋮ come nella scheda Capo (ADR-0013); a voce ogni azione dice di quale Preferito si tratta.
                AltreAzioni("Altre azioni per $nome") { chiudi ->
                    DropdownMenuItem(
                        text = { Text("Rinomina") },
                        onClick = { chiudi(); onRinomina() },
                        modifier = Modifier.semantics { contentDescription = "Rinomina $nome" },
                    )
                    DropdownMenuItem(
                        text = { Text("Elimina", color = MaterialTheme.colorScheme.error) },
                        onClick = { chiudi(); onElimina() },
                        modifier = Modifier.semantics { contentDescription = "Elimina $nome" },
                    )
                }
            }
            val cambiato = preferito.responsabile()
            Row(Modifier.fillMaxWidth()) {
                // Al massimo 5 Capi per Outfit: entrano tutti in una riga.
                preferito.capi.sortedBy { it.categoria }.forEach { RiquadroOutfit(it, Modifier.weight(1f), it.id == cambiato?.id) }
            }
            // Spiegazione e validità seguono i Capi di adesso; un Preferito non più valido resta, segnalato (ADR-0011).
            Box(Modifier.padding(8.dp)) {
                when (val giudizio = preferito.giudizio()) {
                    // La frase come in Outfit, la regola come etichetta (ADR-0012); più piccole, perché qui sono tante.
                    is Giudizio.Valido -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val perché = preferito.perché()
                        Text(perché?.frase ?: giudizio.spiegazione, style = MaterialTheme.typography.bodyLarge)
                        perché?.let {
                            Text(it.regola.maiuscola(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    is Giudizio.NonValido -> NonPiùValido(giudizio.ragione, inGrande = false)
                }
            }
        }
    }
}

/** Il Preferito in grande, composto come in Outfit e giudicato con i Capi di adesso (ADR-0011, ADR-0014); toccare un riquadro apre il Capo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VistaPreferito(
    preferito: PreferitoConCapi,
    validoVisto: Boolean?,
    onVisto: (Boolean) -> Unit,
    onIndietro: () -> Unit,
    onApriCapo: (Capo) -> Unit,
) {
    BackHandler(onBack = onIndietro)
    val giudizio = preferito.giudizio()
    val valido = giudizio is Giudizio.Valido
    val view = LocalView.current
    // Il giudizio cambia solo nella scheda del Capo, che prende il posto di questa vista: al ritorno la vista è nuova e una live
    // region nuova non si annuncia. Così TalkBack sente il cambio confrontando con [validoVisto].
    LaunchedEffect(valido) {
        @Suppress("DEPRECATION") // nessuna alternativa per un annuncio che non nasce da un cambio sotto gli occhi
        if (validoVisto != null && validoVisto != valido) view.announceForAccessibility(if (valido) "Di nuovo valido" else "Non più valido")
        onVisto(valido)
    }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(preferito.preferito.nome ?: "Senza nome", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = onIndietro) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro") }
            },
            windowInsets = WindowInsets(0),
        )
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val cambiato = preferito.responsabile()
            OutfitVerticale(preferito.capi, onApri = onApriCapo, cambiato = cambiato)
            when (giudizio) {
                is Giudizio.Valido -> Spiegazione(preferito.perché(), giudizio.spiegazione)
                is Giudizio.NonValido -> NonPiùValido(giudizio.ragione, inGrande = true, cambiato)
            }
        }
    }
}

/**
 * "Non più valido" come stato da sistemare, non come errore: l'etichetta, la ragione del Motore e il rimedio. [inGrande] nella vista
 * grande, dove il Capo [cambiato] (se si sa) è cerchiato e toccarlo apre la sua scheda.
 */
@Composable
private fun NonPiùValido(ragione: String, inGrande: Boolean, cambiato: Capo? = null) {
    val allineamento = if (inGrande) TextAlign.Center else TextAlign.Start
    Column(
        horizontalAlignment = if (inGrande) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, Modifier.size(18.dp))
                Text("Non più valido", style = MaterialTheme.typography.labelLarge)
            }
        }
        Text(ragione.maiuscola() + ".", style = MaterialTheme.typography.bodyLarge, textAlign = allineamento)
        Text(
            rimedio(ragione) + (if (inGrande && cambiato != null) " Il Capo cambiato è cerchiato: toccalo per correggerlo." else ""),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = allineamento,
        )
    }
}

/** Chiede il nome di un Preferito (nuovo o da rinominare); lasciarlo vuoto vuol dire senza nome. */
@Composable
fun FinestraNome(titolo: String, attuale: String, onChiudi: () -> Unit, onConferma: (String) -> Unit) {
    // Il campo prende il fuoco con il nome attuale tutto selezionato: si riscrive senza cancellarlo a mano.
    var nome by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(attuale, TextRange(0, attuale.length))) }
    val fuoco = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = onChiudi,
        title = { Text(titolo) },
        text = {
            LaunchedEffect(Unit) { fuoco.requestFocus() } // qui dentro: il campo sta nella finestra della dialog, composta a parte
            OutlinedTextField(
                nome,
                { nome = it },
                Modifier.focusRequester(fuoco),
                label = { Text("Nome (opzionale)") },
                placeholder = { Text("Senza nome") },
                singleLine = true,
            )
        },
        confirmButton = { TextButton(onClick = { onConferma(nome.text); onChiudi() }) { Text("Salva") } },
        dismissButton = { TextButton(onClick = onChiudi) { Text("Annulla") } },
    )
}
