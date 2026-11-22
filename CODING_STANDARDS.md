# Coding standards

Read during review (`/code-review`). The project's style is Ponytail (see `CLAUDE.md`); the rules here are the judgement calls a linter can't make.

## Session state lifecycle

Every piece of state held in a ViewModel or a composable (`remember`, `rememberSaveable`, `MutableStateFlow`) has a **lifecycle**: the events that invalidate it and the ones it must survive. For each state the diff adds or touches, check both lists against the events below and flag any event the code doesn't handle.

| Event | Typical effect |
|---|---|
| Guardaroba change (Capo added, edited, deleted) | Proposte, already-seen and history that contain the Capo expire ([[0010-proposta-scade-se-cambiano-i-suoi-capi]]) |
| Importa / Ripristina | the whole Outfit session restarts |
| Stagione filter change | already-seen and history are cleared ([[0006-filtro-stagione-azzera-gia-viste]]) |
| Parti da / return to Genera | history is cleared |
| Opening a sub-page (rules page, opened Preferito) and coming back | state, scroll and open panels **survive**: keep them above the branch that shows the sub-page |
| Process death | only what is in `rememberSaveable` or the database survives; anything else is a `// shortcut:` |

Why: six `fix:` commits in v1 and the design fixes were this one bug class, caught by review only after implementation (stale Proposta, already-seen not pruned, session not reset after Importa, history kept across a Stagione change, scroll lost after the rules page).
