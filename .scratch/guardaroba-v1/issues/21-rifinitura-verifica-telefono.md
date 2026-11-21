# 21: Rifinitura e verifica sul telefono

**What to build:** Un passaggio su tutte le schermate per coerenza (testi in italiano, stati vuoti, errori, tema chiaro/scuro) e l'installazione dell'APK sul telefono dell'utente, con una guida `/wizard` per i passi che solo l'utente può fare.

**Blocked by:** 01–20

**Status:** chiuso

- [x] Tutte le storie utente della [[spec]] verificate a mano su emulatore
- [x] Testi tutti in italiano, stati vuoti presenti in ogni sezione
- [x] Wizard per installare l'APK sul telefono (debug USB / sideload)
- [ ] Nota: dopo aver visto il risultato, l'utente valuta se usare Impeccable (`Platform: android`) per critique/polish

## Comments

- Verifica su emulatore `Pixel_8_Pro` (Android 17), tema chiaro e scuro, con `adb input` / `uiautomator`. Storie 1–55 e 57–67 tutte provate: form completo, foto da galleria e da fotocamera (WebP 3–10 KB, scatto in cache cancellato), rimuovi foto, filtri e stato "nessun capo", Genera / un altro / Esaurite / Ricomincia, Capospalla solo con invernale, Parti da (anche Accessorio) con Colori consigliati lunghi che scorrono, Nessun abbinamento (Genera e Parti da), Ti manca/mancano, Preferiti (salva con nome, rinomina, elimina, miniature, Spiegazione), elimina Capo con avviso 0/1/2 Preferiti e Annulla, avvisi backup 80% e 100% (simulati con un file da 21/26 MB in `files/foto`), Esporta via SAF, Importa con conferma/Annulla, file non valido ("non è cambiato niente") e import valido. Questo chiude anche le verifiche manuali rimaste aperte in 12–17, 19 e 20.
- Trovato e corretto un crash: salvare una Proposta con un Capo eliminato nel frattempo violava la foreign key. Ora `PreferitoDao.salva` non salva e restituisce null, e la Proposta mostrata scade quando cambiano i suoi Capi: [[0010-proposta-scade-se-cambiano-i-suoi-capi]].
- Ritocchi ai testi di Outfit: etichetta "Filtro Stagione"; per MancaCategoria, il suggerimento dell'Intero e "o cambia filtro Stagione"; "colori e motivi" in Nessun abbinamento; "Premi Ricomincia per rivederle". Ritoccare il filtro già scelto non azzera più le già viste.
- **Ancora aperto (solo l'utente):** l'installazione sul telefono e la verifica `bmgr` del backup di #18, che richiede un account Google. Si fa con `bash scripts/installa-sul-telefono.sh`, che chiede di disinstallare solo dopo un `backupnow` riuscito.
- Non verificati: il percorso d'errore di Esporta (scrittura che fallisce a metà, difficile da provocare sull'emulatore) e la ricreazione del processo a form aperto.
- Le ultime correzioni della review (il suggerimento dell'Intero, il nuovo controllo su `salva`) sono verificate dai test e dalla build, non sull'emulatore.
- Impeccable: è una scelta dell'utente dopo aver visto l'app sul telefono, e resta aperta.
