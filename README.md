GodTools Android
================

[![codecov](https://codecov.io/gh/CruGlobal/godtools-android/branch/develop/graph/badge.svg)](https://codecov.io/gh/CruGlobal/godtools-android)

GodTools is a mobile discipleship app built by [Cru](https://www.cru.org/). This repository contains the Android client.

## Documentation

Detailed onboarding and architecture documentation is browsable on the [GitHub Wiki](https://github.com/CruGlobal/godtools-android/wiki); its source of truth is the [`wiki/`](https://github.com/CruGlobal/godtools-android/tree/develop/wiki) directory of this repository, versioned alongside the code and mirrored to the GitHub Wiki by the `Publish Wiki` workflow. Start with [Home](wiki/Home.md) for the page index, or jump straight to [Getting Started](wiki/Getting-Started.md) for a fresh-machine-to-running-emulator walkthrough (prerequisites, first-build downloads, and troubleshooting). If your PR changes behavior a wiki page documents, update the page in the same PR (do not edit the GitHub Wiki directly — syncs overwrite it) — see [Contributing](wiki/Contributing.md).

## Requirements

- **JDK** — `.tool-versions` pins Temurin 25 as the launcher JDK; the compile toolchain is Java 21 (`jvmToolchain(21)`), auto-provisioned by the foojay resolver. See [wiki/Getting-Started](wiki/Getting-Started.md#prerequisites) for details.
- **Android SDK** — compile/target SDK 37, minimum SDK 24
- **Git LFS** — required for Paparazzi snapshot files

## Getting Started

```bash
# Clone the repo (Git LFS must be installed first)
git clone https://github.com/CruGlobal/godtools-android.git
```

### Git LFS

Paparazzi snapshot images are stored in [Git LFS](https://git-lfs.com/). Install it before cloning or working with snapshot tests:

```bash
brew install git-lfs   # macOS
git lfs install
```

## Build & Development

```bash
# Build the app bundle
./gradlew bundle

# Run unit tests (all variants)
./gradlew test

# Run tests for a specific module
./gradlew :library:model:test
./gradlew :app:test

# Verify Paparazzi snapshot tests (requires Git LFS)
./gradlew verifyPaparazzi

# Run ktlint (code style checks)
./gradlew :build-logic:ktlintCheck ktlintCheck

# Run Android lint
./gradlew lint
```

> **Paparazzi Snapshots:** Do not record snapshots locally. Trigger the manual GitHub Actions workflow on your feature branch to generate and commit updated screenshots.

## Project Structure

```
app/                  # Main application module
build-logic/          # Gradle convention plugins
feature/              # Google Play Dynamic Feature modules
library/              # Core non-UI modules
ui/                   # UI and tool-renderer modules
```

### `app/`
Main application module — `GodToolsApplication` (Hilt entry point), `DashboardActivity`, Dagger modules in `dagger/`.

### `library/`
| Module | Purpose |
|---|---|
| `model` | Data models and JSON:API type definitions |
| `db` | Room database, DAOs, and repositories |
| `api` | Retrofit REST and Scarlet WebSocket clients |
| `sync` | WorkManager-based data synchronization |
| `base` | Core utilities, settings, file system abstraction |
| `account` | User authentication |
| `analytics` | Event tracking |
| `download-manager` | Tool download management |
| `initial-content` | Bundled initial content |
| `user-data` | User preferences and state |

### `ui/`
| Module | Purpose |
|---|---|
| `base` | Shared Compose components and Material 3 theme |
| `base-tool` | Base tool rendering infrastructure |
| `tract-renderer` | Tract tool renderer |
| `lesson-renderer` | Lesson tool renderer |
| `article-renderer` | Article tool renderer |
| `article-aem-renderer` | AEM article tool renderer |
| `cyoa-renderer` | Choose Your Own Adventure renderer |
| `tips-renderer` | Tips renderer |
| `tutorial-renderer` | Tutorial renderer |
| `shortcuts` | App shortcuts UI |
| `qr-code` | QR code feature UI |

### `feature/`
Google Play Dynamic Feature modules: `bundledcontent` (bundled initial content delivered via Play).

### `build-logic/`
Gradle convention plugins: `godtools.application-conventions`, `godtools.library-conventions`, `godtools.dynamic-feature-conventions`.

## Architecture & Key Frameworks

| Concern | Technology |
|---|---|
| Dependency injection | Hilt (Dagger 2) |
| UI | Jetpack Compose + Material Design 3 |
| Navigation / UI state | [Slack Circuit](https://slackhq.github.io/circuit/) |
| Networking | Retrofit (REST), Scarlet (WebSocket), OkHttp; CDN downloads via `CdnApi` with SHA-256 verification |
| Database | Room |
| Background work | WorkManager |
| Image loading | Coil |
| Dependency versions | `gradle/libs.versions.toml` version catalog |

**Circuit pattern:** Presenter/UI pairs annotated with `@CircuitInject`. Presenters are `@Composable` functions that return state objects; actions flow through `UiState.eventSink`.

## Build Variants

- **Product flavors** (`env` dimension): `stage` (staging API), `production` (production API)
- **Build types**: `debug`, `qa` (inherits debug), `release`
- The `stage` flavor is only available for `debug` and `qa` builds

## Code Style

Style is enforced by [ktlint](https://pinterest.github.io/ktlint/) with the `android_studio` code style.

- Max line length: 120
- Indent: 4 spaces

Always run ktlint before committing:

```bash
./gradlew :build-logic:ktlintCheck ktlintCheck
```

## Translations (Crowdin)

Strings are managed via [Crowdin](https://crowdin.com/). To push/pull translations, install the [Crowdin CLI](https://crowdin.github.io/crowdin-cli/) and set the `CROWDIN_API_TOKEN` environment variable.

## Contributing

1. Branch from `develop` and open PRs against `develop` (the default branch).
2. Add unit tests for any new functionality.
3. Run ktlint and unit tests before opening a PR.
4. For UI changes, trigger the Paparazzi snapshot workflow on your branch rather than recording locally.
