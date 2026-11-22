# Handoff: sessione 2026-11-21

Stato a colpo d'occhio: [[roadmap]]. Branch `guardaroba-v1` (non ancora su `master`, niente push). Tutti i 21 ticket chiusi e verificati sul telefono.

## Dove siamo
Fase **design con Impeccable**: tutte e quattro le schermate criticate e sistemate (ADR-0012 → 0015).
- Preferiti: provati sul telefono dall'utente, ok.
- Impostazioni: critica 24/40, 5 modifiche fatte e committate (`eb28ad7`, [[0015-impostazioni-ridisegnate]]). **Da provare sul telefono** (ricompilare: y): Esporta → Importa → Annulla / "Ripristina il Guardaroba di prima", "Controlla il backup del telefono", "Come scegliamo gli abbinamenti".
- Scorrimento al ritorno dalle regole: corretto e verificato sull'emulatore (stato del backup calcolato sopra l'if, come lo scorrimento).
- Poi: ricritica di Outfit, Guardaroba, Preferiti, Impostazioni → `/retro` → merge su `master` (solo su richiesta).
- Script di installazione corretto: build giudicata dall'esito di Gradle; con un emulatore acceso sceglie da solo il telefono vero.

## Regole emerse in questa sessione (rispettarle)
- **Telefono dell'utente (serial <serial-del-telefono>):** mai `adb` verso il telefono senza un sì esplicito. Ogni comando adb porta `-s emulator-5554`. Le installazioni sul telefono le fa l'utente con `scripts/installa-sul-telefono.sh`, da PowerShell: `& "C:\Program Files\Git\bin\bash.exe" scripts/installa-sul-telefono.sh`.
- **Mai scrivere fuori dal repo.** Gli agenti usano solo il proprio worktree; scratch in `build/verifica/`. `/x` (subst del repo, serve per Gradle per via della «Π») solo in Bash; nei file tool diventa `C:\x`.
- **Niente Monitor** sui file esterni (consuma troppi token): un solo controllo `find -newermt` dopo ogni agente.
- **Roadmap** aggiornata e committata a ogni fase principale finita.
- **L'utente scrive in italiano:** rispondere in italiano.
- Worktree ancora presenti sul disco da cancellare con `rm -rf` dal disco `/x/.worktrees` (percorsi troppo lunghi per git).
- Per i fotogrammi dei video dell'utente: `ffmpeg` → `build/verifica/video/`, e il file originale non si sposta.

## Note utili
- Brief comune per gli implementer: `build-notes/implementer-brief.md` (gitignored).
- Impeccable: plugin installato, `PRODUCT.md` (platform android), critiche in `.impeccable/critique/`. Per le piattaforme native solo Assessment A, senza detector.
- Emulatore: AVD `Pixel_8_Pro`, `-no-window -no-audio`; dati di prova: t-shirt rossa in tinta unita, gonna nera, sandali neri, più quelli che l'agente dei Preferiti aggiunge per i test.
- Decisioni utente recenti: Preferito "Non più valido" ([[0011-preferito-giudicato-con-i-capi-di-adesso]]); popup fuori stagione ([[0008-parti-da-capo-ignora-filtro]]); "Salva e aggiungi" + "Duplica" approvati.
