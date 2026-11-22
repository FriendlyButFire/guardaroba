# 04: Guardaroba: filtri dei soli Capi presenti, Tipo sempre visibile, barra col titolo

**What to build:**
- Il pannello Filtri mostra solo le Categorie e i Colori (principali) dei Capi nel Guardaroba; un filtro già attivo resta visibile anche se il suo ultimo Capo non c'è più, così si può togliere.
- Nella scheda Capo la sezione Tipo c'è sempre: senza Categoria dice "Scegli prima la Categoria", così scegliere la Categoria non sposta la pagina e il tocco successivo non cade sul Colore sbagliato.
- La lista ha una TopAppBar "Guardaroba" come Preferiti e Impostazioni, con il pulsante Filtri sotto.

**Blocked by:** nessuno

**Status:** chiuso

- [x] Opzioni dei filtri ricavate dai Capi, con test
- [x] Sezione Tipo sempre presente nella scheda
- [x] TopAppBar nella lista

## Comments

- Il pannello offre solo i Colori principali (come dice il ticket), ma scelto un Colore il filtro trova ancora anche i Capi che lo hanno come secondario (`Filtro.ammette` invariato): un Colore presente solo come secondario non compare tra le opzioni. Da mettere nell'ADR-0016.
- La Stagione resta con tutte e tre le voci: il ticket parla solo di Categorie e Colori.
- Il segnaposto "Scegli prima la Categoria" è alto 48 dp come una riga di chip; da guardare sull'emulatore con font grande (07) se la pagina salta ancora un poco.
