# Instructor guide

How to run the CI/CD demo: a green `main`, then one branch per scenario that shows a specific failure or warning.

## Learning outcomes

By the end, you can:

1. Explain why the same API is implemented twice with the same architecture (see [comparison](architecture/comparison.md)).
2. Run the full set of checks locally before pushing.
3. Tell **blocking** checks (the build fails) from **advisory** checks (the build warns).
4. Read a failed or unstable build in Jenkins and in GitHub Actions and find the cause.
5. Fix the findings and watch the build recover.

## Before the course

- [ ] `main` is green in both CIs. Run locally first:
  ```bash
  cd python && uv run pytest && uv run ruff check app tests && uv run ruff format --check app tests && uv run mypy app
  cd ../java && ./gradlew check
  ```
- [ ] Jenkins is up (`docker compose up -d --build jenkins`) with two Multibranch Pipeline jobs, one per Jenkinsfile (`.jenkins/python.Jenkinsfile` and `.jenkins/java.Jenkinsfile`).
- [ ] In both jobs, set Branch Sources → Behaviours → "Filter by name (with wildcards)" to include `main demo/**`. Branch names with slashes appear URL-encoded in Jenkins.
- [ ] The demo branches exist and are pushed (see [Creating the branches](#creating-the-branches)). Scan each Jenkins job once so it discovers them.
- [ ] Draft pull requests are open for the branches you want to show on GitHub (see [GitHub](#github)).
- [ ] Both workflows have run on `main` at least once.

## Suggested session plan (about 2 hours)

| Time | Block | What to do |
|---|---|---|
| 0:00 | Contract and repo tour | Walk through `greetings.yaml` and the [docs index](README.md). Run both apps with `docker compose up` |
| 0:15 | Architecture | Show the layers and C4 diagrams of both versions. Use the [comparison](architecture/comparison.md) |
| 0:40 | Checks locally | Run the Python and Java checks. Open a file in VS Code to show the same Ruff errors live |
| 0:55 | A green `main` | Show the green builds in Jenkins and GitHub Actions |
| 1:05 | Blocking failures | `demo/python/lint-errors`, `demo/python/type-errors`, `demo/java/blocking-errors` |
| 1:25 | Coverage | `demo/python/low-coverage`, then `demo/java/coverage-warning` |
| 1:40 | Warnings | `demo/python/code-smells`, `demo/java/code-smells` |
| 1:50 | Live fix | Students refactor the code smells until the build is green again |

## Scenarios at a glance

| Branch | Result | Cause |
|---|---|---|
| `demo/python/lint-errors` | Fails | Ruff `F401`, `S105`, `S307` |
| `demo/python/type-errors` | Fails | mypy return type error |
| `demo/python/low-coverage` | Fails | Coverage below 80% |
| `demo/python/code-smells` | Unstable, passes | Advisory Ruff rules (complexity, arguments, magic values, commented code, `print`, TODO) |
| `demo/python/coverage-warning` | Unstable, passes | Coverage between 80% and 85% |
| `demo/java/blocking-errors` | Fails | Checkstyle, PMD and SpotBugs findings |
| `demo/java/low-coverage` | Fails | Coverage below 80% |
| `demo/java/code-smells` | Unstable, passes | Checkstyle `warning` rules |
| `demo/java/coverage-warning` | Unstable, passes | Coverage between 80% and 90% |

"Unstable" is the yellow state in Jenkins. In GitHub Actions the job is green and the warnings appear as yellow annotations.

## Creating the branches

Always branch from an up-to-date `main`. Commit and push each one:

```bash
git switch main && git pull
```

### Python

```bash
# demo/python/lint-errors
git switch -c demo/python/lint-errors main
mkdir -p python/app/demo && touch python/app/demo/__init__.py
cp docs/demo/python/lint_errors.py.txt python/app/demo/lint_errors.py
git add -A && git commit -m "demo: ruff errors that block the build"
git push -u origin demo/python/lint-errors

# demo/python/type-errors
git switch -c demo/python/type-errors main
mkdir -p python/app/demo && touch python/app/demo/__init__.py
cp docs/demo/python/type_errors.py.txt python/app/demo/type_errors.py
git add -A && git commit -m "demo: mypy error that blocks the build"
git push -u origin demo/python/type-errors

# demo/python/code-smells
git switch -c demo/python/code-smells main
mkdir -p python/app/demo && touch python/app/demo/__init__.py
cp docs/demo/python/code_smells.py.txt python/app/demo/code_smells.py
git add -A && git commit -m "demo: code smells reported as warnings"
git push -u origin demo/python/code-smells
```

For the coverage branches, generate a module of untested functions, **outside** `app/demo` (that package is omitted from coverage):

```bash
# demo/python/low-coverage   (N = 40 is a starting point)
git switch -c demo/python/low-coverage main
cd python
N=40
{
  echo '"""Code without tests, used to lower coverage."""'
  for i in $(seq 1 $N); do
    printf '\n\ndef step_%02d(x: int) -> int:\n    return x + %d\n' "$i" "$i"
  done
} > app/untested.py
uv run ruff format app/untested.py
uv run pytest        # read the total coverage in the output
cd ..
```
Tune `N` until the total is **below 80%**, then commit and push. For `demo/python/coverage-warning`, repeat with a smaller `N` (start around 14) until the total is **at least 80% and below 85%**.

### Java

```bash
# demo/java/blocking-errors
git switch -c demo/java/blocking-errors main
mkdir -p java/src/main/java/com/example/greetings/demo
cp docs/demo/java/BlockingErrors.java.txt \
   java/src/main/java/com/example/greetings/demo/BlockingErrors.java
git add -A && git commit -m "demo: checkstyle, pmd and spotbugs errors that block the build"
git push -u origin demo/java/blocking-errors

# demo/java/code-smells
git switch -c demo/java/code-smells main
mkdir -p java/src/main/java/com/example/greetings/demo
cp docs/demo/java/CodeSmells.java.txt \
   java/src/main/java/com/example/greetings/demo/CodeSmells.java
git add -A && git commit -m "demo: code smells reported as warnings"
git push -u origin demo/java/code-smells
```

```bash
# demo/java/low-coverage   (N = 30 is a starting point)
git switch -c demo/java/low-coverage main
N=30
{
  echo "package com.example.greetings.application;"
  echo
  echo "final class Unused {"
  echo
  for i in $(seq 1 $N); do
    printf '    static int step%02d(int x) {\n        return x + %d;\n    }\n\n' "$i" "$i"
  done
  echo "}"
} > java/src/main/java/com/example/greetings/application/Unused.java
(cd java && ./gradlew check)    # the coverage check shows the total
```
Tune `N` until coverage is **below 80%**. For `demo/java/coverage-warning`, use a smaller `N` (start around 12) until it is **at least 80% and below 90%**. Both go outside the `demo` package, which is excluded from JaCoCo.

## Scenario scripts

Each script has the same shape: push, show the result in Jenkins, show it in GitHub, explain, fix.

### Python: lint errors (`demo/python/lint-errors`)

- **Show first:** open `python/app/demo/lint_errors.py` in VS Code. The red squiggles show the same Ruff errors before anything is pushed.
- **Jenkins:** the build is red. In the stage view, `Static analysis` fails while `Tests & coverage` is still green, because the stages run in parallel. Open the **Ruff (blocking)** issues report and sort by rule.
- **GitHub:** the `Static analysis` job fails and the annotations show `F401`, `S105` and `S307`.
- **Say:** `S105` and `S307` are security rules. A leaked password and `eval` on input should never merge. `# noqa` hides a finding and doesn't fix it.
- **Fix live:** delete the file. The build recovers on the next push.

### Python: type error (`demo/python/type-errors`)

- **Jenkins:** red build. The mypy issues appear in the same blocking report.
- **GitHub:** the `Static analysis` job fails on the mypy step, with an inline annotation on the `return` line.
- **Say:** the function lies about its return type, and only the type checker notices. The tests wouldn't catch it unless they exercised the function.
- **Compare:** Java gets this class of error from the compiler itself.

### Java: blocking errors (`demo/java/blocking-errors`)

- **Jenkins:** red build. Three reports (Checkstyle, PMD, SpotBugs) list findings. The string `==` comparison appears in **both** PMD and SpotBugs.
- **GitHub:** the static analysis job fails and the annotations point at the lines.
- **Say:** different tools catch different things, and some overlap. With `--continue`, Gradle runs all three, so one run shows every problem instead of one at a time.
- **Fix live:** use `.equals`, log through a logger, and handle the exception.

### Coverage (`demo/python/low-coverage`, `demo/java/coverage-warning`)

- **Low coverage (Python):** the `Tests & coverage` stage fails. The log shows "Required test coverage of 80% not reached" and the total. The HTML coverage report highlights `untested.py` at 0%.
- **Coverage warning (Java):** the build is yellow and the job is green. The Coverage report shows a total between the hard limit and the target.
- **Say:** there are two thresholds. Below the hard limit the build fails. Between the limit and the target it warns. A team can raise the target over time without ever blocking anyone.

### Code smells (`demo/python/code-smells`, `demo/java/code-smells`)

- **Jenkins:** the build is **yellow (UNSTABLE)** and not red. Open the advisory report and point at the rule IDs.
- **GitHub:** the job is green and the Files changed tab shows yellow warning annotations.
- **Python warnings:** `C901`, `PLR0913`, `PLR0911`, `PLR0912`, `PLR2004`, `ERA001`, `T201`, `FIX002`.
- **Java warnings:** `ParameterNumber`, `CyclomaticComplexity`, `NestedIfDepth`, `ReturnCount`, `MagicNumber`, `TodoComment`.
- **Say:** these are judgment calls, not bugs. That is why they warn. The split also shows how a team adopts a rule: advisory first, then blocking once the findings are fixed.
- **Compare:** the Python split needs two Ruff runs, because Ruff has no severity levels. Checkstyle has severities built in.

## Live-fix exercise

Use the `code-smells` branches, one language per group.

**Goal:** refactor `discount` until the advisory report is empty and the build is green.

**Hints, to reveal one at a time:**
1. Replace the nested `if` chain with a lookup table of rates per customer kind.
2. Give the magic numbers names (constants).
3. Group the five parameters into one object (a dataclass in Python, a record in Java).
4. Turn the `TODO` into an issue and delete the comment, and delete the commented-out code.

**Check:** push after each hint and watch the number of warnings fall in Jenkins.

## GitHub

Demo branches get workflow runs because the workflows trigger on `demo/**`. For the inline view, open a **draft pull request** per branch:

```bash
gh pr create --draft --base main --head demo/python/code-smells \
  --title "[DEMO] Python code smells" --body "Course demo. Never merge." --label do-not-merge
```
A draft PR can't be merged, and annotations then show on the diff, as students will see in real projects. If you use required status checks, remember that a workflow skipped by a `paths:` filter leaves its check pending on unrelated PRs.

## After the session

Keep or reset the branches:

```bash
# remove them all
for b in $(git branch -r --list 'origin/demo/*' | sed 's#origin/##'); do
  git push origin --delete "$b"
done
```
To refresh them for the next run, rebase each one onto `main` and push with `--force-with-lease`. A script for that is in `scripts/` (see the next step).

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| Jenkins doesn't show a demo branch | The job wasn't scanned after the push, or the name filter is missing `demo/**` |
| A Java branch appears in the Python Jenkins job (or the other way round) | Expected. Each job builds every matching branch. Its stages skip if you added the `changeset` condition |
| A warning-only branch is red | Coverage fell below the hard limit. Check the `demo` package is excluded and tune the file size |
| The coverage branch shows the wrong result | The app grew or shrank. Re-tune `N` |
| No annotations in the PR | The branch has no pull request, or the run was over GitHub's limit of 10 warnings and 10 errors per step |
| Python Jenkins build is not yellow on warnings | The advisory `recordIssues` block or the gate is missing in `python.Jenkinsfile` |
| `main` itself is red | Fix this before the session. Always run the preparation checklist |
