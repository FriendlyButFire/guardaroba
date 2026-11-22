# Correzioni dalle ricritiche Impeccable (2026-11-22)

Le quattro schermate ricriticate: Outfit 29, Guardaroba 29, Preferiti 29, Impostazioni 32/40. Critiche complete in `.impeccable/critique/2026-11-22T*`.
L'utente ha approvato le quattro funzioni nuove: Proposta di prima, collegamento alle regole, Stagione nei Preferiti, Filtri solo dei Capi presenti (+ menu nel Preferito aperto). Le rifiniture senza funzioni nuove le decide l'agente.

Gruppi per file, per lavorare in parallelo senza conflitti:
- Outfit (`OutfitScreen.kt`, `OutfitViewModel.kt`): 01 → 05 → 02
- Preferiti (`PreferitiScreen.kt`, `PreferitiViewModel.kt`): 03
- Guardaroba (`GuardarobaScreen.kt`, `CapoForm.kt`, `Filtro.kt`): 04
- Impostazioni (`ImpostazioniScreen.kt`, `BackupSezione.kt`): 06
- 07 chiude: ADR, roadmap, prova sull'emulatore.
