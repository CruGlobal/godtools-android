---
name: godtools-authoring
description: How to write code, tests, commits and PR descriptions for GodTools Android the way its code owners do, so the PR needs no style or architecture changes in review. Use this whenever you implement a feature, fix a bug, migrate legacy UI, add a screen, presenter, repository, sync task, string, dependency or test in this repo, or prepare commits or a PR for it, even if the user only says "add X", "fix Y" or "open a PR". Complements pr-review (which checks a finished PR) by guiding the work before and while it is written.
---

# Writing GodTools Android code the owners will merge as-is

This skill distills 12 months of merged code, 360+ human commits, 70 merged PRs and every owner
review comment in that period (mostly Daniel Frett, also Tim Johnson) into how to author a change.
Each rule here was checked against the current code on `develop`. When a rule and an older doc
disagree, the rule follows what the code and the owners actually do today.

The single best predictor of a clean review is **copying the closest existing precedent**. The
owners are consistent, and most review comments on past PRs were "do it the way X already does it".

## Workflow

### 1. Classify the change and load the right reference

Read only what the change touches. Each file has real examples and file:line precedents.

| If the change involves... | Read |
|---|---|
| A screen, presenter, dialog, list or any Compose UI | `references/circuit-compose.md` |
| Models, Room, repositories, API, sync, settings, flags, Dagger modules | `references/data-di.md` |
| Any test, including Paparazzi snapshots | `references/testing.md` (always, since every change ships tests) |
| Kotlin code at all | `references/kotlin-style.md` |
| build.gradle.kts, libs.versions.toml, CI, strings, drawables | `references/build-ci-resources.md` |
| Tool renderers, deep or share links, Fragments, DataBinding, removing legacy code | `references/renderers-migration.md` |
| Commits, the PR description, splitting work | `references/commits-prs.md` (always, before committing) |

For Compose visuals also read `.claude/rules/design_system_rules.md`, keeping in mind the
corrections listed under "Where older docs are out of date" below.

### 2. Find the precedent before writing anything

Locate the most similar existing code and mirror its structure, naming, file layout and tests.

```bash
# Sibling features and how they were built
ls app/src/main/kotlin/org/cru/godtools/ui/<area>/
git log develop --oneline -- <path-of-similar-feature> | head -20
git show <sha> --stat          # how the owner staged that change
```

Good anchors: `ui/dashboard/lessons/` (presenter with FlowProducer, flags, sync, full test set),
`ui/settings/country/` (small Circuit feature), `library/sync/.../ToolSyncTasks.kt` (sync
template), `library/db/.../FollowupsRoomRepository.kt` (repository), PR #4562 (legacy to shared
renderer migration with cleanup).

### 3. Plan the PR and its commits before coding

- **One concern per PR.** A fix does not carry an unrelated feature or cleanup. Big features ship
  as a series of reviewable PRs, bottom up: data model, then sync, then UI, then snapshot
  consolidation.
- **One logical change per commit, each one compiling.** Typical order: preparatory rename or
  refactor, new code with its tests, switch callers over, then a separate
  "Remove ... orphaned by ..." commit.
- If the change needs an unreleased `godtools-shared` (kotlin-mpp-godtools-tool-parser) or
  gto-support build, plan a one-file commit bumping `gradle/libs.versions.toml` to the
  `-SNAPSHOT`, and open the PR before the upstream PR merges with a `## Dependencies` section
  noting CI fails until the snapshot is published. Develop against a local `includeBuild` that
  is never committed.
- If tool content or in-tool UI (lesson app bar, tract cards, tips) must change, that change
  belongs upstream in kotlin-mpp-godtools-tool-parser. This repo only hosts the shared
  composables and routes their events.

### 4. Write code and tests together

Tests land in the same PR as the code, usually the same commit. Every new class gets a test
(see `references/testing.md` for which kind). Visible UI changes get a Paparazzi scenario.

### 5. Self-review against the review blockers

Go through the checklist below on your own diff (`git diff develop...HEAD`) before committing.

### 6. Run the same checks CI runs

```bash
./gradlew :build-logic:ktlintCheck ktlintCheck      # must pass before review
./gradlew lint
./gradlew test verifyPaparazzi                      # unit tests + snapshot verification
./gradlew bundle                                    # full build
```

For a quick loop run only the touched module, e.g. `./gradlew :app:testProductionDebugUnitTest`
or `./gradlew :library:db:test`. Never record Paparazzi snapshots locally. Push and run the
Record Snapshots workflow, then fold its commit in (the `record-screenshots` skill automates it).

### 7. Commit and describe the PR

Follow `references/commits-prs.md` for message format and the PR template. Fold every fix-up,
lint fix, review response and "Record updated snapshots" commit into the commit it belongs to
(`git commit --fixup <sha>` then `GIT_SEQUENCE_EDITOR=: git rebase -i --autosquash develop`).

## Review blockers checklist

These are the things owners have actually asked contributors to change. Each is explained with
examples in the reference files.

### Architecture

- [ ] **New UI is Circuit + Compose only.** No new Fragment, ViewModel, DataModel, LiveData,
      DataBinding layout, RecyclerView adapter or XML layout (none were added in 12 months, 58 were
      deleted). Keeping a legacy Fragment is fine; host Compose inside it with `ComposeView`.
- [ ] **Feature file set:** `FooScreen.kt`, `FooPresenter.kt`, `FooLayout.kt` in
      `ui/<area>/<feature>/`. The UI composable is named `FooLayout`, never `FooScreen` or `FooUi`.
- [ ] **Screen** is `@Parcelize data object FooScreen : ParcelableScreen` (or a data class with
      args). Put it in `:ui:base` only if another module navigates to it.
- [ ] **Presenter** has `UiState` and `UiEvent` nested at the top, and ends with a plain nested
      `@AssistedFactory @CircuitInject(FooScreen::class, SingletonComponent::class) interface Factory`
      with a `create(...)` function. It does not extend `Presenter.Factory`.
- [ ] **`present()` stays short.** Each data source is a private `@Composable rememberX(...)`
      helper. Independent sections of a screen get their own child presenter. An open PR was put
      on hold because one large presenter absorbed several sections.
- [ ] **Non-trivial Flow logic** (fallbacks, filtering, mode switching) lives in an
      `internal class FooFlowProducer @Inject constructor(...)` with its own test.
- [ ] **Anything with a `@Composable` function that tests must replace** is an interface with an
      `internal class DefaultFoo` implementation, `@Binds` in a feature module, and a hand-written
      `FakeFoo` in tests. No new mockposable (`everyComposable`) mocks. Never name impls `FooImpl`.
- [ ] **Navigation:** open Activities with `navigator.goTo(IntentScreen(...))`; show Circuit
      screens from legacy code with `startCircuitActivity`. Do not add an Activity per screen.
- [ ] **Store ids, never Fragments, Views or Activities,** in LiveData, adapters or state.
- [ ] **Handle every model variant.** Page logic in CYOA names ContentPage, CardCollectionPage
      and PageCollectionPage explicitly in a `when` (`Page` is not sealed, so it also needs an
      `else` branch).

### Data and DI

- [ ] **Three layers:** domain model in `:library:model`, `internal` Room `XEntity` with
      `toModel()`, and an `internal SyncX` partial when sync must not overwrite local-only columns.
- [ ] **Repository** is a public `interface XRepository` plus `@Dao internal abstract class
      XRoomRepository`, exposed through `GodToolsRoomDatabase` and `@Provides @Reusable` in
      `DatabaseModule`. No `@Binds`, no `Impl` suffix.
- [ ] **Room schema change** in one commit: entity, `@Database` entities, version bump,
      `AutoMigration`, generated schema JSON, and a migration test.
- [ ] **Sync** follows the `XSyncTasks` template (normalized keys, `MutexMap` or `Mutex` lock, staleness
      check, `HTTP_OK` guard, `*FromSync` store, update sync time) and is reached only through
      `GodToolsSyncService.executeSync`. Presenters register syncs with
      `circuitContext.rememberSyncTask(key) { ... }` from gto-support, inside a private
      `@Composable RegisterSyncTask(...)` helper.
- [ ] **Logging** is `Timber.tag(TAG)` with a `private const val TAG`. `.e` reaches Crashlytics,
      so use `.d` for expected failures. Catch specific exceptions; a broad catch only at a
      boundary, rethrowing `CancellationException` first.

### Kotlin style

- [ ] **Visibility:** `private` by default, then `internal`. Never write the `public` keyword.
- [ ] **`when` instead of `if`/`else if` chains,** and for choosing between named options even
      with two branches. A plain `if (flag) a else b` on a Boolean is fine.
- [ ] **Expression bodies** for single-expression functions. Flow-returning functions end in
      `Flow` (`findToolFlow()`, `getNormalToolsFlow()`).
- [ ] **Non-cancellable work:** `launch(start = CoroutineStart.UNDISPATCHED) { withContext(NonCancellable) { ... } }`.
- [ ] **Locales in URLs and keys:** `locale.toLanguageTag()`, never `toString()`.
- [ ] **UiState collections** are plain `List`, not `ImmutableList`.
- [ ] **Long classes** use labeled `// region Name` ... `// endregion Name` blocks, repeating the label.
- [ ] **Fragment args** use a `private` or `internal` `var foo: T by arg()` set from a secondary
      constructor, not a `newInstance()` companion or Bundle keys.
- [ ] **No commented-out code,** no scratch comments, no stray blank lines. KDoc only where non-obvious.

### Formatting and hygiene

- [ ] **Only your lines changed.** Never reformat a whole file. Reformat just what you edited and
      stage with `git add -p`. Watch for IDE noise: blank lines inserted between annotated
      properties, rewrapped getters, reordered imports.
- [ ] **ktlint passes** (`./gradlew :build-logic:ktlintCheck ktlintCheck`).

### Build and resources

- [ ] **Compose and DataBinding only via helpers:** `configureCompose(project)` (with
      `enableCircuit = true` for Circuit) and `enableDatabinding(project)`. KSP for Dagger, Hilt,
      Room and Circuit. No kapt, no hand-set `buildFeatures.compose`.
- [ ] **Dependencies:** type-safe accessors (`projects.ui.base`), added to the matching group, alphabetical,
      blank lines between groups kept. New libraries go in `libs.versions.toml`.
- [ ] **Never commit `includeBuild`** other than `build-logic`.
- [ ] **Strings** go in the feature's existing `strings_<area>.xml` in the module that uses them
      (there is no plain `strings.xml`). A renamed or deleted key changes every `values-*` locale
      file in the same commit. Search for an existing string (e.g. `grep -rn 'name="menu_' */src/main/res/values/`)
      before adding a new key with the same text.
- [ ] **Constants and helpers live in the module that uses them,** as `internal` or `private`. Do
      not move something into a shared library module unless a second module needs it.
- [ ] **Migrations delete what they orphan** in the same PR: layouts, drawables, styles, dimens,
      binding adapters, tests, module dependencies, `libs.versions.toml` entries, build flags.

### Tests

- [ ] **Every new class and layer has tests in this PR.**
- [ ] **Presenter tests** use circuit-test `presenter.test { }` with `FakeNavigator`, mocks backed
      by `MutableStateFlow`, and events sent through `state.eventSink(...)`.
- [ ] **Layout tests** use `androidx.compose.ui.test.v2.runComposeUiTest` (v1 is a must-fix).
- [ ] **Tests are grouped** in `// region State.<field>` / `// region UiEvent.<Event>` /
      `// region methodName()` blocks. Names are backticked, hierarchical and dash-separated,
      like `` `State - isSyncing - initial sync` `` or `` `UiEvent - TriggerSync` `` (older tests say
      `Event -`; use `UiEvent -` in new ones).
- [ ] **Assertions** come from `kotlin.test`. Robolectric tests use `@Config(application = Application::class)`
      unless they need the Hilt graph (renderer Activity tests use `HiltTestApplication`).
- [ ] **Placement:** presenter, producer, parser and fake classes in `src/test`; in `:app`, layout
      and Paparazzi tests and `*TestData` go in `app/src/testDebug`.
- [ ] **Paparazzi:** every new or changed visible element has a scenario, the full screen is
      snapshotted in its real shell, data is deterministic (no current dates), and goldens come
      only from the CI workflow.
- [ ] **Shared UI changes ripple into other snapshots.** Adding a drawer item, app bar action or
      shared component changes existing goldens (e.g. the "Drawer Open" scenarios in
      `DashboardLayoutPaparazziTest`). Grep the Paparazzi tests for every screen that renders it,
      expect those goldens to be re-recorded, fold them into the commit that changed the shared UI,
      and mention them in the PR.
- [ ] **Layout tests find nodes** by `TEST_TAG_*` constants or resolved string resources, never by
      English literals.

### Commits and PR

- [ ] **Subjects** are capitalized imperative sentences with no trailing period
      ("Switch tract page rendering to the shared RenderTractPage composable"). Bodies explain
      why and what was left out.
- [ ] **No fix-up commits left behind,** and each commit compiles.
- [ ] **PR title** carries the Jira key when there is a ticket (`GT-3101 Hide the Featured Tools
      section ...`). The body has Summary or Changes, anything intentionally not done, a
      Dependencies section for cross-repo SNAPSHOTs, and Tests or a Test plan that names the
      Gradle checks actually run. Run them before opening the PR rather than listing them as to-do.

## When to deviate

Rules marked "Tendency" in the references show a direction, not a blocker. Follow them in new
code and in code you are already changing, but do not rewrite untouched code to match them;
that would be the unrelated churn the owners ask people to avoid. If you find a real reason a
rule does not fit, say so in the PR description rather than silently diverging.

## Where older docs are out of date

The existing CLAUDE.md, design_system_rules.md and pr-review skill are mostly right. These
points disagree with the current code and owner practice; follow the code.

- Strings live in `strings_<area>.xml` files. No module has a plain `strings.xml`.
- Screens implement `ParcelableScreen`, results use `ParcelablePopResult`, UI composables are
  named `FooLayout`, and factories do not extend `Presenter.Factory`.
- Only 3 of 16 UiEvents are `internal`. Use `internal` together with `@ConsistentCopyVisibility`
  and an internal constructor when you restrict UiState, otherwise leave them public.
- Presenter tests use `presenter.test { }`. `presenterTestOf` is only for a `present()` that
  takes arguments. `createComposeRule()` is still needed for `StateRestorationTester`.
- `GodToolsTheme` wraps each Compose root (each Activity `setContent` and each `ComposeView`),
  never inside a composable. Tool renderers use `GodToolsTheme(darkTheme = false)`.
- Screens show a single loading state (`dataLoaded`) rather than separate loading, error and
  empty UIs. Lazy lists space items with per-item padding and `animateItem()`.
- A query field can be a `MutableState` inside UiState (intentional, see dismissed-issues.md).
- For an unreleased shared-library change, the owners bump to a `-SNAPSHOT` and open the PR
  before the upstream PR merges, with a Dependencies note. They don't wait for a release.
- Newer presenters inject dispatchers (`@param:DispatcherType(IO)`) instead of hardcoding `Dispatchers.IO`.
- Broad `catch (e: Exception)` exists at a few sync, account and initial-content boundaries,
  always logged, and in suspend code rethrowing `CancellationException` first. Elsewhere catch
  specific exceptions.
- Feature Dagger modules live in their feature package. Only app-wide modules are in `dagger/`.
- `.tool-versions` pins JDK 25 to run Gradle; the Kotlin toolchain targets JDK 21.
