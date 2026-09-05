# Plan: migrate the test tree to the #3446 layout

Supersedes the first version of this file, which planned the `test.<feature>` grouping that became
PR #3444. That grouping is done and correct as far as it goes, but #3444 got `CHANGES_REQUESTED`:
it stops at an intermediate layout, and #3446 defines the target. This plan takes the tree to the
target instead, so nothing has to be reorganized twice.

## What the review asked for

Three things, from [juherr's review](https://github.com/testng-team/testng/pull/3444#pullrequestreview-5057294193):

1. **Land in the #3446 layout, not an intermediate one.** Executable tests under
   `org.testng.<feature>.*`; classes that exist only to be handed to TestNG under
   `org.testng.<feature>.samples.*`. The `samples` boundary is the point — it is what lets Gradle
   exclude fixtures once suite-driven execution goes away, and `test.<feature>.<issue>.*` cannot do
   that job because those classes carry `@Test` methods and would be discovered as root tests.
2. **Verify the issue references.** Six of them are wrong.
3. **Strengthen the newly activated #1362 assertion**, which cannot detect the regression it exists
   to catch.

Removing `testng.xml` stays out of scope; #3446 tracks it separately.

## How references are verified

The first pass compared issue *titles* to what each test does. That is guesswork dressed up as
checking — a title can plausibly match a test it has nothing to do with, which is how `GITHUB-317`
got written when GitHub #317 is a pull request about repeated parameters.

A reference is now written only when **both** ends check out, via
`scripts/verify-issue-refs.sh <path-fragment> <issue-number>`:

1. **Provenance** — the commit that introduced the test names the issue, or the merge that brought
   it in does.
2. **The issue** — `#<n>` is a real, closed GitHub *issue*, not a pull request, and its subject is
   what the test asserts.

Where GitHub's own timeline for the issue links the introducing commit, the issue points back at
the code, which is as strong as this gets.

The script refuses to answer rather than guess. Looking a file up by basename alone finds seven
different `TestClassSample.java`, and picking the first invents provenance that reads exactly like
the real thing — so it reports `AMBIGUOUS` and asks for the original path.

Run across all 21 references: **15 verified, 6 dropped.** Full evidence in
`docs/test-issue-references.md`.

- **15 GitHub references stand.** Twelve have the introducing commit in the issue's own timeline.
  Two more — 765 and 1417 — say only "Fixing review comments" in the commit and resolve through
  PRs #1374 and #1447, which the issues do link. `GITHUB-107` is the weakest: issue #107 was closed
  by hand in 2011 and links nothing, so it rests on the commit saying "Issue 107" in words plus a
  matching title. That description predates this work and is left alone, flagged.
- **6 tests get no description at all.** Five are JIRA items — TESTNG-106, 195, 249, 285, 387 —
  genuine, and the commits that fixed them quote their titles, but `jira.opensymphony.com` is dead
  so the identifier points at nothing a reader can open. The sixth, `testng317`, has no provenance
  whatsoever: an empty commit message in 2009. For four of these six the same number on GitHub is a
  *pull request* about something else entirely, which is exactly the trap.

This supersedes the earlier proposal to write `TESTNG-<n>` from the package name. The class and
package names carry what these six cover; nothing is invented to fill the gap.

## Two problems this plan has to solve

### `org.testng.thread` already exists in main, and is `@NullMarked`

`testng-core/src/main/java/org/testng/thread/package-info.java` carries `@NullMarked`, and
`testng.errorprone.gradle.kts` says in as many words that the test half of a marked main package is
already marked by the `package-info.class` on the compile classpath. Moving `test.thread` (126
files) to `org.testng.thread` would put all of them under NullAway and merge them into a production
package.

**`test.thread` becomes `org.testng.concurrency`.** It is the one feature that does not follow the
mechanical `test.<feature>` → `org.testng.<feature>` mapping, so the phase adds a
`package-info.java` recording why — a reader who finds `org.testng.concurrency` next to a
production `org.testng.thread` deserves to be told it was deliberate rather than left to rediscover
the NullAway interference. That file gets javadoc only and **no `@NullMarked`**: annotating it would
opt the tests straight back into the check the rename exists to avoid.

Every other feature name is free.

### 33 classes look like tests that never run

Classifying by "listed in `testng.xml`" leaves 33 unregistered classes named `*Test` that hold
`@Test` methods. Most are ordinary fixtures with unfortunate names — `test.thread.Test1Test`,
`test.inheritance.testng234.ChildTest` — fed to TestNG by a driver. Some may be a second
`github1362`: a real regression test that has quietly never executed.

Filing them under `samples` would make that permanent and invisible, so **each phase confirms or
promotes its own share before filing anything**: either establish that some driver feeds the class
to TestNG, or promote it to an executable test and register it. That is the judgement `github1362`
already needed, made at the moment someone is reading the code anyway.

A promoted test may fail — `github1362` passed, but its assertion could not have caught the
regression it was written for. Expect at least one of these to need real work, and keep that work
in the phase that surfaces it rather than deferring it.

The full list is in `scratchpad/suspect-tests.txt`; per-phase counts are in the table below.

## The classification rule

For every file at `test.<feature>[.<sub>].<Class>`:

- **listed in `testng-core/src/test/resources/testng.xml`** (or a suite file it pulls in) →
  executable → `org.testng.<feature>[.<sub>].<Class>`
- **anything else** → sample → `org.testng.<feature>.samples[.<sub>].<Class>`

`samples` goes directly after the feature, per #3446, so the scenario or issue segment stays below
it. Where a path already ends in `samples` (`test.aftergroups.samples`,
`test.configuration.issue2254.samples`) the segment is not doubled.

This is mechanical and checkable, and it makes the executable set after the move exactly the set
`testng.xml` runs today — which is what #3446 step 5 needs in order to compare parity.

206 of the 966 files are executable; 760 are samples.

## Phases

One PR per group, smallest first so the convention is settled on a reviewable diff before the large
packages follow. **#3444 is rebuilt as phase 1** rather than merged as-is: it currently touches all
18 feature packages, so leaving it intermediate is precisely the double reorganization the review
objects to. Its existing work — the legacy `githubNNN`/`issueNNN`/`testngNNN` relocations, the
verified descriptions, the #1362 merge — is redistributed into the phase that owns each feature.

| Phase | Features | Files | Exec | Samples | Suspect |
| --- | --- | --- | --- | --- | --- |
| 1 (#3444) — **done** | `aftergroups`, `memory`, `methodselection`, `conffailure`, `groups` | 37 | 9 | 28 | 3 |
| 2 | `reflect`, `preserveorder`, `priority`, `methodinterceptors`, `skip` | 98 | 25 | 73 | 6 |
| 3 | `invocationcount`, `parameters`, `inheritance` | 96 | 27 | 69 | 4 |
| 4 | `dependent` | 95 | 21 | 74 | 4 |
| 5 | `factory` | 122 | 36 | 86 | 3 |
| 6 | `thread` → `org.testng.concurrency` | 126 | 30 | 96 | 7 |
| 7 | `configuration` | 144 | 27 | 117 | 4 |
| 8 | `listeners` | 248 | 31 | 217 | 2 |
| **total** | | **966** | **206** | **760** | **33** |

Phase 1 also carries the two changes that are not about layout: the six corrected descriptions and
the #1362 assertion. Neither depends on the move, and both are what the review is blocking on.

Phase 1 additionally adds `exclude("org/testng/**/samples/**")` to the test task. With
`suites("src/test/resources/testng.xml")` still in place Gradle does not scan, so the exclude
changes nothing today — it is there so that #3446 step 5 flips one line rather than discovering the
problem then.

## The procedure for one phase

Phase 1 improvised and produced a bug that compiled cleanly and failed only in the suite loader.
Follow the order.

1. **Verify the issue references first, before anything moves.** `git log --follow` cannot see an
   uncommitted rename, so a reference checked after the move needs its old path passed by hand.
2. **Recompute the executable set.** It drifts as master moves, so do not trust the table above.

   ```bash
   grep -hoE '<class name="[^"]+"' testng-core/src/test/resources/testng.xml \
     testng-core/src/test/resources/parent-module-suite.xml \
     testng-core/src/test/resources/188.xml | sed 's/<class name="//;s/"//' | sort -u
   ```

   Also check `<package name="..."/>` in `testng.xml`. A class selected that way is executable
   without being named, and the rule above would file it under `samples`. Three such tags exist
   today (`test.nested.*`, `test.nested2`, `org.testng.internal.invokers`); none is in the 18
   feature packages, so this has not bitten yet.
3. **Confirm or promote this phase's suspect classes** before filing anything under `samples`.
4. **`git mv` the files**, then **set every package line from the file's path** — never from a map
   keyed on the old package. Check it:

   ```bash
   # every moved file: declared package must equal its directory
   ```
5. **Add imports** for each sample a relocated test now references, and widen only what the split
   forces. A static import of a member does not import the type.
6. **Update the resources.** Grep, do not work from a list:

   ```bash
   grep -rl "test\.<feature>\." testng-core/src/test/resources testng-yaml/src/test/resources \
     testng-core/src/test/java testng-test-kit/src testng-jcommander/src
   ```
7. **`autostyleApply`, then compile, then the phase's tests, then the full build.**
8. **Compare the executable set before and after.** Same count, a clean 1:1 rename, nothing
   dropped. A green build does not prove this.

## Things that will bite

- **Derive the new package from the file's path, never from a map keyed on the old one.** A feature
  that splits into both executable and `samples` classes maps one old package to two new ones, and a
  dict silently keeps whichever was written last. In phase 1 that gave all seven executable classes
  a `.samples` package line while they sat in the feature directory. **It compiled** — javac files
  by declared package, not by directory — and surfaced only as
  `Cannot find class in classpath: org.testng.conffailure.ConfigurationFailure`.
- **Package-private access.** Splitting a test from its samples breaks it; widen only where forced.
  Phase 1 needed two: `TestClassSample.logs` and `FailingSuiteFixture.s_invocations`.
- **A static import of a member does not import the type.** `ConfigurationFailure` statically
  imports `ClassWithFailedBeforeTestClassVerification.success` and separately uses the class as a
  `.class` literal, so it needs both.
- **String class names.** Several tests name classes as string literals rather than `.class`.
- **Far more resource files than phase 1 suggested.** Phase 1 touched five. The remaining phases
  are worse — grep before assuming:

  | Feature | Resource files naming it |
  | --- | --- |
  | `thread` | 17 |
  | `parameters` | 12 |
  | `listeners` | 9 |
  | `factory` | 3 |
  | `methodinterceptors` | 2 |
  | each of the rest | 1 |

- **`testng-core/src/test/resources/test/listeners/` mirrors the package path** and holds four
  suite files. Phase 8 has to decide whether that directory moves with the package or stays put;
  the answer depends on how each file is loaded. No other feature has such a directory.
- **Other modules hold the same package names.** `test.groups.issue2232` exists in
  `testng-test-kit` (the shared suite builder) and `testng-jcommander` (a forked-process twin).
  Phase 1 moved only the `testng-core` half. Grep every module, not just `testng-core`.
- **Base classes in `test.*` stay where they are.** `test.SimpleBaseTest`, `test.BaseTest`,
  `test.InvokedMethodNameListener` and `test.TestHelper` are used across the whole tree. Migrated
  tests keep importing them from `test.*`; moving them is a separate job, not part of any phase.
- **Groovy and Kotlin test sources reference none of the 18 features.** Checked, so no phase needs
  to handle them — recorded here so nobody checks again.
- **Stale build output can mask a mistake.** A class compiled into its old package lingers in
  `build/classes`. Add `clean` when a result looks impossible.

## Decisions taken

1. **#3444 is rebuilt as phase 1.** Merging it as-is would move every class it touches twice, which
   is the objection the review raised.
2. **`test.thread` becomes `org.testng.concurrency`**, with a `package-info.java` recording the
   NullAway reason.
3. **The 33 suspect classes are confirmed or promoted per phase**, not deferred to an audit issue.

Raised with the reviewer in
[this comment](https://github.com/testng-team/testng/pull/3444#issuecomment-5550403468) and waiting
on an answer:

- **the `org.testng.concurrency` name**, since it is the one departure from the mechanical mapping.
  It only blocks phase 6, so phases 2 to 5 can run without it;
- **dropping the five JIRA references** rather than writing them as `TESTNG-<n>`. They are verified
  and the commits quote their titles, but `jira.opensymphony.com` no longer resolves, so the
  identifier would point at nothing. Reinstating them as prose is the alternative.

## Left to do when the eight phases are finished

Neither of these belongs to a phase, and both are easy to forget once the migration is done.

1. **Take the scaffolding out of the tree.** `docs/TEST_RELOCATION_PLAN.md`,
   `docs/test-issue-references.md` and `scripts/verify-issue-refs.sh` exist to carry the plan and
   the evidence while the phases run. Delete them at the end, or move whatever is still worth
   keeping into `.github/CONTRIBUTING.md` — the executable/sample convention is the part a
   contributor will still need after the migration.
2. **Decide what happens to `test.test111`.** It is the same shape as the packages this work
   removed, but it does not match `testng<number>` so it was never in scope. Nothing else in the
   tree is named that way now.

There is also a job this migration uncovered but did not take on: **452 `GITHUB-*` descriptions
elsewhere in the test tree that nobody has verified.** `GITHUB-317` was wrong, so others will be.
`scripts/verify-issue-refs.sh` audits them. That wants its own issue under #3446 rather than being
folded into a phase.
