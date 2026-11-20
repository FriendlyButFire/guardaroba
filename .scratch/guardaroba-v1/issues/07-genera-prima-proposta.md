# 07: Genera: prima Proposta

**What to build:** Nella sezione Outfit premo "Genera" e ricevo una Proposta fatta con i miei Capi (Sopra + Sotto + Scarpe) con la sua Spiegazione; "un altro" ne propone un'altra. In questo ticket il Motore accetta solo Outfit di soli Neutri o di Neutri + 1 colore. Se manca una Categoria necessaria vedo "Ti mancano …".

**Blocked by:** 02

**Status:** pronto-per-agente

- [ ] Motore (Kotlin puro): `proponi(guardaroba, richiesta, giàViste) → Esito` come da [[spec]], con caso a seed iniettabile
- [ ] Outfit = Sopra + Sotto + Scarpe; Accessori mai aggiunti in Genera
- [ ] Regole: solo Neutri valido; Neutri + 1 colore non neutro valido; ≥ 2 colori non neutri non valido (per ora)
- [ ] Spiegazione per i due casi (es. "blu navy + beige: neutri")
- [ ] Esito MancaCategoria con le Categorie mancanti, mostrato in UI
- [ ] Schermata Outfit con miniature dei Capi, Spiegazione e "un altro"
- [ ] Test JVM per ciascuna regola del Motore
