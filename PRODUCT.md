# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Il proprietario dell'app e poche persone vicine (amici, famiglia), che la ricevono come APK installato a mano. Nessuno store. Ognuno ha il proprio telefono e il proprio Guardaroba: niente condivisione tra utenti.

Situazione tipica: la mattina, davanti all'armadio, con poco tempo. Il compito è scegliere cosa mettersi tra i propri Capi reali, senza ricadere nelle solite due o tre combinazioni "sicure" e senza abbinare colori che stonano. Gli utenti non conoscono la teoria dei colori.

## Product Purpose

Proporre Outfit fatti solo con i Capi che l'utente ha registrato, validi secondo regole di teoria dei colori (Neutri e Armonie), ognuno con una Spiegazione che insegna perché funziona. Successo: l'utente si veste con più varietà e, col tempo, capisce da solo cosa sta bene insieme.

## Positioning

Offline, solo il tuo armadio vero e una Spiegazione per ogni Proposta. Le app generiche propongono capi che non hai, dipendono dal cloud o dall'AI e non spiegano niente. Guardaroba lavora con regole deterministiche sui tuoi Capi e ti insegna il perché.

## Operating Context

- Uso rapido e ripetuto, in piedi, spesso con una mano. Si registrano i Capi una volta, poi si apre l'app per Genera / "Un altro" / Parti da questo capo.
- Inserire i Capi richiede più tempo: Categoria, Tipo, Colore principale/secondario dalla Palette di 25 Colori, Motivo, Stagione, Taglia, foto opzionale (fotocamera o galleria).
- Filtro Stagione prima di generare.
- Preferiti con nome; Spiegazione ricalcolata dai Capi attuali, con segnale "Non più valido".
- Dati: backup automatico di Android ed export/import ZIP da Impostazioni.

## Capabilities and Constraints

- Android nativo, Kotlin + Jetpack Compose + Material 3, tema chiaro/scuro di sistema, minSdk 26 (vedi `docs/adr/`).
- Completamente offline: niente rete, niente account, niente AI.
- Solo italiano.
- Vocabolario di dominio fisso in `GLOSSARY.md` (Capo, Categoria, Outfit, Proposta, Spiegazione, Preferito, Palette, Neutri, Armonia…): i testi dell'interfaccia lo usano.
- Categorie, Tipi e Colori sono liste fisse; Armonie solo monocromatico, analoghi, complementari e neutri (niente triadi).
- Quattro sezioni nella barra in basso: Guardaroba, Outfit, Preferiti, Impostazioni.
- Aperto: un primo avvio pensato per persone che ricevono l'APK senza spiegazioni. Oggi ci sono gli stati vuoti, ma non un onboarding.

## Evidence on Hand

Nessuna testimonianza, dato d'uso o asset di marca. Non c'è logo né icona personalizzata confermata. Non vanno inventati.

## Product Principles

1. Solo i Capi reali dell'utente: mai proporre qualcosa che non possiede.
2. Ogni Proposta si spiega: la Spiegazione è parte del prodotto, non un extra.
3. Veloce la mattina: arrivare a un Outfit con il minor numero di tocchi possibile.
4. I dati dell'utente non si perdono: backup, export, conferme prima di sostituire o eliminare.
5. Regole prevedibili invece di magia: stesse regole, stesse risposte, niente AI.

## Accessibility & Inclusion

Nessun requisito specifico oltre alle buone pratiche Material 3.
