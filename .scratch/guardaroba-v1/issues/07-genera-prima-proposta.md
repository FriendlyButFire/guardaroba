# 07: Genera: prima Proposta

**What to build:** Nella sezione Outfit premo "Genera" e ricevo una Proposta fatta con i miei Capi (Sopra + Sotto + Scarpe) con la sua Spiegazione; "un altro" ne propone un'altra. In questo ticket il Motore accetta solo Outfit di soli Neutri o di Neutri + 1 colore. Se manca una Categoria necessaria vedo "Ti mancano …".

**Blocked by:** 02

**Status:** chiuso

- [x] Motore (Kotlin puro): `proponi(guardaroba, richiesta, giàViste) → Esito` come da [[spec]], con caso a seed iniettabile
- [x] Outfit = Sopra + Sotto + Scarpe; Accessori mai aggiunti in Genera
- [x] Regole: solo Neutri valido; Neutri + 1 colore non neutro valido; ≥ 2 colori non neutri non valido (per ora)
- [x] Spiegazione per i due casi (es. "blu navy + beige: neutri")
- [x] Esito MancaCategoria con le Categorie mancanti, mostrato in UI
- [x] Schermata Outfit con miniature dei Capi, Spiegazione e "un altro"
- [x] Test JVM per ciascuna regola del Motore

## Comments

- `proponi` già filtra `giàViste` e restituisce `Esaurite` / `NessunAbbinamento` (servivano per avere un Esito sensato); la UI ha già "Ricomincia". Il ticket 12 resta per verificarlo con il test "N combinazioni → N Proposte distinte, poi Esaurite" e rifinire i messaggi.
- `:motore` ha un suo `Capo` minimo (id, categoria, colorePrincipale); `OutfitViewModel` mappa le entità Room con `perMotore()`.
- Verifica manuale della schermata Outfit su emulatore/telefono ancora da fare.
