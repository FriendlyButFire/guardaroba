# Guardaroba

Personal offline Android app that suggests colour-matched outfits from the clothes you own. See [[GLOSSARY]], `docs/adr/` and `.scratch/guardaroba-v1/spec.md`.

Modules:
- `:motore`: Motore abbinamenti, pure Kotlin/JVM with no Android dependencies. Tests: `motore/src/test/kotlin`, plain JUnit.
- `:app`: the Android app (Compose + Material 3). Tests: `app/src/test/java`, JUnit + Robolectric (pinned to SDK 36 in `app/src/test/resources/robolectric.properties`).

## Build

Build straight from the repo (or a worktree). The path must stay ASCII-only: any non-ASCII character in it breaks `./gradlew` on Windows (`ClassNotFoundException: GradleWrapperMain`, because `java.exe` gets its arguments in the ANSI code page), and it breaks AGP too.

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"   # JDK 21; system java is 1.8
export ANDROID_HOME="$LOCALAPPDATA/Android/Sdk"
cd "/c/Users/<utente>/src/guardaroba"   # or .worktrees/NN

./gradlew :motore:test testDebugUnitTest assembleDebug   # all tests + debug APK
./gradlew :motore:test --tests "com.guardaroba.motore.CategoriaTest"
./gradlew testDebugUnitTest --tests "com.guardaroba.ManifestTest"
```

- APK: `app/build/outputs/apk/debug/app-debug.apk`. Test reports: `*/build/reports/tests/`.
- `local.properties` is git-ignored; each checkout or worktree needs one: `sdk.dir=C\:\\Users\\<utente>\\AppData\\Local\\Android\\Sdk` (the backslashes must be doubled).
- On a phone: `bash scripts/installa-sul-telefono.sh` is a step-by-step wizard (USB debugging, `adb install`, sideload alternative, `bmgr` backup/restore check).