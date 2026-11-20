# 17: Eliminare un Capo

**What to build:** Dalla scheda di un Capo posso eliminarlo; se compare in Preferiti vedo un avviso con quanti Preferiti verranno eliminati e posso annullare. Confermando, spariscono il Capo, la sua foto e quei Preferiti.

**Blocked by:** 04, 15

**Status:** pronto-per-agente

- [ ] Avviso con il numero di Preferiti coinvolti (conferma semplice se sono zero)
- [ ] Annulla non tocca nulla
- [ ] La conferma elimina il Capo, il file della foto e i Preferiti che lo contengono
- [ ] Test (Room in memoria, Robolectric): eliminare un Capo elimina in cascata i suoi Preferiti e lascia intatti gli altri
