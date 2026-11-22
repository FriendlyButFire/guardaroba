# Release firmata con la chiave di debug

La build release (`./gradlew assembleRelease`) è firmata con la chiave di debug di questo PC (`signingConfigs.debug`). L'utente l'ha scelta il 2026-11-22 per installare la release sul proprio telefono: con lo stesso certificato della debug si installa sopra e i dati restano. Una chiave nuova avrebbe obbligato a disinstallare, cioè a esportare e reimportare il Guardaroba.

La release non è minificata (niente R8), quindi niente regole ProGuard per Room e Compose.

Per pubblicarla su uno store serve un keystore vero, con password custodita dall'utente. Cambiare chiave dopo vuol dire disinstallare l'app sui telefoni che hanno questa: prima Esporta, poi Importa.
