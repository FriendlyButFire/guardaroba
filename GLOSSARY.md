# Guardaroba

App personale che propone abbinamenti di colore tra i capi che l'utente possiede davvero.

## Capi

**Capo**:
Un singolo indumento posseduto dall'utente, descritto da categoria, tipo, colori, motivo, stagione e taglia.
_Avoid_: Item, articolo, vestito

**Guardaroba**:
L'insieme di tutti i Capi registrati dall'utente.
_Avoid_: Armadio, inventario, collezione

**Categoria**:
Il ruolo del Capo nell'Outfit: Sopra, Sotto, Intero, Capospalla, Scarpe, Accessori.
_Avoid_: Tipo (che è il livello sotto)

**Tipo**:
La sottovoce di una Categoria (es. camicia sotto Sopra, jeans sotto Sotto); solo informativo.
_Avoid_: Modello, genere

**Colore**:
Una voce della Palette, con nome e valore cromatico.
_Avoid_: Tinta (che è una proprietà del Colore)

**Palette**:
L'elenco fisso dei Colori selezionabili per un Capo.

**Neutro**:
Un Colore della Palette che si abbina a qualsiasi altro (nero, bianco, grigi, beige, cammello, marrone, blu navy, denim…).
_Avoid_: Basico

**Colore principale**:
Il Colore dominante del Capo; è quello su cui si giudicano gli abbinamenti.

**Colore secondario**:
Un secondo Colore opzionale presente sul Capo.

**Motivo**:
Il disegno del tessuto: tinta unita, righe, quadri o fantasia.
_Avoid_: Pattern, stampa

**Stagione**:
Il periodo in cui si indossa un Capo: estivo, invernale, mezza stagione o tutto l'anno.

**Taglia**:
La misura del Capo, testo libero opzionale (es. "M", "42", "W32 L34"); solo informativa.

## Abbinamenti

**Outfit**:
Un insieme di Capi indossabili insieme: un Sopra e un Sotto (oppure un Intero) più le Scarpe, con Capospalla e Accessori opzionali.
_Avoid_: Look, completo, combinazione

**Armonia**:
Una regola di teoria dei colori che rende validi insieme i Colori principali non Neutri di un Outfit: monocromatico, analoghi o complementari.
_Avoid_: Schema, combinazione

**Proposta**:
Un Outfit suggerito dall'app, accompagnato dalla sua Spiegazione.
_Avoid_: Suggerimento, risultato

**Spiegazione**:
La motivazione cromatica di una Proposta, espressa con le regole di abbinamento applicate (es. "complementari").

**Preferito**:
Un Outfit salvato dall'utente.
_Avoid_: Salvato, bookmark
