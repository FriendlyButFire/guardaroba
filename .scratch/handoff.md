# Handoff: sessione 2026-11-21

Stato a colpo d'occhio: [[roadmap]]. Branch `guardaroba-v1` (non ancora su `master`, niente push). Tutti i 21 ticket chiusi e verificati sul telefono.

## Dove siamo
Fase **design con Impeccable**, una schermata alla volta: critica → l'utente sceglie → un agente applica in sequenza in un worktree → merge → l'utente prova sul telefono → aggiornare `docs/roadmap.md` e committare.
- Outfit: critica 24/40, 5 modifiche fatte ([[0012-schermata-outfit-ridisegnata]]).
- Guardaroba + scheda Capo: critica 22/40, 5 modifiche fatte ([[0013-guardaroba-e-scheda-capo-ridisegnati]]), più il bug del pannello Filtri che rimbalzava (commit `91db528`, difetto M3 sugli inset; corretto con il padding Top `safeDrawing`).
- **In corso:** un agente in background sta facendo la critica dei **Preferiti** (`PreferitiScreen.kt`). Restituisce il resoconto in italiano; lo snapshot va in `.impeccable/critique/`. Da fare: presentarlo all'utente con le domande finali di critique.md (priorità, aspetto, quantità).
- Poi: Impostazioni, ricritica di Outfit e Guardaroba, retro, merge su `master` (solo su richiesta).

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
