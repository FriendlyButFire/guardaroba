# 12: Nessuna ripetizione ed esaurimento

**What to build:** "Un altro" non ripete Proposte già viste nella sessione; quando le ho viste tutte l'app me lo dice e posso ricominciare. Se i Capi ci sono ma nessuna combinazione è valida, vedo "Nessun abbinamento possibile".

**Blocked by:** 07

**Status:** chiuso

- [x] Un Outfit è identificato dall'insieme degli id dei suoi Capi
- [x] Esito Esaurite quando tutte le combinazioni valide sono già state viste; "ricomincia" azzera la sessione
- [x] Esito NessunAbbinamento distinto da MancaCategoria
- [x] Proposte casuali tra le valide (seed nei test)
- [x] Test JVM: con N combinazioni valide si ottengono N Proposte distinte, poi Esaurite

## Comments

- La logica (giàViste, Esaurite, NessunAbbinamento, Ricomincia) era già arrivata con #07 e #11; qui solo i test che la verificano. Cambiare filtro Stagione azzera le già viste: [[0006-filtro-stagione-azzera-gia-viste]].
- Verifica manuale della schermata Outfit (Ricomincia, messaggi) su emulatore/telefono ancora da fare.
