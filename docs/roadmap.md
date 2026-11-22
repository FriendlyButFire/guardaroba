# Roadmap

Aggiornata il **2026-11-22**. Fase attuale: **retro**. Il design con Impeccable è finito: quattro schermate ridisegnate, ricritiche (29-32/40), correzioni ([[0016-correzioni-dalle-ricritiche]]) provate sul telefono.

Riferimenti: [[spec]] · [[GLOSSARY]] · [[PRODUCT]] · storico completo: `git log --oneline` (116 commit, branch `guardaroba-v1`, non ancora su `master`).

```mermaid
flowchart LR
  A[Idea e decisioni] --> B[Spec] --> C[21 ticket] --> D[Build v1] --> E[Revisione finale] --> F[Prova sul telefono] --> G[Design Impeccable] --> H[Retro] --> I[Merge su master]
  style A fill:#9c9
  style B fill:#9c9
  style C fill:#9c9
  style D fill:#9c9
  style E fill:#9c9
  style F fill:#9c9
  style G fill:#9c9
  style H fill:#fc6
```

## Fasi

- [x] **Idea e decisioni**: grilling, ADR [[0001-offline-regole-senza-ai]] → [[0005-min-sdk-26]]
- [x] **Spec**: [[spec]], 67 storie utente
- [x] **Ticket**: 21 ticket tracer-bullet in `.scratch/guardaroba-v1/issues/`
- [x] **Build v1**: tutti i 21 ticket chiusi (vedi sotto)
- [x] **Revisione finale** dell'intero branch: correzioni sulle foto, sulla sessione Outfit e sugli ADR
- [x] **Decisioni dopo la revisione**: Preferito "Non più valido" ([[0011-preferito-giudicato-con-i-capi-di-adesso]]), popup capo fuori stagione ([[0008-parti-da-capo-ignora-filtro]])
- [x] **Prova sul telefono** (Pixel 8 Pro) con `scripts/installa-sul-telefono.sh`, compreso il backup `bmgr`
- [x] **Design con Impeccable**: una schermata alla volta, critica → modifiche → prova sul telefono
  - [x] [[PRODUCT]] scritto (`/impeccable init`)
  - [x] Outfit: critica 24/40 → 5 modifiche fatte ([[0012-schermata-outfit-ridisegnata]]); ricritica 29/40
  - [x] Guardaroba e scheda Capo: critica 22/40 → 5 modifiche fatte ([[0013-guardaroba-e-scheda-capo-ridisegnati]]): pulsante Filtri, scheda corta con barra ✕/Salva, conferma "Scartare le modifiche?", "Salva e aggiungi un altro", "Duplica", riga "Manca: …"; ricritica 29/40
  - [x] Preferiti: critica 23/40 → 5 modifiche fatte ([[0014-preferiti-ridisegnati]]): apertura in grande, "Non più valido" con rimedio, contrasto sul giallo, Spiegazione in frase, snackbar con Annulla; provati sul telefono; ricritica 29/40
  - [x] Impostazioni: critica 24/40 → 5 modifiche fatte ([[0015-impostazioni-ridisegnate]]): copia automatica prima di Importa con "Ripristina", backup detto come condizione con "Ultima copia", Esporta pieno e Importa contornato, barra in alto e snackbar, pagina "Come scegliamo gli abbinamenti"; provate sul telefono; ricritica 32/40
  - [x] Correzioni dalle ricritiche: Proposta di prima, "Come funziona", Stagione e ⋮ nei Preferiti, filtri dei soli Capi presenti, barre col titolo ([[0016-correzioni-dalle-ricritiche]], ticket in `.scratch/design-ricritica/`); provate sul telefono il 2026-11-22 (con mobile-mcp)
- [ ] **Retro**: problemi ricorrenti → regole nel CLAUDE.md
- [ ] **Merge su `master`** e push: solo su tua richiesta

## Build v1: ticket chiusi

| Area | Ticket |
|---|---|
| Base | [[01-scheletro-app]] |
| Guardaroba | [[02-aggiungere-capo]] · [[03-campi-completi-capo]] · [[04-modificare-capo]] · [[05-foto-capo]] · [[06-filtri-guardaroba]] · [[17-eliminare-capo]] |
| Motore Outfit | [[07-genera-prima-proposta]] · [[08-armonie-due-colori]] · [[09-limite-fantasia]] · [[10-intero]] · [[11-filtro-stagione-capospalla]] · [[12-nessuna-ripetizione]] · [[13-parti-da-capo]] · [[14-colori-consigliati]] |
| Preferiti | [[15-salvare-preferiti]] · [[16-nome-elimina-preferiti]] |
| Dati | [[18-backup-automatico]] · [[19-esporta-zip]] · [[20-importa-zip]] |
| Verifica | [[21-rifinitura-verifica-telefono]] |

## Decisioni (ADR)

[[0001-offline-regole-senza-ai]] · [[0002-android-nativo]] · [[0003-foto-compresse-e-backup]] · [[0004-regole-di-armonia]] · [[0005-min-sdk-26]] · [[0006-filtro-stagione-azzera-gia-viste]] · [[0007-formato-export-zip]] · [[0008-parti-da-capo-ignora-filtro]] · [[0009-import-sostituisce-con-foto-rinominate]] · [[0010-proposta-scade-se-cambiano-i-suoi-capi]] · [[0011-preferito-giudicato-con-i-capi-di-adesso]] · [[0012-schermata-outfit-ridisegnata]] · [[0013-guardaroba-e-scheda-capo-ridisegnati]] · [[0014-preferiti-ridisegnati]] · [[0015-impostazioni-ridisegnate]] · [[0016-correzioni-dalle-ricritiche]]

## Punteggi di design (Impeccable)

| Schermata | Critica | Dopo le modifiche |
|---|---|---|
| Outfit | 24/40 | 29/40 |
| Guardaroba e scheda Capo | 22/40 | 29/40 |
| Preferiti | 23/40 | 29/40 |
| Impostazioni | 24/40 | 32/40 |

Le critiche complete sono in `.impeccable/critique/`.

## Solo tu

- [x] Rinominare le cartelle del percorso senza caratteri speciali (2026-11-22): niente più disco `X:`
- [ ] `npx impeccable login`, opzionale, per lo storico delle critiche nel tuo account
- [ ] Installare sul telefono dopo ogni fase di design: `& "C:\Program Files\Git\bin\bash.exe" scripts/installa-sul-telefono.sh`

## Pianificato dopo la v1

Funzioni decise come **planned**, fuori dalla v1 (vedi [[0001-offline-regole-senza-ai]]). La v1 non contiene codice preparatorio per queste voci.

- [ ] Armocromia personale: stagione cromatica, sottotono, colori che "ti donano"
- [ ] Contesto dell'outfit: meteo, occasione (lavoro/sera/sport), calendario
- [ ] Più utenti / profili
- [ ] Suggerimenti di acquisto ("ti manca un pantalone beige")
- [ ] Stato del Capo (in lavatrice, prestato)
- [ ] Storico "cosa ho indossato quando" e contatore di utilizzi
- [ ] Condivisione degli Outfit
- [ ] Widget, notifiche, outfit del giorno
