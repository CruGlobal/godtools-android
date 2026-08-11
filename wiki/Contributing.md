# Contributing

This page describes the day-to-day contribution workflow for the GodTools Android repository: how branches and pull requests flow, what to run before committing, the code style rules enforced by ktlint, how detekt analysis fits in, and how translated strings flow through Crowdin (and why you must never hand-edit them). It assumes you already have a working build — if not, start with [Getting Started](Getting-Started.md). For details on the CI jobs referenced here, see [Build System & CI](Build-System-and-CI.md); for the test suites your PR is expected to keep green, see [Testing](Testing.md).

## Branching and pull requests

The default branch is **`develop`** (documented in `CLAUDE.md` and `README.md`). All feature work branches from `develop`, and all pull requests target `develop`. `master` and `feature/*` branches also trigger CI (`.github/workflows/build.yml`), but the everyday flow is:

```mermaid
%%{init: { 'gitGraph': { 'mainBranchName': 'develop' } } }%%
gitGraph
    commit id: "base"
    branch my-feature
    commit id: "work"
    commit id: "tests"
    checkout develop
    merge my-feature id: "PR merged"
    commit id: "CI + QA build"
```

1. Branch from `develop` (in the main repository if you have write access, or in your fork — see below).
2. Make your changes, adding unit tests for new functionality (`README.md`, "Contributing").
3. Run the pre-commit checks below.
4. Open a PR against `develop` and wait for the CI checks to pass.

There is no `CONTRIBUTING.md` or PR template in the repo — the checklist in `README.md` plus the CI checks below are the contract.

### Working from a fork

If you don't have write access to `CruGlobal/godtools-android`, contribute through a fork and open a pull request to merge your work back:

1. **Fork the repository** on GitHub — the **Fork** button on [`CruGlobal/godtools-android`](https://github.com/CruGlobal/godtools-android) creates `<your-username>/godtools-android` under your account.
2. **Clone your fork and wire up the upstream remote.** Install Git LFS *before* cloning and do not use a shallow clone — both matter for this repo (see [Getting Started](Getting-Started.md#cloning)):

   ```bash
   git lfs install
   git clone https://github.com/<your-username>/godtools-android.git
   cd godtools-android
   git remote add upstream https://github.com/CruGlobal/godtools-android.git
   ```

3. **Branch from the latest upstream `develop`:**

   ```bash
   git fetch upstream
   git checkout -b my-feature upstream/develop
   ```

4. Make your changes and run the [pre-commit checklist](#pre-commit-checklist).
5. **Push the branch to your fork** (`origin`):

   ```bash
   git push -u origin my-feature
   ```

6. **Open the pull request to merge it back.** GitHub shows a "Compare & pull request" prompt on your fork right after the push; otherwise use **New pull request** on the main repository and choose *compare across forks*. Set the **base repository** to `CruGlobal/godtools-android`, the **base branch** to `develop`, and the head to `<your-username>:my-feature`, then describe what the change does and why.
7. **Keep the PR current while it's under review.** If `develop` moves on, rebase rather than letting the branch drift:

   ```bash
   git fetch upstream
   git rebase upstream/develop
   git push --force-with-lease
   ```

Fork PRs are accepted against `develop` just like same-repo branches, and the CI checks below run on them. Two mechanics, however, only work for branches in the main repository: the **Record Snapshots** workflow (`workflow_dispatch` can only target branches in the base repo, and the workflow pushes a "Record updated snapshots" commit back to the triggering branch — impossible against a fork) and the `Publish PR QA Build` label (applying labels requires triage rights). If your fork PR changes UI snapshots or needs a QA build, ask a maintainer — they can record snapshots (typically by pushing your branch to the main repo and dispatching the workflow there) and apply the label; expect snapshots to be recorded by a maintainer before a UI-changing fork PR merges.

## Pre-commit checklist

Run these locally before committing / opening a PR:

```bash
# Code style — REQUIRED before every commit
./gradlew :build-logic:ktlintCheck ktlintCheck

# Unit tests (all enabled variants)
./gradlew test

# Android lint
./gradlew lint

# Paparazzi snapshot verification (requires Git LFS)
./gradlew verifyPaparazzi
```

Two gotchas worth knowing:

- **ktlint must be invoked twice-scoped.** `build-logic/` is an *included build* (`settings.gradle.kts`), so a plain `ktlintCheck` on the root build does not cover it. Both `README.md` and the CI ktlint job (`.github/workflows/build.yml`) use the exact command `./gradlew :build-logic:ktlintCheck ktlintCheck`.
- **Use the aggregate `test` task.** Unit tests are only enabled for the `debug` build type + `production` flavor (`build-logic/src/main/kotlin/AndroidTestConfiguration.kt`), so flavored modules expose `testProductionDebugUnitTest` while unflavored ones expose `testDebugUnitTest`. `./gradlew test` runs whichever applies without you having to know.

## Code style (ktlint)

Style is enforced by the [`org.jlleitschuh.gradle.ktlint`](https://github.com/JLLeitschuh/ktlint-gradle) Gradle plugin (v14.2.0), pinning **ktlint 1.8.0**, applied to every module by `configureKtlint()` in `build-logic/src/main/kotlin/KtlintConfiguration.kt`. An extra ruleset is added on top of the standard rules: the Compose ktlint rules `io.nlopez.compose.rules:ktlint:0.6.4` (the `ktlint-rulesets` bundle in `gradle/libs.versions.toml`).

The actual style configuration lives in `.editorconfig`:

| Setting | Value |
|---|---|
| Code style | `ktlint_code_style=android_studio` |
| Max line length | `120` |
| Indent | 4 spaces |
| `@Composable` naming | PascalCase allowed (`ktlint_function_naming_ignore_when_annotated_with=Composable`) |
| Import layout | `*,^` (lexicographic, aliases last) |
| Trailing commas | Allowed by the compiler (`ij_kotlin_allow_trailing_comma=true`) but *not enforced* — the `trailing-comma-on-call-site` and `trailing-comma-on-declaration-site` rules are disabled |
| Disabled rules | `standard:filename`, `standard:spacing-between-declarations-with-annotations`, `standard:trailing-comma-on-call-site`, `standard:trailing-comma-on-declaration-site` |

`./gradlew ktlintFormat` (provided by the same plugin) can auto-fix most violations, but always finish with the check command above since not everything is auto-correctable.

## detekt

Detekt runs **in CI only** — there is no detekt Gradle plugin or `detekt.yml` config anywhere in the repo. Note that the workflow is currently **manually disabled** in the repository's GitHub Actions settings, so in practice it does not run at all; the description below applies if it is re-enabled. The workflow `.github/workflows/detekt-analysis.yml`:

- runs on pushes to `develop`/`feature/*`/`master`, PRs to `develop`/`feature/*`, a weekly cron, and manual dispatch;
- downloads the standalone **detekt CLI v1.15.0** and runs it with default rules over the whole workspace;
- runs with `continue-on-error: true`, so findings **never fail your PR**;
- uploads SARIF results to GitHub Code Scanning (the repository's Security tab).

Treat code-scanning annotations from detekt as advisory. You cannot reproduce this run via Gradle — if you want to check locally you would need the same standalone CLI version.

## What CI checks on your PR

All jobs are defined in `.github/workflows/` — see [Build System & CI](Build-System-and-CI.md) for full details.

| Check | Workflow | What it does | Blocking? |
|---|---|---|---|
| Build | `build.yml` (`build` job) | `./gradlew bundle` | Yes |
| ktlint | `build.yml` (`ktlint` job) | `./gradlew :build-logic:ktlintCheck ktlintCheck` | Yes |
| Android lint | `build.yml` (`lint` job) | `./gradlew lint` (config: `analysis/lint/lint.xml`) | Yes |
| Unit tests + snapshots | `build.yml` (`tests` job, 4-shard matrix) | `test verifyPaparazzi` + Kover coverage upload to Codecov | Yes |
| Gradle wrapper validation | `gradle-wrapper-validation.yml` | Checksum-verifies `gradle-wrapper.jar` | Yes |
| Git LFS validation | `git-lfs-validation.yml` | `git lfs fsck --pointers` — catches snapshots committed as real binaries instead of LFS pointers | Yes |
| detekt | `detekt-analysis.yml` | Static analysis → GitHub Code Scanning | No (`continue-on-error`; workflow currently disabled in Actions settings) |

Additional PR mechanics:

- **QA builds from a PR:** adding the label `Publish PR QA Build` to a PR makes the `qa_build` job upload `stageQa` and `productionQa` builds to Firebase App Distribution (tester group `android-testers`). This also happens automatically on every push to `develop` (`.github/workflows/build.yml`). Applying the label requires triage rights — fork contributors should ask a maintainer.
- **PR version suffix:** CI builds PRs with `versionSuffix=PR{n}`, so PR artifacts are identifiable (`.github/workflows/build.yml`).
- **Paparazzi snapshots are recorded in CI, never locally.** If your UI change alters snapshots, trigger the manual **Record Snapshots** workflow (`.github/workflows/record-snapshots.yml`, `workflow_dispatch`) on your feature branch — it runs `./gradlew cleanRecordPaparazzi` on the CI runner and pushes a "Record updated snapshots" commit to your branch. The workflow can only be dispatched on branches in the main repository, so for fork PRs a maintainer must record snapshots (see the fork note above). Local recording produces machine-dependent renders that fail `verifyPaparazzi` in CI. See [Testing](Testing.md).
- **Coverage** is collected via Kover and uploaded to Codecov with `fail_ci_if_error: true` (`.github/workflows/build.yml`).

## Translations (Crowdin)

User-visible strings are translated through [Crowdin](https://crowdin.com/) (project id `805338`, configured in the root `crowdin.yml`). The flow is fully automated in both directions:

```mermaid
flowchart LR
    A["Your PR adds strings to values/strings*.xml"] --> B["Merge to develop"]
    B --> C["crowdin-upload.yml: push source strings to Crowdin"]
    C --> D["Translators work in Crowdin"]
    D --> E["crowdin-download.yml: weekly cron Sun 00:00 UTC"]
    E --> F["PR 'Update Translations' on branch chore/crowdinTranslations"]
    F --> B
```

- **Upload:** `.github/workflows/crowdin-upload.yml` runs on every push to `develop` (and manual dispatch) with `upload_sources: true`, pushing English source strings to Crowdin.
- **Download:** `.github/workflows/crowdin-download.yml` runs weekly (`cron: '0 0 * * 0'`, Sundays 00:00 UTC) and on manual dispatch. It downloads translations (`skip_untranslated_strings: true`) onto the branch `chore/crowdinTranslations` and opens a PR titled **"Update Translations"** with commit message "Download the latest translations from Crowdin".

Source files covered by Crowdin (`crowdin.yml`) are `strings*.xml` under `src/main/res/values/` in these modules:

| Module | Notes |
|---|---|
| `app` | Ignores `app/src/main/res/values/strings_country_native_names.xml` |
| `library/base` | |
| `ui/base` | |
| `ui/base-tool` | |
| `ui/lesson-renderer` | |
| `ui/tips-renderer` | |
| `ui/tract-renderer` | |
| `ui/tutorial-renderer` | |

Translations land in `values-%android_code%/` directories (e.g. `app/src/main/res/values-de/strings_dashboard.xml`) with `update_option: update_as_unapproved`.

**Rules for contributors:**

1. **Only edit the English source files** (`values/strings*.xml`). New strings go there; keys follow the `<screen>_<section>_<purpose>` convention (see `.claude/rules/design_system_rules.md` §13).
2. **Never hand-edit translated `values-*/strings_*.xml` files.** They are machine-managed by the Crowdin download workflow; manual edits will be overwritten by the next "Update Translations" PR and never reach Crowdin's translation memory.
3. **Don't worry about missing translations breaking the build.** `analysis/lint/lint.xml` downgrades the `MissingTranslation` lint issue to a warning specifically so new strings can ship before they are translated.
4. Manual Crowdin CLI use is possible but rarely needed — it requires the `CROWDIN_API_TOKEN` environment variable (`crowdin.yml`, `README.md`).

## AI-assisted work

The repo carries checked-in guidance for AI coding assistants — read (and keep in sync) when relevant:

- **`CLAUDE.md`** — repository-level instructions: default branch, build/test commands, module layout, key frameworks and patterns (Hilt, Circuit, Room, WorkManager), and code style. If you change a workflow or convention documented there, update it in the same PR.
- **`.claude/rules/design_system_rules.md`** — the Jetpack Compose design-system rules: Material3 tokens (`MaterialTheme.colorScheme`/`typography`/`shapes`), `GodToolsTheme.extendedColorScheme`, spacing grid, icon conventions, accessibility requirements, and string-resource conventions. These rules apply to *all* new Compose UI, human- or AI-written; see [UI Architecture](UI-Architecture.md) for the underlying theme code in `ui/base`.
- **`.claude/skills/`** — project-local Claude Code skills: `pr-review` (reviews a PR against project conventions) and `record-screenshots` (drives the Record Snapshots workflow and folds the resulting screenshot commit into your branch).

## Keeping this wiki current

This wiki lives in the `wiki/` directory of the repository and is versioned alongside the code (see [Home](Home.md)). The repository's `README.md` links here via its "Developer Wiki" section — that link is the canonical entry point for new contributors, so keep it intact if you restructure the README or rename/move wiki pages. The same expectation that applies to `CLAUDE.md` above applies here: **if your PR changes behavior, configuration, or a workflow documented in a wiki page, update the affected page in the same PR.** The pages cite concrete specifics — library versions, database schema versions, default values, file and line references — that silently rot when the code moves on without them.

When editing wiki pages, prefer stable anchors (class, function, and constant names) over raw line numbers, which break on unrelated edits to the cited file.

## Quick reference

| Task | Command / action |
|---|---|
| Base branch for PRs | `develop` |
| Contribute without write access | Fork → branch from `upstream/develop` → PR to `CruGlobal:develop` (see [Working from a fork](#working-from-a-fork)) |
| Pre-commit style check | `./gradlew :build-logic:ktlintCheck ktlintCheck` |
| Auto-fix style | `./gradlew ktlintFormat` (then re-run the check) |
| Run all unit tests | `./gradlew test` |
| Verify snapshots | `./gradlew verifyPaparazzi` (Git LFS required) |
| Update snapshots | Trigger the **Record Snapshots** GitHub Actions workflow on your branch (fork PRs: ask a maintainer) |
| Android lint | `./gradlew lint` |
| Get a QA build of your PR | Add the `Publish PR QA Build` label (fork PRs: ask a maintainer) |
| Add a user-visible string | English `values/strings*.xml` only — Crowdin handles the rest |
| Change behavior a wiki page documents | Update the affected `wiki/` page in the same PR |
