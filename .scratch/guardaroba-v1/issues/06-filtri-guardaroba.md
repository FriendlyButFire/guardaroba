# 06: Filtri del Guardaroba

**What to build:** Nella sezione Guardaroba posso filtrare i Capi per Categoria, Colore e Stagione (combinabili) e azzerare i filtri.

**Blocked by:** 03

**Status:** chiuso

- [x] Filtri per Categoria, Colore (principale o secondario) e Stagione
- [x] Il filtro Stagione include i Capi "tutto l'anno"
- [x] Filtri combinabili e azzerabili
- [x] Stato vuoto chiaro quando nessun Capo corrisponde

## Comments

- La regola del filtro Stagione è `Stagione.ammette` nel `:motore`, così il Generatore (filtroStagione) la riusa invece di duplicarla.
- Il filtro Stagione offre solo estivo, invernale e mezza stagione: "tutto l'anno" entra già in ogni filtro, come chip mostrerebbe solo i Capi annuali.
- Filtri a scelta singola per riga; toccare di nuovo la voce la toglie. Lo stato sta nel ViewModel (shortcut: si perde se Android chiude il processo).
- UI verificata solo con `assembleDebug`, non su dispositivo.
