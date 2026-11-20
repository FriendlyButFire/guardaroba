# 15: Salvare tra i Preferiti

**What to build:** Su una Proposta premo "Salva" e l'Outfit finisce nella sezione Preferiti, che mostra la lista con le miniature dei Capi e la Spiegazione.

**Blocked by:** 07

**Status:** chiuso

- [x] Schema Preferito + PreferitoCapo con foreign key in cascata come da [[spec]]
- [x] "Salva" disponibile in Genera e in Parti da questo capo
- [x] Lista dei Preferiti con miniature e Spiegazione, persistente dopo il riavvio
- [x] Salvare due volte lo stesso Outfit non crea doppioni

## Comments

- "Salva" è in `OutfitViewModel.salva()` e salva qualsiasi `Esito.Proposta` in `stato`, qualunque sia la Richiesta: il ticket 13, passando dallo stesso ViewModel, ha "Salva" gratis. Per ora c'è solo in Genera, perché il 13 non esiste ancora.
- La foreign key in cascata su `capoId` toglie solo la riga di `preferito_capo`, e il Preferito resta con un Capo in meno. Il ticket 17 deve eliminare nella stessa transazione i Preferiti che contengono il Capo (c'è un `// shortcut` in `Preferito.kt`).
- Non ho verificato la UI su un emulatore: ho controllato solo che compili (`assembleDebug`).
