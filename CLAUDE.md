# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Default Branch

The default branch for this repository is **`develop`**. Use `develop` as the base branch when creating pull requests.

## Build & Development Commands

```bash
# Build the app bundle
./gradlew bundle

# Run ktlint (code style checks)
./gradlew :build-logic:ktlintCheck ktlintCheck

# Run Android lint
./gradlew lint

# Run all unit tests
./gradlew test

# Run tests for a specific module
./gradlew :library:model:test
./gradlew :app:test

# Run a single test class
./gradlew :library:model:test --tests "org.cru.godtools.model.ToolTest"

# Verify Paparazzi snapshot tests (requires Git LFS)
./gradlew verifyPaparazzi
```

**Requirements:** JDK 21 (Temurin), specified in `.tool-versions`. Git LFS is required for Paparazzi snapshot files.

**Paparazzi Snapshots:** Do not record snapshots locally. Instead, trigger the manual GitHub Actions workflow on the feature branch to generate and commit updated screenshots.

**Pre-commit:** Always run `./gradlew :build-logic:ktlintCheck ktlintCheck` before committing.

## Project Architecture

### Module Layout

- **`app/`** — Main application module. Contains `GodToolsApplication` (Hilt entry point), `DashboardActivity` (main activity), Dagger modules in `dagger/`, and all app-level screens/navigation.
- **`library/`** — Core non-UI modules:
  - `model` — Data models and JSON:API type definitions
  - `db` — Room database, DAOs, and repositories
  - `api` — Retrofit REST and Scarlet WebSocket clients
  - `sync` — WorkManager-based data synchronization
  - `base` — Core utilities, settings, file system abstraction
  - `account` — User authentication
  - `analytics` — Event tracking
  - `download-manager` — Tool download management
  - `initial-content` — Bundled initial content
  - `user-data` — User preferences and state
- **`ui/`** — UI modules, each rendering a different tool type:
  - `base` — Shared Compose components and Material 3 theme
  - `base-tool` — Base tool rendering infrastructure
  - `tract-renderer`, `lesson-renderer`, `article-renderer`, `article-aem-renderer`, `cyoa-renderer`, `tips-renderer`, `tutorial-renderer` — Tool-type-specific renderers
  - `shortcuts`, `qr-code` — Feature UI modules
- **`feature/bundledcontent`** — Google Play Dynamic Feature for bundled content
- **`build-logic/`** — Gradle convention plugins (`godtools.application-conventions`, `godtools.library-conventions`, `godtools.dynamic-feature-conventions`)

### Build Variants

- **Product flavors** (dimension `env`): `stage` (staging API), `production` (production API)
- **Build types**: `debug`, `qa` (inherits from debug), `release`
- **Stage flavor** is only enabled for debug and QA build types
- **Unit test variants**: some modules have product flavors and expose `testProductionDebugUnitTest`; others only expose `testDebugUnitTest`. Use the `test` task to run all variants without needing to know which applies.

### Key Frameworks & Patterns

- **DI:** Hilt (Dagger 2). Application class annotated with `@HiltAndroidApp`. Modules in `app/src/main/kotlin/org/cru/godtools/dagger/`.
- **Navigation & UI state:** Slack Circuit library (v0.34+) — uses `@CircuitInject` annotated Presenter/UI pairs with Hilt codegen. Presenters are `@Composable` functions that return state objects. Back navigation is handled internally by Circuit; `GestureNavigationDecorationFactory` does not accept an `onBackInvoked` parameter.
- **UI:** Jetpack Compose with Material Design 3. Some legacy views use DataBinding/ViewBinding.
- **Networking:** Retrofit for REST APIs, Scarlet for WebSocket, OkHttp as HTTP client. API base URLs configured per flavor in `build-logic/src/main/kotlin/Constants.kt`.
- **Database:** Room with DAOs and type converters.
- **Background work:** WorkManager with Hilt worker injection. When launching non-cancellable work from a Presenter (e.g., committing state on a user action), use `launch(start = CoroutineStart.UNDISPATCHED) { withContext(NonCancellable) { ... } }` — *not* `launch(NonCancellable) { ... }`. The latter doesn't guarantee the block runs if the scope is already cancelling when `launch` is called.
- **Image loading:** Coil.
- **Dependency versions:** Centralized in `gradle/libs.versions.toml` (Gradle version catalog).

## Code Style

Enforced via ktlint with `android_studio` code style. Key settings from `.editorconfig`:
- Max line length: 120
- Indent: 4 spaces
- `@Composable` functions may use PascalCase (naming rule exempted)
- Trailing commas on declaration/call sites are not enforced by ktlint but are allowed by the compiler
