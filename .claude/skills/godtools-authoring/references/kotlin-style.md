# Kotlin style

How the code owners write everyday Kotlin in this repo: visibility, control flow, naming, coroutines and Flow, comments, logging, and formatting. Read this before writing or editing any `.kt` file, and again before opening a PR, since most of these are things a reviewer spots in the diff at a glance. Compose layout rules live in `.claude/rules/design_system_rules.md`; test structure lives in `references/testing.md`.

## Contents

1. Visibility
2. Comments, KDoc and TODO markers
3. Coroutines and Flow
4. Formatting, diffs and ktlint
5. Control flow and expressions
6. Regions
7. Naming
8. Types, collections and constants
9. Locale handling
10. Logging and exceptions
11. Smaller habits

## 1. Visibility

**Make new declarations `private`, widen to `internal` only when another file needs them, and never write the `public` keyword.** Frett flagged wider-than-needed visibility on PRs #4256 and #4267. Main code has about 906 `private` and 597 `internal` declarations and zero written `public`; public API just uses default visibility. New `@Inject lateinit var` fields are `internal`.

```kotlin
@Inject
internal lateinit var settings: Settings

private const val EXTRA_TOOL = "tool"
```

Note: the pr-review `patterns.md` lists `public` as a modifier choice; the code never writes it. Follow the code.

**Test the real entry point; add a test hook only as `@VisibleForTesting internal`.** Do not widen a private helper composable just so a test or snapshot can call it. On PR #4583/#4593 `AccountLayoutHeader` was reverted to private, while an `internal @VisibleForTesting` overload taking a `PagerState` was accepted. For a Dagger class that needs a test constructor, make the `@VisibleForTesting internal constructor(...)` the primary and add a separate `@Inject internal constructor(...)` (see `library/account/src/main/kotlin/org/cru/godtools/account/GodToolsAccountManager.kt:35-47`).

```kotlin
@VisibleForTesting
internal fun TutorialLayout(
    state: UiState,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) { ... }
```

## 2. Comments, KDoc and TODO markers

**Delete dead code instead of commenting it out, and strip scratch comments and stray blank lines before the PR.** Frett on PR #4167: "we want to avoid committing commented out logic like this." Git history keeps the old code, and main has no commented-out code today.

**Add KDoc only where an API is not obvious.** There are about 24 KDoc blocks across roughly 490 main files. Boilerplate KDoc on every function reads as noise here.

**Write follow-ups as `// TODO: <what and when>` and deliberate workarounds as `// HACK: <why>`.** Every TODO and HACK comment in main code uses the colon form with a reason. Use `TODO("reason")` only for an unimplemented stub (e.g. `ui/base/src/main/kotlin/org/cru/godtools/base/ui/circuit/CircuitActivity.kt:82`). Do not add `FIXME` or `XXX` (the two `// XXX:` markers left are legacy).

```kotlin
// TODO: include the DashboardScreen as the parent screen once it's implemented
// HACK: the Material 3 ModalDrawerSheet composable is missing horizontal padding listed in the spec
```

## 3. Coroutines and Flow

**For work that must finish after a user action, use `launch(start = CoroutineStart.UNDISPATCHED) { withContext(NonCancellable) { ... } }`.** Never write `launch(NonCancellable)`. That form detaches from the parent Job and may not run at all if the scope is already cancelling. Main code has 7 `withContext(NonCancellable)` and zero `launch(NonCancellable)`. See `app/src/main/kotlin/org/cru/godtools/ui/drawer/DrawerMenuPresenter.kt:33-35` and `app/src/main/kotlin/org/cru/godtools/ui/tools/DefaultToolCardPresenter.kt:148`.

```kotlin
Event.Logout -> scope.launch {
    launch(start = CoroutineStart.UNDISPATCHED) {
        withContext(NonCancellable) { accountManager.logout() }
    }
    drawerState.close()
}
```

```kotlin
// Wrong: may never run if the scope is already cancelling
scope.launch(NonCancellable) { accountManager.logout() }
```

**Inject the IO dispatcher in new presenters and move heavy work onto it with `flowOn` or `withContext`.** Declare `@param:DispatcherType(IO) private val ioDispatcher: CoroutineDispatcher` (imports `org.ccci.gto.android.common.dagger.coroutines.DispatcherType` and `...DispatcherType.Type.IO`) so tests can pass a test dispatcher and stay deterministic. See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/lessons/LessonsPresenter.kt:75,184`.

```kotlin
@Composable
private fun rememberLanguagesFlow() = remember {
    languagesRepository.getLanguagesFlow()
        .combine(settings.appLanguageFlow) { languages, appLanguage ->
            languages.sortedWith(Language.displayNameComparator(context, appLanguage))
        }
        .flowOn(ioDispatcher)
}
```

Note: pr-review `SKILL.md` suggests a hardcoded `Dispatchers.IO`; newer presenters inject the dispatcher instead. Follow the code.

**Read flows in presenters and composables with `collectAsState(initial)`, not `collectAsStateWithLifecycle`.** There are 58 `collectAsState` uses and no lifecycle variant. Wrap the flow in `remember { }` so it is not rebuilt on every recomposition.

```kotlin
val tools by remember { toolsRepository.getNormalToolsFlow().flowOn(ioDispatcher) }
    .collectAsState(emptyList())
```

**Tendency: use `SharingStarted.WhileSubscribed()` or `WhileSubscribed(5_000)` for `shareIn`/`stateIn`, with `replay = 1` on `shareIn`.** This keeps upstream work idle when nobody listens. Write the timeout with an underscore literal (`5_000`). `Eagerly` appears only where the value must always be warm (e.g. `activeProviderFlow` in `GodToolsAccountManager.kt:58`). Which of the two `WhileSubscribed` forms is used varies by class; match the file.

```kotlin
val userFlow = accountManager.userIdFlow
    .flatMapLatest { it?.let { userRepository.findUserFlow(it) } ?: flowOf(null) }
    .shareIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), 1)
    .distinctUntilChanged()
```

(`library/user-data/src/main/kotlin/org/cru/godtools/user/data/UserManager.kt:24-27`)

**Tendency: guard a single shared resource with `Mutex`, not `Semaphore`.** Main code has 14 `Mutex()` and no `Semaphore`.

## 4. Formatting, diffs and ktlint

**Run `./gradlew :build-logic:ktlintCheck ktlintCheck` and fix every failure before asking for review.** Frett requested changes on PR #4254 for failing ktlint alone. Style is `android_studio`, max line length 120, 4-space indent.

**Review the formatter's diff and revert anything your change did not need.** Frett on PR #4185: "the auto-formatter doesn't always do what it should." Undo odd wrapping, added blank lines, and edits to untouched lines. He reverted exactly this kind of churn in d3f8154.

**Stack related annotated properties with no blank line between them; use one blank line only between unrelated groups.** The IDE likes to insert a blank line after every annotated property. Frett on PR #4263: "I'll usually try and group related properties together without extra line returns." See `ui/tract-renderer/src/main/kotlin/org/cru/godtools/tract/activity/TractActivity.kt:90-94`. Leave existing blank-separated sites (e.g. `BaseToolActivity.kt:75`) alone unless you are already editing them.

```kotlin
@Inject
internal lateinit var settings: Settings
@Inject
internal lateinit var toolsRepository: ToolsRepository
```

```kotlin
// Formatter output to undo
@Inject
internal lateinit var settings: Settings

@Inject
internal lateinit var toolsRepository: ToolsRepository
```

**Do not add or remove trailing commas on lines you are not otherwise changing.** ktlint does not enforce them, so toggling them is pure diff noise. In new multi-line declarations follow the file: newer Circuit UiState/Presenter code usually has them, older Layouts and Activities usually do not.

## 5. Control flow and expressions

**Use `when` to choose between named options, even with only two branches, and never write `} else if` chains.** Frett on PR #4263: "I tend to like using when blocks better to switch between multiple options instead of an if-else" (applied in ead927d). Main has about 240 `when` uses against 4 legacy `else if` chains. A plain `if (flag) a else b` on a Boolean is still fine.

```kotlin
when (page) {
    Page.LIVE_SHARE_START -> Row { ... }
    else -> Button { ... }
}
```

```kotlin
// Avoid
if (page == Page.LIVE_SHARE_START) {
    Row { ... }
} else {
    Button { ... }
}
```

The same shape handles nullable choices, as in `Settings.kt:162-165`:

```kotlin
when (locale) {
    null -> remove(KEY_DASHBOARD_FILTER_LOCALE)
    else -> set(KEY_DASHBOARD_FILTER_LOCALE, locale.toLanguageTag())
}
```

**Keep `when` branches separate when each body needs its own smart cast.** Collapsing to `is A, is B ->` leaves the subject typed as the common supertype, so it will not compile if the body uses a subtype-only member. Frett dismissed this "simplification" on PR #4459 (see `build-logic/src/main/kotlin/AndroidConfiguration.kt:77-81`).

**Use expression bodies for single-expression functions and getters.** Write `fun x() = expr` or `get() = expr`; main has no block bodies that only `return` a value. Chain `?.` and `?:` across lines instead of adding temporaries.

```kotlin
override fun getItemPositionFromId(id: Long) = manifest?.pages
    ?.indexOfFirst { id == Ids.generate(it.id) }
    ?.takeUnless { it < 0 }
    ?: POSITION_NONE
```

(`ui/tract-renderer/src/main/kotlin/org/cru/godtools/tract/adapter/ManifestPagerAdapter.kt:95-98`)

**Handle nulls with `?.let {}`, early `?: return`, `.orEmpty()` and `takeIf`/`takeUnless`; do not add `!!`.** Only 7 legacy `!!` remain in main, and Frett pushed back on unsafe access on PR #4568. Use `requireNotNull`/`checkNotNull` only for real invariants.

```kotlin
val url = intent.getStringExtra(EXTRA_URL) ?: run { finish(); return }
```

**Tendency: when an `indexOf`/`indexOfFirst` result feeds a framework API, map -1 to the right sentinel.** `PagerAdapter` reads -1 as `POSITION_UNCHANGED`, a bug Frett fixed in 7bd9733. Use `?.takeUnless { it < 0 } ?: POSITION_NONE` (above) or `?.takeIf { it != -1 } ?: return`.

## 6. Regions

**Group members of larger classes in `// region <Name>` ... `// endregion <Name>` pairs, repeating the exact name on the endregion line.** All 491 endregion markers in the repo carry a label (only a couple of old tests have a mismatched one), and none use `//region` without a space, so a bare or mismatched marker stands out and breaks IDE folding labels. Common names: `Lifecycle`, `Intent Processing`, `UI`, `Data Model`, `Sync Methods`, `UiState / UiEvent`, or an implemented interface name. See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/lessons/LessonsPresenter.kt:79` and `app/src/main/kotlin/org/cru/godtools/ui/dashboard/DashboardActivity.kt:65-169`.

```kotlin
// region Lifecycle
override fun onCreate(savedInstanceState: Bundle?) { ... }
override fun onDestroy() { ... }
// endregion Lifecycle
```

**In test classes, use one region per member under test and add new cases inside the existing region.** Name it after the member: `// region syncTools()`, `// region State.mode`, `// region UiEvent.Dismiss`, `// region Property userId`. Frett asked for this on PR #4167. Do not make one region per test case. See `app/src/test/kotlin/org/cru/godtools/ui/dashboard/lessons/LessonsPresenterTest.kt:136,163,174`. A few older tests have mismatched endregion names (`TutorialPresenterTest.kt:45`, `TranslationTest.kt:51`); do not copy them.

## 7. Naming

**Put `Flow` in the name of anything that returns a Flow, with qualifiers after it.** Examples: `getToolsFlowByType`, `getLanguagesFlowForLocales`, `getTranslationsFlowForTools`. When a one-shot value also exists, give the suspend version the bare name (`findTool()` / `findToolFlow()`). Name repository methods that write sync data `store<Thing>FromSync` and make them `suspend`. 65 of 66 Flow-returning functions follow this. See `library/db/src/main/kotlin/org/cru/godtools/db/repository/ToolsRepository.kt:13-57`.

```kotlin
suspend fun findTool(code: String): Tool?
fun findToolFlow(code: String): Flow<Tool?>
suspend fun storeToolsFromSync(tools: Collection<Tool>)
```

```kotlin
// Avoid
fun observeTool(code: String): Flow<Tool?>
```

**Tendency: name the outer lambda parameter when lambdas nest and both would use `it`.** This is a low-priority readability nit; existing code (e.g. `LessonsFlowProducer.kt:30,33`) still nests `it`, so do not rewrite old code for it.

```kotlin
tools.map { tools -> tools.filter { it.isFavorite } }
```

## 8. Types, collections and constants

**Model closed hierarchies as `sealed interface` with `data object` and `data class` members.** 73 `data object` and 31 `sealed interface` versus 4 legacy `sealed class`. `data object` gives a readable `toString` and correct `equals` for free. Use `sealed class` only when it must extend a class (like an Exception). New singleton Screens are `data object`.

```kotlin
internal sealed interface UiEvent : CircuitUiEvent {
    data object Back : UiEvent
    data class SelectLanguage(val language: Locale) : UiEvent
}
```

Note: pr-review `SKILL.md` says a Screen "is an object or data class"; new singleton screens use `data object`. Follow the code.

**Type UiState collections as plain `List`, not `ImmutableList`/`PersistentList`.** Frett converted existing UiStates to `List` in PRs #4389 and #4403 to settle on one convention. `ToolDetailsScreen.kt:33-34` still has two `ImmutableList` fields; do not copy them.

```kotlin
@ConsistentCopyVisibility
data class UiState internal constructor(
    val lessons: List<ToolCardPresenter.UiState> = emptyList(),
    internal val eventSink: (UiEvent) -> Unit = {},
) : CircuitUiState
```

**Declare Intent extras, preference keys and work names as `const val`, usually private or internal at file level.** Put them in a companion object only when they are part of the class API (see `CircuitActivity.kt:33-37`). Reuse the same constant in producer and consumer so keys cannot drift.

**Tendency: put qualifier annotations on constructor properties with the `@param:` use-site target.** Frett added this deliberately (bd763f3) so the qualifier lands on the constructor parameter Hilt reads, and newer presenters all do it. Own-line and inline placement both occur; match the file. Older files like `ToolDetailsPresenter.kt:81,95` lack the target.

```kotlin
class HomePresenter @AssistedInject constructor(
    @param:ApplicationContext
    private val context: Context,
    ...
    @param:DispatcherType(IO) private val ioDispatcher: CoroutineDispatcher,
)
```

**Pass Fragment arguments with a no-arg primary constructor, a secondary constructor, and a splitties `by arg()` property.** Frett suggested exactly this shape on PR #4263. Note the space before `: this()` and the blank line before the property. See `ui/tract-renderer/src/main/kotlin/org/cru/godtools/tract/ui/liveshare/LiveShareStartingDialogFragment.kt:21-27`.

```kotlin
class LiveShareStartingDialogFragment() : DialogFragment() {
    constructor(showQrCode: Boolean) : this() {
        this.showQrCode = showQrCode
    }

    internal var showQrCode: Boolean by arg()
}
```

## 9. Locale handling

**Convert `Locale` to and from strings with `toLanguageTag()` and `Locale.forLanguageTag()`.** Frett asked for this on PRs #4185 and #4588. `locale.toString()` gives `zh_CN`-style strings that do not round-trip as BCP-47 tags. Do not use the deprecated `Locale("lv")` constructor in main code, and when an API is deprecated, switch to its replacement rather than suppressing the warning. See `library/base/src/main/kotlin/org/cru/godtools/base/Settings.kt:164`.

```kotlin
val locale = Locale.forLanguageTag("lv")
prefs.edit { putString(KEY_LANGUAGE, locale.toLanguageTag()) }
```

```kotlin
// Avoid
val locale = Locale("lv")
prefs.edit { putString(KEY_LANGUAGE, locale.toString()) }
```

Tests still use `Locale("..")` in many places; the rule is enforced on main code.

## 10. Logging and exceptions

**Log only through `Timber.tag(TAG)`, never `android.util.Log` or `println`.** Every Timber call in main code is tagged. Declare the tag as a file-level `private const val TAG = "ClassName"` (see `library/account/src/main/kotlin/org/cru/godtools/account/provider/facebook/FacebookAccountProvider.kt:38,115`). A few files pass an inline string tag instead; do not copy that in new code. `Timber.e` is reported to Crashlytics, so log expected failures with `.d` (see `references/data-di.md`, Logging and exception handling).

**Catch specific exception types, and name an unused exception `_`.** Only use a broad `catch (e: Exception)` at a subsystem boundary whose failure must not crash the app (`executeSync` in `GodToolsSyncService.kt:57-66`, account login, initial-content import): rethrow `CancellationException` first, treat `IOException` as expected, and log the rest with `.e`. Everywhere else a broad catch hides real bugs.

```kotlin
} catch (_: IOException) {
    false
}
```

(`library/download-manager/src/main/kotlin/org/cru/godtools/downloadmanager/GodToolsDownloadManager.kt:350`)

## 11. Smaller habits

**Tendency: when fixing a crash in derived Compose state, key `remember` on every input it captures and add the narrowest guard at the failing expression.** Explain the cause and the guard in the commit body with `Fixes #issue`. See 7b5f324 in `ui/tutorial-renderer/.../TutorialLayout.kt:62-64`.

```kotlin
val currentPage by remember(pages) {
    derivedStateOf { pages[pagerState.currentPage.coerceAtMost(pages.lastIndex)] }
}
```

**Name tests with backtick strings like `` `Subject - member - case` `` and declare fixtures as `val`.** Frett called a vague generated name "a poor name" on PR #4267 and raised `var` fields on PR #4415. Use `var` only for state individual tests reassign. `references/testing.md` covers Turbine, `TestScope` and the rest.

**Tendency: if an AI tool generated tests, rename them to this style, merge duplicates, keep only meaningful cases, and say so in the PR body.** Frett asked for this on PR #4267.
