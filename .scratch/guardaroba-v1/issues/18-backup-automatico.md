# 18: Backup automatico e stato in Impostazioni

**What to build:** Guardaroba, Preferiti e foto rientrano nel backup automatico di Android. In Impostazioni vedo la dimensione attuale rispetto ai 25 MB, un avviso oltre l'80% e un avviso chiaro se il limite è raggiunto e il backup è fermo. Vedi [[0003-foto-compresse-e-backup]].

**Blocked by:** 05

**Status:** chiuso

- [x] Regole di backup che includono database e cartella foto ed escludono la cache (sia formato legacy sia data-extraction-rules)
- [x] Impostazioni mostrano MB usati / 25 MB e numero di foto
- [x] Avviso a partire dall'80%, avviso di backup fermo dal 100%
- [x] Verifica manuale con `adb shell bmgr`, documentata nel ticket alla chiusura

## Comments

- Il contenuto delle regole (`res/xml/backup_regole.xml`, `backup_estrazione.xml`) è verificato da `BackupTest`; la cache è fuori sia perché ci sono solo `include` sia perché Android non salva mai `cacheDir`.
- La dimensione è una stima (somma dei file, senza l'overhead dell'archivio di Android): "backup fermo" è dedotto dai byte, non chiesto ad Android.
- Il testo degli avvisi rimanda all'export ZIP, che arriva con #19.
- **Ancora aperta: verifica manuale su dispositivo** (serve un telefono o emulatore con account Google). Passi:
  ```bash
  adb shell bmgr enabled                      # deve dire "enabled"
  adb shell bmgr backupnow com.guardaroba     # backup immediato
  adb shell dumpsys backup | grep -A3 com.guardaroba
  adb uninstall com.guardaroba && adb install app/build/outputs/apk/debug/app-debug.apk
  # alla reinstallazione Guardaroba, Preferiti e foto devono riapparire; scatti in cache no
  ```

- 2026-11-21: l'utente ha eseguito `scripts/installa-sul-telefono.sh` sul proprio Pixel 8 Pro (installazione + verifica `bmgr`) e conferma che funziona tutto.
