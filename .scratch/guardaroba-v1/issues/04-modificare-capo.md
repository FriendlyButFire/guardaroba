# 04: Modificare un Capo

**What to build:** Toccando un Capo nella griglia apro la sua scheda e posso modificarne ogni campo; le modifiche si salvano e si vedono subito nel Guardaroba.

**Blocked by:** 03

**Status:** chiuso

- [x] Tocco sul Capo apre la scheda in modifica con i valori attuali
- [x] Il salvataggio aggiorna il Capo esistente (stesso id)
- [x] Annullare non modifica nulla

## Comments

- `CapoDao.inserisci` è diventato `salva` (`@Upsert`): id 0 inserisce, un id esistente aggiorna. `CapoForm(iniziale = capo)` costruisce il risultato con `iniziale.copy(...)`, così id, `creatoIl` e i campi che il form non mostra (es. la foto del ticket 05) restano.
- La scheda è `CapoForm` stesso (titolo "Modifica capo"): i ticket 05/13/17 vi aggiungono le loro azioni. Annulla e Indietro chiudono senza salvare.
- Lo stato della schermata è l'id del Capo aperto (`null` = chiusa, `0` = nuovo). Se il Capo non è nella lista (lista ancora in caricamento), si vede la griglia. Il ticket 17 (elimina) dovrà rimettere `aperto = null` dopo l'eliminazione.
- Code review fatta in linea, senza sub-agenti (l'utente ha rifiutato il sub-agente di review). UI verificata solo con `assembleDebug`, non a mano su emulatore.
