# Tool renderers and legacy migration

How the owners move legacy View/Fragment/DataBinding code to Compose and Circuit, how they host shared tool composables inside the remaining legacy shells, and the rules for tool activities, deep links and share links. Read this before touching anything under `ui/*-renderer`, `ui/base-tool`, a legacy Fragment, or when planning a migration PR.

## Contents

1. [New UI and the legacy frontier](#new-ui-and-the-legacy-frontier)
2. [Migration recipe: switch, then delete orphans](#migration-recipe-switch-then-delete-orphans)
3. [What belongs upstream in kotlin-mpp-godtools-tool-parser](#what-belongs-upstream-in-kotlin-mpp-godtools-tool-parser)
4. [Hosting Compose in legacy Fragments and pagers](#hosting-compose-in-legacy-fragments-and-pagers)
5. [Handling shared renderer state and events](#handling-shared-renderer-state-and-events)
6. [Tool activities and DataModels](#tool-activities-and-datamodels)
7. [Page subtypes](#page-subtypes)
8. [Deep links](#deep-links)
9. [Share links and locales](#share-links-and-locales)
10. [Legacy Fragment and class conventions](#legacy-fragment-and-class-conventions)
11. [Tests](#tests)

## New UI and the legacy frontier

**Build every new screen, dialog or list as a Circuit Presenter + Layout (or Overlay).** Do not add new Fragments, ViewModels/DataModels, LiveData, DataBinding layouts, RecyclerView adapters or XML layouts. In the last 12 months the owners added zero such files and deleted 58, while adding 23 Presenter/Layout/Screen files, so a new legacy class is just future migration work. Precedent: `9a3b5e7` replaced `LessonResumeDialogFragment` with a Circuit Overlay; `88dedd3` moved `CategoriesFragment` off DataBinding/RecyclerView.

```kotlin
// Wrong: new legacy classes
class FooDialogFragment : DialogFragment() { ... }
class FooViewModel : ViewModel() { val items: LiveData<List<X>> = ... }
```

**Know what is still legacy and change it only as needed.** DataBinding is still enabled in `app`, `ui/base` (1 layout), `ui/base-tool` (8), `ui/cyoa-renderer` (5), `ui/tract-renderer` (1) and `ui/article-renderer`. About 13 Fragments remain (CYOA pages, articles, AEM, live share dialogs, tool settings/shareable/tip sheets), plus 16 ViewModel/DataModel classes, LiveData in 26 main files, and RecyclerView in 3 (`SettingsActionsAdapter`, `PageCollectionPageController`, `CardCollectionPageController`). When you must fix something there, extend the existing class. Do not spread its pattern into new code. Dashboard, Drawer and Account are the next Circuit targets (see the TODO in `ui/base/.../circuit/CircuitActivity.kt`).

**Launch Circuit screens from legacy code with `startCircuitActivity(screen)` / `createCircuitActivityIntent(screen)`,** not a new Activity. See `ui/base/src/main/kotlin/org/cru/godtools/base/ui/circuit/CircuitActivity.kt:27-28`.

## Migration recipe: switch, then delete orphans

**Migrate one leaf at a time, in this commit shape.** Every migration by the owners follows it, so reviewers can read the behavior change apart from the mechanical deletions, and nothing dead is left behind.

1. **Switch commit.** Point the host at the new Compose/Circuit/shared renderer, and in the same commit delete the classes, layouts and tests the switch makes directly unreachable.
2. **Orphan commit(s), same PR.** Delete what became transitively unused: binding adapters, styles, drawables, strings, module deps, version catalog entries, and the `enableDatabinding`/kapt setup when the last `<layout>` goes. Title them like `Remove X orphaned by Y` or `remove unused X`.

Each commit must compile on its own. Precedents: `7e83d9c` "Switch tract page rendering to the shared RenderTractPage composable" (+169/-2094) then `2a93b37` "Remove tract code and resources orphaned by the RenderTractPage migration"; `971b3f7` then `c945f14` (disables databinding for tips-renderer); `88dedd3` then `9753065`; `9a3b5e7` then `82623df`.

Anti-pattern: one squashed "Migrate X and cleanup" commit, or leaving old layouts/adapters "for a follow-up PR".

**Shrink APIs in the same commit that removes their last user.** Drop the dead param from the `@AssistedFactory` signature and its callers, make now-local handlers `private`, and expose only the narrow `internal` values the remaining callers need instead of chains through holders or bindings. In `7e83d9c`, `ManifestPagerAdapter.Factory.create(lifecycleOwner, enableTips, toolState)` became `create(lifecycleOwner, toolState)` and gained `internal val primaryPage` / `primaryPageActiveCard`. In `88dedd3`, `CategoriesFragment.onCategorySelected` became private.

**Delete dead legacy API and move its test coverage to a plain unit test of the new class.** When a migration removes the last caller of a method, enum, or intent extra, delete it and its tests. Coverage moves off slow Robolectric activity tests: deep link tests moved from `DashboardActivityTest` to `DashboardDeepLinkParserTest` in `e9ac6fc`, then `07afa08` removed the legacy `Page` enum. Circuit deep links go in an `object` implementing `CircuitDeepLinkParser`:

```kotlin
object LanguageSettingsDeepLinkParser : CircuitDeepLinkParser {
    override fun isDeepLinkSupported(uri: Uri) = when {
        uri.scheme in listOf("https", "http") &&
            uri.host == HOST_GODTOOLSAPP_COM &&
            uri.path == "/deeplink/settings/language" -> true
        else -> false
    }

    override fun parseDeepLink(uri: Uri) = listOf(LanguageSettingsScreen)
}
```

See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/DashboardDeepLinkParser.kt:16`.

**Do not keep the old view stack behind a flag.** The switch commit replaces it outright.

## What belongs upstream in kotlin-mpp-godtools-tool-parser

**Put tool content rendering and in-tool chrome in `CruGlobal/kotlin-mpp-godtools-tool-parser`, not in Android.** That covers lesson app bar actions, tract page and card UI, tips, and pager helpers like `LessonPagerState.settledPage`. Android modules only host those composables and route their events. Shared UI keeps Android and iOS in sync, and the owners redirected both a lesson share button (PR #4254) and a `settledPage` helper (PR #4568, upstream #1234) there in review.

```kotlin
// Wrong: chrome added in LessonActivity instead of RenderLesson upstream
ShareButton(onClick = ::shareLesson)
```

**Add a new event to the existing UiEvent/eventSink rather than a parallel callback.** For example add `LessonScreen.UiEvent.ShareLesson` and handle it where the other lesson events are handled (PR #4254 r2591599160).

**Open the upstream PR, then bump `godtoolsShared` to its `-SNAPSHOT` without waiting for the merge.** Bump `godtoolsShared` in `gradle/libs.versions.toml` in its own one-file commit (it is currently `1.4.0-SNAPSHOT`), open the Android PR while the upstream PR is still in review, and add a `## Dependencies` section linking the upstream PR and noting that CI fails until the snapshot is published (PR #4562, #4511). Never commit an `includeBuild` substitution; use it locally only. Details in `references/commits-prs.md` (Cross-repo dependencies).

## Hosting Compose in legacy Fragments and pagers

**When a legacy Fragment must stay, return a `ComposeView` with `DisposeOnViewTreeLifecycleDestroyed`.** Keep the existing DataModel as the data source and read its flows with `collectAsState()`. Without the strategy the composition outlives the Fragment's view. See `ui/article-renderer/.../categories/CategoriesFragment.kt:61-72`, `ArticlesFragment.kt:81-84`, `TipBottomSheetDialogFragment.kt:84-90`.

```kotlin
override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
    ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            GodToolsTheme {
                ProvideRendererServices(resourceFileSystem) {
                    CategoriesContent()
                }
            }
        }
    }
```

Note: `design_system_rules.md` section 2 (and pr-review `patterns.md`) say to apply `GodToolsTheme` exactly once at the app root. The code wraps `GodToolsTheme` once per Compose root: every Activity `setContent` and every legacy `ComposeView` host (CategoriesFragment, ArticlesFragment, TipBottomSheetDialogFragment, ManifestPagerAdapter, TractActivity, LessonActivity, CircuitActivity, and others). Follow the code: once per Compose root, never inside a composable.

**Wrap shared tool composables in `GodToolsTheme(darkTheme = false)` and `ProvideRendererServices(resourceFileSystem, tipsRepository)`.** Tool content is authored for a light background, and the renderer needs the resource file system and tips repository in composition to load images and tips. Inject the file system with `@Named(TOOL_RESOURCE_FILE_SYSTEM) FileSystem` and `TipsRepository` via Hilt.

```kotlin
setContent {
    GodToolsTheme(darkTheme = false) {
        ProvideRendererServices(resourceFileSystem, tipsRepository) {
            val tip by dataModel.tip.collectAsState()
            tip?.let { RenderTip(it, state = toolState.toolState, onDismiss = { dismissAllowingStateLoss() }) }
        }
    }
}
```

Wrapper order varies (`ManifestPagerAdapter` and the tips sheet put the theme outside, the `TractActivity` overlay and `LessonActivity` put services outside), the article renderer uses plain `GodToolsTheme`, and the legacy CYOA controllers use `ProvideRendererServices` only. Match the file you are in. Precedents: `ManifestPagerAdapter.kt:171-172`, `TractActivity.kt:298-299`, `LessonActivity.kt:215-216`.

Note: `design_system_rules.md` section 2 says feature code never calls `GodToolsTheme`; tool renderer hosts do, with `darkTheme = false`, by design. Follow the code.

**For ViewPager renderers, give each holder its own `ComposeView` and a `ConstrainedStateLifecycleOwner`.** Extend `ViewHolderPagerAdapter`, create one `ComposeView` per holder, attach a `ConstrainedStateLifecycleOwner(activityOwner, CREATED)` with `setViewTreeLifecycleOwner`, raise `maxState` to STARTED on bind and RESUMED for the primary item, and drop back to CREATED on recycle. Lifecycle promotion is what limits analytics and effects to the visible page. Keep bound state in `mutableStateOf` on the holder, read lifecycle in Compose with `LocalLifecycleOwner.current.lifecycle.currentStateAsState()`, and hold the outer owner `by weak(...)`. See `ui/tract-renderer/.../adapter/ManifestPagerAdapter.kt:110-131,153-170`.

```kotlin
inner class PageViewHolder internal constructor(composeView: ComposeView) : ViewHolder(composeView) {
    internal val lifecycleOwner = this@ManifestPagerAdapter.lifecycleOwner
        ?.let { ConstrainedStateLifecycleOwner(it, Lifecycle.State.CREATED) }
    internal var pageState: TractPageState? by mutableStateOf(null)
}

override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
    ...
    holder.lifecycleOwner?.apply { maxState = maxOf(maxState, Lifecycle.State.STARTED) }
}
```

**Store ids, never Fragments, Views or Activities, in long-lived state.** LiveData, DataModels, adapters and UiState hold a page id or tool code and look the object up. Adapters map ids back to positions and return `POSITION_NONE` for unknown ids (`7bd9733`). The owners flagged a Fragment stored in LiveData as a leak risk and asked for "just store what page the fragment is for" (PR #4588).

```kotlin
val activePageId = MutableLiveData<String?>()        // right
val activeFragment = MutableLiveData<CyoaPageFragment?>()  // wrong
```

## Handling shared renderer state and events

**Hoist renderer state per model id and persist it with the shared Saver.** Keep state objects outside the composable keyed by stable id, save/restore with the shared lib's Saver (`TractPageState.Saver`), and never overwrite live state with stale saved state. State keyed by id survives language switches and config changes. In composition use the shared `remember*` helpers (`rememberLessonPagerState`). See `ManifestPagerAdapter.kt:104-106,217-233` and `LessonActivity.kt:167-170`.

```kotlin
private val pageStates = mutableMapOf<String, TractPageState>()
private fun pageState(page: TractPage) =
    pageStates.getOrPut(page.id) { TractPageState(page) }.also { it.updatePage(page) }
```

**Use the single renderer `State` from `ToolStateHolder`; never construct a new `State()`.** Activities use `toolState` from `BaseToolActivity`, fragments and dialogs use `private val toolState: ToolStateHolder by activityViewModels()`, and shared composables get `toolState.toolState`. One State is what lets events, tips and analytics reach the activity. See `TipBottomSheetDialogFragment.kt:75`.

**Map a shared renderer's events in one private `onXEvent` function with an exhaustive `when` (no `else`).** The compiler then flags new upstream events, and all mappings live in one place. Delete host plumbing the renderer now covers instead of adding new host APIs.

```kotlin
private fun onPageEvent(holder: PageViewHolder, event: TractPageEvent) {
    when (event) {
        is TractPageEvent.OpenModal -> callbacks?.showModal(event.modal)
        TractPageEvent.GoToNextPage -> holder.page?.let { callbacks?.goToPage(it.position + 1) }
        TractPageEvent.CardTapped -> settings.setFeatureDiscovered(FEATURE_TRACT_CARD_CLICKED)
        TractPageEvent.CardSwiped -> settings.setFeatureDiscovered(FEATURE_TRACT_CARD_SWIPED)
    }
}
```

**Handle renderer `State.Event`s once, in `BaseToolActivity.subscribeToRendererStateEvents`.** ScreenView, ContentEvent, OpenUrl, SubmitForm and OpenTip are collected there with `flowWithLifecycle(lifecycle, STARTED)`. A new event type gets a branch there; subclasses only override hooks like `showTip(tip)`. This keeps analytics and URL handling identical across tool types. See `ui/base-tool/.../activity/BaseToolActivity.kt:293-312`.

**Observe hoisted renderer state with `snapshotFlow`, one `LaunchedEffect` per concern.** Do not pack unrelated values into a Pair in one effect; the owners asked for exactly this split (PR #4568). Prefer a derived property on the shared state, and add it upstream if missing.

```kotlin
// record the current page position for the share link
LaunchedEffect(lessonPagerState) {
    snapshotFlow { lessonPagerState.settledPage?.position ?: 0 }
        .collect { dataModel.currentPagePosition.value = it }
}
```

Anti-example: `snapshotFlow { pagerState.settledPage to pages }.collect { (p, pages) -> /* two jobs */ }`. See `LessonActivity.kt:172-200`.

**Record feature discovery with `settings.setFeatureDiscovered(Settings.FEATURE_*)`** and read it with `isFeatureDiscoveredFlow(...)`. Per-tool keys are concatenated (`"$FEATURE_TUTORIAL_LIVE_SHARE$tool"`). Do not add a separate preference store. See the `// region Feature Discovery` block in `BaseToolActivity.kt`.

## Tool activities and DataModels

**Extend `MultiLanguageToolActivity` or `BaseSingleToolActivity`, passing the `Manifest.Type`.** Use `MultiLanguageToolActivity` for tools with primary/parallel/active locales (tract, CYOA) and `BaseSingleToolActivity` for one tool + locale (lesson, article). The base classes already handle download, sync, offline, not-found and wrong-type states through `LoadingState.determineToolState`, so do not re-implement any of that in the subclass.

```kotlin
class CyoaActivity :
    MultiLanguageToolActivity<CyoaActivityBinding>(R.layout.cyoa_activity, Manifest.Type.CYOA),
```

See `TractActivity.kt:85`, `CyoaActivity.kt:40`, `LessonActivity.kt:80`, `BaseArticleActivity.kt:12`.

**Call `trackToolOpen(tool, Manifest.Type.X)` in `onCreate` only when `savedInstanceState == null`.** Otherwise every rotation counts as a new open.

```kotlin
if (savedInstanceState == null) dataModel.toolCode.value?.let { trackToolOpen(it, Manifest.Type.CYOA) }
```

**Tendency: load manifests through the existing renderer DataModels.** Activities use `BaseToolRendererViewModel` / `BaseSingleToolActivityDataModel` / `MultiLanguageToolActivityDataModel`; fragments and dialogs subclass `LatestPublishedManifestDataModel` with `@HiltViewModel` (as `TipBottomSheetDialogFragmentDataModel` does). Do not query `ManifestManager` directly or add a new ViewModel; extend the existing model (`a2014e9` added `manifestFlow` there). Naming is mixed (9 `*DataModel`, 7 `*ViewModel`), so follow the name of the class you extend.

**Tool page screen views subclass `ToolAnalyticsScreenEvent` with names from `ToolAnalyticsScreenNames.forX`, and each event class gets a unit test.** Shared names keep analytics identical to iOS. See `ui/tract-renderer/.../analytics/model/TractPageAnalyticsScreenEvent.kt:9` and its test.

```kotlin
class TractPageAnalyticsScreenEvent(page: TractPage, card: Card? = null) :
    ToolAnalyticsScreenEvent(ToolAnalyticsScreenNames.forTractPage(page, card), page.manifest)
```

**Create intents for activities in other modules with helpers in `Activities.kt`.** Use `Context.createXIntent(...)` / `startX(...)` built on `Intent().setClassName(this, ACTIVITY_CLASS_X)`, grouped in `// region XActivity`. String class names avoid compile-time deps between sibling renderer modules. Tests launch activities through these helpers too. Same-module activities may keep a local `Intent(this, X::class.java)` helper. See `ui/base/src/main/kotlin/org/cru/godtools/base/ui/Activities.kt:45-63`.

Note: pr-review docs name only `ui/base` Activities.kt; `LessonActivity`'s helper lives in `ui/base-tool/src/main/kotlin/org/cru/godtools/base/tool/Activities.kt`. Follow the code and put the helper in the lowest module that can see what it needs.

## Page subtypes

**Any logic tied to the current page must handle every page subtype.** That includes navigation, initial page, share links, analytics and position params. In CYOA, branch on `ContentPage`, `CardCollectionPage` and `PageCollectionPage` explicitly and carry the child position (`CyoaPageFragment.PARAM_POSITION`) for collection pages, mirroring `showPage` / `showInitialPageIfNecessary`. The owners rejected a PR for missing page collection and card collection pages (PR #4588).

```kotlin
val fragment = when (page) {
    is CardCollectionPage -> CyoaCardCollectionPageFragment(page.id)
    is ContentPage -> CyoaContentPageFragment(page.id)
    is PageCollectionPage -> CyoaPageCollectionPageFragment(page.id)
    else -> return
}
```

`Page` is not sealed, so the existing code needs an `else`. Still list all three subtypes. See `ui/cyoa-renderer/.../ui/CyoaActivity.kt:193-208,272-278`.

## Deep links

**Put each renderer's knowgod.com deep link in an `internal data class XDeepLink` with a pure `parseKnowGodDeepLink(uri): XDeepLink?`.** It only validates and extracts: a private `isKnowGodDeepLink` (http/https, `HOST_KNOWGOD_COM`, min path size, fixed segments), private `KNOWGOD_PATH_*` index constants inside `// region <sample url>` / `// endregion <sample url>`, and `null` for non-matching URIs. Optional trailing segments are nullable with default `null` and parsed with `getOrNull(...)?.toIntOrNull()`, so bad values become null rather than failing. Manifest lookups and navigation stay in the Activity. Cover it with an `XDeepLinkTest` that includes invalid cases. See `ui/lesson-renderer/src/main/kotlin/org/cru/godtools/tool/lesson/LessonDeepLink.kt`, `TractDeepLink.kt`, `CyoaDeepLink.kt`, `LessonDeepLinkTest.kt:34-50`.

```kotlin
internal data class LessonDeepLink(val lesson: String, val locale: Locale, val page: Int? = null) {
    companion object {
        // region https://knowgod.com/en/lesson/lessonhs/1
        private const val KNOWGOD_PATH_LOCALE = 0
        private const val KNOWGOD_PATH_LESSON = 2
        private const val KNOWGOD_PATH_PAGE = 3

        fun parseKnowGodDeepLink(uri: Uri): LessonDeepLink? {
            if (!isKnowGodDeepLink(uri)) return null
            return LessonDeepLink(
                lesson = uri.pathSegments[KNOWGOD_PATH_LESSON],
                locale = Locale.forLanguageTag(uri.pathSegments[KNOWGOD_PATH_LOCALE]),
                page = uri.pathSegments.getOrNull(KNOWGOD_PATH_PAGE)?.toIntOrNull()?.takeUnless { it < 0 },
            )
        }

        private fun isKnowGodDeepLink(uri: Uri) = (uri.scheme == "http" || uri.scheme == "https") &&
            uri.host.equals(HOST_KNOWGOD_COM, true) &&
            uri.pathSegments.size >= 3 &&
            uri.pathSegments[1].equals("lesson", true)
        // endregion https://knowgod.com/en/lesson/lessonhs/1
    }
}
```

**Tendency: apply the parsed deep link in `processIntent`.** Override `processIntent(intent, savedInstanceState)`, call `super`, parse the Uri, and assign `dataModel.toolCode` / locales and the initial page. Legacy formats (dynalinks, godtoolsapp.com, `godtools://`) stay as private `Uri` extension checks inside `// region Intent Processing`. Tract and CYOA only parse when `savedInstanceState == null || !isValidStartState`, try `XDeepLink` first and return; lesson parses every time and tries `LessonDeepLink` last. Match whichever activity you are editing. See `CyoaActivity.kt:81-95`, `TractActivity.kt:147-168`, `LessonActivity.kt:278-308`.

## Share links and locales

**Enable sharing by overriding `shareLinkUriLiveData by lazy { ... }` and building the URL in a private `Manifest.buildShareLink(...)`.** Return null when code or locale is missing, then `URI_SHARE_BASE.buildUpon()`, the locale, the tool-type segment if any, the tool code, the page only when > 0, and `.appendQueryParameter("icid", "gtshare")`. The format has to match knowgod.com and iOS: lessons are `/{locale}/lesson/{code}/{position}` with no `tool` segment (the owners rejected a mismatched format in PR #4254).

```kotlin
override val shareLinkUriLiveData by lazy {
    combine(viewModel.manifest, viewModel.currentPagePosition) { manifest, position ->
        manifest?.buildShareLink(position)?.build()?.toString()
    }.asLiveData()
}

private fun Manifest.buildShareLink(position: Int = 0): Uri.Builder? {
    val tool = code ?: return null
    val locale = locale ?: return null
    return URI_SHARE_BASE.buildUpon()
        .appendEncodedPath(locale.toString().lowercase(Locale.ENGLISH))
        .appendPath("lesson")
        .appendPath(tool)
        .apply { if (position > 0) appendPath(position.toString()) }
        .appendQueryParameter("icid", "gtshare")
}
```

See `LessonActivity.kt:354-370`, `TractActivity.kt:383-395`.

**Page numbers in share links and deep links are manifest page positions (`page.position`, counting hidden pages), never visible-pager indexes.** Shared links must land on the exact page that was shared. This came out of GT-3014 (`d8e0d42`, PR #4568), which also overloaded `rememberLessonPagerState` to take a `LessonPage`.

Only lesson and CYOA share links round-trip through their own `XDeepLink` parser. Tract share links (`knowgod.com/{locale}/{tool}/{page}`) are not parsed by `TractDeepLink` (which accepts `/{locale}/tool/v1/{tool}`), so do not "fix" that by assumption.

**Keep live share in tract-renderer and reuse `buildShareLink`.** Publishing and subscribing go through `TractPublisherController` / `TractSubscriberController` via `viewModels()`. The live share URL is `Manifest.buildShareLink()` plus language params, and a navigation event is sent whenever page, card or locale changes. See `TractActivity.kt:399-400,451,476`.

**Tendency: format Java locales for URLs with `toLanguageTag()` and parse with `Locale.forLanguageTag()`.** `locale.toString()` breaks region locales (flagged in PR #4588). Convert godtools-shared locales with `.toPlatform()` before treating them as `java.util.Locale` (`BaseToolActivity.kt:300`). Existing share link path segments still use `locale.toString().lowercase(Locale.ENGLISH)`; leave those unless you are fixing them deliberately.

```kotlin
uri.appendQueryParameter("primaryLanguage", locales.joinToString(",") { it.toLanguageTag() })
```

## Legacy Fragment and class conventions

**Pass Fragment arguments with splitties `by arg()` / `by argOrNull()` set from a secondary constructor.** Import `splitties.fragmentargs.arg`, keep a no-arg primary constructor, and never use `requireArguments()` or Bundle keys (the repo has zero). Return results through a nested `Callbacks`/listener interface found with `findListener<...>()`. Use a companion `create(...)` returning null only where construction can fail (`TipBottomSheetDialogFragment`). See `CategoriesFragment.kt:40-47`.

```kotlin
class CategoriesFragment() : Fragment() {
    constructor(code: String, locale: Locale) : this() {
        tool = code
        this.locale = locale
    }

    private var tool by arg<String>()
    private var locale by arg<Locale>()
}
```

**Organize long classes with labeled `// region X` / `// endregion X` blocks, always repeating the label on `endregion`.** Common regions are Lifecycle, Intent Processing, UI, Share Menu Logic, Renderer State. All 209 `// endregion` lines in main source carry a label, so a bare `// endregion` stands out. Group related `@Inject` properties with no blank lines between them, and put a blank line only between unrelated groups (PR #4263). Reformat only the lines you changed, never the whole file.

## Tests

**Test renderer activities with Robolectric + Hilt scenarios and the module's `@TestInstallIn` mocks.** Use `@HiltAndroidTest`, `@RunWith(AndroidJUnit4::class)`, `@Config(application = HiltTestApplication::class)`, a `HiltAndroidRule` (plus `InstantTaskExecutorRule` where LiveData is involved), and `ActivityScenario.launch(intent).use { }` with intents from the `Activities.kt` helpers. Replace Hilt modules with `Mock*Module` classes (for example `ui/lesson-renderer/src/test/kotlin/org/cru/godtools/tool/lesson/MockBaseToolRendererModule.kt`). Name tests with backticks like `` `processIntent() - Deep Link - knowgod_com` `` and group them in `// region method()`. See `CyoaActivityTest.kt:61-69`, `TractActivityTest.kt:105-123`. Pure parsing logic (deep links, share link builders) goes in plain unit tests instead.
