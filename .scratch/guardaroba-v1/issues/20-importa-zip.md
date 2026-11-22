# 20: Importa il Guardaroba da ZIP

**What to build:** In Impostazioni premo "Importa", scelgo un file ZIP esportato in precedenza, confermo che sostituirà il Guardaroba attuale, e ritrovo esattamente Capi, Preferiti e foto. Se il file non è valido vedo un errore e il Guardaroba attuale resta intatto.

**Blocked by:** 19

**Status:** chiuso

- [x] Conferma esplicita prima della sostituzione
- [x] Validazione completa dell'archivio prima di toccare i dati
- [x] Sostituzione in un'unica transazione, con ripristino delle foto
- [x] Test (Robolectric): il giro export → import ripristina dati e foto identici
- [x] Test: l'import di un file non valido lascia i dati intatti

## Comments

- Le foto importate prendono un nome nuovo (UUID) e le vecchie si cancellano solo dopo il commit: nessuna uscita a metà lascia il database con foto mancanti (ADR-0009). Dopo l'import `percorsoFoto` cambia, il contenuto no.
- Da verificare a mano su emulatore/telefono: selettore, dialogo di conferma, messaggi d'errore.
