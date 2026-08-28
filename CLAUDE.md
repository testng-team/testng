# CLAUDE.md

Read [AGENTS.md](AGENTS.md) first — it holds everything about this repository. This file only adds
what is specific to Claude Code.

## Selecting the build JDK

The ambient `java` is whatever the version manager resolved for the session, not necessarily what
the build wants. With `mise`, derive it from the property so it stays correct across bumps:

```bash
export JAVA_HOME="$(mise where java@$(sed -n 's/^jdkBuildVersion=//p' gradle.properties))"
```

This needs the `java@<N>` alias to point at an installed version; `mise where` fails otherwise.

## Long-running commands

`./gradlew build` takes several minutes whenever it has real work to do — well over the default
`Bash` timeout. Pass an explicit one:

```text
timeout: 900000
```

Filter the output rather than reading it whole; a full build log runs to thousands of lines, and the
test logger prints a line per test class, so match on the summary only. Set `pipefail` first —
without it the pipeline reports grep's status, so a failed build still exits 0:

```bash
set -o pipefail
./gradlew --console=plain build 2>&1 | grep -E "^BUILD (SUCCESSFUL|FAILED)|[1-9][0-9]* failed"
```

For a failure, extract the useful part instead of scrolling:

```bash
set -o pipefail
./gradlew --console=plain build 2>&1 | grep -A15 "What went wrong"
```

Gradle names the HTML report on failure, but locating the failing test is quicker from the result
files:

```bash
grep -l '<failure\|<error' testng-core/build/test-results/test/*.xml
grep -o 'message="[^"]*"' testng-core/build/test-results/test/TEST-<the-class>.xml | head
```

## Subagents

A dispatched agent reports back on its own: the harness re-invokes you when it completes.
Polling for it re-sends the whole conversation on every filler turn, and a review wave costs
dozens of them — easily the largest avoidable cost in a session. Dispatch, run the independent
work you already know you need, then stop and let the notification arrive.

If plan mode is what makes you keep the turn alive, that is the signal to ask the question now
rather than to manufacture another `echo waiting`.

An agent's finding is a hypothesis. Reproduce it here before acting on it, and before relaying
it — the reproduction is usually one command, and findings have been wrong in both directions:
false positives, and real defects whose stated cause was not the cause.

## Editing and committing

- **Chain staging to the commit**: `git add <paths> && git commit …`. `git add` aborts the whole
  invocation on one bad path — a file already removed with `git rm`, say — so nothing is staged,
  and a bare `git commit` afterwards still succeeds on whatever the index already held. The result
  is a commit that is not the one the message describes.
- **Edit prose before `autostyleApply`, not after.** The formatter rewraps javadoc, so a scripted
  replacement written against the pre-format text silently stops matching. When patching after a
  format pass, re-read the exact lines with `sed -n '<a>,<b>p'` rather than reusing the string you
  wrote earlier.
- **An absolute claim about the repository is paid for with a command, before it is written.**
  "the only caller", "no other test", "all three", "two of the six" — each is one grep, and each
  one written from memory has been wrong. Sweep what is about to be committed:

  ```bash
  git diff --cached -U0 | grep -nE \
    '^\+.*\b(only|no other|nothing else|never|always|every|all|[0-9]+ of|the (two|three))\b'
  ```

  Every hit needs the command that settles it, run in this session, before the commit lands.
- **Publish deltas and zeros in durable text, not absolute counts.** Upstream moves fast enough that
  an `8 → 6` in a commit message is stale by the next rebase and has to be remeasured; `−82`,
  `0 failures, 0 errors` and "down to zero" survive it.

## Environment

- The workspace may be reset to an older commit between sessions. Run the staleness check from
  `AGENTS.md` before anything else, and fast-forward if needed.
- The SSH agent is not always loaded. If `git push` fails with `Permission denied (publickey)`, push
  over HTTPS using the `gh` credentials:
  `git push https://github.com/<owner>/testng.git <branch>`. With no remote-tracking ref,
  `--force-with-lease` needs the expected SHA spelled out: `--force-with-lease=<branch>:<sha>`.
