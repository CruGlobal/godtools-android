# Build, CI and resources

How the owners write module build scripts, manage the version catalog and shared-library versions, run the PR checks, record snapshots, and handle string, drawable and other Android resources. Read this before touching any `build.gradle.kts`, `gradle/libs.versions.toml`, `settings.gradle.kts`, a `res/` file, or before pushing a branch.

## Contents

1. [Module build scripts](#module-build-scripts)
2. [Dependency declarations](#dependency-declarations)
3. [Version catalog and shared libraries](#version-catalog-and-shared-libraries)
4. [PR hygiene and local checks](#pr-hygiene-and-local-checks)
5. [Paparazzi snapshots and Git LFS](#paparazzi-snapshots-and-git-lfs)
6. [String resources and locales](#string-resources-and-locales)
7. [Drawables, layouts and what library modules may hold](#drawables-layouts-and-what-library-modules-may-hold)
8. [Other CI facts](#other-ci-facts)

## Module build scripts

**Turn on Compose, DataBinding and EventBus indexes only through the build-logic helpers.** Use `configureCompose(project)` (add `enableCircuit = true` for Circuit screens, which also applies ksp, parcelize, the circuit bundle and `circuit.codegen.mode=hilt`), `enableDatabinding(project)`, and `createEventBusIndex("<fqcn>")`. The helpers bundle the plugin, flags and dependencies together, so hand-rolled setup drifts and gets reverted (cf43f58 migrated every module to `enableDatabinding()`). Flags with no helper (`buildConfig`, `viewBinding`, `vectorDrawables`) are still set directly. See `build-logic/src/main/kotlin/AndroidConfiguration.kt:76-134` and `EventBusConfiguration.kt`.

```kotlin
android {
    namespace = "org.cru.godtools.tool"

    configureCompose(project)
    createEventBusIndex("org.cru.godtools.base.tool.BaseToolEventBusIndex")
    enableDatabinding(project)

    defaultConfig.vectorDrawables.useSupportLibrary = true
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
```

Anti-example (gets reverted):

```kotlin
plugins { kotlin("kapt") }
android {
    buildFeatures {
        compose = true
        dataBinding = true
    }
}
```

Keep `targetSdk` only in `godtools.application-conventions`, and use the `androidComponents` API instead of `afterEvaluate` or `applicationVariants` (6e8d54e, AGP 9 migration).

**Use KSP for every annotation processor; kapt only exists inside `enableDatabinding()`.** Dagger, Hilt, Room, androidx-hilt and Circuit compilers go in `ksp(...)` (and `kspTest(...)` for test Hilt) with `alias(libs.plugins.ksp)`. No module script mentions kapt. When a module's last DataBinding layout goes away, remove `enableDatabinding(project)` in the same change, and when migrating a DataBinding screen to Compose, delete the XML layout and binding adapter too (e7634cd, c945f14, f3e7834).

```kotlin
dependencies {
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)
    kspTest(libs.hilt.compiler)
}
```

Note: `.claude/skills/pr-review/SKILL.md:92` says to keep kapt only with a TODO comment when DataBinding is present; that TODO pattern no longer exists anywhere, and `enableDatabinding()` replaced it. Follow the code.

**Write every library module script in the same shape.** `plugins {}` starts with `id("godtools.library-conventions")`, then `alias(libs.plugins.*)` / `id(...)`. `android {}` starts with `namespace = "org.cru.godtools.<x>"`, then the helper calls, then remaining flags. Then `dependencies {}`. Add extra top-level blocks (`ksp {}` args, `tasks.withType<Test>`) only when the module really needs them. Convention plugins already set SDKs, toolchain, lint config and test options, so do not repeat those. See `ui/shortcuts/build.gradle.kts` and `ui/tutorial-renderer/build.gradle.kts`.

```kotlin
plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tutorial"

    configureCompose(project, enableCircuit = true)
}

dependencies { ... }
```

Do not copy `ui/qr-code/build.gradle.kts` as a model. It is a contributor file that uses `project(":ui:base")` and redeclares bundle deps.

**Leave the JDK toolchain alone.** Kotlin and Android compile with `jvmToolchain(21)` from the convention plugin (`AndroidConfiguration.kt:68`); do not set it in module scripts. Any launcher JDK works because the foojay resolver fetches 21.

Note: CLAUDE.md says "JDK 21 (Temurin), specified in .tool-versions", but `.tool-versions` pins temurin-25; 21 is only the Gradle/Kotlin toolchain. Follow the code.

**Room config lives in `:library:db`.** It passes `room.schemaLocation` and `room.incremental` as ksp args and adds `room-schemas` to test assets. When you bump the database version, commit the generated JSON under `library/db/room-schemas/`, because migration tests read it. See `library/db/build.gradle.kts:13-22`.

**Unit tests only run for debug with the production flavor (or no flavor).** Put them in `src/test`; there are no stage or qa test variants (`AndroidTestConfiguration.kt:35-43`). Run `./gradlew test` to hit every module.

## Dependency declarations

**Keep dependencies in blank-line separated groups, sorted alphabetically inside each group.** Project modules first, then platform BOMs, then library families (androidx, gtoSupport, firebase/play, godtoolsShared, other third-party), then `debugImplementation`, `ksp`, `test*`, `testFixtures*`. `api` lines usually sit just before or after the matching `implementation` lines. Add a new line inside the matching group and never remove the existing blank lines: Frett restored deleted blank lines in a contributor PR before approving (PR #4256, 563dbc1). The order of the library-family groups varies per module (firebase before gtoSupport in `ui/shortcuts`, ksp after test in `library/sync`), so follow the module's existing order.

```kotlin
dependencies {
    implementation(projects.library.base)
    implementation(projects.ui.base)

    implementation(libs.androidx.core.ktx)

    implementation(libs.gtoSupport.util)

    implementation(libs.hilt)

    testImplementation(libs.kotlin.coroutines.test)

    ksp(libs.hilt.compiler)
}
```

Anti-example:

```kotlin
dependencies {
    implementation(projects.ui.base)
    implementation(libs.zxing)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.turbine)
}
```

**Reference modules with type-safe project accessors.** Write `projects.library.base`, `projects.ui.baseTool`, `testFixtures(projects.ui.base)`. `TYPESAFE_PROJECT_ACCESSORS` is on (`settings.gradle.kts:2`) and typos become compile errors. Never `project(":ui:base")`.

**Do not redeclare what the convention plugins already add.** Every module gets `libs.bundles.common` (kotlin-stdlib, timber) as `implementation` and `libs.bundles.test-framework` (junit, kotlin-test-junit, androidx-test-junit, mockk, robolectric) as `testImplementation`. `configureCompose` adds the `androidx-compose` bundle (runtime, ui, tooling-preview) plus the debug and testing bundles (ui-test, circuit-test). Redeclaring them is noise. `testFixtures` configurations do not get the test bundle, so `testFixturesImplementation(libs.junit)` is fine. See `AndroidConfiguration.kt:33` and `AndroidTestConfiguration.kt:21`.

```kotlin
// Wrong: already provided
implementation(libs.timber)
testImplementation(libs.junit)
```

**Declare every library you use directly; use `api` only when its types leak.** AGP 9 fails builds on missing direct deps, so declare a library even if it arrives transitively (14d6802, ae12592). Use `api(...)` only when the dependency's types appear in the module's public API, like `api(projects.library.model)` in `library/db` or `api(libs.okio)` in `ui/base-tool` (1d27705).

**Share test helpers through `testFixtures`, consumed with `testImplementation`.** Enable with `testFixtures.enable = true` in `android {}`. Consume with `testImplementation(testFixtures(projects.x))` or `testImplementation(testFixtures(libs.gtoSupport.*))`, never `testApi` or `implementation` (72bee67). Fixture-only deps use `testFixturesApi` / `testFixturesImplementation`. Prefer a published testing artifact such as `libs.gtoSupport.testing.androidx.room` over copying helpers (d4e2fe5).

```kotlin
android { testFixtures.enable = true }

dependencies {
    testImplementation(testFixtures(projects.library.db))
}
```

**To add Paparazzi to a library module,** add `alias(libs.plugins.paparazzi)` and these test deps (the shared `BasePaparazziTest` lives in `:ui:base` testFixtures). In `:app` use `testDebugImplementation(testFixtures(projects.ui.base))` (`app/build.gradle.kts:222`).

```kotlin
plugins { alias(libs.plugins.paparazzi) }

dependencies {
    testImplementation(libs.paparazzi)
    testImplementation(libs.testparameterinjector)
    testImplementation(testFixtures(projects.ui.base))
}
```

## Version catalog and shared libraries

**Never commit an `includeBuild` for a shared library.** `settings.gradle.kts` only includes `build-logic`. For an unreleased change in kotlin-mpp-godtools-tool-parser (`godtoolsShared`) or gto-support, use an uncommitted local `includeBuild` while developing. Bump the version in `gradle/libs.versions.toml` to the upstream `-SNAPSHOT` in its own one-file commit and open this PR without waiting for the upstream PR to merge. Add a `## Dependencies` section to the PR body linking the upstream PR and noting that CI fails until the snapshot is published (PR #4562, #4511). Frett on PR #4254: "this is good for developing locally, but shouldn't be included in the PR". CI cannot see your local checkout. See `references/commits-prs.md` (Cross-repo dependencies).

```toml
# gradle/libs.versions.toml
godtoolsShared = "1.4.0-SNAPSHOT"
gtoSupport = "4.6.0-SNAPSHOT"
```

**Leave routine version bumps to Renovate.** 256 of 293 catalog commits in the past year came from Renovate (automerge, squash, label `dependencies`, grouped families in `.github/renovate.json`). Unrelated bumps widen a feature PR's review. The exception is `godtoolsShared` or `gtoSupport` when the feature needs it; call that out in the PR body.

**Declare everything in the catalog, keep it sorted, and prune in the same change.** No hardcoded coordinates in `build.gradle.kts`. Keep `[versions]`, `[libraries]`, `[bundles]` and `[plugins]` alphabetical, with kebab-case keys grouped by family (`androidx-compose-material3`, `gtoSupport-androidx-compose`). Delete a catalog entry in the same change that removes its last usage (2a93b37, ade57ff). Entries that only exist so Renovate tracks them go under `# HACK: dependencies used to trigger renovate upgrades`.

**Tendency: inline versions for single-use libraries, `version.ref` for shared versions.** A library used by only one entry may carry an inline `"group:artifact:version"` string. When two or more entries share a version, move it to `[versions]` and use `version.ref` (1d27705 did this for okio). Some `[versions]` entries are single-use, so match the style already used for that library family.

## PR hygiene and local checks

**Keep each PR to one concern.** Frett asked a contributor to "remove the changes you made for the lesson share button from this PR" (PR #4255). Split unrelated work out. Small fixes that reference the issue and explain the root cause merge fastest.

**Commit only intentional changes; revert IDE reformatting of untouched code.** Frett: styling changes "clutter up the PR review process because it'll be a bunch of changes unrelated to what you actually did" (PR #4263). Reformat only the lines you changed, and revert noise like blank lines inserted between related annotated properties or rewrapped unrelated code (d3f8154, 31188af). Group related properties without blank lines; separate unrelated groups with one.

```kotlin
// Wrong: IDE-inserted blank lines between related properties
@Inject
lateinit var a: A

@Inject
lateinit var b: B
```

**Run the same commands the "Build App" workflow runs before pushing.** These jobs block merge on PRs to `develop`, `master` and `feature/*` (`.github/workflows/build.yml`):

| CI job | Local command |
|---|---|
| ktlint | `./gradlew :build-logic:ktlintCheck ktlintCheck` (plain `ktlintCheck` skips the included build-logic) |
| Lint Checks | `./gradlew lint` |
| Build App | `./gradlew bundle` |
| Unit Tests | `./gradlew test verifyPaparazzi` |

ktlint uses `android_studio` style, 120 columns, the compose ruleset, and imports layout `*,^` (`.editorconfig`, `build-logic/src/main/kotlin/KtlintConfiguration.kt`).

**Hand-check XML formatting.** ktlint does not check XML. Frett: "our linters unfortunately don't check xml files currently. Let's adjust the indention of this line" (PR #4185). Use 4-space indentation in any `res/` XML you touch, including locale files.

**Fix Android lint errors instead of suppressing them.** There is no lint baseline. Only `MissingTranslation` is downgraded to a warning (`analysis/lint/lint.xml`), so English-only new strings pass.

**Do not "fix" patterns the owner already accepted.** `.claude/skills/pr-review/dismissed-issues.md` lists them: `MutableState` fields in a Circuit `UiState`, no `@AnyThread` on suspend sync task methods, and the separate identical `when` branches for `LibraryExtension` / `ApplicationExtension` in `enableDatabinding()` (needed for smart casts).

## Paparazzi snapshots and Git LFS

**Never record Paparazzi snapshots locally.** Local renders differ from CI and fail `verifyPaparazzi`. Push the branch, run the "Record Snapshots" workflow (`workflow_dispatch`, runs `./gradlew cleanRecordPaparazzi`, pushes a "Record updated snapshots" commit), then fold that commit into the UI change with fixup and autosquash. The `record-screenshots` skill does all of this. `cleanRecordPaparazzi` also deletes stale PNGs for renamed or removed tests.

**Snapshot PNGs are stored in Git LFS.** `**/snapshots/**/*.png` is LFS-tracked (`.gitattributes:13`) and the "Validate Git LFS" workflow fails if a PNG lands as a raw blob. Install git-lfs before pulling or committing snapshot changes.

## String resources and locales

**Put new text in the feature's existing `strings_<area>.xml`, in the module that uses it.** Examples: `ui/tutorial-renderer/src/main/res/values/strings_tutorial_liveshare.xml`, `app/src/main/res/values/strings_dashboard.xml`. A new feature area gets a new `strings_<area>.xml`. Frett on PR #4263: "this needs to be a string added to the live share strings file" (fixed in ead927d). Never inline English in Kotlin.

```xml
<!-- ui/tutorial-renderer/src/main/res/values/strings_tutorial_liveshare.xml -->
<string name="tutorial_live_share_action_generate_qr_code">Generate QR Code</string>
```

```kotlin
Text(stringResource(R.string.tutorial_live_share_action_generate_qr_code))
```

Note: `design_system_rules.md` section 13 and `pr-review` SKILL.md:104 / patterns.md:390 say strings live in `strings.xml`; no module has a `strings.xml`, all use `strings_<area>.xml`. Follow the code.

**Start each section with a comment followed by `<eat-comment />`.** Without it, the comment is attached to the first string as a translator note. The existing section comments in `values/strings_*.xml` do this. Add new strings inside the matching section.

```xml
<!-- Live Share Tutorial -->
<eat-comment />
<string name="tutorial_live_share_action_continue">Continue</string>
```

**Name keys `<screen>_<section>_<purpose>` in snake_case.** Actions end in `_action_<what>`, not `_button`. Variants use a suffix (`dashboard_lessons_header_title_all` / `_personalized`, 54fc413). Legacy prefixes like `menu_`, `tool_`, `share_`, and `tract_` in tips-renderer exist; do not copy them for new screens.

**Add new strings to `res/values/` only.** Do not hand-write `values-xx` entries for new text. Crowdin uploads on push to `develop`, and the weekly "Update Translations" PR from `chore/crowdinTranslations` brings translations back and would overwrite hand edits.

**When you rename, delete or move a key, touch every locale in the same commit.** Renaming only the base key throws away every translation. Rename in `values/` and all `values-*/` files (54fc413 touched 23 locale files). Delete an unused key from all locales (315d64e). When moving a strings file to another module, `git mv` the base and all locale copies unchanged (25b5c75).

**Tendency: keep unused strings whose translations are being ported to godtools-shared.** If a string is no longer referenced here but Crowdin is porting its translations to the shared repo, leave it and its locale copies and say so in the PR body (PR #4562 kept `tract_card_previous` / `tract_card_next`). This only applies when a port is actually in progress.

**Register a new module with translatable strings in `crowdin.yml`.** Crowdin only syncs listed paths, so copy the shape of an existing `files` entry. Modules with only `translatable="false"` strings (cyoa-renderer) are not listed.

**Mark non-text strings `translatable="false"` and use positional placeholders.** Format-only strings, brand names and native language names are not translated. Use `%1$s` / `%1$d` so languages can reorder arguments, escape apostrophes as `\'`, and use the `…` character.

```xml
<string name="tract_card_position" translatable="false">%1$d/%2$d</string>
```

## Drawables, layouts and what library modules may hold

**Make single-color icons `res/drawable/ic_<name>.xml` vectors with `@color/tintable` fills,** placed in the module that uses them. `Icon()` can only tint drawables that use the tintable color (defined in `ui/base/src/main/res/values/colors.xml`). See `app/src/main/res/drawable/ic_priority_high.xml`.

```xml
<path android:fillColor="@color/tintable" android:pathData="..." />
```

Note: `design_system_rules.md` section 9 states this for all icons; 18 of 67 `ic_` drawables use fixed colors, mostly multi-color illustrations such as tips-renderer `ic_tips_*`. The rule covers single-color UI icons. Follow the code.

**Build new UI in Compose only, and delete everything a migration orphans.** When you delete a layout or View, delete in the same change the strings, styles, dimens, drawables, binding adapters and catalog or Gradle deps that only it used (2a93b37, 315d64e). The exception is strings kept for the godtools-shared port. Only 18 legacy layouts remain.

**Keep app-level resources out of feature library modules.** Do not add `colors.xml`, new `Theme.*` styles, launcher icons, `app_name` or application attributes to a feature module. Shared colors and `app_name` live in `:ui:base`; app-only config lives in `:app`. The legacy `Theme.*` styles in `ui/base`, `ui/base-tool`, `ui/lesson-renderer` and `ui/tract-renderer` are not a pattern to extend.

Note: `design_system_rules.md` section 13 says app_name, colors and themes belong in `:app`; the code keeps `app_name` and `colors.xml` in the `:ui:base` library as the shared home. Follow the code.

## Other CI facts

- **Codecov:** overall project coverage may not drop by more than 1% (`.github/codecov.yml`). Add tests with new logic.
- **Tendency: keep tests shard-safe.** CI splits unit tests across 4 shards by module path hash (`AndroidTestConfiguration.kt:45-52`), so one module's tests must not depend on another module's test output.
- **QA builds:** add the "Publish PR QA Build" label to a PR to get a Firebase App Distribution build. Pushes to `develop` deploy automatically.
- **Detekt is advisory:** it runs with `continue-on-error` and only uploads SARIF. Do not add detekt config or suppressions to satisfy it.
