# GodTools Android — PR Review Patterns

Detailed reference for the `godtools-pr-review` skill. Load this when reviewing specific file types.

---

## Build Scripts (`build.gradle.kts`)

### Convention Plugins Already Provide
`godtools.library-conventions` and `godtools.application-conventions` supply:
- `minSdk`, `compileSdk`, `targetSdk`
- Kotlin JVM toolchain (JDK 21)
- Core Library Desugaring
- ktlint configuration
- `buildFeatures.buildConfig` default
- Compose + Circuit setup when `configureCompose(project)` is called
- Proguard/R8 default rules (no need to add `proguard-android-optimize.txt`)

Flag as **Important** any `build.gradle.kts` that re-declares these.

### Compose Setup
```kotlin
// Correct — just call configureCompose
android {
    namespace = "org.cru.godtools.feature.foo"
    configureCompose(project)              // adds Compose + Circuit deps
    // configureCompose(project, enableCircuit = true) if Circuit screens needed
}
```
Do not manually add Compose BOM or Circuit dependencies if `configureCompose` is called.

### AndroidManifest in Library Modules
Flag as **Critical** if a library `AndroidManifest.xml`:
- Defines `application` attributes (theme, label, icon) — these belong in `:app`
- Declares `android:label` on activities that don't need one
- Contains auto-generated `colors.xml`, `strings.xml` with `app_name`, or default launcher icons

---

## `gradle/libs.versions.toml`

- No duplicate version keys (e.g., two entries for `android-gradle-plugin`)
- No unused `[plugins]` entries (common AS wizard artifact)
- No unused `[libraries]` entries
- All new dependencies should be added here, not hardcoded in `build.gradle.kts`

---

## Kotlin Idioms

### Prefer `when` for Multi-Branch Logic
```kotlin
// Preferred
when (page) {
    Page.LIVE_SHARE_START -> Row { ... }
    else -> Button { ... }
}

// Avoid
if (page == Page.LIVE_SHARE_START) { Row { ... } } else { Button { ... } }
```

### Visibility Modifiers
- `public` — only for API surface intentionally exposed outside the module
- `internal` — for composables and functions shared within a module but not exported
- `private` — default for everything else

### Logging
```kotlin
// Correct
Timber.tag("FeatureName").e(e, "Error message")

// Wrong
println("Error: $e")
Log.e("TAG", "Error", e)
```

### Constants for Shared Keys
Define `Intent` extras, `Bundle` keys, and `WorkManager` work names as `const val` in a companion object or at file level. Reuse the constant in both the producer and consumer.

### Exception Handling
```kotlin
// Correct — catch specific exceptions
} catch (_: IOException) {
    Result.retry()
}

// Wrong — hides unexpected errors
} catch (e: Exception) { ... }
```

### Sealed Interfaces for Events
```kotlin
sealed interface UiEvent : CircuitUiEvent {
    data object Back : UiEvent
    data class SelectLanguage(val language: Locale) : UiEvent
    sealed interface Settings : UiEvent {
        data object Open : Settings
    }
}
```

### Nullability with Early Returns
```kotlin
val url = intent.getStringExtra(EXTRA_URL) ?: run { finish(); return }
```

---

## Jetpack Compose

### Composable Signature
Every public or internal composable must accept a `Modifier`:
```kotlin
@Composable
internal fun FooScreen(
    state: FooPresenter.UiState,
    modifier: Modifier = Modifier,
) { ... }
```

### Theme Usage
- Always use `GodToolsTheme` from `:ui:base` — never define per-module themes
- Access colors via `MaterialTheme.colorScheme.*`
- Custom extended colors via `GodToolsTheme.extendedColorScheme`
- Check `GodToolsTheme.isLightColorSchemeActive` for theme-aware logic

```kotlin
// Correct
GodToolsTheme { FooScreen(...) }

// Wrong
MaterialTheme(colorScheme = lightColorScheme()) { FooScreen(...) }
```

### State Management
- `remember { }` for expensive computations
- `rememberSaveable` for state that survives configuration changes
- `collectAsState()` to convert Flows to Compose state
- `produceState { }` for background-thread state production

### Background Work in Composables
Bitmap generation, image processing, and other CPU-heavy work must happen off the main thread:
```kotlin
val bitmap by produceState<Bitmap?>(null, url) {
    value = withContext(Dispatchers.IO) { generateBitmap(url) }
}
```

---

## Circuit Presenter/UI Patterns

### Presenter Structure
```kotlin
class FooPresenter @AssistedInject constructor(
    private val repository: FooRepository,
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: FooScreen,
) : Presenter<FooPresenter.UiState> {

    @Composable
    override fun present(): UiState {
        val items by repository.getItemsFlow().collectAsState(emptyList())
        return UiState(items = items) { event ->
            when (event) {
                UiEvent.Back -> navigator.pop()
                is UiEvent.Select -> { /* ... */ }
            }
        }
    }

    data class UiState(
        val items: List<Foo> = emptyList(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data object Back : UiEvent
        data class Select(val item: Foo) : UiEvent
    }

    @AssistedFactory
    @CircuitInject(FooScreen::class, SingletonComponent::class)
    interface Factory : Presenter.Factory<FooScreen, FooPresenter>
}
```

### UI Actions Must Go Through eventSink
```kotlin
// Correct — action flows through UiState event
Button(onClick = { state.eventSink(UiEvent.Share) }) { Text("Share") }

// Wrong — direct lambda, bypasses Circuit event system
Button(onClick = { viewModel.share() }) { Text("Share") }
```

### Screen Classes
```kotlin
@Parcelize
data class FooScreen(val toolCode: String) : Screen {
    sealed interface Result : PopResult {
        data object Dismissed : Result
    }
}
```

---

## Cross-Module Activity / Intent Creation

All `Intent` and `PendingIntent` creation for Activities must live in:
```
ui/base/src/main/kotlin/org/cru/godtools/base/ui/Activities.kt
```

Pattern:
```kotlin
private const val ACTIVITY_CLASS_FOO = "org.cru.godtools.ui.foo.FooActivity"
private const val EXTRA_FOO_ID = "FOO_ID"

fun Context.createFooIntent(id: String) =
    Intent().setClassName(this, ACTIVITY_CLASS_FOO)
        .putExtra(EXTRA_FOO_ID, id)

fun Activity.startFooActivity(id: String) =
    startActivity(createFooIntent(id))
```

This avoids hard compile-time dependencies between sibling UI modules.

---

## Repository & DAO Patterns

### DAO Interface
```kotlin
@Dao
internal interface FooDao {
    @Query("SELECT * FROM foo WHERE id = :id")
    suspend fun find(id: String): FooEntity?

    @Query("SELECT * FROM foo WHERE id = :id")
    fun findFlow(id: String): Flow<FooEntity?>

    @Upsert(entity = FooEntity::class)
    suspend fun upsert(items: Collection<SyncFoo>)
}
```

- Suspend functions for single-shot queries
- `Flow<T>` for reactive queries
- `@RewriteQueriesToDropUnusedColumns` when selecting partial projections
- Use `@Upsert` for sync operations

### Composable Repository Extensions
```kotlin
@Composable
fun FooRepository.produceFooState(id: String?) =
    remember(id) { id?.let { findFlow(it) } ?: flowOf(null) }
        .collectAsState(null)
```

---

## WorkManager Patterns

```kotlin
@HiltWorker
internal class SyncFooWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncTasks: FooSyncTasks,
) : CoroutineWorker(context, params) {
    override suspend fun doWork() = try {
        if (syncTasks.sync()) Result.success() else Result.retry()
    } catch (_: IOException) {
        Result.retry()
    }
}
```

- `@HiltWorker` + `@AssistedInject` required
- Extend `CoroutineWorker` for suspend support
- Network-constrained work requests via `Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)`

---

## Testing Patterns

### Presenter Tests
```kotlin
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class FooPresenterTest {
    private val itemsFlow = MutableStateFlow(listOf(randomFoo()))
    private val repository: FooRepository = mockk {
        every { getItemsFlow() } returns itemsFlow
    }
    private val presenter = FooPresenter(repository, FakeNavigator(), FooScreen())

    @Test
    fun `UiState - items loaded`() = runTest {
        presenterTestOf({ presenter.present() }) {
            val state = awaitItem()
            assertEquals(itemsFlow.value, state.items)
        }
    }
}
```

### Paparazzi Snapshot Tests
```kotlin
@RunWith(TestParameterInjector::class)
class FooPaparazziTest(
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(nightMode = nightMode, accessibilityMode = accessibilityMode) {

    @Test
    fun `FooScreen - default`() = centerInSnapshot(Modifier.fillMaxSize()) {
        FooScreen(UiState())
    }
}
```

- Extend `BasePaparazziTest` from `:ui:base` testFixtures
- Use `@TestParameter` for night mode and accessibility matrix
- **Do not record snapshots locally** — trigger the GitHub Actions workflow on the feature branch

---

## Settings / Dependency Management

### Local `includeBuild` in `settings.gradle.kts`
```kotlin
// NEVER commit this
includeBuild("../kotlin-mpp-godtools-tool-parser") {
    dependencySubstitution { ... }
}
```

Changes to the shared KMP project (`kotlin-mpp-godtools-tool-parser`) must be:
1. Merged into that repo first
2. Published as a versioned artifact
3. Version updated in this repo's `gradle/libs.versions.toml`

### KMP / Multiplatform UI Changes
Tool UI elements (app bar actions, lesson/tract rendering) belong in the `kotlin-mpp-godtools-tool-parser` repo, not in the Android renderer modules. Android renderer modules consume the multiplatform output.

---

## String Resources

- Extract hardcoded strings to `strings.xml` — inline Kotlin string literals in composables are flagged
- Place strings under appropriate section comments (e.g., `<!-- Share Tool Functionality -->`)
- Do not define `app_name` or generic color/theme resources in library modules
- Strings used only as internal `Intent` extras do not need to be in resources

---

## PR Hygiene Checklist

- [ ] No unrelated auto-formatter whitespace changes
- [ ] No AS wizard scaffolding (generated colors, themes, icons, `app_name`)
- [ ] No local `includeBuild` substitutions
- [ ] ktlint passes (`./gradlew :build-logic:ktlintCheck ktlintCheck`)
- [ ] No `println` — use Timber
- [ ] All composables have `modifier: Modifier = Modifier`
- [ ] Uses `GodToolsTheme`, not a local theme
- [ ] Circuit actions go through `eventSink`
- [ ] Cross-module intents use `Activities.kt`
- [ ] `libs.versions.toml` has no duplicates or unused entries
- [ ] Library `build.gradle.kts` doesn't re-declare convention plugin defaults
