# 12: Nessuna ripetizione ed esaurimento

**What to build:** "Un altro" non ripete Proposte già viste nella sessione; quando le ho viste tutte l'app me lo dice e posso ricominciare. Se i Capi ci sono ma nessuna combinazione è valida, vedo "Nessun abbinamento possibile".

**Blocked by:** 07

**Status:** pronto-per-agente

- [ ] Un Outfit è identificato dall'insieme degli id dei suoi Capi
- [ ] Esito Esaurite quando tutte le combinazioni valide sono già state viste; "ricomincia" azzera la sessione
- [ ] Esito NessunAbbinamento distinto da MancaCategoria
- [ ] Proposte casuali tra le valide (seed nei test)
- [ ] Test JVM: con N combinazioni valide si ottengono N Proposte distinte, poi Esaurite
