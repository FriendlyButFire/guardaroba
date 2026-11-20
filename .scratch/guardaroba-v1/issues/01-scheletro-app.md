# 01: Scheletro dell'app

**What to build:** Un'app Android installabile che si apre con una barra in basso a 4 sezioni (Guardaroba, Outfit, Preferiti, Impostazioni), ognuna con una schermata vuota segnaposto. Kotlin + Jetpack Compose + Material 3, tema chiaro/scuro di sistema, testi in italiano. Vedi [[spec]], [[0002-android-nativo]].

**Blocked by:** None (can start immediately)

**Status:** chiuso

- [x] Il progetto Gradle compila un APK debug da riga di comando
- [x] La barra inferiore naviga tra le 4 sezioni
- [x] Il tema segue chiaro/scuro di sistema (Material 3)
- [x] Il manifest non dichiara il permesso INTERNET
- [x] Esiste un'area Kotlin pura per il Motore abbinamenti con un test JVM che passa
- [x] `minSdk` scelto e registrato in un nuovo ADR
- [x] `.gitignore` copre gli output di build Android/Gradle

## Comments

- Build: `./gradlew` non parte dal percorso con `Π` (argomenti a `java.exe` nel code page ANSI). Si builda da un drive `subst X:` sul percorso corto 8.3; comandi esatti nel `README.md`.
- `compileSdk` 37 (richiesto da `androidx.core` 1.19), `targetSdk` 36 (limite di Robolectric), `minSdk` 26 ([[0005-min-sdk-26]]).
- Navigazione tra sezioni senza libreria (stato `rememberSaveable`): si aggiunge navigation-compose quando una sezione avrà sotto-schermate.
- Verifica manuale su emulatore/telefono ancora da fare (tema chiaro/scuro, barra inferiore): l'APK compila.
