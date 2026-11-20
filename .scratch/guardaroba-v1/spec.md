# Spec: Guardaroba v1

Status: pronto-per-agente

Riferimenti: [[GLOSSARY]], [[0001-offline-regole-senza-ai]], [[0002-android-nativo]], [[0003-foto-compresse-e-backup]], [[0004-regole-di-armonia]], [[roadmap]]

## Problem Statement

Ogni mattina scelgo cosa mettermi tra i capi che ho, ma non so bene quali colori stiano bene insieme. Finisco per indossare sempre le stesse due o tre combinazioni "sicure", oppure abbino colori che stonano. Non conosco abbastanza la teoria dei colori per ragionarci da solo, e le app esistenti sono generiche, online o non conoscono il mio armadio reale.

## Solution

Un'app Android personale e offline. Registro il mio Guardaroba (ogni Capo con Categoria, Tipo, Colore principale/secondario dalla Palette, Motivo, Stagione, Taglia e una foto opzionale) e l'app mi propone Outfit fatti **solo con i miei Capi**, validi secondo regole di teoria dei colori (Neutri e Armonie). Ogni Proposta ha una Spiegazione che mi insegna perché funziona. Posso chiedere outfit casuali ("Genera") o costruirne uno attorno a un Capo ("Parti da questo capo"), salvare quelli che mi piacciono tra i Preferiti, e non perdo i dati grazie al backup automatico di Android e all'export ZIP.

## User Stories

### Guardaroba
1. Come utente, voglio aggiungere un Capo scegliendo la Categoria da una lista fissa, così ogni capo ha un ruolo chiaro nell'Outfit.
2. Come utente, voglio scegliere il Tipo come sottovoce della Categoria (es. camicia sotto Sopra), così riconosco il capo.
3. Come utente, voglio scegliere il Colore principale da una Palette di 25 Colori con nome e campione visivo, così non devo indovinare valori cromatici.
4. Come utente, voglio indicare opzionalmente un Colore secondario, così descrivo capi bicolore.
5. Come utente, voglio indicare il Motivo (tinta unita, righe, quadri, fantasia), così l'app evita outfit con troppe fantasie.
6. Come utente, voglio indicare la Stagione del Capo (estivo, invernale, mezza stagione, tutto l'anno), così posso filtrare le Proposte.
7. Come utente, voglio scrivere la Taglia come testo libero (es. "M", "42", "W32 L34"), così la ricordo.
8. Come utente, voglio aggiungere una foto opzionale dalla fotocamera, così riconosco subito il Capo.
9. Come utente, voglio aggiungere una foto opzionale dalla galleria, così uso foto già scattate.
10. Come utente, voglio che la foto venga ridotta automaticamente, così il Guardaroba resta leggero e sta nel backup.
11. Come utente, voglio salvare un Capo anche senza foto, così posso catalogare velocemente.
12. Come utente, voglio vedere il Guardaroba come griglia raggruppata per Categoria, così trovo i capi a colpo d'occhio.
13. Come utente, voglio che un Capo senza foto mostri un riquadro del suo Colore principale, così resta riconoscibile.
14. Come utente, voglio filtrare il Guardaroba per Categoria, così vedo solo ad esempio le Scarpe.
15. Come utente, voglio filtrare il Guardaroba per Colore, così vedo tutti i capi beige.
16. Come utente, voglio filtrare il Guardaroba per Stagione, così preparo il cambio di stagione.
17. Come utente, voglio modificare ogni campo di un Capo, così correggo gli errori.
18. Come utente, voglio sostituire o rimuovere la foto di un Capo, così la tengo aggiornata.
19. Come utente, voglio eliminare un Capo, così il Guardaroba riflette ciò che possiedo.
20. Come utente, quando elimino un Capo che compare in uno o più Preferiti, voglio un avviso che dice quanti Preferiti verranno eliminati, così non perdo dati a sorpresa.
21. Come utente, voglio poter annullare l'eliminazione da quell'avviso, così non perdo nulla per errore.

### Outfit: Genera
22. Come utente, voglio premere "Genera" e ricevere una Proposta completa fatta con i miei Capi, così non devo pensarci.
23. Come utente, voglio che ogni Proposta contenga un Sopra e un Sotto oppure un Intero, più le Scarpe, così è un outfit indossabile.
24. Come utente, voglio scegliere un filtro Stagione prima di generare, così ottengo outfit adatti al periodo.
25. Come utente, voglio che i Capi "tutto l'anno" entrino in qualsiasi filtro Stagione, così non devo duplicarli.
26. Come utente, voglio che con il filtro invernale o mezza stagione venga aggiunto un Capospalla compatibile se ne ho uno, così l'outfit è completo per il freddo.
27. Come utente, voglio che con il filtro estivo o senza filtro non venga aggiunto un Capospalla, così le Proposte restano essenziali.
28. Come utente, voglio che gli Accessori non vengano mai aggiunti automaticamente da "Genera", così le Proposte restano semplici.
29. Come utente, voglio premere "un altro" per ricevere una Proposta diversa, così esploro le combinazioni.
30. Come utente, voglio che "un altro" non ripeta Proposte già viste nella sessione finché ne esistono di nuove, così non vedo doppioni.
31. Come utente, quando ho visto tutte le Proposte valide, voglio saperlo e poter ricominciare, così capisco di aver esaurito le combinazioni.
32. Come utente, voglio che le Proposte siano scelte a caso tra tutti gli Outfit validi, così vedo varietà.
33. Come utente, se nel Guardaroba manca una Categoria necessaria, voglio un messaggio chiaro (es. "Ti mancano le Scarpe"), così so cosa aggiungere.
34. Come utente, se i Capi ci sono ma nessuna combinazione è valida, voglio un messaggio chiaro, così capisco che il problema sono i colori.

### Outfit: Parti da questo capo
35. Come utente, voglio toccare un Capo e scegliere "Parti da questo capo", così costruisco un outfit attorno a un capo che voglio indossare.
36. Come utente, voglio che la Proposta contenga sempre il Capo di partenza, così è rispettata la mia scelta.
37. Come utente, voglio poter partire anche da un Capospalla o un Accessorio, così vengono inclusi nell'outfit.
38. Come utente, voglio premere "un altro" anche in questa modalità, così vedo alternative con lo stesso Capo di partenza.
39. Come utente, voglio applicare il filtro Stagione anche qui, così le alternative sono coerenti con il periodo.
40. Come utente, voglio vedere quali Colori della Palette starebbero bene con il Capo di partenza, così so cosa cercare o comprare.
41. Come utente, se non esiste nessun Outfit valido per quel Capo, voglio un messaggio "Nessun abbinamento per questo capo" insieme ai Colori consigliati, così il consulente mi è utile comunque.

### Regole e Spiegazione
42. Come utente, voglio che i Neutri (nero, bianco, panna, grigio chiaro, grigio antracite, beige, cammello, marrone, blu navy, denim) si abbinino a tutto, così le combinazioni base funzionano sempre.
43. Come utente, voglio che un outfit di soli Neutri sia sempre valido, così ho sempre un'opzione sobria.
44. Come utente, voglio che Neutri più un solo colore non neutro sia sempre valido, così posso aggiungere un tocco di colore.
45. Come utente, voglio che due colori non neutri siano ammessi solo se monocromatici, analoghi o complementari, così l'outfit è armonioso.
46. Come utente, voglio che un outfit non abbia mai più di 2 colori non neutri, così non è caotico.
47. Come utente, voglio che un outfit non abbia mai più di 1 Capo non a tinta unita, così le fantasie non litigano.
48. Come utente, voglio che il Colore secondario e gli Accessori non limitino gli abbinamenti, così le regole restano semplici e prevedibili.
49. Come utente, voglio vedere sotto ogni Proposta una Spiegazione (es. "blu navy + beige: neutri", "blu royal + arancione: complementari"), così imparo la teoria dei colori.

### Preferiti
50. Come utente, voglio salvare una Proposta tra i Preferiti con un tocco, così ritrovo gli outfit che mi piacciono.
51. Come utente, voglio dare un nome opzionale a un Preferito, così distinguo ad esempio "colloquio" da "weekend".
52. Come utente, voglio vedere la lista dei Preferiti con le miniature dei Capi, così li riconosco senza nome.
53. Come utente, voglio vedere la Spiegazione anche nei Preferiti, così ricordo perché funziona.
54. Come utente, voglio eliminare un Preferito, così tengo la lista pulita.
55. Come utente, voglio rinominare un Preferito, così aggiorno il nome.

### Backup e dati
56. Come utente, voglio che Guardaroba, Preferiti e foto rientrino nel backup automatico di Android, così non perdo tutto se cambio telefono.
57. Come utente, voglio vedere nelle Impostazioni quanto spazio occupa il backup rispetto al limite di 25 MB, così so se sto per superarlo.
58. Come utente, voglio un avviso quando il backup supera l'80% del limite, così posso intervenire in tempo.
59. Come utente, voglio un avviso chiaro se il backup ha raggiunto il limite e non funziona più, così so che devo esportare a mano.
60. Come utente, voglio esportare l'intero Guardaroba (Capi, Preferiti e foto) in un unico file ZIP salvato dove voglio, così ho una copia di sicurezza mia.
61. Come utente, voglio importare un file ZIP esportato in precedenza, così ripristino il Guardaroba su un altro telefono.
62. Come utente, voglio una conferma prima che l'import sostituisca il Guardaroba attuale, così non lo sovrascrivo per sbaglio.
63. Come utente, se il file da importare non è valido, voglio un errore chiaro e il Guardaroba attuale intatto, così non perdo dati.

### Generale
64. Come utente, voglio una barra in basso con Guardaroba, Outfit, Preferiti e Impostazioni, così mi muovo velocemente.
65. Come utente, voglio che l'app segua il tema chiaro/scuro del telefono, così è coerente con il resto del sistema.
66. Come utente, voglio un'interfaccia in italiano, così è naturale da usare.
67. Come utente, voglio che l'app funzioni senza connessione internet, così la uso ovunque.

## Implementation Decisions

- **Piattaforma**: app Android nativa, Kotlin + Jetpack Compose + Material 3 (ADR-0002). Una sola activity, navigazione con barra inferiore a 4 sezioni: Guardaroba, Outfit, Preferiti, Impostazioni. `minSdk` lo sceglie il builder e lo registra in un ADR.
- **Nessuna rete**: l'app non richiede il permesso INTERNET (ADR-0001).
- **Moduli**:
  1. **Motore abbinamenti** (Kotlin puro, nessuna dipendenza Android). Contiene la Palette, le Categorie con i Tipi, le regole di Armonia, la composizione degli Outfit e la generazione delle Proposte. È il modulo profondo del progetto: interfaccia piccola, tutta la logica dentro.
  2. **Archivio** (Room + file delle foto). Gestisce CRUD dei Capi, Preferiti, eliminazione a cascata, compressione foto, export/import ZIP e calcolo della dimensione del backup.
  3. **UI** (Compose + ViewModel): schermate sottili che chiamano Motore e Archivio.
- **Interfaccia del Motore** (forma indicativa):
  - `proponi(guardaroba, richiesta, giàViste) → Esito`
  - `richiesta` = Genera(filtroStagione?) | PartiDa(capo, filtroStagione?)
  - `Esito` = Proposta(outfit, spiegazione) | Esaurite | MancaCategoria(categorie) | NessunAbbinamento
  - `coloriConsigliati(capo) → lista di Colori della Palette` compatibili col Colore principale del capo
  - Il caso è iniettabile (seed), così i test sono deterministici.
- **Palette**: 25 Colori fissi, ognuno con id stabile, nome italiano, valore cromatico (HSL) e flag Neutro. Neutri: nero, bianco, panna, grigio chiaro, grigio antracite, beige, cammello, marrone, blu navy, denim. Colori: rosso, bordeaux, rosa, corallo, arancione, giallo, senape, verde oliva, verde bosco, verde salvia, azzurro, blu royal, turchese, viola, lilla. Nel database si salvano gli id, mai i valori.
- **Categorie e Tipi** (lista fissa):
  - Sopra: t-shirt, camicia, polo, maglione, felpa, top
  - Sotto: pantaloni, jeans, shorts, gonna
  - Intero: vestito, tuta
  - Capospalla: giacca, cappotto, giubbotto, blazer
  - Scarpe: sneakers, eleganti, stivali, sandali
  - Accessori: cintura, borsa, cappello, sciarpa
- **Regole di validità di un Outfit** (ADR-0004), valutate sui Colori principali di tutti i Capi tranne gli Accessori:
  - al massimo 2 colori non neutri distinti
  - 0 o 1 colore non neutro: sempre valido
  - 2 colori non neutri: validi se monocromatici, analoghi o complementari, secondo soglie di tinta HSL che il builder fissa con una ricerca e aggiunge all'ADR-0004
  - al massimo 1 Capo con Motivo diverso da tinta unita
  - il Colore secondario è ignorato
- **Composizione**: (Sopra + Sotto | Intero) + Scarpe, obbligatori. Il Capospalla si aggiunge in Genera solo con filtro invernale o mezza stagione, se ne esiste uno compatibile. Gli Accessori entrano solo come Capo di partenza.
- **Filtro Stagione**: un Capo entra se ha la Stagione del filtro oppure "tutto l'anno". Senza filtro entrano tutti i Capi.
- **Spiegazione**: è generata dal Motore e indica le coppie o l'Armonia applicata (neutri / un colore + neutri / monocromatico / analoghi / complementari), con i nomi dei Colori.
- **Proposte casuali senza ripetizione**: il Motore riceve l'insieme degli Outfit già visti nella sessione (tenuto dal ViewModel) e restituisce Esaurite quando non ce ne sono di nuovi. Un Outfit si identifica con l'insieme degli id dei suoi Capi. *shortcut*: enumerazione completa delle combinazioni, adeguata fino a qualche centinaio di Capi.
- **Schema dati (Room)**:
  - `Capo`: id, categoria, tipo, colorePrincipale, coloreSecondario?, motivo, stagione, taglia?, percorsoFoto?, creatoIl
  - `Preferito`: id, nome?, spiegazione, creatoIl
  - `PreferitoCapo`: preferitoId, capoId, con foreign key in cascata
  - Eliminare un Capo elimina i Preferiti che lo contengono. La UI mostra prima quanti sono e chiede conferma.
- **Foto** (ADR-0003): al salvataggio, ridimensionamento a 720 px sul lato lungo, WebP qualità circa 75, con la rotazione EXIF corretta. Si salva solo la versione compressa, nello storage interno dell'app. Fotocamera via intent di sistema, galleria via Photo Picker: nessun permesso di storage.
- **Backup automatico**: `allowBackup` attivo, con regole che includono database e cartella foto ed escludono la cache. Le Impostazioni mostrano la dimensione attuale rispetto ai 25 MB, un avviso oltre l'80% e un avviso di backup fermo al 100%.
- **Export/Import ZIP**: un archivio con un JSON (Capi, Preferiti, versione del formato) più le foto. L'export passa dal selettore di file di sistema (SAF). L'import valida tutto l'archivio prima di toccare i dati, poi sostituisce il Guardaroba in un'unica transazione; se qualcosa fallisce, i dati attuali restano intatti.

## Testing Decisions

- **Un buon test** verifica il comportamento esterno attraverso l'interfaccia pubblica del modulo (input → Esito), mai i dettagli interni. I test usano il vocabolario del [[GLOSSARY]] nei nomi.
- **Motore abbinamenti**: copertura completa con test JVM puri (JUnit), seed fisso per il caso. Da coprire:
  - ogni Armonia (valida e non valida)
  - limite di 2 colori non neutri
  - limite di 1 Motivo non tinta unita
  - Neutri sempre validi
  - Colore secondario e Accessori ignorati
  - composizione Sopra+Sotto / Intero
  - Capospalla in base al filtro
  - filtro Stagione con "tutto l'anno"
  - PartiDa che include sempre il Capo di partenza (anche Capospalla e Accessori)
  - Esaurite / MancaCategoria / NessunAbbinamento
  - nessuna ripetizione
  - coloriConsigliati
  - contenuto della Spiegazione
- **Archivio**: solo i test più critici, con Room in memoria su Robolectric:
  1. eliminare un Capo elimina a cascata i suoi Preferiti
  2. il giro export → import ripristina Capi, Preferiti e foto identici
  3. un import di un file non valido lascia i dati intatti
  4. la foto compressa resta sotto i ~60 KB e dentro i 720 px
- **UI**: nessun test automatico; verifica manuale avviando l'app su emulatore o telefono.
- **Prior art**: nessuna, il progetto parte da zero. I primi test del Motore diventano il modello da seguire.

## Out of Scope

Tutte le voci in [[roadmap]] sono planned per dopo la v1:
- armocromia personale
- meteo, occasione e calendario
- più utenti e profili
- suggerimenti di acquisto
- stato del Capo (lavatrice, prestato)
- storico di utilizzo e contatore
- condivisione degli Outfit
- widget, notifiche, outfit del giorno

Fuori dalla v1 anche:
- Categorie, Tipi o Colori personalizzati
- ruota cromatica libera ed estrazione del colore dalla foto
- triadi e altre Armonie
- punteggio di "bellezza" delle Proposte
- AI e cloud
- pubblicazione sugli store
- lingue diverse dall'italiano
- test automatici della UI

## Further Notes

- La v1 non contiene codice preparatorio per le voci della roadmap (stile Ponytail).
- Le soglie di tinta per analoghi e complementari vanno decise con una piccola ricerca di teoria dei colori, come chiede [[descrizione]], e registrate nell'ADR-0004 prima di implementare le Armonie.
- Il builder deve avere Android SDK e JDK installati. La verifica sul telefono fisico è un passo che può fare solo l'utente.
