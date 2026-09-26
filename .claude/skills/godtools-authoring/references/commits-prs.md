# Commits and pull requests

How the code owners slice work into commits and PRs, how they word commit messages and PR descriptions, and what hygiene they expect before review. Read this before your first commit on a branch, again before opening the PR, and whenever you are about to add a fix-up commit.

Owner PRs are either merge-committed (every curated commit lands on `develop`) or squash-merged (the PR title becomes the subject and commit messages become bullets). Both paths put your wording on `develop` for good, so every commit and the PR title have to read well on their own.

## Contents

- [Commit granularity and ordering](#commit-granularity-and-ordering)
- [Folding fix-ups and branch hygiene](#folding-fix-ups-and-branch-hygiene)
- [Diff hygiene](#diff-hygiene)
- [Commit message format](#commit-message-format)
- [PR scope and feature series](#pr-scope-and-feature-series)
- [Cross-repo dependencies](#cross-repo-dependencies)
- [PR titles, ticket keys, branches](#pr-titles-ticket-keys-branches)
- [PR description](#pr-description)
- [Review process](#review-process)

## Commit granularity and ordering

**Make each commit one nameable change, and make every commit compile.** Introduce a class, wire it in, delete one orphan, add one test class. Owner PRs are reviewed commit by commit and bisected later, so a commit that mixes concerns or breaks the build gets sent back. Daniel's median commit touches 2 files. For multi-commit PRs, build each commit and say so in the PR body ("Both commits verified to compile individually", PR #4562). A good split, from #4416:

```
596412b Add LessonsFlowProducer
718ccae Wire LessonsPresenter to LessonsFlowProducer
54fc413 Update LessonsLayout header title based on mode
```

**Order migration commits as prep, add new, switch callers, delete orphans, then fixes and tests.** Small prep refactors come first, then the new implementation, then the commit that switches callers over, then a separate deletion commit whose subject says "orphaned by" or "now that", then follow-up fixes and tests. Each step is small and the deletion commit is trivial to verify. See #4562 (`7e83d9c` Switch tract page rendering to the shared RenderTractPage composable, `2a93b37` Remove tract code and resources orphaned by the RenderTractPage migration, `7bd9733` fix) and #4406 (`07afa08` Remove legacy Page enum now that Dashboard is fully migrated to Circuit, then `4f91040` Add DashboardPresenterTest).

**Commit pure moves and renames alone, with no logic change.** Git then records a rename and review shows 0/0 lines. Put the behavior change in the next commit. See `def8d01` Move LessonsPresenterTest to src/test, and #4403 (one package move per commit).

**Ship tests in the same PR as the code.** Either in the same commit ("Add getFeaturedToolsFlow() to ToolsRepository with tests", `e38a1b3`) or in an "Add XTest" commit right after it (#4373, #4406). Paparazzi test classes may get their own "Add Paparazzi snapshot tests for ..." commit. A PR with no tests invites a request for them. If you defer tests, say why in the PR body (#4461 did this).

**Remove everything a migration orphans, in the same PR.** After a switch-over, delete every now-unused layout, drawable, style, dimen, binding adapter, test, `implementation`/`testImplementation` dependency, `buildFeatures` flag (drop `viewBinding`, keep `dataBinding` if any binding remains) and `gradle/libs.versions.toml` entry. If a constant survives, move it next to its only consumer as `private const val`. The pr-review bot and owners call out leftover dead resources (#4511). List what you removed in the PR body. See `2a93b37`, which also drops cardview, constraintlayout and picasso.transformations.

**Remove dead code outside-in, one orphan per commit, and say who last used it.** First make the entry point stop using the framework. Then delete each unreferenced class in its own commit, outermost (fragment, adapter) to leaves (items, interfaces). Delete a class's exclusive layout and its tests in the same commit as the class (never `@Ignore` them). Layouts and styles next, strings last. See #4456: `35f61df` Remove ShareBottomSheetDialogFragment, `ad606a0` Remove LiveShareItem, `f1c0507` Remove ShareAppsAdapter, ..., `315d64e` Remove tool_share_sheet_more_apps string resource.

```
Remove LiveShareItem

Only used by OtherActionsAdapter in the now-deleted ShareBottomSheetDialogFragment.
```

**Delete an unused string from every locale in one commit.** Remove it from `res/values/` and every `res/values-*/` copy together (`315d64e` touched 24 locale files). Leftover translations create lint noise and orphan Crowdin entries.

**Keep strings that are slated for godtools-shared, and say so.** Strings being ported to godtools-shared via Crowdin stay even when Android no longer references them (for example `tract_card_previous`/`tract_card_next` in `ui/tract-renderer/src/main/res/values/strings_tract_renderer.xml`). State it in the PR body so the reviewer does not flag it: "The tract_card_previous/tract_card_next translations are intentionally kept for the Crowdin port to godtools-shared" (#4562).

**Separate a library bump from the code it forces.** Keep the version bump (often the renovate commit itself) as one commit and the code changes as a separate "Migrate ... to ..." commit. See #4459 (`6e8d54e` Migrate build scripts to AGP 9-compatible APIs after the renovate merges) and #4578.

**Replace one deprecated API per commit, and migrate every usage.** Subject "Replace deprecated <old> with <new>" (`97b0d25`, `14b14ce`). Grep for all remaining usages and migrate them in the same PR, and drop helpers that only existed for the old API. A half migration leaves two patterns in the code. If the new API is not identical in behavior or pixels, add a behavior-change note to the PR body (#4583). Do not bundle deprecation fixes with feature work.

**Do sweeps one category or one instance per commit.** For code-quality sweeps, one commit per category (`c5294fb` Remove redundant else branches from exhaustive when expressions). For repeated mechanical transforms, one instance per commit with a patterned subject (#4409, twelve "Collapse DB auto-migrations X→Y" commits). A lint suppression gets its own commit with the reason in the body (`ea3eb68`).

**Swap a local helper for the library version in one commit once it is published.** "Replace local X with <library> implementation", deleting the local copy in the same commit (`cb4a1b1` Replace local SyncTaskRegistry with gto-support-sync library implementation).

**Keep a delegate when renaming an API teammates may be calling.** Add the new name and leave the old one as a one-line delegate, then remove it in a later PR. This avoids breaking someone's open branch.

```kotlin
fun getCountrySettingFlow() = getPersonalizationCountryFlow()
```

See `library/base/src/main/kotlin/org/cru/godtools/base/Settings.kt:172` and the owner's note on PR #4392.

Tendency: **Put tooling and docs changes in their own commits.** Changes to CLAUDE.md, `.claude/skills`, `.claude/rules`, README or CI workflows go in separate commits, often a separate PR (#4457). pr-review dismissals get their own "Dismiss ... finding" commit (`62f1cff`). Sometimes these ride inside a feature PR as their own commit (`ed978cf` in #4411), so a separate PR is not required, but never mix them into a code commit.

## Folding fix-ups and branch hygiene

**Fold fix-up, review and snapshot commits into the commit they fix.** "Lint fix", "address review", "revert formatting" and CI "Record updated snapshots" commits would land on `develop` permanently. The owner asks contributors to squash them (#4198: "go ahead and squash the 2 commits together"). The only PR that left them in is #4263 (`fa9e340` Lint fix). Fix in place and force-push:

```bash
git commit --fixup 596412b
GIT_SEQUENCE_EDITOR=: git rebase -i --autosquash develop
git push --force-with-lease
```

Anti-example of a branch ready for review:

```
abc123 Add LessonsFlowProducer
def456 address review comments
789abc Lint fix
```

A pr-review dismissal (a `dismissed-issues.md` change) may stay as its own commit.

**Let CI record Paparazzi snapshots, then fold them in.** Never record locally, since local renders differ from CI. Push, run the record workflow with the record-screenshots skill, and fold its commit into the commit that changed the UI. Large snapshot reshuffles (merging test classes, deleting stale PNGs) go in their own PR (#4415, a series like `47f7136` Fold LessonsLayout Paparazzi tests into DashboardLayoutPaparazziTest).

**Rebase on develop; do not merge develop into your branch.** All four "Merge branch 'develop'" commits in the last year are on the long-lived `feature/compose` integration branch. Merging a prerequisite renovate branch into an upgrade branch is the one accepted exception (#4459).

## Diff hygiene

**Do not reformat code you did not change.** Reformat only your own lines and stage with `git add -p` so formatter churn stays out. The owner requested changes on #4263 for this and reverted it himself ("try selecting just the part of the file you changed and using the reformat command"). In particular, do not split existing one-line getters, reorder imports you did not touch, or add blank lines between annotated properties. XML is not linted, so check its indentation by hand. Run `./gradlew :build-logic:ktlintCheck ktlintCheck` before committing.

```kotlin
// Leave this as it is
override val settingsActionsFlow get() = combine(a, b) { ... }
```

```kotlin
// Not this (formatter churn, reverted in d3f8154)
override val settingsActionsFlow
    get() = combine(a, b) { ... }
```

**Delete code instead of commenting it out, and remove scratch leftovers.** No commented-out logic, placeholder `//` lines, doubled blank lines or extra trailing blank lines. The owner flags these directly ("we want to avoid committing commented out logic like this", #4167; "extraneous comment & whitespace here", #4263). A temporarily disabled test must say why and when it comes back (`1a99a54` "until Compose 1.10.0 is released with a fix").

## Commit message format

**Write the subject as a capitalized imperative, no period, no prefix.** Start with a verb like Add, Remove, Move, Migrate, Replace, Switch, Extract, Collapse, Update. No `feat:`/`fix:`/`chore:` and no sed shorthand like `s/State/UiState`. Every owner commit since May 2026 follows this (Daniel 112/115, Tim 48/48, zero trailing periods), so anything else stands out. Lowercase subjects in older history (`367e027`, `b3fc61a`) are from before that and are not a model.

**Name the concrete symbol and where it lives.** The subject is the index of the change for a commit-by-commit reviewer. Aim for 50 to 72 characters and go longer only to fit the symbol names. Never "Lint fix" or "small change".

```
Extract shared toToolCardState() helper in ToolsPresenter
Move ProgressBarBindingAdapter from download-manager to base-tool
Add CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED feature flag
```

**Add a body only when the subject and diff do not explain why.** Use it for the bug mechanism, the upstream change that caused it, why something is now dead, or why one approach beat a close alternative. Skip it for mechanical moves. Wrap near 72 characters. Reviewers (especially Tim) ask "why did you make a function out of this?" when the reason is missing (#4392), so answer it up front. Real examples:

```
Return POSITION_NONE from getItemPositionFromId for unknown page ids

indexOfFirst returns -1 for a missing id, which PagerAdapter interprets
as POSITION_UNCHANGED, keeping a holder for a removed or renamed page
alive and rebinding it at its stale index.
```

```
Update gtoSupport to v4.6.0-SNAPSHOT

Pulls in updated jsonapi R8 rules to fix API response
deserialization under release minification.
```

```
Add LessonsFlowProducer

Loads lessons based on the current mode: PERSONALIZATION uses
getPersonalizedLessonsFlow with the country from Settings, falling back
to language-only when no country-specific results exist; ALL_LESSONS
uses getLessonsFlowByLanguage. Both modes filter hidden lessons.
```

**End Claude-assisted commits with the Co-Authored-By trailer, and keep the "Generated with Claude Code" footer for the PR body only.** Use the trailer text from the session's attribution instructions. No commit on `develop` contains "Generated with"; every recent staff PR body ends with it.

Tendency: **Give each commit a full message even when the PR will be squashed.** GitHub folds commit messages into the squash body as bullets, and Tim's squash commits read as a changelog because of it (`a5b4a07`, #4573). Some squashes have no body (`2c3016d`), so this is not a blocker.

## PR scope and feature series

**Keep each PR to one ticket or concern.** Typically 1 to 8 curated commits and a diff you can read in one sitting. Only mechanical series (one removal or one collapse per commit, like #4212 or #4409) justify 15 or more commits. Narrow PRs get approved with no comments (#4175, #4176, #4573). Put follow-up work in a new PR instead of adding to an open one.

**Ship a large feature as a bottom-up series of PRs that each merge alone.** Room model plus repository plus tests, then API/sync, then a presenter prep refactor, then UI behind a remote-config feature flag, then the next screen, then cleanup. The personalization feature landed as #4373 (Room model), #4387 (sync), #4389 (refactor prep), #4392 (UI, first commit `84e700d` adds the feature flag), #4411, #4415, #4416. Use a long-lived `feature/<name>` branch only when the change cannot ship piecemeal (#4209 `feature/compose`).

**Land a refactor-only PR before the feature or risky upgrade that needs it.** No behavior change, then build on it. This keeps the risky diff small and separately revertable (#4389 before #4392, #4410 before #4411, #4458 before the AGP 9 upgrade in #4459). If a review finding will be fixed by the follow-up PR, reply saying so instead of fixing it twice (#4458).

**Turn out-of-scope findings into follow-up Jira tickets, not scope creep.** When review turns up adjacent work, file a ticket, mention it, and have the follow-up PR cite the review it came from. The owner approves with "there are potentially 2 followup jira tickets that should be created" (#4583), and the follow-ups #4592 (GT-3107) and #4593 (GT-3108) say "Follow-up from the review of #4583". A written deferral in the PR body is also accepted.

Tendency: **Stack a dependent PR on its prerequisite branch.** Base it on the unmerged branch so the diff shows only its own changes, add the `onhold` label, and explain the stacking in the description (#4593). Only one instance so far.

## Cross-repo dependencies

**For an unreleased godtools-shared or gto-support build, bump to the -SNAPSHOT, declare the dependency, and never commit `includeBuild`.** Bump `gradle/libs.versions.toml` (`godtoolsShared`, `gtoSupport`) in its own one-file commit (`ebb666b`). Develop against an uncommitted `includeBuild` in `settings.gradle.kts`; the owner rejects it in PRs ("this is good for developing locally, but shouldn't be included in the PR", #4254). Add a Dependencies section to the PR body linking the upstream PR and noting CI fails until the SNAPSHOT is published. Before merging a feature branch, point back to the main snapshot.

```markdown
## Dependencies
Depends on CruGlobal/kotlin-mpp-godtools-tool-parser#1230. CI will fail
until it merges and 1.4.0-SNAPSHOT is published. Verified locally via an
includeBuild substitution (not committed).
```

Note: `.claude/skills/pr-review/references/patterns.md` says upstream changes must be merged and published as a versioned artifact before this repo updates; the code does otherwise (owner PRs #4562 and #4511 bump to a -SNAPSHOT and open before the upstream PR merges, with a Dependencies note). Follow the code.

## PR titles, ticket keys, branches

**Title the PR `GT-#### <imperative description of the change>`.** Capitalize the first word and describe what the change does, never a status. The owner renamed "QA Fail Fix" himself to something "more descriptive of the actual change" (#4198). The title becomes the squash subject or the merge body on `develop`. For untracked maintenance, drop the key but keep the capitalized imperative (#4552 "Remove explicit setup-java distribution...", #4456 "Remove unused share actions framework").

```
GT-3072 Remove Flipper
GT-3064 Switch tract page rendering to the shared RenderTractPage composable
```

**Put the Jira key at the front, bare.** `GT-#### ` with a space, no brackets, no colon, not at the end. Forms like `[GT-3013] ...`, `GT-3045: ...` and `... (GT-3080)` exist but are off-pattern. In commit subjects the key is optional: Tim prefixes `GT-#### ` on some commits, Daniel never does. Either is fine; the PR title is where it matters.

**Link the ticket in the body as a Jira URL.** First line `Resolves [GT-####](https://jira.cru.org/browse/GT-####)`. Jira is the tracker, not GitHub issues.

Tendency: **Name the branch after the ticket or topic.** Tim uses `GT-####-Title-Case-Words` (`GT-2786-Replace-Deprecated-TabRow`), Daniel uses a short lowerCamelCase topic (`composeTractPage`, `dbSchemaCleanup`). Kebab-case also shows up (`remove-flipper`). Nobody reviews branch names; include the ticket when there is one.

## PR description

**Write a structured body.** Every 2026 staff PR uses sections; one-line bodies are 2025 style. The shape:

- Optional `Resolves [GT-####](...)` first line, then one or two sentences of context (the problem, or the precedent you followed, like "following the same structure as the Dashboard and Tool Details conversions").
- `## Changes` or `## Summary`: one bullet per commit-sized change, class names in backticks, explaining the mechanism, not just "updated X". Say what was deleted.
- Explicit notes on what you intentionally did not do or kept, and why, plus any behavior change (#4562 "intentionally kept", #4588 "intentionally not included", #4583 "Behavior change to be aware of").
- `## Dependencies` when you wait on another repo.
- `## Tests` (or `## Testing` / `## Test plan`): the exact gradle tasks you ran and any manual checks.
- The Claude Code footer from the session's attribution instructions when Claude helped.

Fill-in template:

```markdown
Resolves [GT-####](https://jira.cru.org/browse/GT-####)

<One or two sentences: what problem this solves, or which existing pattern it follows.
Mention the originating review if this is a follow-up, e.g. "Follow-up from the review of #NNNN.">

## Changes
- `ClassName`: <what changed and how it works, one bullet per commit-sized change>
- `OtherClass`: <...>
- Removed <layouts / adapters / deps / catalog entries> orphaned by <the migration>

<Intentionally not done / kept: "<thing>" is intentionally <kept | not included> because <reason>.>
<Behavior change to be aware of: <what users or callers will see differently>.>

## Dependencies
<Only if needed. Depends on CruGlobal/<repo>#NNNN. CI will fail until <version>-SNAPSHOT is published.>

## Tests
- `./gradlew :<module>:test` passes (<new test class> covers <cases>)
- `./gradlew :build-logic:ktlintCheck ktlintCheck` passes
- `./gradlew verifyPaparazzi` passes (snapshots recorded via the CI workflow)
- <Each commit compiles individually, for multi-commit PRs>
- <Manual check: what you did in the app and what you saw>

<Claude Code footer>
```

## Review process

**Self-review with the pr-review skill before asking for review.** Fix Must Fix findings in the commit that introduced them (fixup plus autosquash), and post the result on the PR as a COMMENT, never an approval ("Posted as COMMENT because this is a self-review", #4461). Answer or dismiss each finding with a concrete technical reason. Recurring false positives go into `.claude/skills/pr-review/dismissed-issues.md` in the same PR (#4459). For large AI-assisted changes, say so and ask the other owner to look.

Tendency: **Reply in each addressed review thread with what changed.** Name the fixing commit when you can ("Done in ada10b62d, countryFlow now defaults to "US"...", #4586) and leave resolving to the reviewer. For bot findings, either apply them or reply with the technical reason you did not.

Tendency: **Wait for code-owner approval on feature and behavior-changing PRs, then merge.** The author usually merges after approval ("Looks good, feel free to merge whenever", #4176). Owners sometimes self-merge build and cleanup PRs after green CI; contributors should not.

Tendency: **Pull before pushing again after review.** The owner may push small mechanical fixes (whitespace, formatter rollbacks) to your branch (#4263). Do not force-push over them. When a comment is labeled a preference, apply it anyway.
