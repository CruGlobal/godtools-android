# Dismissed Review Issues

Issues listed here are suppressed in future PR reviews.

---

## MutableState in UiState
**Pattern**: Flagging `MutableState<T>` (or other Compose `State<T>` objects) as fields in a Circuit `UiState` data class as an architectural violation.
**Reason**: Circuit only requires `UiState` to be `@Stable`, not strictly immutable. Embedding `MutableState<T>` (which is stable) is valid and intentional — it allows the UI to mutate local state (e.g. search query) without routing every keystroke through `eventSink`.
**Dismissed**: 2026-03-30
**Dismissed by**: Daniel Frett
