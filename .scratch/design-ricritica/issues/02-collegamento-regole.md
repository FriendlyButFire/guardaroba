# 02: "Come funziona" porta alla pagina delle regole

**What to build:** Sotto la Spiegazione, in Outfit e nel Preferito aperto, un collegamento "Come funziona" apre `RegoleScreen` (oggi raggiungibile solo dalle Impostazioni). Indietro torna dove si era, con lo stesso scorrimento.

**Blocked by:** 01, 03, 05 (tocca `OutfitScreen.kt` e `PreferitiScreen.kt`)

**Status:** chiuso

- [x] Collegamento in Outfit sotto la regola della Spiegazione
- [x] Collegamento nel Preferito aperto
- [x] Indietro (freccia e sistema) torna alla schermata di prima senza perdere la Proposta né lo scorrimento

## Comments

- Il collegamento sta dentro `Spiegazione` (condivisa da Outfit e Preferito aperto). Verificato sull'emulatore: andata e ritorno con Proposta, cronologia e scorrimento intatti.
