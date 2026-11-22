# Armonia dei colori: soglie e Palette

Ricerca per [[0004-regole-di-armonia]] e la spec `guardaroba-v1` (Palette, Regole). Il risultato va copiato nell'ADR-0004.

## Decisione

1. Si salva in HSL (serve per i campioni a schermo), ma l'armonia si calcola sulla **tinta RYB** (la ruota del pittore), ottenuta dalla tinta HSL con una mappatura lineare a tratti.
2. Si usa la distanza circolare `d = min(|a-b| mod 360, 360 - |a-b| mod 360)` sulla tinta RYB di due colori non neutri distinti:
   - **monocromatici**: `d <= 12°`
   - **analoghi**: `12° < d <= 75°`
   - **complementari**: `d >= 150°` (180° ± 30°)
   - altrimenti (75° < d < 150°): **non valido**
3. Saturazione e luminosità **non entrano nella regola**. Due Colori diversi con la stessa famiglia di tinta nella Palette differiscono sempre di ΔL >= 21. Grigi e marroni poco saturi sono già Neutri per flag. *shortcut*: se un giorno ci fossero colori personalizzati, aggiungere "monocromatico solo se ΔL >= 15" e "S < 15 → neutro".

## Perché RYB e non HSL diretto

- La ruota HSL/RGB ha primari R, G, B a 0/120/240°, quindi il complementare del rosso è il ciano e quello del blu è il giallo. La ruota RYB tradizionale (Itten, pittori, moda) ha primari rosso, giallo e blu, con le coppie **rosso-verde, blu-arancione, giallo-viola**. Il motivo: la RYB dà molto più spazio ai caldi (rosso→giallo occupa 120° invece di 60°). Fonti: [Wikipedia: Color wheel](https://en.wikipedia.org/wiki/Color_wheel), [Wikipedia: Complementary colors](https://en.wikipedia.org/wiki/Complementary_colors).
- Le guide di moda usano le coppie RYB: "blue and orange, red and green, yellow and purple", e navy + marrone conta come complementare ([Real Men Real Style](https://www.realmenrealstyle.com/color-wheel-menswear/), [Lookiero](https://lookiero.co.uk/blog/colour-wheel-matching-clothes), [Fashion-a-holic](https://fashion-a-holic.com/learn/color-wheel)).
- Prova sulla Palette. Con l'HSL diretto, rosso + verde bosco sono a 135° e verrebbero rifiutati, giallo + viola sarebbero a 135° (rifiutati), mentre rosso + turchese sarebbero a 180° e verrebbero dati per "complementari". Con la RYB le stesse coppie finiscono a 170°, 175° e 148° (rifiutata): coincide con la moda.
- È comunque semplice: una funzione pura con 7 punti di interpolazione, nessuna dipendenza.

```kotlin
// HSL hue -> RYB hue, piecewise linear (red 0, orange 60, yellow 120, green 180, blue 240, violet 300)
private val RYB = listOf(0 to 0, 30 to 60, 60 to 120, 120 to 180, 240 to 240, 300 to 300, 360 to 360)
fun rybHue(h: Double): Double {
    val (a, b) = RYB.zipWithNext().first { (p, q) -> h >= p.first && h <= q.first }
    return a.second + (h - a.first) * (b.second - a.second) / (b.first - a.first)
}
```

## Perché queste soglie

- **Analoghi**: sono colori vicini sulla ruota a 12 tinte (un passo = 30°). La forma classica è base ± 30°, quella "near-analogous" ± 60° ([Wikipedia: Color scheme](https://en.wikipedia.org/wiki/Color_scheme), [Wikipedia: Analogous colors](https://en.wikipedia.org/wiki/Analogous_colors)). Oltre circa 90° l'effetto di armonia si perde ([colorarchive.org](https://colorarchive.org/guides/color-wheel-guide/)). Si sceglie **75°**: 60° più 15° di tolleranza, perché ogni nome della Palette copre una fascia di tinte. Con 60° si perderebbero coppie ovvie nella moda come rosa + lilla (70°) e senape + verde salvia (69°).
- **Monocromatici**: stessa tinta, cambiano chiarezza o saturazione (navy + azzurro, [RMRS](https://www.realmenrealstyle.com/color-wheel-menswear/)). Si sceglie **12°**, meno di mezzo passo della ruota a 12 tinte. Con 15° azzurro + turchese (15°) sarebbero "monocromatici", ma sono analoghi.
- **Complementari**: esattamente opposti (180°). Si sceglie una tolleranza di **±30°** (un passo), così entrano anche gli split-complementari vicini, considerati "meno rischiosi" ([RMRS](https://www.realmenrealstyle.com/color-wheel-menswear/)). Con ±35° entrerebbero rosso + turchese e verde oliva + viola, che stonano.

## Palette (25 Colori)

| id | Neutro | H | S | L | tinta RYB |
|---|---|---|---|---|---|
| nero | sì | 0 | 0 | 5 | - |
| bianco | sì | 0 | 0 | 98 | - |
| panna | sì | 45 | 60 | 92 | - |
| grigio chiaro | sì | 0 | 0 | 78 | - |
| grigio antracite | sì | 210 | 8 | 25 | - |
| beige | sì | 38 | 35 | 78 | - |
| cammello | sì | 33 | 45 | 58 | - |
| marrone | sì | 25 | 50 | 28 | - |
| blu navy | sì | 220 | 60 | 20 | - |
| denim | sì | 215 | 40 | 42 | - |
| rosso | no | 355 | 80 | 48 | 355 |
| bordeaux | no | 345 | 65 | 27 | 345 |
| rosa | no | 345 | 70 | 82 | 345 |
| corallo | no | 8 | 80 | 64 | 16 |
| arancione | no | 25 | 90 | 52 | 50 |
| giallo | no | 50 | 95 | 55 | 100 |
| senape | no | 43 | 75 | 45 | 86 |
| verde oliva | no | 70 | 35 | 35 | 130 |
| verde bosco | no | 130 | 55 | 25 | 185 |
| verde salvia | no | 95 | 20 | 60 | 155 |
| azzurro | no | 205 | 75 | 70 | 222 |
| blu royal | no | 225 | 75 | 45 | 232 |
| turchese | no | 175 | 65 | 45 | 208 |
| viola | no | 275 | 50 | 40 | 275 |
| lilla | no | 275 | 45 | 78 | 275 |

L'HSL dei Neutri serve solo per il campione. Non entra nell'armonia.

## Coppie valide tra i 15 colori (105 coppie: 5 + 35 + 16 valide, 49 rifiutate)

Tra parentesi la distanza RYB.

**Monocromatici (5)**: rosso+bordeaux (10), rosso+rosa (10), bordeaux+rosa (0), azzurro+blu royal (10), viola+lilla (0).

**Analoghi (35)**: rosso+corallo (21), rosso+arancione (55), bordeaux+corallo (31), bordeaux+arancione (65), bordeaux+viola (70), bordeaux+lilla (70), rosa+corallo (31), rosa+arancione (65), rosa+viola (70), rosa+lilla (70), corallo+arancione (34), corallo+senape (70), arancione+giallo (50), arancione+senape (36), giallo+senape (14), giallo+verde oliva (30), giallo+verde salvia (55), senape+verde oliva (44), senape+verde salvia (69), verde oliva+verde bosco (55), verde oliva+verde salvia (25), verde bosco+verde salvia (30), verde bosco+azzurro (38), verde bosco+blu royal (48), verde bosco+turchese (22), verde salvia+azzurro (68), verde salvia+turchese (52), azzurro+turchese (15), azzurro+viola (52), azzurro+lilla (52), blu royal+turchese (25), blu royal+viola (42), blu royal+lilla (42), turchese+viola (68), turchese+lilla (68).

**Complementari (16)**: rosso+verde bosco (170), rosso+verde salvia (160), bordeaux+verde bosco (160), bordeaux+verde salvia (170), rosa+verde bosco (160), rosa+verde salvia (170), corallo+verde bosco (169), corallo+azzurro (154), corallo+turchese (168), arancione+azzurro (172), arancione+blu royal (178), arancione+turchese (158), giallo+viola (175), giallo+lilla (175), senape+viola (171), senape+lilla (171).

## Verifica di buon senso

- Esce giusto: azzurro + blu royal monocromatici, verde oliva + senape analoghi, blu royal + arancione complementari, bordeaux + rosa monocromatici (stessa tinta, ΔL 55), giallo + viola complementari.
- Rifiutate come stonate: rosso + giallo (105), rosso + viola (80), arancione + viola (135), arancione + verde bosco (135), rosso + turchese (148), rosa + giallo (115), verde oliva + viola (145), corallo + blu royal (144).
- Casi limite consapevoli:
  - senape + blu royal (146) viene rifiutata. Senape + blu navy resta sempre valida, perché navy è Neutro.
  - giallo + senape (14) risultano analoghi, non monocromatici. Va bene lo stesso: l'Outfit è comunque valido, cambia solo la Spiegazione.
- Test consigliati (JUnit): una coppia per ogni gruppo, più quattro casi al confine: azzurro + turchese a 15° (analoghi, non monocromatici), corallo + senape a 70° (analoghi), corallo + azzurro a 154° (complementari), rosso + turchese a 148° (rifiutati).
