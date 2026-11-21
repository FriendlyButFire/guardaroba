# 14: Colori consigliati

**What to build:** In "Parti da questo capo" vedo l'elenco dei Colori della Palette che starebbero bene con il Capo di partenza (con l'Armonia accanto), anche quando non c'è nessun abbinamento possibile con i miei Capi.

**Blocked by:** 08, 13

**Status:** chiuso

- [x] `coloriConsigliati(capo)` nel Motore: Neutri + Colori in Armonia con il Colore principale
- [x] Mostrati con campione, nome e Armonia
- [x] Visibili anche con "Nessun abbinamento per questo capo"
- [x] Test JVM: l'elenco atteso per un Colore Neutro e per uno non neutro

## Comments

- `coloriConsigliati` restituisce `Consiglio(colore, regola)`, non solo Colori: la regola usa il vocabolario della Spiegazione ("un colore + neutri", "monocromatico"…). Il Colore stesso del Capo è escluso (ADR-0004, sezione ticket 14).
- Da verificare a mano sul telefono: lo scorrimento della schermata Outfit con l'elenco lungo (24 Colori per un Neutro).
