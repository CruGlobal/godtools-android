# Tests

How the owners test presenters, layouts, repositories and snapshots in this repo, and where each kind of test lives. Read this before adding or editing any test, and before opening a PR that adds a production class or changes visible UI.

## Contents

1. [What every change needs](#what-every-change-needs)
2. [Presenter tests](#presenter-tests)
3. [Fakes, mocks and fixtures](#fakes-mocks-and-fixtures)
4. [Layout tests](#layout-tests)
5. [Paparazzi snapshots](#paparazzi-snapshots)
6. [Repository and Room tests](#repository-and-room-tests)
7. [Placement, naming and grouping](#placement-naming-and-grouping)

## What every change needs

**Ship a test for every new class, at every layer, in the same PR.** A new producer or parser gets a JVM test, a presenter gets a presenter test, a layout gets a layout test with `TestEventSink`, and each visual mode gets a Paparazzi scenario. Feature PRs like #4416 added `LessonsFlowProducerTest`, `LessonsPresenterTest`, `LessonsLayoutTest` and snapshots together, and 16 of 17 presenters have a test class. List each test class and what it covers in the PR body:

```markdown
## Test plan
- LessonsFlowProducerTest - flow per mode
- LessonsPresenterTest - mode state, save & restore
- LessonsLayoutTest - toggle visibility and click events
- Paparazzi snapshots for Personalization / All Lessons / Disabled
```

**Add or update a Paparazzi scenario whenever a PR adds or reveals visible UI.** A new button, banner, mode or empty state, or a swapped UI API, needs a snapshot with sample data that keeps it on screen. Presenter tests alone did not satisfy review on #4167 ("a new screenshot test showing the learn to share button"). If a snapshot truly needs a refactor to capture, write the reason in the PR; #4461 was accepted because the deferral was explained.

**Tendency: cover corner cases, and clean up generated tests.** Test invalid and edge inputs (a non-numeric page segment in a deep link, a null country). If a tool drafted the tests, read them, merge duplicates, rename vague names, and say so in the PR (see #4267 review).

## Presenter tests

**Test a `@CircuitInject` presenter with `presenter.test { }` from circuit-test.** Construct the presenter directly with named args, pass a `FakeNavigator`, and use `awaitItem()` per change, `expectMostRecentItem()` for the settled state, and `awaitItemMatching { }` (gto-support turbine) to skip loading states. Drive events with `state.eventSink(UiEvent.X)`. This is how 13 of 16 presenter tests are written, including every one the owner added this year. See `app/src/test/kotlin/org/cru/godtools/ui/dashboard/lessons/LessonsPresenterTest.kt` and `app/src/test/kotlin/org/cru/godtools/ui/dashboard/DashboardPresenterTest.kt`.

Note: `.claude/skills/pr-review/SKILL.md:178` and `.claude/skills/pr-review/references/patterns.md:339` say presenter tests use `presenterTestOf { }`; the code uses `presenter.test { }` (13 files vs 2). Follow the code.

**Call `AndroidUiDispatcherUtil.runScheduledDispatches()` and assert the navigator is empty in `@AfterTest`.** The dispatcher call flushes pending Compose work between tests. The `assert*IsEmpty()` calls turn any navigation the test did not check into a failure instead of a silent bug. Assert expected navigation with `navigator.awaitNextScreen()`, `awaitPop()` or `awaitResetRoot()`.

**Gate suspend calls with a `Channel` or `Mutex(true)` to observe in-flight state.** `coAnswers { channel.receive() }` or `coAnswers { lock.withLock { true } }` holds a sync open so you can assert `isSyncing = true`, then release it and assert the next item. See `syncLock` in `DashboardPresenterTest.kt:47` and `toolOrderSync` in `LessonsPresenterTest.kt:83`.

A complete skeleton, modeled on `LessonsPresenterTest`:

```kotlin
package org.cru.godtools.ui.dashboard.foo

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.support.turbine.awaitItemMatching
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.dashboard.foo.FooPresenter.UiEvent
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class FooPresenterTest {
    private val toolsFlow = MutableStateFlow(emptyList<Tool>())
    private val toolOrderSync = Channel<Boolean>()

    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns toolsFlow
    }
    private val syncService: GodToolsSyncService = mockk {
        coEvery { syncToolOrder(any(), any(), any()) } coAnswers { toolOrderSync.receive() }
    }

    private val navigator = FakeNavigator(FooScreen)
    private val presenter = FooPresenter(
        toolsRepository = toolsRepository,
        syncService = syncService,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()
        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
        navigator.assertResetRootIsEmpty()
    }

    // region State.tools
    @Test
    fun `State - tools - reflects repository`() = runTest {
        presenter.test {
            val tool = randomTool("kgp")
            toolsFlow.value = listOf(tool)
            assertEquals(listOf(tool), awaitItemMatching { it.dataLoaded }.tools)
        }
    }
    // endregion State.tools

    // region UiEvent.Refresh
    @Test
    fun `UiEvent - Refresh - shows syncing until sync completes`() = runTest {
        presenter.test {
            val state = awaitItemMatching { it.dataLoaded }
            state.eventSink(UiEvent.Refresh)
            assertTrue(awaitItem().isSyncing)

            toolOrderSync.send(true)
            assertFalse(awaitItem().isSyncing)
        }
    }
    // endregion UiEvent.Refresh
}
```

Adjust the repository and sync calls to what your presenter really uses; the shape (fields, `presenter`, `@AfterTest`, regions) is the part to copy.

**Use `presenterTestOf` or `moleculeFlow` only for non-Circuit presenter shapes.** If `present()` takes arguments, use `presenterTestOf({ presenter.present(args) }) { }` (see `app/src/testDebug/.../tools/DefaultToolCardPresenterTest.kt`). For a custom `@Composable` interface or producer (`BannerPresenter<T>`, `StateProducer`, a settings flow), wrap it in `moleculeFlow(RecompositionMode.Immediate)` and test with Turbine. The owner called `moleculeFlow` the right tool for `BannerPresenter<T>` on #4398.

```kotlin
private fun presenterFlow() = moleculeFlow(RecompositionMode.Immediate) {
    withCompositionLocal(LocalContext provides context) { presenter.present() }
}
```

See `app/src/test/kotlin/org/cru/godtools/ui/banner/tutorial/TutorialFeaturesBannerPresenterTest.kt:46-58`.

**Tendency: test `rememberSaveable` state with `StateRestorationTester`.** When a presenter keeps state in `rememberSaveable`, add a `State - <field> - persisted through state save & restore` test that calls `emulateSavedInstanceStateRestore()`, and keep the helper in a `// region StateRestorationTester Support` block. Only `LessonsPresenterTest.kt:136-163,194-245` does this so far, so treat it as a strong suggestion. This is the one case where `createComposeRule()` is still correct, because `StateRestorationTester` needs a `ComposeTestRule`.

**Run Android-dependent tests on Robolectric with a plain `Application`.** Use `@RunWith(AndroidJUnit4::class)` and `@Config(application = Application::class)`. This avoids booting `GodToolsApplication`'s Hilt graph and keeps tests fast. Use `HiltTestApplication` with `@HiltAndroidTest` only for Activity or binding tests that need the graph (renderer `*ActivityTest`). Pure JVM tests (producers, parsers, models) take no runner.

## Fakes, mocks and fixtures

**Write a hand-made `Fake<Name>` for anything that exposes a `@Composable` function.** If a collaborator is a Presenter, `StateProducer`, `BannerPresenter` or `ToolCardPresenter`, extract an interface and back a small fake in `src/test` with a `MutableStateFlow` or lambda. The owner removed mockposable because it delays uptake of new Kotlin versions (78d17a4), so do not add new `everyComposable`/`verifyComposable` usages even though legacy files like `DashboardPresenterTest` still have them. Mention a new fake briefly in the PR body.

```kotlin
class FakeBannerPresenter<T : Banner.UiState>(initialState: T? = null) : BannerPresenter<T> {
    private val uiState = MutableStateFlow(initialState)
    fun updateState(state: T?) { uiState.value = state }

    @Composable
    override fun present(): T? = uiState.collectAsState().value
}
```

See `app/src/test/kotlin/org/cru/godtools/ui/banner/FakeBannerPresenter.kt` and `app/src/test/kotlin/org/cru/godtools/ui/tools/FakeToolCardPresenter.kt`.

**Configure mockk collaborators inline at the field.** Declare `private val repo: ToolsRepository = mockk { every { ... } returns flow }`, back flows with `MutableStateFlow` fields, and change data through `.value` inside tests. Prefer `mockk(relaxUnitFun = true)` over `relaxed = true` (30 files vs 9) so a missing stub on a value-returning call still fails loudly. Use `coVerifyAll`/`confirmVerified(...)` when the test must prove nothing else was called.

**Set fixture defaults to the normal case, and override only in edge-case tests.** Initialize shared fields to what the code sees when its preconditions hold, for example `MutableStateFlow<String?>("US")`, and set `null` at the top of the one test about a missing country. The owner requested changes on #4586 for this. See `LessonsPresenterTest.kt:78` and `app/src/test/.../dashboard/tools/FeaturedToolsFlowProducerTest.kt:23`.

**Build models with the `random*` builders and override only what the assertion needs.** Use `randomTool("kgp", isFavorite = true)`, `randomTranslation(toolCode = tool.code, languageCode = Locale.ENGLISH)`, `randomLanguage(locale)`, `randomUser()`. Random unrelated fields catch accidental dependencies. Add new builders to the module's `src/testFixtures` following `library/model/src/testFixtures/kotlin/org/cru/godtools/model/Language+TestFixtures.kt`, not to `main` (`Tool.kt` carries a TODO to move `randomTool` there).

**Share cross-module helpers through `testFixtures`, consumed with `testImplementation`.** Modules enable `testFixtures.enable = true` (`:ui:base` for `BasePaparazziTest`, `:library:model`, `:library:db`). Consumers use `testImplementation(testFixtures(...))`, or `testDebugImplementation` for `BasePaparazziTest` in `:app` (`app/build.gradle.kts:209-222`). Do not use `testApi` (#4458) and do not copy a helper into another module (#4240 moved `BasePaparazziTest` so `:ui:qr-code` could reuse it).

```kotlin
testImplementation(testFixtures(projects.library.model))
testDebugImplementation(testFixtures(projects.ui.base))
```

**Use kotlin.test assertions and annotations.** Import `kotlin.test.Test`, `assertEquals`, `assertIs`, `assertNotNull`, `assertFailsWith`, `@BeforeTest`, `@AfterTest`. Do not add `org.junit.Assert`, `org.junit.Test/Before/After`, Hamcrest, Truth or assertk; the last year added 75 kotlin.test imports and removed 20 `org.junit.Assert` imports with none added. `org.junit.Rule`, `org.junit.runner.RunWith` and `org.junit.Assume` stay since kotlin.test has no equivalent. When you touch a legacy file, swapping its `org.junit.Assert` imports is welcome (7e83d9c).

## Layout tests

**Write new Compose UI tests with `androidx.compose.ui.test.v2.runComposeUiTest`.** Put `@OptIn(ExperimentalTestApi::class)` on the class and write each test as `= runComposeUiTest { }`. The v1 import is deprecated and is a Must Fix in the pr-review skill; older files still use v1 or `createComposeRule()`, so do not copy them. See `app/src/testDebug/kotlin/org/cru/godtools/ui/dashboard/lessons/LessonsLayoutTest.kt:8`.

Note: `.claude/skills/pr-review/SKILL.md:179` bans `createComposeRule()` outright; the code still needs it for `StateRestorationTester`. Follow the code for that one case.

**Render the layout from a hand-built `UiState` with `TestEventSink`.** Test the UI as a pure function of state. Find nodes by a `TEST_TAG_*` constant or `context.getString(R.string.x)`, never an English literal, so the test does not break on copy changes. Assert `events.assertEvent(...)` after a click, `assertNoEvents()` when nothing should fire, and `assertExists()`/`assertDoesNotExist()` for visibility gates.

```kotlin
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class LessonsLayoutTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val events = TestEventSink<UiEvent>()

    // region PersonalizationToggle
    @Test
    fun `PersonalizationToggle - click Personalized fires ChangeMode(PERSONALIZATION)`() = runComposeUiTest {
        setContent {
            LessonsLayout(UiState(isPersonalizationEnabled = true, mode = UiState.Mode.ALL_LESSONS, eventSink = events))
        }

        onNodeWithText(context.getString(R.string.dashboard_lessons_toggle_personalized)).performClick()
        events.assertEvent(UiEvent.ChangeMode(UiState.Mode.PERSONALIZATION))
    }
    // endregion PersonalizationToggle
}
```

Avoid `onNodeWithText("Change Language")` (legacy in `AppLanguageLayoutTest`).

## Paparazzi snapshots

**Snapshot the full screen in its real shell, not an extracted piece.** Dashboard pages go through `DashboardLayoutPaparazziTest` with `initialPage` so the nav bar, scaffold and app bar render too; other screens render their whole `Layout`. The owner on #4583: "I would prefer that these paparazzi tests were for the entire AccountLayout, and not just the header." Keep component-level tests only for variants that cannot all show on one screen (card variants, badge states). Do not make a private sub-composable `@VisibleForTesting internal` just to snapshot it; if the full screen cannot be snapshotted yet, say so and suggest a follow-up ticket.

```kotlin
@Test
fun `ToolsLayout() - Personalization`() {
    toolsState = toolsState.copy(mode = ToolsPresenter.UiState.Mode.PERSONALIZATION)
    snapshotDashboardLayout(state.copy(initialPage = ToolsScreen))
}
```

`AccountLayoutPaparazziTest.kt:26-33` (header only) is the known exception still on develop; do not copy it.

**Extend `BasePaparazziTest` with the standard parameter matrix.** Use `@RunWith(TestParameterInjector::class)`, take `nightMode` and `accessibilityMode` (plus `deviceConfig` from `DeviceConfigProvider` for full screens), and render with `snapshot { }` or `centerInSnapshot { }`. The base class applies `GodToolsTheme`, overlays, device, locale and diff threshold, and skips redundant accessibility combos, so do not wrap content in `GodToolsTheme` yourself. A complete skeleton modeled on `app/src/testDebug/kotlin/org/cru/godtools/ui/account/globalactivity/GlobalActivityLayoutPaparazziTest.kt`:

```kotlin
package org.cru.godtools.ui.account.globalactivity

import app.cash.paparazzi.DeviceConfig
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.time.Year
import kotlin.test.Test
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.model.GlobalActivityAnalytics
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class GlobalActivityLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    val state = GlobalActivityScreen.UiState(
        year = Year.of(2025),
        activity = GlobalActivityAnalytics(users = 1234, gospelPresentations = 4321, countries = 123, launches = 54321),
    )

    @Test
    fun `GlobalActivityLayout()`() = snapshot { GlobalActivityLayout(state) }
}
```

`GlobalActivityScreen` still holds its `UiState` in the Screen (legacy); for a new screen build `FooPresenter.UiState` instead.

**Keep snapshot data deterministic.** Clock, random and network values in a golden break CI later. Hoist `Year.now()`/`Instant.now()` into the `UiState` with a default and pin it in the test (`year = Year.of(2025)`); #4283 fixed goldens that broke on New Year because the layout read `Year.now()`. Use fixed values in `*TestData`, and if you use `randomTool`, pin every field that renders. Stub images with Coil's `FakeImageLoaderEngine` in `@BeforeTest` and call `Coil.reset()` in `@AfterTest` (see `DashboardLayoutPaparazziTest.kt:67-89`).

**Reuse `*TestData` objects for snapshot state.** Build state from `ToolCardStateTestData` and `DrawerMenuScreenStateTestData` (in `app/src/testDebug`) with `.copy()`, and add new shared states there instead of hand-building `UiState` per test (#4415). Use `var` only for state a test mutates.

**Limit locale variants with `assumeTrue(locale == null)`.** To add a locale axis, take `@TestParameter("null", "in") locale: String?`, pass it to `BasePaparazziTest`, and call `assumeTrue(locale == null)` at the top of every test that does not need a translated variant. This keeps golden count and LFS size down. Only `DashboardLayoutPaparazziTest` has a locale axis so far.

**Name the class `<Composable>PaparazziTest` and tests `<Composable>()` or `<Composable>() - <variant>`.** Golden file names come from package, class and test name, so a rename or move renames goldens. Delete the stale PNGs and re-record through CI (old files were removed on #4416).

**Record goldens only through the Record Snapshots workflow.** Local rendering differs from CI, so never run `recordPaparazzi` locally. Push the branch, run `.github/workflows/record-snapshots.yml` (it runs `./gradlew cleanRecordPaparazzi` and commits "Record updated snapshots"), then fold that commit into the commit that changed the UI with fixup and autosquash. The `record-screenshots` skill does all of this. CI verifies with `./gradlew test verifyPaparazzi`.

```bash
gh workflow run record-snapshots.yml --ref my-branch
# then run the record-screenshots skill (fixup + autosquash)
```

**Keep goldens in Git LFS.** `.gitattributes` tracks `**/snapshots/**/*.png`, and the Validate Git LFS workflow fails on a raw PNG blob. Run `git lfs install && git lfs pull` before working with snapshots.

**Give every `@Ignore` a reason, and remove it when the blocker is gone.** Use `@Ignore("reason")` only for real tooling limits such as LayoutLib popups. When a refactor removes the blocker (for example a ViewModel dependency replaced by Circuit), remove the `@Ignore` in the same PR (#4398). See `DashboardLayoutPaparazziTest.kt:177`.

## Repository and Room tests

**Test repository methods in the abstract `<Name>RepositoryIT`.** Write cases in `library/db/src/test/kotlin/org/cru/godtools/db/repository/<Name>RepositoryIT.kt`; the Room subclass (for example `db/room/repository/ToolsRoomRepositoryIT.kt`) only wires `RoomDatabaseRule` and overrides the repository properties, so every implementation gets the same coverage. Group by `// region methodName()` and name tests `methodName() - behavior`. Cover empty state, ordering and filtering, scoping keys (including null), reactive updates through Turbine, and that upserts do not overwrite unrelated data. See `ToolsRepositoryIT.kt:326-362`.

```kotlin
// region getPersonalizedToolsFlow()
@Test
fun `getPersonalizedToolsFlow() - empty when no order stored`() = testScope.runTest {
    repository.storeInitialTools(listOf(randomTool("tool", Tool.Type.TRACT)))
    assertTrue(repository.getPersonalizedToolsFlow(Locale.ENGLISH, null).first().isEmpty())
}
// endregion getPersonalizedToolsFlow()
```

Room migration tests keep their `testMigrateXToY` names.

## Placement, naming and grouping

**Put presenter, producer, parser, repository and sync tests (and `Fake*` classes) in `src/test`; in `:app`, `src/testDebug` is for Paparazzi tests, layout tests and `*TestData`.** The owner moved six presenter tests from `testDebug` to `src/test` (def8d01, 18843fe, 8729a0f) and left layout and Paparazzi tests where they were. Legacy presenter tests still in `testDebug` (ToolDetails, DrawerMenu, and others) are not precedent; if you restructure one, move it to `src/test`. The split is organizational: unit tests only run for `productionDebug` anyway (`build-logic/src/main/kotlin/AndroidTestConfiguration.kt:40`), and other modules do run Compose UI tests from `src/test`.

```text
app/src/test/kotlin/.../lessons/LessonsPresenterTest.kt
app/src/test/kotlin/.../lessons/LessonsFlowProducerTest.kt
app/src/testDebug/kotlin/.../lessons/LessonsLayoutTest.kt
app/src/testDebug/kotlin/.../dashboard/DashboardLayoutPaparazziTest.kt
```

**Group all tests for one subject in a single region pair, and repeat the subject on `endregion`.** Presenter tests use `State.<field>`, `UiEvent.<Event>` and `SideEffect - <name>`; repository ITs use `methodName()`; layout tests use the UI element. The owner asked for this in review (#4167), and 83 test files follow it. Do not leave related tests loose or give each test its own region.

```kotlin
// region State.isSyncing
@Test
fun `State - isSyncing - initial sync`() = runTest { ... }
// endregion State.isSyncing

// region UiEvent.TriggerSync
@Test
fun `UiEvent - TriggerSync`() = runTest { ... }
// endregion UiEvent.TriggerSync
```

**Name tests in backticks with the hierarchy `Subject - member - case`, matching the region.** Examples: `State - hasTips - no tips`, `UiEvent - TriggerSync`, `getPersonalizedToolsFlow() - scoped to locale and country`, `LessonsLayout() - Personalization`. The owner posted rename suggestions on #4167 to follow "the hierarchical naming structure we are using in these tests," and 323 of 346 test functions added in the last year use it (the rest are migration tests). Avoid names like `testHasTips2()`. Some older presenter tests use `Event - X` and `// region Event.X` from before the `UiEvent` rename; new tests use `UiEvent - X`.
