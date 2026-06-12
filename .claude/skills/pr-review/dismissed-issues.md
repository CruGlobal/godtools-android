# Dismissed Review Issues

Issues listed here are suppressed in future PR reviews.

---

## MutableState in UiState
**Pattern**: Flagging `MutableState<T>` (or other Compose `State<T>` objects) as fields in a Circuit `UiState` data class as an architectural violation.
**Reason**: Circuit only requires `UiState` to be `@Stable`, not strictly immutable. Embedding `MutableState<T>` (which is stable) is valid and intentional — it allows the UI to mutate local state (e.g. search query) without routing every keystroke through `eventSink`.
**Dismissed**: 2026-03-30
**Dismissed by**: Daniel Frett

---

## Missing @AnyThread on sync task methods
**Pattern**: Flagging absence of `@AnyThread` annotation on `internal suspend fun` methods in `ToolSyncTasks` (or similar sync task classes).
**Reason**: `@AnyThread` is a vestigial remnant from older versions of the sync logic. It's unnecessary on suspend functions and not required going forward.
**Dismissed**: 2026-05-06
**Dismissed by**: Daniel Frett

---

## Duplicate when branches (smart cast)
**Pattern**: Flagging identical adjacent `when` branches (e.g. `is LibraryExtension -> X` and `is ApplicationExtension -> X`) as redundant and suggesting collapse into a multi-match branch.
**Reason**: Kotlin does not union smart-cast types in a multi-condition `when` branch — the receiver stays as the common supertype, losing access to subtype-specific members. Separate branches are required when each branch body accesses a property only available on the narrowed type (e.g. `buildFeatures.dataBinding` on `LibraryExtension`/`ApplicationExtension` but not on `CommonExtension`).
**Dismissed**: 2026-06-12
**Dismissed by**: Daniel Frett
