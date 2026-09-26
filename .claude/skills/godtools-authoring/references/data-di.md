# Data layer, sync and dependency injection

How the owners structure models, Room entities, repositories, DAOs, API interfaces, sync tasks, settings, remote config flags, Hilt wiring, logging and exception handling. Read this before you add a table or column, a repository method, an API endpoint, a sync, a setting or flag, or a Dagger module. Each rule cites a real file or commit you can open as a precedent.

## Contents

1. Model, entity and sync layering
2. Repositories and DAOs
3. Room schema change checklist
4. API interfaces and JSON:API models
5. Sync tasks, GodToolsSyncService and workers
6. Settings and remote config flags
7. Dagger and Hilt wiring
8. Logging and exception handling
9. Tests for the data layer
10. Module boundaries, Gradle deps and commit shape

## 1. Model, entity and sync layering

**Keep three layers: a domain model in `:library:model`, an `internal` Room entity, and an `internal` Sync partial for API writes.** Room types never leave `:library:db`, and sync never wipes local-only columns like `isAdded`, `isFavorite`, `order` or progress. Each entity in `db/room/entity` has a secondary constructor taking the domain model and a `fun toModel()`. When sync must not clobber local columns, add `internal class SyncX(model: X)` in `db/room/entity/partial` that copies only API-owned fields, and write it with `@Upsert(entity = XEntity::class)`. See `library/db/src/main/kotlin/org/cru/godtools/db/room/entity/LanguageEntity.kt` and `.../entity/partial/SyncToolPlaceholder.kt`.

```kotlin
@Entity(tableName = "languages")
internal class LanguageEntity(@PrimaryKey val code: Locale, val name: String?, /* ... */) {
    constructor(language: Language) : this(code = language.code, name = language.name /* ... */)
    fun toModel() = Language(code = code, name = name /* ... */)
}

internal class SyncToolPlaceholder(tool: Tool) {
    val apiId = tool.apiId
    val code = tool.code.orEmpty()
}
```

Do not make entities public, and do not `@Upsert` a full entity from sync data. (`SyncLanguage` is public for legacy reasons; do not copy that.)

**Return `Flow` from new reactive DAO and repository APIs, never LiveData.** The owners removed all LiveData from `library/db` over the past year. When you touch a legacy LiveData screen, add a Flow version (see a2014e9, `manifestFlow`) and move consumers to it.

**Tendency: track local edits that must be pushed back to the API through `ChangeTrackingModel`.** Add `const val ATTR_X = "x"` in the model companion, write inside `trackChanges { }` (repositories take `trackChanges: Boolean = true`), and pick dirty rows in sync with `isFieldChanged(Model.ATTR_X)`. Only `isFavorite` uses this today, so follow it when doing similar work, but it is thin precedent. See `library/sync/src/main/kotlin/org/cru/godtools/sync/task/UserFavoriteToolsSyncTasks.kt`.

## 2. Repositories and DAOs

**Wire every repository the same way: public interface, `@Dao internal abstract class XRoomRepository`, and a `@Provides @Reusable` in DatabaseModule.** All 11 repositories use this exact shape, so any other wiring stands out in review. Consumers see only the interface, which makes fakes easy.

1. `interface XRepository` in `org.cru.godtools.db.repository`.
2. `@Dao internal abstract class XRoomRepository(private val db: GodToolsRoomDatabase) : XRepository` in `db/room/repository`.
3. `abstract val xRepository: XRoomRepository` in the `// region Repositories` block of `GodToolsRoomDatabase`.
4. In `DatabaseModule`:

```kotlin
@Provides
@Reusable
internal fun followupsRepository(db: GodToolsRoomDatabase): FollowupsRepository = db.followupsRepository
```

Do not use `@Binds`, an `@Inject` constructor, or an `Impl` suffix. See `library/db/src/main/kotlin/org/cru/godtools/db/DatabaseModule.kt` and `.../room/repository/FollowupsRoomRepository.kt`.

**Name repository methods by shape and by who owns the write.** Readers can tell one-shot from reactive, and sync writes from user writes, without opening the body.

- Single item: `suspend fun findX(...)` plus non-suspend `fun findXFlow(...)`.
- Lists: `suspend fun getXs()` plus `fun getXsFlow()`.
- Sync writes: `storeXFromSync`, `storeXsFromSync`, `removeXsMissingFromSync`, grouped in `// region Sync Methods`.
- Bundled content writes: `storeInitialXs` in `// region Initial Content Methods`.
- Feature groups: `// region <Feature>` in both the interface and the DAO.
- Put `@Transaction` on RoomRepository overrides that make more than one DAO call.

```kotlin
interface LanguagesRepository {
    suspend fun findLanguage(locale: Locale): Language?
    fun findLanguageFlow(locale: Locale): Flow<Language?>

    // region Sync Methods
    suspend fun storeLanguagesFromSync(languages: Collection<Language>)
    suspend fun removeLanguagesMissingFromSync(synced: Collection<Language>)
    // endregion Sync Methods
}
```

Avoid `suspend fun ...Flow()` (a Flow getter is never suspend), `get` for a single item, and vague names like `saveX`. See `library/db/src/main/kotlin/org/cru/godtools/db/repository/LanguagesRepository.kt`.

**Filter by tool type in SQL, not in Kotlin.** Put `type IN (:types)` in the DAO `@Query` and have the repository pass `*Tool.Type.NORMAL_TYPES.toTypedArray()`. Frett called this out in the PR #4373 description: it avoids loading every row, and the Flow does not re-emit for rows you would throw away. See `library/db/src/main/kotlin/org/cru/godtools/db/room/dao/ToolsDao.kt` and e38a1b3.

```kotlin
// wrong: dao.getAllToolsFlow().map { it.filter { it.type in Tool.Type.NORMAL_TYPES } }
override fun getFeaturedToolsFlow(locale: Locale, country: String?) =
    dao.getFeaturedToolsFlow(locale, country.orEmpty(), *Tool.Type.NORMAL_TYPES.toTypedArray())
        .map { it.map { it.toModel() } }
```

**Keep primary-key columns non-null and map optional key parts with `.orEmpty()` at the Room repository.** SQLite treats NULLs as distinct in composite keys, so a nullable part breaks upserts and resets. The repository interface keeps `country: String?`; the Room repository calls `.orEmpty()` before every DAO call (query, reset, upsert), and sync uses the same `.orEmpty()` value in the last-sync key. See `library/db/src/main/kotlin/org/cru/godtools/db/room/entity/PersonalizedFeaturedToolOrderEntity.kt` and `ToolsRoomRepository.kt`.

**Tendency: declare DAOs as `@Dao internal interface XDao` that return entities, using `suspend` or `Flow`.** Use `@Insert(onConflict = OnConflictStrategy.IGNORE)` for initial content and `@Upsert(entity = XEntity::class)` with a Sync partial for sync. When you add a non-suspend DAO method, suffix it `Blocking` (`upsertLanguagesBlocking`) so callers know it does I/O on the calling thread. Many older non-suspend methods lack the suffix (`AttachmentsDao`); treat them as legacy, not a model. See `library/db/src/main/kotlin/org/cru/godtools/db/room/dao/LanguagesDao.kt`.

**Put `@Composable` read helpers next to the repository interface: `produceXState` returns `State`, `rememberX` returns the value.** Use `flowOf(null)` for a null key and include every input in the `remember` keys. See `library/db/src/main/kotlin/org/cru/godtools/db/repository/ToolsRepository.kt`.

```kotlin
@Composable
fun ToolsRepository.produceToolState(toolCode: String?) =
    remember(toolCode) { toolCode?.let { findToolFlow(it) } ?: flowOf(null) }.collectAsState(null)
```

## 3. Room schema change checklist

**Ship every schema change as one commit with all of these pieces.** The database falls back to destructive migration (`fallbackToDestructiveMigration(dropAllTables = true)`), so a missing migration silently wipes users' favorites and progress. The schema JSON plus the migration test are the only guard. Precedents: e3775e5 (v27) and 6be7583 (v26).

1. Add or change an `internal` entity. A new table gets an explicit snake_case `tableName`, `primaryKeys` for composite keys, a `ForeignKey` to its parent with `onUpdate = ForeignKey.CASCADE` and `onDelete = ForeignKey.CASCADE`, and an `Index` on the FK column plus the columns you query by.
2. Give any new NOT NULL column `@ColumnInfo(defaultValue = "...")` so AutoMigration can fill old rows.
3. Register the entity in `@Database(entities = [...])`, bump `version`, and add `AutoMigration(from = N - 1, to = N)`. Write a manual `Migration` only when an `AutoMigrationSpec` cannot express the change.
4. Add a dated line `N: YYYY-MM-DD` under the app version label in the `Version history` comment.
5. Commit the generated `library/db/room-schemas/.../N.json`.
6. Add `testMigrate{N-1}To{N}()` to `library/db/src/test/kotlin/org/cru/godtools/db/room/GodToolsRoomDatabaseMigrationIT.kt`, using `helper.runMigrationsAndValidate(...)` like `testMigrate26To27()`.

```kotlin
@Entity(
    tableName = "personalized_featured_tool_order",
    primaryKeys = ["locale", "country", "tool"],
    foreignKeys = [
        ForeignKey(
            entity = ToolEntity::class,
            parentColumns = ["code"],
            childColumns = ["tool"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tool"), Index("locale", "country", "order")],
)
internal data class PersonalizedFeaturedToolOrderEntity(
    val locale: Locale,
    val country: String,
    val tool: String,
    val order: Int,
)
```

```kotlin
autoMigrations = [
    // ...
    AutoMigration(from = 26, to = 27),
],
/*
 * v6.3.7
 * 26: 2026-04-07
 * 27: 2026-05-04
 */
```

**When pruning old migration history, collapse one adjacent pair per commit.** First remove obsolete SQL handlers and constants. Then merge auto-migrations stepwise (7->8 plus 8->15 into 7->15, and so on), deleting the stale schema JSON and merging the matching migration tests each time, and fold `RenameColumn` or `onPostMigrate` logic into the combined spec. Each step stays checkable by the migration test. See PR #4409 and `Migration7To22` in `GodToolsRoomDatabase.kt`.

## 4. API interfaces and JSON:API models

**Write Retrofit endpoints as `suspend fun` returning `Response<JsonApiObject<T>>`, with path and param names in file-level constants.** All 21 endpoints are `suspend` and return a `Response`; JSON:API resources use `JsonApiObject<T>`. Returning `Response` lets sync check `code() == HTTP_OK` instead of catching `HttpException`. Use `Response<ResponseBody>` with `@Streaming` for files, give optional filters `= null` defaults, and pass sparse fields and includes as `@QueryMap params: JsonApiParams`. See `library/api/src/main/kotlin/org/cru/godtools/api/ToolsApi.kt`.

```kotlin
@GET("$PATH_RESOURCES/featured")
suspend fun getFeaturedTools(
    @Query(PARAM_FILTER_LANGUAGE) locale: Locale,
    @Query(PARAM_FILTER_COUNTRY) country: String? = null,
    @QueryMap params: JsonApiParams,
): Response<JsonApiObject<Tool>>
```

Provide each API in `ApiModule` as `@Provides @Reusable fun xApi(@Named(MOBILE_CONTENT_API) retrofit: Retrofit): XApi = retrofit.create()`. Use `MOBILE_CONTENT_API_AUTHENTICATED` for user-scoped endpoints.

**Shape JSON:API models so sparse fieldsets keep working.** Sync builds sparse fieldsets from `JSONAPI_FIELDS`, so an attribute left out of it comes back silently null.

- Attribute names are file-level `private const val JSON_X = "api-name"`. Move one to the companion as public only when sync needs it for includes or fields.
- The companion holds `const val JSONAPI_TYPE` and, for synced types, `val JSONAPI_FIELDS = arrayOf(...)`. Add every new attribute to it.
- Mark local-only fields `@JsonApiIgnore` and the server id `@JsonApiId val apiId: Long?`.
- Keep an `internal constructor()` for the converter, and register new types in `ApiModule.jsonApiConverter()` with `.addClasses(...)`.

See `library/model/src/main/kotlin/org/cru/godtools/model/Language.kt` and `Tool.kt`.

## 5. Sync tasks, GodToolsSyncService and workers

**Only `:library:sync` touches `XSyncTasks`. Everyone else calls `GodToolsSyncService`.** `executeSync` is the one place that handles the dispatcher, cancellation, `IOException` and crash logging, so callers get a plain `Boolean`. Add a method to `GodToolsSyncService` and, for a new tasks class, a map binding in `SyncTaskModule`. See `library/sync/src/main/kotlin/org/cru/godtools/sync/GodToolsSyncService.kt` and 5cd699a.

```kotlin
// GodToolsSyncService, inside // region Sync Tasks
suspend fun syncToolOrder(locale: Locale, country: String?, force: Boolean = false) =
    executeSync<ToolSyncTasks> { syncToolOrder(locale, country, force) }

// SyncTaskModule
@Binds
@IntoMap
@SyncTaskKey(ToolSyncTasks::class)
internal abstract fun toolSyncTasks(tasks: ToolSyncTasks): BaseSyncTasks
```

For syncs that need background retry, schedule the matching work when the result is false, and also on `CancellationException` before rethrowing (see `syncTools` and `syncLanguages` in the same file). Never inject `ToolSyncTasks` into a presenter.

**Write each sync method with the same template.** `@Singleton internal class XSyncTasks @Inject internal constructor(...) : BaseSyncTasks()`. The mutex de-dupes concurrent refreshes ("a Mutex is effectively a single permit semaphore", Frett, PR #4392), and reusing one normalized key everywhere avoids stale-cache bugs.

1. Normalize inputs once (`val normalizedCountry = country?.uppercase()`) and use that value for the lock, API call, store and sync-time key.
2. Lock with `MutexMap().withLock(key)` for keyed syncs or a plain `Mutex` for unkeyed ones.
3. Return `true` early when `!force && !lastSyncTimeRepository.isLastSyncStale(...)`.
4. Fetch with `.takeIf { it.code() == HTTP_OK }?.body()?.data ?: return false`, requesting only needed fields through a private `buildXApiParams()`.
5. Store through a `*FromSync` repository method, call `updateLastSyncTime` with the same parts, and return `true`.
6. Wrap each keyed sync and its mutex in a `// region`. Run independent syncs in parallel with `coroutineScope { launch { ... }; launch { ... } }`.

```kotlin
// region Featured Tools
private val featuredToolsMutex = MutexMap()

internal suspend fun syncFeaturedTools(locale: Locale, country: String?, force: Boolean = false): Boolean {
    val normalizedCountry = country?.uppercase()

    featuredToolsMutex.withLock(locale to normalizedCountry) {
        if (!force &&
            !lastSyncTimeRepository.isLastSyncStale(
                SYNC_TIME_FEATURED_TOOLS,
                locale,
                normalizedCountry.orEmpty(),
                staleAfter = STALE_DURATION_TOOLS,
            )
        ) {
            return true
        }

        val tools = toolsApi.getFeaturedTools(locale, normalizedCountry, buildToolPlaceholderParams())
            .takeIf { it.code() == HTTP_OK }?.body()?.data ?: return false

        toolsRepository.storeFeaturedToolsFromSync(locale, normalizedCountry, tools)
        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_FEATURED_TOOLS, locale, normalizedCountry.orEmpty())
        return true
    }
}
// endregion Featured Tools
```

See `library/sync/src/main/kotlin/org/cru/godtools/sync/task/ToolSyncTasks.kt`.

**Declare sync-time keys as `last_synced.*` constants and pass key parts as varargs.** `LastSyncTimeRepository` builds the key from its varargs, so hand-concatenated keys cannot be matched by `resetLastSyncTime(..., isPrefix = true)`. Use `const val SYNC_TIME_X = "last_synced.x"` (file-private, or `internal` in the companion when tests read it) and `const val STALE_DURATION_X = TimeConstants.DAY_IN_MS` (or `WEEK_IN_MS`).

```kotlin
// wrong: updateLastSyncTime("last_synced.tool_order.$locale.$country")
lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_TOOL_ORDER, locale, normalizedCountry.orEmpty())
```

**Lay out each worker file as `WORK_NAME`, a schedule extension, then the `@HiltWorker`.** The schedule function next to the worker keeps work names private and consistent. Sync workers use `SyncWorkRequestBuilder`, which adds `TAG_SYNC` and a connected-network constraint. The `@AssistedInject` constructor is not `internal`. Inject WorkManager as `dagger.Lazy<WorkManager>` so DI setup does not start it. See `library/sync/src/main/kotlin/org/cru/godtools/sync/work/SyncToolsWorker.kt`.

```kotlin
private const val WORK_NAME = "SyncTools"

internal fun WorkManager.scheduleSyncToolsWork() =
    enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, SyncWorkRequestBuilder<SyncToolsWorker>().build())

@HiltWorker
internal class SyncToolsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val toolSyncTasks: ToolSyncTasks,
) : CoroutineWorker(context, workerParams)
```

**Register dashboard page syncs with `circuitContext.rememberSyncTask`, not `LaunchedEffect`.** The parent `DashboardPresenter` runs every registered task on pull-to-refresh, and each registration updates when its key changes. See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/tools/ToolsPresenter.kt`.

```kotlin
@Composable
private fun RegisterSyncTask(locale: Locale) {
    circuitContext.rememberSyncTask(locale) { syncData(locale, it) }
}

private fun SyncTracker.syncData(locale: Locale, force: Boolean = false) = launchSync {
    val country = settings.getCountrySettingFlow().first()
    coroutineScope {
        launch { syncService.syncFeaturedTools(locale, country, force) }
        launch { syncService.syncToolOrder(locale, country, force) }
    }
}
```

**For downloads, try the CDN then fall back to the API, and verify SHA-256 and size.** Join small private suspend helpers with `||` inside `withContext(ioDispatcher)` using the injected dispatcher. Each helper catches `IOException` and returns false. The shared store step checks the manifest's sha256 and size and deletes the partial file on mismatch ("Sha256 is a checksum... not corrupted", Frett, PR #4424). Test CDN ok, CDN 404, IOException fallback, both fail, and good or bad hash and size. See `GodToolsDownloadManager.kt` (`downloadPublishedFileIfNecessary`). Older code there still uses `Dispatchers.IO` directly; do not copy that.

## 6. Settings and remote config flags

**Add user preferences to `Settings` as DataStore keys with a `getXFlow()` and a `suspend fun updateX(value: T?)` where null removes.** All settings added in the last year use this shape. Do not add new SharedPreferences settings. Store Locales as language tags, and group by `// region <Feature> Settings`. See `library/base/src/main/kotlin/org/cru/godtools/base/Settings.kt` and f893cdd.

```kotlin
fun getPersonalizationCountryFlow() = dataStorePreferences.data
    .map { it[KEY_PERSONALIZATION_COUNTRY] }
    .distinctUntilChanged()

suspend fun updateCountrySetting(isoCode: String?) {
    dataStorePreferences.updateData {
        it.toMutablePreferences().apply {
            when (isoCode) {
                null -> remove(KEY_PERSONALIZATION_COUNTRY)
                else -> set(KEY_PERSONALIZATION_COUNTRY, isoCode)
            }
        }
    }
}
```

**Add remote config flags to `Config.kt` with their default in the same commit, and read them once with `rememberSaveable`.** Name them `const val CONFIG_UI_<AREA>_<NAME> = "ui_<area>_<name>..."`, add the entry to `CONFIG_DEFAULTS` (new features default to `false`), and wrap related flags in a shared `// region` in both places. In the presenter, inject `FirebaseRemoteConfig`. "We load it once and store it via rememberSaveable" so the UI does not jump when config refreshes mid-screen (Frett, PR #4392). See `library/base/src/main/kotlin/org/cru/godtools/base/Config.kt` and `ToolsPresenter.kt`.

```kotlin
val isPersonalizationEnabled = rememberSaveable {
    remoteConfig.getBoolean(CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED)
}
```

**Tendency: move non-trivial Flow graphs out of presenters into an injected `XFlowProducer`.** Expose `fun getFlow(...)` from `internal class XFlowProducer @Inject constructor(settings, repositories)`, and call `remember(keys) { producer.getFlow(...) }.collectAsState(null)` in the presenter. Use `flatMapLatest` to switch, `.distinctUntilChanged()` after `combine`, and apply shared filters once at the end. Frett extracted all three dashboard producers this way in 2026, so it is the direction, but several presenters still build flows inline. See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/lessons/LessonsFlowProducer.kt`.

**Tendency: let personalized lists fall back to the `country = null` list when they should never be empty.** Combine both flows with `lessons.ifEmpty { fallback }`, then `.distinctUntilChanged()`. Skip the fallback only when the product wants the section hidden (featured tools emit `emptyList()` with no country). See `LessonsFlowProducer.kt` and `FeaturedToolsFlowProducer.kt`.

## 7. Dagger and Hilt wiring

**Scope stateless providers `@Reusable` and stateful ones `@Singleton`.** APIs, repositories, Retrofit instances and config objects are `@Provides @Reusable`. The Room DB, OkHttpClient, EventBus, app CoroutineScope, and any class holding locks, caches or scopes are `@Singleton` (on the class itself for constructor-injected types). Name providers after the type in lowerCamelCase and give an explicit return type when returning an interface. See `library/api/src/main/kotlin/org/cru/godtools/api/ApiModule.kt`.

**Pick the module shape by what it contains.** `object XModule` for only `@Provides` (use `@get:Provides val` for constants). `abstract class` or `interface` for only `@Binds`/`@Multibinds`. `abstract class` with `@Provides` in a `companion object` for mixed. Always `@InstallIn(SingletonComponent::class)`, and mark feature-internal modules `internal`.

**Put feature-scoped modules next to the feature; keep `app/.../dagger/` for app-wide wiring.** `app/dagger` holds Config, Services, EventBus, Account and dynamic feature wiring. A module that binds a feature's interface or contributes a deep-link parser lives in that feature package, usually `internal`. See `app/src/main/kotlin/org/cru/godtools/ui/dashboard/tools/ToolsModule.kt` and `ui/settings/language/LanguageSettingsModule.kt`.

Note: CLAUDE.md says Dagger modules live in `app/.../dagger/`; the code only does that for app-wide modules. Follow the code.

**When a presenter depends on a helper with `@Composable` functions, use interface + `DefaultX` + `FakeX`.** Bind `internal class DefaultX @Inject constructor(...) : X` with `@Binds` in the feature module and write `FakeX` in test sources. Frett (PR #4392): "I want to start using Fakes when the object being faked/mocked has a composable function", because mockposable delays Kotlin upgrades. Never name an impl `XImpl`, and do not add new `everyComposable` mocks (the 5 test files still using mockposable are legacy). See 78d17a4 and `app/src/test/kotlin/org/cru/godtools/ui/dashboard/tools/FakeToolFiltersStateProducer.kt`.

**Give classes that own a CoroutineScope a `@VisibleForTesting` primary constructor and an `@Inject` secondary one.** Tests pass a `TestScope` and can advance background work instead of racing a real scope. Make any class with a scope or Mutex `@Singleton`. See `library/user-data/src/main/kotlin/org/cru/godtools/user/activity/UserActivityManager.kt`.

```kotlin
@Singleton
class UserActivityManager @VisibleForTesting internal constructor(
    private val syncService: GodToolsSyncService,
    private val coroutineScope: CoroutineScope,
) {
    @Inject
    internal constructor(syncService: GodToolsSyncService) : this(syncService, CoroutineScope(SupervisorJob()))
}
```

**Qualify same-typed bindings with `@Named(CONST)` using a `const val` from the providing module.** An inline string silently breaks when the provider's value changes. Import the constant at the injection site (`@Named(IS_CONNECTED_STATE_FLOW)` from `BaseModule`). There are no custom `@Qualifier` annotations in the repo; do not add one.

**Pass flavor-specific values into libraries through a typed config object provided from `:app`.** Only `:app` has the stage and production flavors. Add a data class in the library (like `ApiConfig`) and provide it from `app/.../dagger/ConfigModule.kt` with `@get:Provides val apiConfig = ApiConfig(mobileContentApiUrl = BuildConfig.MOBILE_CONTENT_API, ...)`. Libraries never read flavor BuildConfig, and a stringly typed `@Named("..._URL") String` is not the pattern. See 2bf32ed.

**Start background services with `@Binds @IntoSet @EagerSingleton` in the owning module, not in `Application.onCreate`.** Use `LifecycleEvent.ACTIVITY_CREATED` with `ThreadMode.ASYNC` for heavy work and MAIN only when needed. Debug-only services go in a `src/debug` module. See `library/download-manager/src/main/kotlin/org/cru/godtools/downloadmanager/DownloadManagerModule.kt`.

**Use `@EntryPoint` only where constructor injection is impossible, and wrap it once.** Composables, Views and dynamic features qualify. Wrap access in a `LocalX` CompositionLocal (falling back to `EntryPointAccessors.fromApplication` with a stub for `LocalInspectionMode`) or an `internal val Context.x` extension. Never call `EntryPointAccessors` inline in a screen. See `ui/base/src/main/kotlin/org/cru/godtools/base/ui/compose/LocalEventBus.kt`.

**Tendency: contribute Circuit deep-link parsers with `@get:Provides @get:IntoSet`.** Write `object XDeepLinkParser : CircuitDeepLinkParser` with a `when` over scheme, host and path, register it as `@get:Provides @get:IntoSet val xDeepLinkParser: CircuitDeepLinkParser get() = XDeepLinkParser`, and add the manifest intent-filter. Only one parser uses this so far. See `LanguageSettingsModule.kt`.

**Post analytics from the presenter's event handler through the injected EventBus.** Record screen views with `RecordAnalyticsScreen(AnalyticsScreenEvent(...))` in the layout. Event classes keep action names as companion `const val`s and implement equals/hashCode so tests can `verify { eventBus.post(...) }`. Layout-side posts exist only in pre-Circuit code. See `ToolsPresenter.kt` (`OpenAnalyticsActionEvent`).

## 8. Logging and exception handling

**Always log with a tag, and remember that `Timber.e` becomes a Crashlytics non-fatal.** Use a file-level `private const val TAG = "ClassName"` and `Timber.tag(TAG)`. Log unexpected errors with `.e(e, ...)` and expected failures (network errors, token refresh) with `.d(e, ...)`, or the Crashlytics dashboard floods. See `GodToolsSyncService.kt` and `library/account/.../GoogleAccountProvider.kt` (`.d` for expected).

Note: pr-review `patterns.md` shows an inline tag string and does not mention that `.e` goes to Crashlytics; the code mostly uses a private const `TAG`. Follow the code (inline tags are tolerated, not preferred).

**Catch specific exceptions inside tasks, workers and repositories; use a broad catch only at a subsystem boundary, in this shape.** Swallowing `CancellationException` breaks structured concurrency, and a silent `catch (e: Exception)` hides bugs. At a boundary whose failure must not crash the app (`executeSync`, account login, initial-content import), rethrow cancellation first, treat `IOException` as expected, and log the rest at `.e`.

```kotlin
try {
    with<T, Boolean> { block() }
} catch (e: CancellationException) {
    throw e
} catch (_: IOException) {
    false
} catch (e: Exception) {
    Timber.tag(TAG).e(e, "Unhandled sync exception")
    false
}
```

Note: pr-review `SKILL.md` and `patterns.md` say never catch bare `Exception`; the owners do it at boundaries, always with a Timber `.e` log and a `CancellationException` rethrow first. Follow the code, and only at a real boundary.

## 9. Tests for the data layer

**Write repository contract tests once in `abstract class XRepositoryIT`, with a thin `XRoomRepositoryIT`.** The contract is tied to the interface, not Room, and runs on the JVM under Robolectric. Both go in `src/test` (there are no androidTest sources), grouped by `// region methodName()` with backtick names like `` `getAllTools() - Returns All Tool Types` ``. Suffix DB and integration tests with `IT`. See `library/db/src/test/kotlin/org/cru/godtools/db/repository/ToolsRepositoryIT.kt`.

```kotlin
@RunWith(AndroidJUnit4::class)
internal class ToolsRoomRepositoryIT : ToolsRepositoryIT() {
    @get:Rule
    internal val dbRule = RoomDatabaseRule(
        GodToolsRoomDatabase::class.java,
        StandardTestDispatcher(testScope.testScheduler)
    )
    override val repository get() = dbRule.db.toolsRepository
}
```

**Test sync tasks with three cases per sync and the in-memory last-sync fake.** Build the tasks class directly, mock APIs and repositories with mockk (`coEvery { ... } returns Response.success(JsonApiObject.of(...))`), and use `InMemoryLastSyncTimeRepository` from db testFixtures. Cover the default path, `force = false - already synced` (assert `toolsApi wasNot Called`), and `force = true - already synced`, and assert stored time with `isLastSyncStale(KEY, parts..., staleAfter = ...)`. See `library/sync/src/test/kotlin/org/cru/godtools/sync/task/ToolSyncTasksTest.kt`.

**Build test data with `randomX(...)` factories and put new ones in the owning module's `src/testFixtures`.** Random defaults catch tests that pass by accident, and testFixtures keep test code out of the APK. Every field is a named parameter with a random default: `.takeIf { Random.nextBoolean() }` for nullables, `entries.random()` for enums, `Uuid.random()` for ids. Override only what the test asserts on. Consume with `testImplementation(testFixtures(projects.library.x))`. Do not add more `@RestrictTo(TESTS)` fixtures to main (the ones in `Tool.kt` carry a TODO to move). See `library/model/src/testFixtures/kotlin/org/cru/godtools/model/Language+TestFixtures.kt`.

## 10. Module boundaries, Gradle deps and commit shape

**Keep UI out of `library/*` and keep fakes in the module that owns the interface.** Frett moved a binding adapter out of download-manager (334fa73) and moved `InMemoryLastSyncTimeRepository` into db testFixtures (eae8639). The only Composables in libraries are repository read helpers. App screens and presenters go in `:app`, tool renderers in `ui/*`, and cross-module activity launches go through `ui/base/.../Activities.kt`.

**Use `api(...)` only for dependencies whose types appear in the module's public API, and declare every direct dependency.** Leaking deps through `api` slows builds, and reviewers asked for explicit declarations of directly used modules (PR #4458). Keep the blank-line groups and alphabetical order described in `references/build-ci-resources.md` (Dependency declarations). Room modules set `ksp { arg("room.schemaLocation", ...) }`. See `library/db/build.gradle.kts`:

```kotlin
dependencies {
    api(projects.library.model)
    implementation(projects.library.base)

    implementation(libs.androidx.room)
    implementation(libs.androidx.room.ktx)

    implementation(libs.dagger)
    implementation(libs.hilt)

    ksp(libs.androidx.room.compiler)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.turbine)
}
```

**Tendency: land a data feature bottom-up, one layer per commit.** (1) table and migration, (2) repository method and repository IT, (3) API endpoint, sync task, sync test and GodToolsSyncService entry, (4) presenter or UI consumer. Each commit compiles with its own tests, and pure renames get their own commit. This is how Frett builds his PRs (e3775e5, e38a1b3, 5cd699a); squash-merged PRs from others rarely get asked for it.
