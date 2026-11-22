# 03: Campi completi del Capo

**What to build:** Nel form del Capo posso indicare anche Colore secondario (opzionale), Motivo (tinta unita / righe / quadri / fantasia), Stagione (estivo / invernale / mezza stagione / tutto l'anno) e Taglia (testo libero opzionale); li vedo nella scheda del Capo.

**Blocked by:** 02

**Status:** chiuso

- [x] Campi aggiunti allo schema (migrazione Room, oppure schema ricreato se non esistono ancora dati reali: annotarlo)
- [x] Default: Motivo tinta unita, Stagione tutto l'anno
- [x] La scheda del Capo mostra tutti i campi
- [x] La griglia mostra anche il Colore secondario nel riquadro

## Comments

- Schema Room v2 con `AutoMigration(1→2)` (non ricreato): i default `TINTA_UNITA` / `TUTTO_L_ANNO` sono `@ColumnInfo(defaultValue)`, i Capi v1 restano. `MigrazioneTest` crea a mano un file v1 e lo apre con Room (MigrationTestHelper non vede gli schemi sotto Robolectric).
- `Motivo` e `Stagione` sono enum in `:motore` (li userà il Motore), salvati per nome come `Categoria`.
- Non esiste ancora una scheda di un Capo esistente: qui la "scheda" è il form, che mostra tutti i campi. Il ticket 04 dovrà dare a `CapoForm` i valori iniziali del Capo.
- Nel form, toccare di nuovo il Colore secondario scelto lo toglie. In griglia il Colore secondario è una fascia in basso nel riquadro.
- UI verificata solo con `assembleDebug`, non a mano su emulatore.
