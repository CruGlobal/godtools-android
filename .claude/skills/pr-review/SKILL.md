---
name: pr-review
description: Review a pull request against GodTools Android project conventions. Use when asked to review a PR, check code quality, or audit changes.
argument-hint: [pr-number]
allowed-tools: Bash, Read, Grep, Glob, Write, Edit
---

Review pull request $ARGUMENTS against the GodTools Android project conventions.

## Steps

1. Check for dismissed issues by reading `.claude/skills/pr-review/dismissed-issues.md` if it exists.
   Load all dismissed entries — each has a **Pattern** and **Reason**. You will use these to suppress matching findings later.

2. Fetch the PR diff and metadata:
```
gh pr diff $ARGUMENTS
gh pr view $ARGUMENTS
```

3. Identify all changed files and categorize them (Kotlin, build scripts, resources, manifests, tests).

4. Pre-flight check — run ktlint. This is a hard blocker:
```
./gradlew :build-logic:ktlintCheck ktlintCheck
```
If it fails, report as **Must Fix** before reviewing anything else.

5. Review each category using the checklist below.

6. Before outputting, cross-reference every finding against dismissed patterns. A finding matches a dismissed pattern when it describes the same class of issue (not necessarily the exact file/line — match by concept). Move matched findings to a separate suppressed list.

7. Output a structured review (format below).

8. After the review output, print:

```
---
To dismiss a finding so it won't appear in future reviews, say:
  dismiss: <short title> — <reason>
```

---

## Review Checklist

### Build Scripts (`build.gradle.kts`)

- [ ] `godtools.library-conventions` / `godtools.application-conventions` applied — do not re-declare `minSdk`, `compileSdk`, `targetSdk`, Kotlin toolchain, or proguard rules
- [ ] Compose enabled via `configureCompose(project)` in `android {}` — do not manually add Compose BOM or Circuit deps
- [ ] No redundant `buildFeatures` flags already covered by the convention plugin
- [ ] New modules use `godtools.library-conventions`, `godtools.application-conventions`, or `godtools.dynamic-feature-conventions` as appropriate
- [ ] KSP used for new modules; `kapt` only if DataBinding is still present (see TODO comment pattern)

### `gradle/libs.versions.toml`

- [ ] No duplicate version keys
- [ ] No unused `[plugins]` entries (common AS wizard artifact)
- [ ] No unused `[libraries]` entries
- [ ] All new dependencies declared here, not hardcoded in `build.gradle.kts`

### Android Resources & Manifests

- [ ] No AS wizard scaffolding in library modules: `colors.xml`, `strings.xml` with `app_name`, default launcher icons, per-module theme definitions
- [ ] Library `AndroidManifest.xml` does not declare `application` attributes (theme, label, icon) — those belong in `:app`
- [ ] Hardcoded strings extracted to `strings.xml` (inline string literals in composables are flagged)
- [ ] No `app_name` or generic color/theme resources in library modules

### `settings.gradle.kts`

- [ ] No local `includeBuild` substitutions for `kotlin-mpp-godtools-tool-parser` committed — publish artifact and update version in `libs.versions.toml` instead

### Jetpack Compose

- [ ] Every public or internal composable has `modifier: Modifier = Modifier` parameter
- [ ] Always uses `GodToolsTheme` from `:ui:base` — no per-module `MaterialTheme(colorScheme = …)` wrappers
- [ ] Colors accessed via `MaterialTheme.colorScheme.*` or `GodToolsTheme.extendedColorScheme`
- [ ] CPU-heavy work (bitmap generation, etc.) done off the main thread via `produceState { withContext(Dispatchers.IO) { … } }`

### Circuit Presenter/UI Patterns

**Presenter**
- [ ] Uses `@AssistedInject` constructor; `Navigator`/`Screen` injected via `@Assisted`
- [ ] Contains nested `Factory` interface annotated with `@AssistedFactory` and `@CircuitInject(<Screen>::class, SingletonComponent::class)`
- [ ] `UiState` is a `data class` implementing `CircuitUiState`
- [ ] `UiEvent` is a `sealed interface` implementing `CircuitUiEvent`, marked `internal`
- [ ] `UiState` and `UiEvent` defined as nested types inside the Presenter
- [ ] `UiState` exposes `val eventSink: (UiEvent) -> Unit`
- [ ] Presenter contains no UI logic — pure state/event handling

**UI Composable**
- [ ] Annotated with `@CircuitInject(<Screen>::class, SingletonComponent::class)`
- [ ] Signature: `(state: <Presenter>.UiState, modifier: Modifier = Modifier)`
- [ ] All user interactions delegated via `state.eventSink(UiEvent.*)` — no direct function calls from UI

**Screen**
- [ ] Annotated with `@Parcelize`
- [ ] Is an `object` or `data class` implementing `Screen`

### Cross-Module Activity / Intent Creation

- [ ] `Intent`/`PendingIntent` creation for Activities uses extension functions in `ui/base/src/main/kotlin/org/cru/godtools/base/ui/Activities.kt` (string class names, no hard compile-time deps between sibling UI modules)

### Repository & DAO Patterns

- [ ] DAOs use `suspend fun` for single-shot queries, `Flow<T>` for reactive queries
- [ ] `@Upsert` for sync operations
- [ ] `@RewriteQueriesToDropUnusedColumns` when selecting partial projections

### WorkManager

- [ ] Workers annotated with `@HiltWorker` + `@AssistedInject`
- [ ] Extend `CoroutineWorker` for suspend support
- [ ] Catch `IOException` (specific), not `Exception` (broad)

### Kotlin Code Quality

- [ ] Logging uses `Timber` — no `println` or `Log.*`
- [ ] Exception handling catches specific types, not bare `Exception`
- [ ] Visibility is intentional: `internal` for module-scoped symbols, `private` where possible
- [ ] Multi-branch conditionals use `when`, not chained `if/else`
- [ ] `Bundle`/`Intent` extra keys are `const val` shared between producer and consumer

### Testing

- [ ] Presenter tests use `presenterTestOf { }` (Circuit test API)
- [ ] Paparazzi tests extend `BasePaparazziTest` from `:ui:base` testFixtures with `@TestParameter` night/accessibility matrix
- [ ] Snapshots not recorded locally — triggered via GitHub Actions workflow on the feature branch

### PR Hygiene

- [ ] No unrelated auto-formatter whitespace changes (check with `git diff develop...HEAD --stat`)
- [ ] KMP tool UI changes (app bar, lesson/tract rendering) belong in `kotlin-mpp-godtools-tool-parser`, not Android renderer modules

For detailed examples of each pattern, see `references/patterns.md`.

---

## Output Format

```
## PR Review: <title> (#<number>)

### Summary
<1–2 sentence summary of what the PR does>

### Checklist Findings

#### ✅ Looks Good
- <item>

#### ⚠️ Minor Issues
- <file:line> — <issue> — <suggested fix>

#### ❌ Must Fix
- <file:line> — <issue> — <suggested fix>

#### ⏭️ Suppressed
- <short title> — dismissed: <reason>
(omit this section entirely if nothing was suppressed)

### Overall Verdict
APPROVE / REQUEST CHANGES / COMMENT
<brief rationale>
```

Be specific. Reference file paths and line numbers. Cite the relevant convention when flagging an issue.

---

## Handling Dismissals

When the user says `dismiss: <title> — <reason>` (in any form — "dismiss the X issue because Y", etc.):

1. Read `.claude/skills/pr-review/dismissed-issues.md` if it exists (create it if not).
2. Run `git config user.name` to get the current user's name.
3. Append a new entry in this format:

```markdown
## <title>
**Pattern**: <describe the class of issue broadly enough to match future occurrences>
**Reason**: <reason the user gave>
**Dismissed**: <today's date as YYYY-MM-DD>
**Dismissed by**: <git user.name>
```

4. Confirm to the user what was added and that it will be suppressed in future reviews.
