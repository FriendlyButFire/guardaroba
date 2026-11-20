# 05: Foto del Capo

**What to build:** Nel form del Capo posso scattare una foto (fotocamera) o sceglierla dalla galleria; l'app la ridimensiona a 720 px sul lato lungo in WebP (qualità ~75, rotazione EXIF corretta), conserva solo quella nello storage interno e la mostra come miniatura in griglia. Posso sostituirla o rimuoverla. Vedi [[0003-foto-compresse-e-backup]].

**Blocked by:** 04

**Status:** pronto-per-agente

- [ ] Fotocamera via intent di sistema, galleria via Photo Picker, nessun permesso di storage
- [ ] Si salva solo la versione compressa; l'originale non viene copiato
- [ ] Sostituire o rimuovere la foto cancella il file precedente
- [ ] Capo senza foto: resta il riquadro del colore
- [ ] Test (Robolectric o JVM): da un'immagine grande di prova, foto compressa ≤ 720 px sul lato lungo e ≤ ~60 KB
