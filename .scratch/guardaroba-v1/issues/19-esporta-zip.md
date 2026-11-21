# 19: Esporta il Guardaroba in ZIP

**What to build:** In Impostazioni premo "Esporta" e salvo, dove voglio tramite il selettore di file di sistema, un unico file ZIP con tutti i Capi, i Preferiti e le foto.

**Blocked by:** 05, 15

**Status:** chiuso

- [x] ZIP con un JSON (versione del formato, Capi, Preferiti) + cartella foto
- [x] Salvataggio via SAF, nessun permesso di storage
- [x] Messaggio chiaro di conferma o di errore

## Comments

- Formato del JSON e Capi senza foto sul disco: ADR-0007.
- Da verificare a mano su un telefono: il selettore SAF, il messaggio di conferma/errore e la cancellazione del file a metà (il percorso d'errore del ViewModel non ha test automatici).
