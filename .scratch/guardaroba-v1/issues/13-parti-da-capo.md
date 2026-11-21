# 13: Parti da questo capo

**What to build:** Dalla scheda di un Capo scelgo "Parti da questo capo" e ricevo Proposte che lo includono sempre (anche se è un Capospalla o un Accessorio), con filtro Stagione e "un altro".

**Blocked by:** 11

**Status:** chiuso

- [x] Richiesta PartiDa(capo, filtroStagione?) nel Motore
- [x] Il Capo di partenza è sempre nella Proposta; se è un Capospalla o un Accessorio viene incluso a prescindere dal filtro
- [x] Nessuna ripetizione ed Esaurite come in Genera
- [x] "Nessun abbinamento per questo capo" quando non esistono Outfit validi
- [x] Test JVM per ogni Categoria di partenza

## Comments

- Il Capo di partenza ignora il filtro Stagione qualunque sia la Categoria, non solo Capospalla e Accessori: vedi [[0008-parti-da-capo-ignora-filtro]]. ADR-0004 aggiornato: gli Accessori non compaiono nella Spiegazione.
- Da verificare a mano sul telefono: pulsante "Parti da questo capo" nella scheda del Capo, intestazione "Parti da" e "Genera a caso" in Outfit.
