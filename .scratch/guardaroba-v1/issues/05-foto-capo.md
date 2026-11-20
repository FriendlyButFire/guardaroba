# 05: Foto del Capo

**What to build:** Nel form del Capo posso scattare una foto (fotocamera) o sceglierla dalla galleria; l'app la ridimensiona a 720 px sul lato lungo in WebP (qualità ~75, rotazione EXIF corretta), conserva solo quella nello storage interno e la mostra come miniatura in griglia. Posso sostituirla o rimuoverla. Vedi [[0003-foto-compresse-e-backup]].

**Blocked by:** 04

**Status:** chiuso

- [x] Fotocamera via intent di sistema, galleria via Photo Picker, nessun permesso di storage
- [x] Si salva solo la versione compressa; l'originale non viene copiato
- [x] Sostituire o rimuovere la foto cancella il file precedente
- [x] Capo senza foto: resta il riquadro del colore
- [x] Test (Robolectric o JVM): da un'immagine grande di prova, foto compressa ≤ 720 px sul lato lungo e ≤ ~60 KB

## Comments

- La foto si salva come nome di file in `Capo.percorsoFoto` (schema v4), relativo a `filesDir/foto/`; l'unico punto che cancella i file è `Foto.elimina`, da riusare in 17 e 20.
- Shortcut: l'EXIF gestisce solo le rotazioni, non le orientazioni specchiate; un file può restare orfano se Android uccide il processo col form aperto e l'utente non ci torna. Il ticket 20 può ripulire i file non usati da nessun Capo.
- Da verificare a mano sul telefono: fotocamera e Photo Picker (non coperti dai test automatici), e il peso reale delle WebP con l'encoder di API 26-29.
