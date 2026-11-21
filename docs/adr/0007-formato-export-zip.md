# Formato dell'export ZIP: un JSON con versione più le foto

ADR-0003 parla di "database + foto". L'export però non copia il file Room: scrive `guardaroba.json` con `versione` (oggi 1), `capi` e `preferiti`, più una voce `foto/<nome>` per ogni foto. Un JSON non dipende dallo schema Room né dalle sue migrazioni, quindi l'import può validarlo campo per campo prima di toccare i dati. Ogni campo ha il nome della proprietà dell'entità (`Capo`, `Preferito`), gli enum vanno come `name`, i colori come id della Palette, e ogni Preferito elenca gli id dei suoi Capi in `capi`. Se cambia la struttura, `versione` sale.

Un Capo la cui foto non è più sul disco esce con `percorsoFoto = null` e l'export riesce lo stesso: la foto non si può recuperare comunque, e così ogni nome nel JSON ha il suo file nello ZIP. Se l'export fallisce o viene interrotto, il file a metà viene cancellato e l'app mostra un errore.
