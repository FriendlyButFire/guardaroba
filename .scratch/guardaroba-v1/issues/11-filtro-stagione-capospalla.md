# 11: Filtro Stagione e Capospalla in Genera

**What to build:** Prima di generare scelgo un filtro Stagione (nessuno / estivo / invernale / mezza stagione): entrano i Capi di quella Stagione e quelli "tutto l'anno". Con invernale o mezza stagione la Proposta include un Capospalla compatibile, se ne ho uno.

**Blocked by:** 03, 07

**Status:** chiuso

- [x] Filtro Stagione in UI e nella richiesta Genera
- [x] "Tutto l'anno" entra in ogni filtro; le altre Stagioni solo nella propria
- [x] Capospalla aggiunto solo con invernale/mezza stagione e solo se rispetta le regole
- [x] Senza Capospalla compatibile la Proposta resta valida senza
- [x] Test JVM su filtro e Capospalla

## Comments

- Il filtro si applica prima di tutto: se esclude tutte le Scarpe (o Sopra/Sotto/Intero), l'Esito è MancaCategoria per quella Categoria.
- Con invernale/mezza stagione ogni Capospalla compatibile dà una Proposta a sé; l'Outfit senza Capospalla si propone solo se nessuno è compatibile.
- Cambiare filtro azzera le Proposte già viste: [[0006-filtro-stagione-azzera-gia-viste]].
- Verifica manuale della UI su emulatore/telefono ancora da fare.
