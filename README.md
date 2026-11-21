# Guardaroba

Personal offline Android app that suggests colour-matched outfits from the clothes you own. See [[GLOSSARY]], `docs/adr/` and `.scratch/guardaroba-v1/spec.md`.

Modules:
- `:motore`: Motore abbinamenti, pure Kotlin/JVM with no Android dependencies. Tests: `motore/src/test/kotlin`, plain JUnit.
- `:app`: the Android app (Compose + Material 3). Tests: `app/src/test/java`, JUnit + Robolectric (pinned to SDK 36 in `app/src/test/resources/robolectric.properties`).

## Build

The repo path contains `Π`. Launched from that path, `./gradlew` fails with `ClassNotFoundException: GradleWrapperMain`, because Windows hands non-ASCII arguments to `java.exe` in the ANSI code page. **Always build from a `subst` drive** that points at the repo's ASCII 8.3 short path. It creates no files, and it lasts until logout or reboot.

```bash
# once per login (no-op if X: already exists); X: = repo root, worktrees are X:/.worktrees/NN
[ -d /x ] || cmd //c "subst X: $(cygpath -d "/c/Users/<utente>/src/guardaroba")"

export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"   # JDK 21; system java is 1.8
export ANDROID_HOME="$LOCALAPPDATA/Android/Sdk"
cd /x                      # or /x/.worktrees/NN inside a worktree

./gradlew :motore:test testDebugUnitTest assembleDebug   # all tests + debug APK
./gradlew :motore:test --tests "com.guardaroba.motore.CategoriaTest"
./gradlew testDebugUnitTest --tests "com.guardaroba.ManifestTest"
```

- APK: `app/build/outputs/apk/debug/app-debug.apk`. Test reports: `*/build/reports/tests/`.
- `local.properties` is git-ignored; each checkout or worktree needs one: `sdk.dir=C\:\\Users\\<utente>\\AppData\\Local\\Android\\Sdk` (the backslashes must be doubled).
- `android.overridePathCheck=true` in `gradle.properties` lets Android Studio open the project from its real path. AGP prints an "experimental option" warning because of it; ignore the warning.
- On a phone: `bash scripts/installa-sul-telefono.sh` is a step-by-step wizard (USB debugging, `adb install`, sideload alternative, `bmgr` backup/restore check).
- `subst` must get the short path (`cygpath -d`): the `Π` in a `cmd` argument gets mangled too. To remove the drive: `cmd //c "subst X: /d"`.
