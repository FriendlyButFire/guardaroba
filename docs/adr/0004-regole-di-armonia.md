# Regole di armonia: neutri liberi, niente triadi

Un Outfit è valido se i Colori principali non neutri (al massimo 2) formano una di queste armonie: nessun colore, un solo colore, monocromatico, analoghi o complementari. In più ammette al massimo un Capo non a tinta unita. Blu navy e denim contano come neutri, come si fa nella moda. Le triadi sono escluse perché in pratica sono troppo azzardate. Colore secondario e Accessori non sono vincolati. Le soglie numeriche (gradi di tinta per "analoghi" e "complementari") si fissano con una ricerca in fase di build e si aggiungono a questo ADR.

## Soglie (ticket 08)

Le soglie e le loro ragioni sono tutte qui sotto. La ricerca con le fonti è in `build-notes/color-harmony.md`, una nota di lavoro locale che non sta nel repo.

- **Tinta RYB, non HSL.** I Colori si salvano in HSL (servono per i campioni), ma l'armonia si calcola sulla tinta della ruota del pittore (rosso, giallo, blu), quella usata nella moda: le coppie complementari sono rosso-verde, blu-arancione, giallo-viola. Con l'HSL diretto rosso + verde bosco e giallo + viola verrebbero rifiutati e rosso + turchese passerebbe per complementare. La tinta RYB si ottiene dalla tinta HSL con una mappatura lineare a tratti: HSL 0, 30, 60, 120, 240, 300, 360 → RYB 0, 60, 120, 180, 240, 300, 360.
- **Distanza** tra due Colori non neutri distinti: $d = \min(|a-b| \bmod 360,\ 360 - |a-b| \bmod 360)$ sulla tinta RYB.
  - monocromatico: $d \le 12°$ (meno di mezzo passo della ruota a 12 tinte)
  - analoghi: $12° < d \le 75°$ (vicini fino a ±60°, più 15° di tolleranza perché ogni nome copre una fascia di tinte)
  - complementari: $d \ge 150°$ (180° ± 30°, così entrano anche gli split-complementari vicini)
  - altrimenti, $75° < d < 150°$: non valido
- **Saturazione e luminosità non entrano nella regola.** Nella Palette fissa due Colori con la stessa tinta differiscono sempre di almeno 21 punti di luminosità, e i grigi e marroni poco saturi sono già Neutri. *shortcut*: con colori personalizzati servirebbero "monocromatico solo se ΔL ≥ 15" e "S < 15 → neutro".
- Sui 15 Colori non neutri escono 105 coppie: 5 monocromatiche, 35 analoghe, 16 complementari, 49 rifiutate (elenco in `ArmoniaTest`).
- La Spiegazione elenca tutti i Colori principali distinti dell'Outfit e poi l'Armonia, es. "blu royal + arancione + nero: complementari".
