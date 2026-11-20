# 16: Nome ed eliminazione dei Preferiti

**What to build:** Posso dare un nome opzionale a un Preferito al salvataggio, rinominarlo in seguito ed eliminarlo dalla lista.

**Blocked by:** 15

**Status:** chiuso

- [x] Nome opzionale al salvataggio
- [x] Rinomina dalla lista
- [x] Eliminazione con conferma; i Capi restano intatti

## Comments

- Lo schema non cambia: `Preferito.nome` esisteva già (v3, ticket 15). Il nome viene ripulito dagli spazi ai bordi, e un nome vuoto vale "nessun nome" (`pulito()` in `Preferito.kt`), sia al salvataggio sia nella rinomina.
- Il campo "Nome (opzionale)" sta sopra "Salva" nella schermata Outfit, così salvare resta un tocco solo (storia 50).
- Lo stato "Tra i Preferiti" si ricava dalla lista dei Preferiti (`OutfitViewModel.salvati`) e non è più un flag: se elimini il Preferito, la Proposta mostrata torna salvabile. Il campo nome sparisce quando l'Outfit è già salvato, quindi il nome non si perde con un salvataggio doppio. Se capita comunque, per una corsa, `salva` tiene il Preferito che c'è già e il suo nome.
- Non ho verificato la UI su un emulatore: ho controllato solo che compili (`assembleDebug`).
