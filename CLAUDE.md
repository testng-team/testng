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

## Environment

- The workspace may be reset to an older commit between sessions. Run the staleness check from
  `AGENTS.md` before anything else, and fast-forward if needed.
- The SSH agent is not always loaded. If `git push` fails with `Permission denied (publickey)`, push
  over HTTPS using the `gh` credentials:
  `git push https://github.com/<owner>/testng.git <branch>`. With no remote-tracking ref,
  `--force-with-lease` needs the expected SHA spelled out: `--force-with-lease=<branch>:<sha>`.
