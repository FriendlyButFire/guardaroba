# 03: Preferiti: filtro Stagione e menu nel Preferito aperto

**What to build:** Sopra l'elenco dei Preferiti, gli stessi chip Stagione di Outfit ("Qualsiasi stagione", Estivo, Invernale, Mezza stagione): con una Stagione scelta restano i Preferiti i cui Capi la ammettono tutti (stessa regola di `fuoriStagione` del Motore; "tutto l'anno" va con tutte). Il Preferito aperto ha nella barra lo stesso menu ⋮ della scheda (Rinomina, Elimina con Annulla); eliminato da aperto, si torna all'elenco.

**Blocked by:** nessuno

**Status:** chiuso

- [x] Chip Stagione sopra l'elenco, filtro con test sulla logica
- [x] Stato vuoto quando il filtro non lascia niente, con un modo per toglierlo
- [x] Menu ⋮ nel Preferito aperto; Elimina torna all'elenco e mostra lo snackbar con Annulla

## Comments

- Il filtro vive nel ViewModel come in Outfit (`mutableStateOf`): resta aprendo un Preferito o un Capo, si perde se Android chiude l'app; la scelta si tocca diretta, perché qui non c'è niente da azzerare come le "già viste" di Outfit.
- Si guardano i Capi che il Preferito ha adesso: un Capo eliminato non conta più (il Preferito resta, segnalato "Non più valido").
- Stato vuoto col filtro: "Nessun Preferito con tutti i Capi adatti a questa stagione." + "Mostra tutti i Preferiti"; i chip restano in vista.
- Il Preferito aperto ora è uno Scaffold con lo stesso snackbar dell'elenco: "Preferito rinominato" compare lì, Elimina torna all'elenco dove compare Annulla.
- Per ADR-0016 (07): ADR-0014 descriveva la barra del Preferito aperto con solo freccia e nome; ora ha anche il ⋮. La riga dei chip Stagione è copiata da `OutfitScreen.kt` (non toccabile qui): da unire in un composable comune.
