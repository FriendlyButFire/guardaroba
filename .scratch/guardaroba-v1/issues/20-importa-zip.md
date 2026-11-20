# 20: Importa il Guardaroba da ZIP

**What to build:** In Impostazioni premo "Importa", scelgo un file ZIP esportato in precedenza, confermo che sostituirà il Guardaroba attuale, e ritrovo esattamente Capi, Preferiti e foto. Se il file non è valido vedo un errore e il Guardaroba attuale resta intatto.

**Blocked by:** 19

**Status:** pronto-per-agente

- [ ] Conferma esplicita prima della sostituzione
- [ ] Validazione completa dell'archivio prima di toccare i dati
- [ ] Sostituzione in un'unica transazione, con ripristino delle foto
- [ ] Test (Robolectric): il giro export → import ripristina dati e foto identici
- [ ] Test: l'import di un file non valido lascia i dati intatti
