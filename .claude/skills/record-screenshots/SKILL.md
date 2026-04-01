---
name: record-screenshots
description: Push branch to GitHub, run the record-snapshots workflow, fetch the screenshot commit it creates, and fold it into the appropriate commit via fixup + autosquash.
allowed-tools: Bash, Read, Grep, Glob
---

Record updated Paparazzi snapshots for the current branch and fold the resulting CI commit into the right local commit.

## Steps

### 1. Capture branch state

```bash
git branch --show-current
git log --oneline develop..HEAD
```

Note the current branch name and list of commits since `develop`.

### 2. Push to origin

```bash
git push origin HEAD
```

If this fails (rejected), explain why and stop — do not force-push without user confirmation.

### 3. Trigger the workflow

Before triggering, check whether a run is already in progress for this branch:

```bash
gh run list --workflow=record-snapshots.yml --branch=<branch> --limit=1 --json databaseId,status,conclusion
```

If a run is already `in_progress` or `queued`, inform the user and ask whether to wait for that run or trigger a new one. Otherwise, trigger:

```bash
gh workflow run record-snapshots.yml --ref <branch>
```

Wait a few seconds for GitHub to register the run, then get its ID:

```bash
sleep 5
gh run list --workflow=record-snapshots.yml --branch=<branch> --limit=1 --json databaseId,status,conclusion
```

### 4. Wait for completion

```bash
gh run watch <run-id> --interval 30
```

If the conclusion is not `success`, fetch and display the run log and stop:

```bash
gh run view <run-id> --log-failed
```

### 5. Fetch the screenshot commit

```bash
git fetch origin <branch>
```

Check how many new commits the remote has:

```bash
git log --oneline HEAD..origin/<branch>
```

- **0 commits ahead**: no snapshots changed (CI skipped the commit) — inform the user and stop.
- **1 commit ahead**: proceed.
- **More than 1 commit ahead**: warn the user and stop — the situation is unexpected and needs manual handling.

### 6. Find the target commit

Look for the most recent commit on the branch (i.e. in `develop..HEAD`) that touched Paparazzi test sources or related composables. Check in this order:

a. Commits that modified `*Paparazzi*.kt` files:
```bash
git log --oneline develop..HEAD -- '*Paparazzi*.kt'
```

b. If none found, commits that modified Composable UI source files (files in main source sets containing `@Composable`):
```bash
git log --oneline develop..HEAD -- $(grep -rl "@Composable" --include="*.kt" . | grep "/src/main/")
```

c. If still none found, commits that modified any test source file:
```bash
git log --oneline develop..HEAD -- '*/src/test/**' '*/src/androidTest/**'
```

If there are multiple candidates at any step, show them and ask the user which commit the screenshots belong to before proceeding.

### 7. Apply the snapshot changes locally

Stage the snapshot files from the remote commit:

```bash
# Get the list of files changed by the screenshot commit
git diff --name-only HEAD origin/<branch>

# Stage those files at the remote version
git checkout origin/<branch> -- $(git diff --name-only HEAD origin/<branch>)
```

### 8. Create a fixup commit

```bash
git commit --fixup=<target-sha>
```

### 9. Rebase with autosquash

Calculate how many commits back to rebase (number of commits in `develop..HEAD` plus the new fixup commit):

```bash
N=$(git rev-list --count develop..HEAD)
GIT_SEQUENCE_EDITOR=true git rebase -i --autosquash HEAD~$((N + 1))
```

If the rebase fails (conflict), explain the conflict and stop — do not attempt to auto-resolve.

### 10. Confirm and force-push

Show the user the resulting log:

```bash
git log --oneline develop..HEAD
```

**Ask for explicit confirmation before force-pushing.** Then, with confirmation:

```bash
git push --force-with-lease origin <branch>
```

Use `--force-with-lease` (not `--force`) to guard against unexpected remote changes.