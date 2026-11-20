# 18: Backup automatico e stato in Impostazioni

**What to build:** Guardaroba, Preferiti e foto rientrano nel backup automatico di Android. In Impostazioni vedo la dimensione attuale rispetto ai 25 MB, un avviso oltre l'80% e un avviso chiaro se il limite è raggiunto e il backup è fermo. Vedi [[0003-foto-compresse-e-backup]].

**Blocked by:** 05

**Status:** pronto-per-agente

- [ ] Regole di backup che includono database e cartella foto ed escludono la cache (sia formato legacy sia data-extraction-rules)
- [ ] Impostazioni mostrano MB usati / 25 MB e numero di foto
- [ ] Avviso a partire dall'80%, avviso di backup fermo dal 100%
- [ ] Verifica manuale con `adb shell bmgr`, documentata nel ticket alla chiusura
