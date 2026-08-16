# Working on TestNG

Notes for coding agents, limited to what is easy to get wrong here. General contributor
documentation lives in [`.github/CONTRIBUTING.md`](.github/CONTRIBUTING.md) and [`docs/`](docs/) —
read those for the build model, the Java versions and the release process rather than assuming.

Where this file contradicts the build, the build wins: fix the file.

## Verification

**`./gradlew build -x test` does not compile test sources.** It is a smoke check, never the gate.
Checks that only fail once tests are compiled, or after a `clean`, slip straight through it — Error
Prone rules that fire on test fixtures, formatter tasks that were up to date, dependency upgrades
that break test code but not main.

The gate is `./gradlew build`, the same task CI runs. Let Gradle decide how much of it to re-run:
a change that reaches nothing finishes in seconds, so there is no need to judge by hand what a diff
can affect. Add `clean` only to rule out stale output.

```bash
./gradlew build     # minutes when it has work to do, seconds when it does not; 0 failures
```

**The gate is not the edit loop.** Letting Gradle skip work only helps when the change reaches
little; anything in `testng-core-api` reaches everything, so every run costs the full five minutes.
While iterating, run the tests that would actually fail if the change is wrong, and spend the five
minutes once, before committing:

```bash
./gradlew :testng-core:test \
  --tests "org.testng.xml.XmlRoundTripTest" \
  --tests "test.xml.XmlVerifyTest" > /tmp/t.log 2>&1
rc=$?; echo "EXIT=$rc"; (exit $rc)
```

Naming that set before editing is the point: it is the question the change has to answer. A run like
the one above returns in seconds rather than minutes.

**A new `testng-core` test class does not run until it is listed in
`testng-core/src/test/resources/testng.xml`.** The task is suite-driven, not classpath-scanned, so
an unregistered class is skipped by `build` without a word — the file can be committed and never
execute — and `--tests` rejects it with `No tests found for given includes`, which reads like a
typo in the filter. Register it in the same edit that creates it, next to its neighbours:
`org.testng.xml.*` goes in `<test name="XML">`, `test.xml.*` in `<test name="Regression2">`. Then
confirm it ran, rather than trusting the exit code:

```bash
CLASS=org.testng.xml.XmlRoundTripTest
grep -o 'tests="[1-9][0-9]*"' "testng-core/build/test-results/test/TEST-$CLASS.xml"
```

A green local build is not a green CI. Pull requests against `master` also run an OpenRewrite check
that `.github/CONTRIBUTING.md` documents, and a wrapper validation. Pushes are weaker than they
look: the `branches: ['*']` filter in `test.yml` does not match `/`, so pushing a branch named
`docs/foo` triggers no build until a pull request exists.

`verifyPublishedPomDependencies` (in `testng/testng-build.gradle.kts`, wired into `check`) pins the
exact dependency set of the published pom, versions ignored. When it fails, either the change is
wrong or the expected list needs updating — decide which, do not just update the list.

## Declaring a package null-marked

How the check is scoped is documented where it is configured, in
`build-logic/code-quality/src/main/kotlin/testng.errorprone.gradle.kts`. What that comment cannot
say is that a green build proves nothing on its own: it is also what an unchecked package looks
like.

Several of the main packages span several modules, and there the placement decides the coverage.
Module A's `package-info.class` reaches B's compile classpath only if B depends on A, so putting the
file on the wrong side leaves the other half compiling **unchecked**.

Prove the coverage per module traversed, not once per package. Drop a throwaway

```java
private static Object nullAwayProbe() { return null; }
```

into one file of **each** module the package lives in, and compile those modules. Before the
`package-info.java` every probe must compile clean; after it every probe must fail with
`[NullAway] returning @Nullable expression from method with @NonNull return type`. A probe that
still passes marks a half that is not under the check, and the error count for that package means
nothing until it does.

`@NullMarked` does not descend into sub-packages: `org.testng.internal.thread.graph` was marked long
before `org.testng.internal.thread`.

## Layout

Modules are listed in `settings.gradle.kts`; only `:testng` is published, as a shaded jar merging
the others. `docs/BUILD_SYSTEM.md` describes the structure.

Before writing documentation, read what is already in `docs/` and `.github/`. Most build topics are
covered there; a second copy drifts from the first, and this file was itself written as a fifth copy
before being cut back to links.

Two things that are not documented elsewhere and cost tool calls to discover:

- Per-project build files are named `<module>/<module>-build.gradle.kts`, not `build.gradle.kts`.
- Build logic lives in two included builds: `build-logic-commons/` builds the plugins that
  `build-logic/` uses. Conventions are precompiled script plugins,
  `build-logic/*/src/main/kotlin/testng.*.gradle.kts` — prefer changing a convention over repeating
  configuration per module.

The `guice` and `yaml` optional features are hand-rolled rather than declared with
`registerFeature`; the reasoning is in the KDoc of
`build-logic/jvm/src/main/kotlin/buildlogic/OptionalFeatureVariants.kt`. If you touch them, check
the published pom and Gradle Module Metadata are unchanged.

## Changing the build JDK

`docs/JAVA_VERSIONS_QUICK_REFERENCE.md` explains the three Java versions. Read the current values
rather than assuming them:

```bash
grep -E '^jdkBuildVersion|^targetJavaVersion' gradle.properties
```

The root `gradle.properties` drives local builds, but it is **not** the only place the build JDK
appears. Moving it means updating all of:

- `gradle.properties`
- the fallback defaults in `build-logic/build-parameters/build.gradle.kts`
- the fallbacks in `build-logic-commons/gradle-plugin/build.gradle.kts` and its
  `build-logic.kotlin-dsl-gradle-plugin.gradle.kts` (that build is configured before the root
  properties are available, so it reads them from disk)
- `.github/workflows/` — `test.yml` passes `jdkBuildVersion` as a Gradle property, which
  **overrides** `gradle.properties`, and the publish workflows pin the JDK they install

Bytecode is produced with `--release targetJavaVersion`, so the launching JDK does not leak into the
artifacts. Gradle itself needs JDK 17+; the build then resolves a `jdkBuildVersion` toolchain,
auto-detected or auto-provisioned. Where no matching JDK is available and auto-provisioning is off
(as in CI), the failure is `Cannot find a Java installation on your machine ... matching:
{languageVersion=N, ...}`.

## Dependencies

- Everything reachable from `:testng-core` ends up in the shaded jar, and any third-party dependency
  it pulls lands in the published pom. Treat major bumps as API decisions and check the pom diff.
- `targetJavaVersion` blocks some upgrades outright — Gradle says so plainly, e.g. *"only compatible
  with JVM runtime version 17 or newer"*. Those are tracked for the next major release.
- Dependabot covers `github-actions` and `gradle`. Actions are pinned to a commit SHA with the
  version as a trailing comment; keep that shape, and check the SHA really is the tag it claims.
- A version that must not be taken belongs in `ignore` in `.github/dependabot.yml`, with the reason
  written there. Closing the pull request alone makes it come back.

## Git

The canonical repository is `testng-team/testng`. When working from a fork, its `master` is often
behind, so diff, branch and rebase against the canonical remote — reasoning from a stale `master`
produces confident, wrong analysis. Check before starting:

```bash
git remote -v                                  # which remote is canonical?
git fetch <canonical> master
git rev-list --count HEAD..<canonical>/master  # 0 means you are current
```

From a fork, pull requests are cross-fork:

```bash
gh pr create --repo testng-team/testng --base master --head <fork-owner>:<branch>
```

Conventional Commits. Record user-visible changes in `CHANGES.txt`, newest first, under the current
version heading.

Split a pull request that mixes a large mechanical sweep with changes that need judgement. The two
halves have different review costs: a tool's output — an OpenRewrite run, a formatter pass, a
rename across hundreds of files — is re-derivable from the recipe that produced it and needs little
more than green CI, while the design decisions around it need a maintainer's attention. Bundled,
the small half queues behind the large one and a reviewer becomes the bottleneck for both.

Send the mechanical half first, then stack the reviewed half on its branch:

```bash
gh pr create --repo testng-team/testng --base <mechanical-branch> --head <fork-owner>:<branch>
```

The pull request then shows only the delta, and GitHub retargets it to `master` when the first one
merges. Within each pull request, keep the machine output in its own commit, separate from the hand
edits that follow it, so a reviewer can tell which hunks a human actually judged.

## Working style

- **Verify claims before acting on them** — including your own. A pull request description, an issue
  comment, a memory or a conclusion from an earlier session are all hearsay until re-checked against
  whatever settles them: the build for anything about this repository, the registry or upstream
  release notes for anything about a dependency. A release date costs a minute to check; acting on a
  wrong premise costs hours. This has already caused one full migration to be written and reverted,
  and one obsolete memory to be copied into a committed file.
- Running a documented command proves it executes, not that it does what the text claims. If the
  text promises an effect — filtering, failing, producing a value — measure that effect.
- Report what you actually observed. If a run was flaky, say so and show both runs.
- When an upgrade is refused, record the error that refused it, so the next person does not retry it
  blind.
