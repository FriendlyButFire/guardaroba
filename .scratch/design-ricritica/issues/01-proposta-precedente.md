# 01: Tornare alla Proposta di prima

**What to build:** In Outfit, "Un altro" non butta più via la Proposta: un pulsante "Precedente" accanto a "Un altro" e il tasto Indietro di sistema riportano alle Proposte già mostrate, a ritroso. Vale anche da Esaurite e da Nessun abbinamento. Le Proposte di prima con un Capo cambiato o sparito escono dalla cronologia (ADR-0010); "Parti da" e il ritorno a Genera la svuotano.

**Blocked by:** nessuno

**Status:** in corso (agente principale)

- [ ] `OutfitViewModel.precedente()` / `cèPrecedente`, con test
- [ ] Pulsante "Precedente" e Indietro di sistema in `OutfitScreen`
- [ ] Indietro: prima la Proposta di prima, poi l'uscita da "Parti da"
