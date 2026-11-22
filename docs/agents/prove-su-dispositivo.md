# Prove su dispositivo

Per provare l'app su un emulatore o sul telefono, a mano o con un agente.

## Dove

- **Emulatore** `Pixel_8_Pro` (`emulator-5554`) di default: niente notifiche, niente dati dell'utente.
- **Telefono dell'utente** (serial `<serial-del-telefono>`) solo con un sì esplicito, valido per quella volta. Il telefono è personale: le notifiche in arrivo rubano i tap (una volta un tap è finito su WhatsApp).

## Strumenti

- **mobile-mcp** (server `mobile` in `.mcp.json`, fissato alla 1.0.17: la 1.0.18 non parte). Leggi lo schermo con `mobile_list_elements_on_screen`; `at=x,y` è l'angolo in alto a sinistra, tocca al centro (`x+w/2`, `y+h/2`). Screenshot solo per giudicare l'aspetto.
- Sul telefono, **prima di ogni tap** controlla che in primo piano ci sia `com.guardaroba` e che non ci sia una notifica a comparsa:
  `adb shell dumpsys window | grep mCurrentFocus` e `adb shell dumpsys activity service com.android.systemui/.SystemUIService | grep -m1 hasPinnedHeadsUpRow` (deve dire `false`). I menu ⋮ sono una finestra `popup`: vale se `mFocusedApp` è `com.guardaroba`. Altrimenti fermati.
- Lo schermo si spegne dopo 60 s e compare il blocco (il PIN lo mette solo l'utente): `adb shell svc power stayon usb` durante la prova, `adb shell svc power stayon false` alla fine.
- Ogni comando `adb` porta `-s <serial>`: con emulatore e telefono collegati insieme, senza serial adb fallisce.

## Dati di prova

`scripts/dati-di-prova.zip`: 14 Capi (tutte le Categorie, le quattro Stagioni, un Intero, un Capospalla, Motivi diversi, due Accessori) e 2 Preferiti, "Mare" (solo estivo) e uno senza nome (mezza stagione). Il filtro Invernale dei Preferiti mostra lo stato vuoto.

- Caricarli: `adb -s <serial> push scripts/dati-di-prova.zip /sdcard/Download/`, poi Impostazioni → Importa. Sostituisce il Guardaroba: sul telefono dell'utente prima Esporta, o usa "Ripristina il Guardaroba di prima" alla fine.
- Modificarli: cambia `scripts/dati-di-prova/guardaroba.json` (formato in [[0007-formato-export-zip]]), poi ricrea lo zip:
  `cd scripts && python -c "import zipfile; zipfile.ZipFile('dati-di-prova.zip','w',zipfile.ZIP_DEFLATED).write('dati-di-prova/guardaroba.json','guardaroba.json')"`
  `ImportazioneTest` controlla che lo zip contenga il JSON e che si importi con Preferiti validi.

## Installare

`./gradlew assembleDebug`, poi `adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk`. Per l'utente c'è il wizard `scripts/installa-sul-telefono.sh`.
