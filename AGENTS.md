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

The gate is a full build, which is also what CI runs on every push and pull request:

```bash
./gradlew clean build     # several minutes, must be 0 failures
```

`verifyPublishedPomDependencies` (in `testng/testng-build.gradle.kts`, wired into `check`) pins the
exact dependency set of the published pom, versions ignored. When it fails, either the change is
wrong or the expected list needs updating — decide which, do not just update the list.

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
