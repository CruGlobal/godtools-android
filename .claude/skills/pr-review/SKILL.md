---
name: pr-review
description: This skill should be used when the user asks to review a pull request, check code quality, run a PR review, or asks "is this PR ready to merge?" for the godtools-android project. It performs a structured review checking project-specific patterns learned from historical PR feedback.
version: 1.0.0
---

# GodTools Android PR Review

Perform a structured pull request review for the godtools-android project. This skill applies patterns learned from historical code reviews and the project's established conventions.

## Review Workflow

### Step 1: Get the Diff

```bash
# If a PR exists
gh pr diff

# Otherwise compare to base branch
git diff develop...HEAD --name-only
git diff develop...HEAD
```

Identify all changed files and categorize them by type (Kotlin, build scripts, resources, manifests, tests).

### Step 2: Pre-Flight Check — ktlint

Always verify ktlint passes. This is a hard blocker:

```bash
./gradlew :build-logic:ktlintCheck ktlintCheck
```

If it fails, report as **Critical** — PR cannot merge until resolved.

### Step 3: Run the Checklist

For each changed file, apply the relevant checks from `references/patterns.md`. Use the priority levels below to triage findings:

- **Critical** — Hard blocker, must fix before merge
- **Important** — Should fix in this PR
- **Suggestion** — Nice to have, can be deferred

### Step 4: PR Hygiene Check

```bash
git diff develop...HEAD --stat
```

Look for signs of unrelated auto-formatter changes:
- Large line-count diffs on files tangentially touched by the PR
- Whitespace-only changes in unrelated sections
- Reformatted import blocks or trailing commas not part of the feature

If found, flag as **Important**: instruct to use git patch-mode staging or scope-selective reformatting in Android Studio.

### Step 5: Report Findings

Structure the output as:

```
## PR Review: <branch or PR title>

### Critical Issues (must fix before merge)
- [FILE:LINE] Issue description

### Important Issues (should fix)
- [FILE:LINE] Issue description

### Suggestions
- [FILE:LINE] Suggestion

### Looks Good
- What's well done in this PR
```

## Quick Reference: Top Issues from Historical Reviews

These are the patterns most frequently flagged in past reviews. Check these first:

1. **ktlint failures** — Run check before reporting anything else.
2. **AS wizard scaffolding left in** — Delete generated `colors.xml`, `strings.xml` with `app_name`, default launcher icons, and per-module theme definitions from library modules.
3. **Unrelated auto-formatter changes** — Flag if diff contains formatting-only changes in untouched sections.
4. **Convention plugin boilerplate in `build.gradle.kts`** — `godtools.library-conventions` already provides minSdk, namespace (via `android {}` block), Kotlin toolchain, proguard, and Compose setup. Don't re-declare them.
5. **`libs.versions.toml` clutter** — No duplicate version entries, no unused plugin declarations.
6. **Composables missing `modifier: Modifier = Modifier`** — Required on all public/internal composables.
7. **Wrong theme** — Use `GodToolsTheme` from `:ui:base`, never define a per-module theme.
8. **Cross-module Activity intents** — Intent/PendingIntent creation for Activities must use extension functions in `ui/base/src/main/kotlin/org/cru/godtools/base/ui/Activities.kt`.
9. **Circuit actions as direct calls** — User-triggered actions must flow through named `UiEvent` entries in `UiState.eventSink`, not direct function calls from the UI composable.
10. **Local `includeBuild` in settings.gradle.kts** — Never commit local KMP project substitutions. Publish the KMP artifact and update the version in `libs.versions.toml` instead.
11. **`println` for logging** — Use `Timber` instead.
12. **Overly broad exception catching** — Catch specific exception types, not `Exception`.
13. **Visibility too wide** — Prefer `internal` for module-scoped composables/functions, `private` where possible.

For full pattern details, consult `references/patterns.md`.
