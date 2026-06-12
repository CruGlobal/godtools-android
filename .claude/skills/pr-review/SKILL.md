---
name: pr-review
description: Review a pull request against GodTools Android project conventions. Use when asked to review a PR, check code quality, or audit changes.
argument-hint: [pr-number]
allowed-tools: Bash, Read, Grep, Glob, Write, Edit
---

Review pull request $ARGUMENTS against the GodTools Android project conventions.

## Steps

1. Check for dismissed issues by reading `.claude/skills/pr-review/dismissed-issues.md` if it exists.
   Load all dismissed entries — each has a **Pattern** and **Reason**. You will use these to suppress matching findings later.

2. Fetch the PR diff and metadata. If `$ARGUMENTS` is provided, use it as the PR number:
```
gh pr diff $ARGUMENTS
gh pr view $ARGUMENTS
```
If no PR number is given (or the above fails because no upstream PR exists), fall back to reviewing the current branch against `develop`:
```
git diff develop...HEAD
git log develop...HEAD --oneline
```
Use the branch name and commit log as the "title" in the review header.

3. Identify all changed files and categorize them (Kotlin, build scripts, resources, manifests, tests).

4. Pre-flight check — run ktlint. This is a hard blocker:
```
./gradlew :build-logic:ktlintCheck ktlintCheck
```
If it fails, report as **Must Fix** before reviewing anything else.

5. Review each category using the checklist below.

6. Before outputting, cross-reference every finding against dismissed patterns. A finding matches a dismissed pattern when it describes the same class of issue (not necessarily the exact file/line — match by concept). Move matched findings to a separate suppressed list.

7. Output a structured review (format below).

8. Post inline comments to the PR for every ⚠️ and ❌ finding that references a specific file and line number. Before posting, deduplicate against all existing comments (resolved or not) to avoid re-posting anything already raised:

```bash
# Get the head SHA, repo, and all existing review comments (resolved and unresolved)
HEAD_SHA=$(gh pr view $ARGUMENTS --json headRefOid -q .headRefOid)
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
EXISTING=$(gh api repos/$REPO/pulls/$ARGUMENTS/comments --jq '[.[] | select(.in_reply_to_id == null) | {path:.path, line:.line, body:.body}]')
```

For each finding, check whether any existing comment (resolved or not) already covers the same file + line (or contains substantially the same text). Skip any finding that is already covered. Then bundle the remaining new comments into a single review submission:

```bash
gh api repos/$REPO/pulls/$ARGUMENTS/reviews \
  --method POST \
  --field commit_id="$HEAD_SHA" \
  --field event="COMMENT" \
  --field "comments[][path]=<file path>" \
  --field "comments[][line]=<line number>" \
  --field "comments[][side]=RIGHT" \
  --field "comments[][body]=<finding text>

🤖 Posted by [Claude Code](https://claude.ai/code)" \
  # repeat --field "comments[]..." for each new finding
```

Use the exact file path from the diff and the line number in the current version of the file (RIGHT side). Each comment body should contain the full finding description. Always append the attribution footer `\n\n🤖 Posted by [Claude Code](https://claude.ai/code)` to each comment. If no new actionable findings exist (only ✅ items or all already commented), skip this step.

9. If the review has **no ❌ or ⚠️ findings** (only ✅ and/or ⏭️ items), ask the user whether to post the full review. If they say yes:
   - Check whether the PR author matches the current git user (`gh pr view $ARGUMENTS --json author -q .author.login` vs `gh api user -q .login`)
   - If it is a **self-review**, post with `--comment` (GitHub does not allow self-approval)
   - If it is **someone else's PR**, ask whether to approve or just comment, then post with `--approve` or `--comment` accordingly
   - Always append `\n\n🤖 Posted by [Claude Code](https://claude.ai/code)` to the body

10. After the review output, print:

```
---
To dismiss a finding so it won't appear in future reviews, say:
  dismiss: <short title> — <reason>
```

---

## Review Checklist

### Build Scripts (`build.gradle.kts`)

- [ ] `godtools.library-conventions` / `godtools.application-conventions` applied — do not re-declare `minSdk`, `compileSdk`, `targetSdk`, Kotlin toolchain, or proguard rules
- [ ] Compose enabled via `configureCompose(project)` in `android {}` — do not manually add Compose BOM or Circuit deps
- [ ] No redundant `buildFeatures` flags already covered by the convention plugin
- [ ] New modules use `godtools.library-conventions`, `godtools.application-conventions`, or `godtools.dynamic-feature-conventions` as appropriate
- [ ] KSP used for new modules; `kapt` only if DataBinding is still present (see TODO comment pattern)

### `gradle/libs.versions.toml`

- [ ] No duplicate version keys
- [ ] No unused `[plugins]` entries (common AS wizard artifact)
- [ ] No unused `[libraries]` entries
- [ ] All new dependencies declared here, not hardcoded in `build.gradle.kts`

### Android Resources & Manifests

- [ ] No AS wizard scaffolding in library modules: `colors.xml`, `strings.xml` with `app_name`, default launcher icons, per-module theme definitions
- [ ] Library `AndroidManifest.xml` does not declare `application` attributes (theme, label, icon) — those belong in `:app`
- [ ] Hardcoded strings extracted to `strings.xml` (inline string literals in composables are flagged)
- [ ] No `app_name` or generic color/theme resources in library modules

### `settings.gradle.kts`

- [ ] No local `includeBuild` substitutions for `kotlin-mpp-godtools-tool-parser` committed — publish artifact and update version in `libs.versions.toml` instead

### Jetpack Compose

- [ ] Every public or internal composable has `modifier: Modifier = Modifier` parameter
- [ ] Always uses `GodToolsTheme` from `:ui:base` — no per-module `MaterialTheme(colorScheme = …)` wrappers
- [ ] Colors accessed via `MaterialTheme.colorScheme.*` or `GodToolsTheme.extendedColorScheme`
- [ ] CPU-heavy work (bitmap generation, etc.) done off the main thread via `produceState { withContext(Dispatchers.IO) { … } }`
- [ ] `LazyColumn`/`LazyRow` `key` lambdas handle nullable IDs safely — do not use `.orEmpty()` on a nullable key field; two `null` IDs both become `""` and crash with `IllegalArgumentException: Key "" was already used`. Either omit `key` (falls back to position) or use the object as fallback: `key = { it.id ?: it }`
- [ ] Vector drawables intended for use inside `Icon {}` composables use `@color/tintable` in the XML rather than hardcoded colors — project convention for consistency across vector assets

### Circuit Presenter/UI Patterns

**Presenter**
- [ ] Uses `@AssistedInject` constructor; `Navigator`/`Screen` injected via `@Assisted`
- [ ] Contains nested `Factory` interface annotated with `@AssistedFactory` and `@CircuitInject(<Screen>::class, SingletonComponent::class)`
- [ ] `UiState` is a `data class` implementing `CircuitUiState`
- [ ] `UiEvent` is a `sealed interface` implementing `CircuitUiEvent`, marked `internal`
- [ ] `UiState` and `UiEvent` defined as nested types inside the Presenter
- [ ] `UiState` exposes `val eventSink: (UiEvent) -> Unit`
- [ ] `eventSink` lambda passed directly when constructing `UiState` — not extracted to a local variable first:
  ```kotlin
  // Correct
  return UiState(items = items) { event -> when (event) { … } }
  // Wrong
  val eventSink: (UiEvent) -> Unit = { … }
  return UiState(items = items, eventSink = eventSink)
  ```
- [ ] `navigator.pop()` (or any navigation call) placed *inside* the `launch { }` block when it follows an async operation — `rememberCoroutineScope()` is canceled on composition disposal and can cancel an in-flight write before it completes
- [ ] Coroutine launches in `present()` use a Compose-aware mechanism (`LaunchedEffect`, inside an `eventSink` callback, etc.) — bare `scope.launch { }` at the top level of `present()` re-runs on every recomposition
- [ ] Presenter contains no UI logic — pure state/event handling

**UI Composable**
- [ ] Annotated with `@CircuitInject(<Screen>::class, SingletonComponent::class)`
- [ ] Signature: `(state: <Presenter>.UiState, modifier: Modifier = Modifier)`
- [ ] All user interactions delegated via `state.eventSink(UiEvent.*)` — no direct function calls from UI

**Screen**
- [ ] Annotated with `@Parcelize`
- [ ] Is an `object` or `data class` implementing `Screen`

### Cross-Module Activity / Intent Creation

- [ ] `Intent`/`PendingIntent` creation for Activities uses extension functions in `ui/base/src/main/kotlin/org/cru/godtools/base/ui/Activities.kt` (string class names, no hard compile-time deps between sibling UI modules)

### Repository & DAO Patterns

- [ ] DAOs use `suspend fun` for single-shot queries, `Flow<T>` for reactive queries
- [ ] `@Upsert` for sync operations
- [ ] `@RewriteQueriesToDropUnusedColumns` when selecting partial projections

### WorkManager

- [ ] Workers annotated with `@HiltWorker` + `@AssistedInject`
- [ ] Extend `CoroutineWorker` for suspend support
- [ ] Catch `IOException` (specific), not `Exception` (broad)

### Kotlin Code Quality

- [ ] Logging uses `Timber` — no `println` or `Log.*`
- [ ] Exception handling catches specific types, not bare `Exception`
- [ ] Visibility is intentional: `internal` for module-scoped symbols, `private` where possible
- [ ] Multi-branch conditionals use `when`, not chained `if/else`
- [ ] `Bundle`/`Intent` extra keys are `const val` shared between producer and consumer
- [ ] Non-cancellable critical sections use `launch(start = CoroutineStart.UNDISPATCHED) { withContext(NonCancellable) { … } }` — not `launch(NonCancellable) { … }` (passing `NonCancellable` to `launch` replaces the parent `Job`, breaking structured concurrency)

### Testing

- [ ] Presenter tests use `presenterTestOf { }` (Circuit test API)
- [ ] Compose UI tests use `runComposeUiTest { }` — not `createComposeRule()` / `ComposeTestRule`
- [ ] `runComposeUiTest` import is `androidx.compose.ui.test.v2.runComposeUiTest` — the `androidx.compose.ui.test.runComposeUiTest` (v1) is deprecated and is a **Must Fix**
- [ ] Flow-based tests use Turbine (`flow.test { … }`) rather than manual `collect` + coroutine coordination
- [ ] Interfaces with `@Composable` functions use a hand-written `Fake*` class in tests, not `mockk<>()` — mockposable delays Kotlin version uptake and is incompatible with newer compiler versions
- [ ] Paparazzi tests extend `BasePaparazziTest` from `:ui:base` testFixtures with `@TestParameter` night/accessibility matrix
- [ ] Snapshots not recorded locally — triggered via GitHub Actions workflow on the feature branch
- [ ] Paparazzi tests include enough sample data to keep the feature under test visible in the snapshot — verify the target content isn't pushed off-screen by surrounding layout

### PR Hygiene

- [ ] No unrelated auto-formatter whitespace changes (check with `git diff develop...HEAD --stat`)
- [ ] KMP tool UI changes (app bar, lesson/tract rendering) belong in `kotlin-mpp-godtools-tool-parser`, not Android renderer modules

For detailed examples of each pattern, see `references/patterns.md`.

When reviewing any Compose UI code, also load `.claude/rules/design_system_rules.md` — it defines the authoritative conventions for color tokens, typography, spacing, icons, components, and accessibility.

---

## Output Format

```
## PR Review: <title> (#<number>)

### Summary
<1–2 sentence summary of what the PR does>

### Checklist Findings

#### ✅ Looks Good
- <item>

#### ⚠️ Minor Issues
- <file:line> — <issue> — <suggested fix>

#### ❌ Must Fix
- <file:line> — <issue> — <suggested fix>

#### ⏭️ Suppressed
- <short title> — dismissed: <reason>
(omit this section entirely if nothing was suppressed)

### Overall Verdict
APPROVE / REQUEST CHANGES / COMMENT
<brief rationale>
```

Be specific. Reference file paths and line numbers. Cite the relevant convention when flagging an issue.

---

## Handling Dismissals

When the user says `dismiss: <title> — <reason>` (in any form — "dismiss the X issue because Y", etc.):

1. Read `.claude/skills/pr-review/dismissed-issues.md` if it exists (create it if not).
2. Run `git config user.name` to get the current user's name.
3. Append a new entry in this format:

```markdown
## <title>
**Pattern**: <describe the class of issue broadly enough to match future occurrences>
**Reason**: <reason the user gave>
**Dismissed**: <today's date as YYYY-MM-DD>
**Dismissed by**: <git user.name>
```

4. If the current session reviewed a PR, find any open (unresolved) comment thread on that PR matching the dismissed issue. Use the GraphQL API to locate threads and resolve the matching one, replying with the dismissal reason first:

```bash
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
OWNER=${REPO%%/*}
REPONAME=${REPO##*/}

# Find unresolved review threads
gh api graphql -f query="
{
  repository(owner: \"$OWNER\", name: \"$REPONAME\") {
    pullRequest(number: $PR_NUMBER) {
      reviewThreads(first: 100) {
        nodes {
          id
          isResolved
          comments(first: 1) {
            nodes { id body path line }
          }
        }
      }
    }
  }
}"
```

Match the thread by file path, line number, or substantial text overlap with the dismissed finding. Then reply to the thread and resolve it:

```bash
# Reply to the thread's first comment explaining the dismissal
gh api repos/$REPO/pulls/$PR_NUMBER/comments \
  --method POST \
  --field in_reply_to=<comment_id> \
  --field body="Dismissed: <reason given by user>

🤖 [Claude Code](https://claude.ai/code)"

# Resolve the thread via GraphQL
gh api graphql -f query="
mutation {
  resolveReviewThread(input: { threadId: \"<thread_node_id>\" }) {
    thread { id isResolved }
  }
}"
```

5. Confirm to the user what was added and that it will be suppressed in future reviews.
