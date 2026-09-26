# Circuit screens and Compose UI

How the code owners build Circuit features (Screen, Presenter, Layout), split presenters into smaller pieces, navigate between screens and Activities, and write Compose UI. Read this before adding or changing a screen, presenter, Layout, or any Compose entry point. The last section lists where `.claude/rules/design_system_rules.md` and the pr-review docs disagree with the real code; follow the code.

## Contents

1. [Feature file layout](#feature-file-layout)
2. [Screen shape](#screen-shape)
3. [Presenter shape](#presenter-shape)
4. [Presenter decomposition](#presenter-decomposition)
5. [State inside present()](#state-inside-present)
6. [Navigation and activity hosting](#navigation-and-activity-hosting)
7. [Layout shape](#layout-shape)
8. [Lists, loading and Compose conventions](#lists-loading-and-compose-conventions)
9. [Where the code differs from the docs](#where-the-code-differs-from-the-docs)

## Feature file layout

**Give each feature its own package with FooScreen.kt, FooPresenter.kt and FooLayout.kt.** All 13 `@CircuitInject` pairs use this shape, and Daniel moved features into `ui/settings/<feature>` packages in a run of commits (8639474, 8729a0f). Optional siblings are `FooLayout+Preview.kt`, `FooModule.kt`, `FooDeepLinkParser.kt`, `*FlowProducer.kt` and `*StateProducer.kt`. Composables used by several features go in `app/.../ui/common` (93f8711).

```
ui/settings/country/
  CountrySettingsScreen.kt      // @Parcelize screen (+ Result)
  CountrySettingsPresenter.kt   // presenter, nested UiState/UiEvent, Factory
  CountrySettingsLayout.kt      // @CircuitInject UI named CountrySettingsLayout
```

Name the UI composable `FooLayout`, never `FooScreen`, `FooContent` or `FooUi`. See `app/src/main/kotlin/org/cru/godtools/ui/settings/language/` for a full set with a module and deep link parser.

**When adding a variant of something that already exists, copy the sibling.** Reuse its names, shapes, constants and test layout, and say "Mirrors X" in the commit body (830fe94 mirrors the Tools personalization toggle in Lessons). Reviewers approve parallel code fast because they can diff it against the original.

## Screen shape

**Declare screens as `@Parcelize data object FooScreen : ParcelableScreen`, or a `data class` when it has arguments.** `CircuitActivity` and `startCircuitActivity` only accept `ParcelableScreen`, so a bare `Screen` cannot be hosted. Keep the screen in the feature package. Only move it to `ui/base/src/main/kotlin/org/cru/godtools/base/ui/circuit/screen/` when another module has to navigate to it (dashboard pages, `AppLanguageScreen`), which avoids sibling module dependencies (03aa9e2).

```kotlin
@Parcelize
data object DeleteAccountScreen : ParcelableScreen

@Parcelize
data class ToolDetailsScreen(val initialTool: String, val secondLanguage: Locale? = null) : ParcelableScreen
```

A few older screens are plain `object` (LanguageSettingsScreen, AllFavoritesScreen). New ones are `data object`. Group sibling pages under an abstract base, like `abstract class DashboardPage : ParcelableScreen`.

**Put screen results inside the Screen as a sealed `ParcelablePopResult` with `@Parcelize` variants.** Parcelable results survive process death and are how `CircuitActivity` hands results back to legacy callers.

```kotlin
@Parcelize
data object CountrySettingsScreen : ParcelableScreen {
    sealed interface Result : ParcelablePopResult {
        @Parcelize data object CountrySelected : Result
        @Parcelize data object Dismissed : Result
    }
}
```

See `app/src/main/kotlin/org/cru/godtools/ui/settings/country/CountrySettingsScreen.kt`. Do not put UiState or UiEvent in the Screen file (see below).

## Presenter shape

**Nest `UiState` and `UiEvent` at the top of the Presenter and name them exactly that.** Daniel moved state types out of Screens (c924197) and renamed `State`/`Event` to `UiState`/`UiEvent` (d67661a). Import the nested type so signatures read `Presenter<UiState>` and `FooLayout(state: UiState, ...)`. Shared child presenters (ToolCardPresenter) nest them in the interface.

```kotlin
class HomePresenter @AssistedInject constructor(...) : Presenter<UiState> {
    data class UiState(
        val dataLoaded: Boolean = true,
        val favoriteTools: List<ToolCardPresenter.UiState> = emptyList(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data object ViewAllFavorites : UiEvent
    }
```

Legacy spots still hold UiState in the Screen (ToolDetailsScreen, DownloadableLanguagesScreen) or use `State`/`Event` (DrawerMenuScreen). Do not copy them.

**Give every UiState field a default and type collections as `List<T> = emptyList()`.** Defaults let tests and previews call `UiState()`. A review on #4389 moved an `ImmutableList` back to `List` to match the project, so do not add `persistentListOf` to new UiState.

**If UiEvent is `internal`, lock down UiState with `@ConsistentCopyVisibility`, an internal constructor and an internal eventSink.** A public UiState cannot expose an internal event type, and this Kotlin 2 idiom keeps other modules from building or `copy()`ing state (owner review on #4416, #4406). Make the presenter `@AssistedInject internal constructor` too. A fully public UiState with a public UiEvent is also fine and is what most presenters use.

```kotlin
@ConsistentCopyVisibility
data class UiState internal constructor(
    val isSyncing: Boolean = false,
    internal val eventSink: (UiEvent) -> Unit = {},
) : CircuitUiState

internal sealed interface UiEvent : CircuitUiEvent {
    data object TriggerSync : UiEvent
}
```

See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/DashboardPresenter.kt:40-52`, `.../dashboard/lessons/LessonsPresenter.kt:80-96`.

**End the presenter with a nested `@AssistedFactory @CircuitInject` interface that has a plain `create(...)`.** Every presenter does this. Do not extend `Presenter.Factory`, which only appears in the ui/base multibinding. List only the assisted params the presenter uses (Navigator, the Screen, CircuitContext).

```kotlin
@AssistedFactory
@CircuitInject(LessonsScreen::class, SingletonComponent::class)
interface Factory {
    fun create(circuitContext: CircuitContext, navigator: Navigator): LessonsPresenter
}
```

**List injected dependencies first and all `@Assisted` params last.** 12 of 13 presenters do this: context first, plain deps roughly alphabetical, then qualified ones like `@param:DispatcherType(IO) ioDispatcher`. Tendency: new code writes qualifiers with the `@param:` target (`@param:ApplicationContext`), which silences Kotlin 2 target warnings (bd763f3). Bare `@ApplicationContext` still appears in about 6 files.

**Pass eventSink inline when building UiState, as the trailing lambda.** Daniel refactored ToolDetailsPresenter into this shape (e64ee51). Keep each branch short and move multi-line logic into a private helper on the presenter (662f857).

```kotlin
return UiState(
    dataLoaded = favoriteTools != null,
    favoriteTools = favoriteTools.orEmpty(),
) {
    when (it) {
        UiEvent.ViewAllFavorites -> navigator.goTo(AllFavoritesScreen)
        UiEvent.ViewAllTools -> navigator.resetRoot(ToolsScreen, Navigator.StateOptions.SaveAndRestore)
    }
}
```

Four older presenters pass a named `eventSink = { ... }` argument, which is also inline and fine. Avoid a separate `val eventSink` for a single-variant UiState (AppLanguagePresenter.kt:57 is the one outlier).

Tendency: the exception is a sealed UiState with several variants. Declare `val eventSink` on the interface, build one `val eventSink = remember { { ... } }`, and pass it to whichever variant you return. See `app/src/main/kotlin/org/cru/godtools/ui/account/delete/DeleteAccountPresenter.kt:26-73`.

**Route user actions through a new UiEvent, not ad hoc lambdas from the Activity or Layout.** The owner asked for exactly this on #4254 ("you should add a new share link event"), because presenter logic is what tests cover. Leaf reusable components may still take `onX: () -> Unit` and the Layout wires it to `eventSink`.

**Use plain callbacks, not UiEvents, for wiring inside the presenter.** Only actions the Layout sends are UiEvents. When a child (tool card, variant picker) needs to change presenter-local state, pass a callback like `onVariantSelect: (String) -> Unit` into the private helper and read it with `rememberUpdatedState`. bfbe1ff removed a `SwitchVariant` UiEvent the Layout never sent. Keep card events (`ToolCardEvent`) separate from screen UiEvents. See `app/src/main/kotlin/org/cru/godtools/ui/tooldetails/ToolDetailsPresenter.kt:224-242`.

Tendency: `// region UiState / UiEvent` ... `// endregion UiState / UiEvent` markers appear only in the three dashboard page presenters. Keep them when the file has them; do not add them elsewhere unless you want to.

## Presenter decomposition

**Keep `present()` a short list of `rememberX()` / `childPresenter.present()` calls plus the UiState return.** The owner blocked #4592 until global and account activity were split into sub-presenters. Put each data source in a private `@Composable fun rememberXxx(...)` that returns a value. Name side-effect-only composables in PascalCase (`RegisterSyncTask(locale)`). Give independent screen sections their own child presenter.

```kotlin
@Composable
override fun present(): UiState {
    val appLanguage by settings.appLanguageFlow.collectAsState()
    val languageFilter = rememberLanguagesFilter()
    RegisterSyncTask(languageFilter.selectedItem?.code ?: appLanguage)
    val lessons = rememberLessons(mode, languageFilter.selectedItem?.code ?: appLanguage)
    return UiState(lessons = lessons.orEmpty(), ...) { ... }
}
```

See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/lessons/LessonsPresenter.kt:98-128` and `DashboardPresenter.kt:60` (`drawerMenuPresenter.present()`).

**Put non-composable Flow logic in a concrete `internal class FooFlowProducer @Inject constructor` with its own JVM test.** Mode switching, fallbacks, filtering and sorting belong there, exposed as a plain `fun getFlow(...): Flow<...>`. It is a concrete class with no interface or `@Binds`; presenter tests mock it with plain mockk because it is not composable. Add `FooFlowProducerTest` in `app/src/test`.

```kotlin
internal class LessonsFlowProducer @Inject constructor(
    private val settings: Settings,
    private val toolsRepository: ToolsRepository,
) {
    fun getFlow(mode: Mode, locale: Locale): Flow<List<Tool>> = ...
}

// in the presenter
val lessons by remember(mode, locale) { lessonsFlowProducer.getFlow(mode, locale) }.collectAsState(null)
```

See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/lessons/LessonsFlowProducer.kt`, `.../tools/FilteredToolsFlowProducer.kt`.

**Make composable pieces that tests must replace into an interface with a `Default*` impl, `@Binds`, and a hand-written `Fake*`.** Composable functions cannot be mocked with plain mockk. The owner said on #4392 he wants Fakes for anything with a composable function, and 78d17a4 swapped mockposable mocks for `FakeToolCardPresenter`. Do not add new `everyComposable` mocks.

```kotlin
interface ToolFiltersStateProducer {
    @Composable
    fun produce(mode: Mode): Filters
}

internal class DefaultToolFiltersStateProducer @Inject constructor(...) : ToolFiltersStateProducer

// FooModule: @Binds abstract fun toolFiltersStateProducer(impl: DefaultToolFiltersStateProducer): ToolFiltersStateProducer

// src/test
class FakeToolFiltersStateProducer : ToolFiltersStateProducer {
    val filters = MutableStateFlow(Filters())
    var lastMode: Mode? = null

    @Composable
    override fun produce(mode: Mode): Filters {
        lastMode = mode
        return filters.collectAsState().value
    }
}
```

See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/tools/ToolFiltersStateProducer.kt`, `ToolsModule.kt`, and `app/src/test/kotlin/org/cru/godtools/ui/dashboard/tools/FakeToolFiltersStateProducer.kt`.

**Wrap per-item child presenter calls in `key(id)`.** Without it, reordering hands the wrong remembered state to a child; the owner added missing `key()` calls in #4411. Drop null-id items first when the Layout keys by that id. Use `lateinit var` when the child's eventSink reads the child's own state.

```kotlin
?.mapNotNull { lesson ->
    val lessonCode = lesson.code ?: return@mapNotNull null
    key(lessonCode) {
        lateinit var lessonState: ToolCardPresenter.UiState
        lessonState = toolCardPresenter.present(lesson) { ... lessonState.translation ... }
        lessonState
    }
}
```

See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/home/HomePresenter.kt:90-116`.

**Register pull-to-refresh sync with `circuitContext.rememberSyncTask` from gto-support-sync.** cb4a1b1 replaced the old local registry and its `DisposableEffect`, so ignore older advice praising `DisposableEffect` for sync. Inject `@Assisted private val circuitContext: CircuitContext` and add it to `Factory.create`. Only the root (`DashboardPresenter`) calls `circuitContext.rememberSyncTaskRegistry()`.

```kotlin
@Composable
private fun RegisterSyncTask(locale: Locale) {
    circuitContext.rememberSyncTask(locale) { syncData(locale, it) }
}

private fun SyncTracker.syncData(locale: Locale, force: Boolean = false) = launchSync {
    syncService.syncToolOrder(locale, settings.getCountrySettingFlow().first(), force)
}
```

## State inside present()

**Build flows inside `remember(keys) { ... }` and collect with `.collectAsState(initial)`.** `remember` keeps one upstream subscription across recompositions. The repo has 58 `collectAsState` calls and no `collectAsStateWithLifecycle`, so do not introduce it. Reach for existing composable repository helpers first (`toolsRepository.produceToolState`, `languagesRepository.rememberLanguage`, `attachmentsRepository.rememberAttachmentFile`, `settings.produceAppLocaleState`). Put heavy `combine`/`map` chains on an injected `@param:DispatcherType(IO) ioDispatcher` with `.flowOn(ioDispatcher)`. Add `@SuppressLint("FlowOperatorInvokedInComposition")` only where mapping a collected value is on purpose (see `LessonsPresenter.rememberLessons`).

**When a flow depends on an input that changes, turn it into a flow once and `flatMapLatest`.** Use gto-support `rememberStateFlow(value)` or `snapshotFlow { }`, and annotate with `@OptIn(ExperimentalCoroutinesApi::class)`. Read values used inside long-lived lambdas through `rememberUpdatedState`. A plain `remember(key)` rebuild is also used (LessonsPresenter), and neither is wrong. See `app/src/main/kotlin/org/cru/godtools/ui/tools/DefaultToolCardPresenter.kt:69-85`.

**Use `rememberSaveable` for user choices, and to read layout-deciding flags once per screen.** The owner on #4392: load the remote config value once with `rememberSaveable` so the UI does not jump if it changes while the user is looking. Use plain `remember` for derived or transient state; `rememberRetained` is rare.

```kotlin
val isPersonalizationEnabled = rememberSaveable {
    remoteConfig.getBoolean(CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED)
}
var mode by rememberSaveable {
    mutableStateOf(if (isPersonalizationEnabled) UiState.Mode.PERSONALIZATION else UiState.Mode.ALL_LESSONS)
}
```

**Gate a new user-visible mode behind a Remote Config flag in `library/base/src/main/kotlin/org/cru/godtools/base/Config.kt`.** Add `const val CONFIG_UI_<AREA>_<FEATURE>_ENABLED = "ui_<area>_<feature>_enabled"` and a default in `CONFIG_DEFAULTS` (false for unreleased work). Snapshot it as above, expose `isXEnabled` on UiState with a `// TODO: temporary until ... rolled out` comment, and stub `every { getBoolean(FLAG) } answers { isXEnabled }` in tests. Personalization shipped this way (#4392).

**Model a screen toggle as an `enum class Mode` nested in UiState, changed only by `UiEvent.ChangeMode(mode)`.** Hold it in `rememberSaveable`, expose `UiState.mode`, and pass `mode` into producers and remember keys. Tools and Lessons share this exact shape (`ToolsPresenter.kt:77`, `LessonsPresenter.kt:89`).

**Put search queries and menu-expanded flags in UiState as `MutableState` that the Layout writes directly.** This avoids an event round trip per keystroke, and Daniel dismissed review flags on it as intentional (`.claude/skills/pr-review/dismissed-issues.md`). Create it with `rememberSaveable { mutableStateOf("") }` in the presenter and read `query.value` there.

```kotlin
data class UiState(
    val query: MutableState<String> = mutableStateOf(""),
    ...
)
// Layout
var query by state.query
SearchBarDefaults.InputField(query = query, onQueryChange = { query = it }, ...)
```

See `app/src/main/kotlin/org/cru/godtools/ui/settings/country/CountrySettingsPresenter.kt:42` and `CountrySettingsLayout.kt:66`.

**When the presenter must drive a Compose controller, create it in the presenter and expose it on UiState.** `rememberPagerState`, `rememberDrawerState` and `remember { SnackbarHostState() }` live in the presenter so event handling can call `pagerState.animateScrollToPage()` without extra events. See `app/src/main/kotlin/org/cru/godtools/ui/onboarding/OnboardingPresenter.kt:60-82`.

**Keep one-shot side effects in presenter `LaunchedEffect`s; Layout `LaunchedEffect`s are only for view behavior.** Scroll-to-top, animation timers and drag handling are fine in a Layout. Loading data or navigating from a Layout effect is not, since tests would not see it.

```kotlin
// Presenter
LaunchedEffect(Unit) { settings.setFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING) }
// Layout
LaunchedEffect(state.banner?.type) { if (state.banner != null) columnState.animateScrollToItem(0) }
```

**Post action analytics from the presenter and record screen views in the Layout.** Call `eventBus.post(XAnalyticsActionEvent(...))` in event handling just before navigating, so presenter tests can assert it. Call `RecordAnalyticsScreen(FooScreenEvent(...))` in the Layout, never in a presenter. See `app/src/main/kotlin/org/cru/godtools/ui/tooldetails/ToolDetailsLayout.kt:149`.

## Navigation and activity hosting

**Open Activities from a presenter with `navigator.goTo(IntentScreen(...))`.** Build the intent with the existing helpers and bail out if it is null.

```kotlin
val intent = context.createToolIntent(type = toolType, toolCode = toolCode, languages = languages) ?: return
eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL, toolCode, SOURCE_TOOL_DETAILS))
navigator.goTo(IntentScreen(intent))
```

**Show a Circuit screen from non-Circuit code with `context.startCircuitActivity(FooScreen)`, not a new Activity.** b65cdda replaced `ToolDetailsActivity` with a Circuit-hosted screen; `CircuitActivity` already applies `GodToolsTheme`, overlays and back handling. Use `createCircuitActivityIntent(screen)` when you need an Activity result contract. See `ui/base/src/main/kotlin/org/cru/godtools/base/ui/circuit/CircuitActivity.kt:27-28`.

**Get screen results with `rememberAnsweringNavigator`, not Activity result contracts or bridge classes.** The callee calls `navigator.pop(FooScreen.Result.X)`. #4337 deleted bridge classes once this was in place.

```kotlin
val countrySettingsNavigator = rememberAnsweringNavigator<CountrySettingsScreen.Result>(navigator) {
    hasSeenCountrySettings = true
}
...
countrySettingsNavigator.goTo(CountrySettingsScreen)
```

See `app/src/main/kotlin/org/cru/godtools/ui/onboarding/OnboardingPresenter.kt:62-77`.

**Add deep links as a pure `object FooDeepLinkParser : CircuitDeepLinkParser`, bound into a set in FooModule, and unit test it directly.** `LanguageSettingsDeepLinkParser` is the one parser bound this way so far (`DashboardDeepLinkParser` is still called directly from `DashboardActivity`). #4406 replaced a Hilt Activity test with a parser unit test (`app/src/test/kotlin/org/cru/godtools/ui/dashboard/DashboardDeepLinkParserTest.kt`). Parsers only parse and expose values (nullable when optional); the caller owns defaults and loading (owner on #4267).

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object LanguageSettingsModule {
    @get:Provides
    @get:IntoSet
    val languagesDeepLinkParser: CircuitDeepLinkParser get() = LanguageSettingsDeepLinkParser
}
```

**Put cross-module Activity launch helpers in `ui/base/src/main/kotlin/org/cru/godtools/base/ui/Activities.kt`.** Add the extension next to the existing intent builder, like `fun Activity.startQrCodeActivity(data: String) = startActivity(createQrCodeActivityIntent(data))` (line 121). This avoids compile-time links between sibling UI modules.

**For a screen with inner pages, keep the page back stack in the Layout and forward other navigation as `UiEvent.NestedNavEvent`.** The Layout owns `rememberSaveableBackStack`, `rememberCircuitNavigator` and `NavigableCircuitContent`; the presenter handles `is UiEvent.NestedNavEvent -> navigator.onNavEvent(it.event)`. Switch pages with `resetRoot(page, ...)` using save/restore state. See 08e2934 and `app/src/main/kotlin/org/cru/godtools/ui/dashboard/DashboardLayout.kt`.

**For Fragments with arguments, use `by arg()` and a secondary constructor.** Daniel replaced a contributor's Bundle plumbing with this on #4263, and there are no `newInstance()` factories in the code.

```kotlin
constructor(showQrCode: Boolean) : this() {
    this.showQrCode = showQrCode
}

internal var showQrCode: Boolean by arg()
```

## Layout shape

**Declare the Layout as `@Composable`, then any `@OptIn`, then `@CircuitInject`, taking exactly `(state: UiState, modifier: Modifier = Modifier)`.** All 13 Layouts use this signature, and 8 of 13 are `internal`. Apply `modifier` to the outermost element.

```kotlin
@Composable
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DashboardScreen::class, SingletonComponent::class)
internal fun DashboardLayout(state: UiState, modifier: Modifier = Modifier) {
```

**Pass `state` to private Layout pieces and read eventSink with `rememberUpdatedState`.** This avoids long parameter lists that only forward state (19 uses of `rememberUpdatedState(state.eventSink)`). Leaf reusable components take explicit values and `onX` callbacks instead.

```kotlin
@Composable
@VisibleForTesting
internal fun ToolDetailsActions(state: UiState, modifier: Modifier = Modifier) = Column(modifier = modifier) {
    val eventSink by rememberUpdatedState(state.eventSink)
    Button(onClick = { eventSink(UiEvent.OpenTool) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_tools_open_tool))
    }
}
```

**Expose test hooks as `internal const val TEST_TAG_*` and put `.testTag(...)` first in the modifier chain.** Mark sub-composables that tests render directly `@VisibleForTesting internal`. Do not widen visibility just for Paparazzi; the owner asked on #4583 to snapshot the whole Layout instead. See `ToolDetailsLayout.kt:81-84`.

**Put previews in a sibling `FooLayout+Preview.kt` as private functions wrapped in `GodToolsTheme`.** `DeleteAccountLayout+Preview.kt` is the model (a few older preview files skip the theme; do not copy them). One preview per state variant. Previews are optional; Paparazzi tests in `src/testDebug` are the real visual check.

```kotlin
@Preview
@Composable
private fun DeleteAccountLayoutErrorPreview() {
    GodToolsTheme { DeleteAccountLayout(UiState.Error()) }
}
```

## Lists, loading and Compose conventions

**Show loading with a nullable flow and a `dataLoaded` flag, and render nothing until it is loaded.** This keeps the empty state from flashing before Room emits; no screen uses a spinner for initial load. Collect with `collectAsState(null)`, set `dataLoaded = a != null` (default `true` in UiState), and pass `a.orEmpty()`. In the Layout, bail early and show an empty item only when loaded and empty. Use `Modifier.invisibleIf(!isLoaded)` for per-card placeholders; save progress indicators for real progress (downloads, deleting).

```kotlin
LazyColumn(state = columnState, modifier = modifier) {
    if (!state.dataLoaded) return@LazyColumn
    ...
}
```

See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/home/HomeLayout.kt:52`, `.../lessons/LessonsLayout.kt:75`.

**In lazy feeds, use a string key plus contentType, start item modifiers with `animateItem()`, and pad each item with a file-level margin constant.** Per-item padding lets conditional rows animate in and out without stray gaps; the owner swapped literal `16.dp` for the margin constant in b283b28. Use the same string for key and contentType on fixed rows, and prefix keys when lists share a column (`"tool:${...}"`).

```kotlin
internal val MARGIN_LESSONS_LAYOUT_HORIZONTAL = 16.dp

item("header", "header") { ... }
items(state.lessons, { it.toolCode.orEmpty() }, { "lesson" }) { toolState ->
    LessonToolCard(
        toolState,
        modifier = Modifier
            .animateItem()
            .padding(top = 16.dp, horizontal = MARGIN_LESSONS_LAYOUT_HORIZONTAL)
    )
}
```

**Do not key a lazy list with `.orEmpty()` on a nullable id unless the presenter already dropped null ids.** Two null ids both become `""` and crash with "Key was already used"; the owner flagged this on #4440. Otherwise omit `key` or use `key = { it.id ?: it }`. The dashboard `toolCode.orEmpty()` keys are only safe because HomePresenter drops null codes and tools always have codes.

**Wrap each Compose entry point in `GodToolsTheme`, and never call it inside a Circuit Layout.** There is no single app root: about 11 Activities, Fragments and pager holders host Compose. `CircuitActivity` applies the theme for all Layouts. Tool renderers pass `darkTheme = false`. Set `DisposeOnViewTreeLifecycleDestroyed` on ComposeViews in Fragments.

```kotlin
ComposeView(requireContext()).apply {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent { GodToolsTheme { ArticlesContent() } }
}
```

See `ui/article-renderer/src/main/kotlin/org/cru/godtools/article/ui/articles/ArticlesFragment.kt:81-87`.

**Write new UI in Compose, and prefer migrating DataBinding code you touch.** The owner calls this ongoing work to move away from DataBinding (#4440). For tract pages and tips, host the shared composables (`RenderTractPage`, `RenderTip`) in a ComposeView with `GodToolsTheme` and `ProvideRendererServices` (#4511, #4562).

**Change shared renderer UI in CruGlobal/kotlin-mpp-godtools-tool-parser, not by wrapping it in Android.** The owner requested changes on #4254 for adding a share button around the lesson app bar in `LessonActivity`.

**Use gto-support Compose helpers before writing your own.** Prefer the multiplatform `org.ccci.gto.android.common.compose.*` packages where a helper exists there: `compose.foundation.layout.padding` (not the deprecated `...androidx.compose.foundation...` copy), `compose.ui.draw.invisibleIf`, `compose.util.rememberStateFlow`. Material3 helpers such as `LazyDropdownMenu` and `AppBarActionButton` still come from `org.ccci.gto.android.common.androidx.compose.material3.*`. Also check `ElevatedCard`, `minLinesHeight` and `annotatedStringResource`. b6eca20 updated the deprecated imports.

**Use Material 3 building blocks over hand-drawn equivalents.** `SingleChoiceSegmentedButtonRow`/`SegmentedButton` with `SegmentedButtonDefaults.itemShape`, `Label` + `PlainTooltip`, `SliderDefaults.Thumb`. Keep sizes on the 4dp grid, and keep `Color(0xFF...)` literals out of feature code (there are none outside the ui/base theme).

Tendency: show tool banners with `AsyncImage(model = state.banner, contentDescription = null, contentScale = ContentScale.Crop)` where `banner: File?` comes from `attachmentsRepository.rememberAttachmentFile(...)` in the presenter. Layouts do not build URLs or ImageRequests.

Tendency: in tool renderers, colors from the tool manifest go through `toComposeColor()`. This is the accepted exception to token-only colors (owner self-review on #4362).

## Where the code differs from the docs

These are cases where `.claude/rules/design_system_rules.md` or the pr-review docs say one thing and the code does another. Follow the code.

- Note: design_system_rules.md section 2 says apply `GodToolsTheme` exactly once at the app root; the code applies it at each of about 11 Compose roots (CircuitActivity, Fragments, ComposeViews), never inside Layouts. Follow the code.
- Note: design_system_rules.md section 8 shows text fields sending `UiEvent.QueryChanged(it)`; no such event exists, and all query fields are `MutableState` in UiState. Follow the code.
- Note: design_system_rules.md section 14 item 10 asks for distinct `isLoading`/`error`/`isEmpty` UI; screens use a `dataLoaded` flag and render nothing while loading. Follow the code.
- Note: design_system_rules.md section 5 says prefer `Arrangement.spacedBy`; lazy feeds use per-item `padding(top = ...)` with `animateItem()` (48 padding-top uses vs 15 spacedBy in app). Keep spacedBy for plain Column/Row, not lazy feeds. Follow the code.
- Note: design_system_rules.md section 10 bans `.orEmpty()` keys outright; four dashboard keys use `toolCode.orEmpty()` where null ids cannot reach the list. Follow the rule unless the presenter already drops nulls.
- Note: design_system_rules.md does not mention manifest colors; `toComposeColor()` on manifest colors is accepted in renderers.
- Note: pr-review SKILL.md says a Screen implements `Screen`; every screen implements `ParcelableScreen`. Follow the code.
- Note: pr-review patterns.md shows `interface Factory : Presenter.Factory<FooScreen, FooPresenter>`; no presenter does this. Use the plain `@AssistedFactory` interface. Follow the code.
- Note: pr-review patterns.md shows `Result : PopResult` without `@Parcelize`; all real results are `ParcelablePopResult` with `@Parcelize` variants. Follow the code.
- Note: pr-review patterns.md names the UI composable `FooScreen(...)`; the code always uses `FooLayout`. Follow the code.
- Note: pr-review SKILL.md says UiEvent is marked internal; only 3 of 16 are, and each pairs with the `@ConsistentCopyVisibility` lock-down. Public UiState plus public UiEvent is fine. Follow the code.
- Note: pr-review SKILL.md says eventSink is never extracted to a local; sealed multi-variant UiState (DeleteAccountPresenter) shares one remembered local on purpose. Follow the code.
