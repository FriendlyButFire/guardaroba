# 08: Armonie a due colori

**What to build:** Le Proposte possono contenere due colori non neutri quando sono monocromatici, analoghi o complementari, e la Spiegazione nomina l'Armonia (es. "blu royal + arancione: complementari"). Prima si fissano le soglie di tinta con una breve ricerca di teoria dei colori. Vedi [[0004-regole-di-armonia]].

**Blocked by:** 07

**Status:** chiuso

- [x] Ricerca sulle soglie (gradi di tinta HSL per monocromatico, analoghi, complementari) da fonti affidabili; valori aggiunti all'ADR-0004
- [x] Al massimo 2 colori non neutri distinti per Outfit
- [x] Le coppie armoniche sono accettate, le altre rifiutate
- [x] Spiegazione per ogni Armonia
- [x] Test JVM con coppie reali della Palette per ogni Armonia, sia valide sia non valide (es. azzurro + blu royal, verde oliva + senape, blu royal + arancione)

## Comments

- L'armonia si calcola sulla tinta **RYB** (ruota del pittore), derivata dall'HSL salvato, non sulla tinta HSL diretta citata nella spec: con l'HSL rosso + verde bosco e giallo + viola verrebbero rifiutati e rosso + turchese passerebbe per complementare. Soglie e motivazione in [[0004-regole-di-armonia]].
- La tolleranza dei complementari (180° ± 30°) fa entrare anche gli split-complementari vicini; la Spiegazione li chiama comunque "complementari".
- La Spiegazione elenca tutti i Colori distinti dell'Outfit, Neutri compresi (es. "blu royal + arancione + nero: complementari"), come già nel ticket 07.
