# 02: Aggiungere un Capo e vederlo nel Guardaroba

**What to build:** Dalla sezione Guardaroba premo "+", scelgo Categoria → Tipo dalla lista fissa e il Colore principale dalla Palette (25 Colori con nome e campione, Neutri marcati), salvo, e il Capo appare nella griglia raggruppata per Categoria come riquadro del suo colore. I dati restano dopo il riavvio dell'app.

**Blocked by:** 01

**Status:** chiuso

- [x] Palette di 25 Colori come da [[spec]], con id stabile, nome, HSL e flag Neutro
- [x] Categorie e Tipi come da [[spec]]
- [x] Capo salvato in Room (si salvano gli id dei Colori, non i valori)
- [x] Griglia raggruppata per Categoria, riquadro del Colore principale
- [x] Il Capo persiste dopo il riavvio
- [x] Test JVM: la Palette ha 25 Colori con 10 Neutri e id unici

## Comments

- Palette, Categorie e Tipi stanno in `:motore` (`Palette.kt`, `Categoria.kt`); id stabili in snake_case (`blu_navy`), il nome è l'id con gli spazi. La Room entity `Capo` è in `:app` (`archivio/`), schema v1 esportato in `app/schemas/`: un campo nuovo = version+1 e una AutoMigration con `@ColumnInfo(defaultValue = ...)`.
- Nel form i Neutri sono marcati raggruppandoli sotto l'intestazione "Neutri", separati dai "Colori".
- La persistenza è coperta da `CapoDaoTest` (database su file, chiuso e riaperto). Verificato a mano su emulatore (Pixel_8_Pro): form → Salva → riquadro blu navy sotto "Sopra", ancora presente dopo force-stop e riavvio. Telefono fisico non verificato.
