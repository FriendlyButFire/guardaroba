# 10: Intero al posto di Sopra + Sotto

**What to build:** Le Proposte possono essere fatte con un Intero (vestito, tuta) + Scarpe al posto di Sopra + Sotto + Scarpe; MancaCategoria tiene conto di questa alternativa.

**Blocked by:** 07

**Status:** chiuso

- [x] Outfit validi sia con Sopra + Sotto sia con Intero, mai entrambi insieme
- [x] Con solo Interi e Scarpe non si segnala MancaCategoria per Sopra/Sotto
- [x] Test JVM per entrambe le composizioni

## Comments

- MancaCategoria: Sopra/Sotto sono segnalati solo se nel Guardaroba non c'è nemmeno un Intero; il messaggio non suggerisce l'Intero come alternativa (es. Guardaroba vuoto → "Sopra, Sotto, Scarpe", come prima). Con Intero + Sopra senza Sotto, se nessun Intero armonizza l'Esito è NessunAbbinamento, non MancaCategoria.
- Armonia e limite di un Capo a fantasia contano anche l'Intero (girano su tutti i Capi dell'Outfit).
