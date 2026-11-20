# 01: Scheletro dell'app

**What to build:** Un'app Android installabile che si apre con una barra in basso a 4 sezioni (Guardaroba, Outfit, Preferiti, Impostazioni), ognuna con una schermata vuota segnaposto. Kotlin + Jetpack Compose + Material 3, tema chiaro/scuro di sistema, testi in italiano. Vedi [[spec]], [[0002-android-nativo]].

**Blocked by:** None (can start immediately)

**Status:** pronto-per-agente

- [ ] Il progetto Gradle compila un APK debug da riga di comando
- [ ] La barra inferiore naviga tra le 4 sezioni
- [ ] Il tema segue chiaro/scuro di sistema (Material 3)
- [ ] Il manifest non dichiara il permesso INTERNET
- [ ] Esiste un'area Kotlin pura per il Motore abbinamenti con un test JVM che passa
- [ ] `minSdk` scelto e registrato in un nuovo ADR
- [ ] `.gitignore` copre gli output di build Android/Gradle
