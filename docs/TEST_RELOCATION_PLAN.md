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
   it in does. A pull request number does not count. Pull requests and issues share one number
   space, so "Merge pull request #765" and "Some fix (#765)" say nothing about issue #765. Both
   forms are removed before the text is searched.
2. **The issue** — `#<n>` is a real GitHub *issue* and not a pull request, and its subject is what
   the test asserts. The issue's state is reported but never enforced: a regression test may
   legitimately reference an issue that is still open.

Where GitHub's own timeline for the issue links the introducing commit, the issue points back at
the code, which is as strong as this gets.

The script refuses to answer rather than guess. Looking a file up by basename alone finds seven
different `TestClassSample.java`, and picking the first invents provenance that reads exactly like
the real thing — so it reports `AMBIGUOUS` and asks for the original path.

Run across every class in phase 1's scope, not only the ones that already carried a description:
**17 verified, 6 dropped.** Full evidence in `docs/test-issue-references.md`.

- **17 GitHub references stand.** Twelve have the introducing commit in the issue's own timeline.
  Three of the seventeen -- `GITHUB-182`, `GITHUB-1461` and `GITHUB-1496` -- were found only by
  sweeping every class in the phase rather than checking the descriptions that already existed.
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

Phase 1 found three fixtures and promoted none. Phase 2 found five fixtures and one real test:
`test.methodinterceptors.Issue521Test`, written in 2015 for issue #521, in no suite file, never run.
It is registered now and it passes.

To list the suspects for a phase, take the classes in its packages that are named `*Test`, hold a
`@Test` method, and are not named in `testng.xml`.

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
packages follow. Phases 3 to 8 each have an issue, linked below and as sub-issues of #3446. **#3444 is rebuilt as phase 1** rather than merged as-is: it currently touches all
18 feature packages, so leaving it intermediate is precisely the double reorganization the review
objects to. Its existing work — the legacy `githubNNN`/`issueNNN`/`testngNNN` relocations, the
verified descriptions, the #1362 merge — is redistributed into the phase that owns each feature.

| Phase | Features | Files | Exec | Samples | Suspect |
| --- | --- | --- | --- | --- | --- |
| 1 (#3444) — **done** | `aftergroups`, `memory`, `methodselection`, `conffailure`, `groups` | 37 | 9 | 28 | 3 |
| 2 — **done** | `reflect`, `preserveorder`, `priority`, `methodinterceptors`, `skip` | 98 | 25 | 73 | 6 |
| 3 ([#3493](https://github.com/testng-team/testng/issues/3493)) | `invocationcount`, `parameters`, `inheritance` | 90 | 25 | 65 | 4 |
| 4 ([#3494](https://github.com/testng-team/testng/issues/3494)) | `dependent` | 95 | 21 | 74 | 4 |
| 5 ([#3495](https://github.com/testng-team/testng/issues/3495)) | `factory` | 122 | 36 | 86 | 3 |
| 6 ([#3496](https://github.com/testng-team/testng/issues/3496)) | `thread` → `org.testng.concurrency` | 126 | 30 | 96 | 7 |
| 7 ([#3497](https://github.com/testng-team/testng/issues/3497)) | `configuration` | 144 | 27 | 117 | 4 |
| 8 ([#3498](https://github.com/testng-team/testng/issues/3498)) | `listeners` | 248 | 31 | 217 | 2 |
| **total** | | **960** | **204** | **756** | **33** |

Phase 3 counts 90 files, not the 96 first written here. The difference is three legacy packages
that the count assumed phase 3 would absorb. Two of them belong elsewhere:

| Package | Files | Owner | Why |
| --- | --- | --- | --- |
| `test.github1417` | 4 | phase 3, `parameters` | parameter injection into `@BeforeClass` |
| `test.testng37` | 1 | phase 3, `parameters` | `@Parameters` with a null value |
| `test.testng387` | 2 | phase 3, `invocationcount` | asserts `getFailedInvocationNumbers()` |
| `test.testng317` | 3 | **phase 4**, `dependent` | `dependsOnMethods` across classes with matching names |
| `test.issue107` | 3 | **phase 8**, `listeners` | a suite listener changing suite parameters |

Two classes in phase 3 have never run. Neither is registered in any suite file, and neither appears
in `execution-inventory.txt`.

`test.testng37.NullParameterTest` moves with its feature and stays unregistered. Registering it
needs work beyond a relocation, and the work is not small:

- `testng-37.xml` sits beside the class in the source tree and declares its two parameters. No
  suite file, no test and no build script loads it.
- That file gives `nullvalue` the string `"NULL"`. The parameter it feeds is an `int`.
- The method asserts `isNull()` on that `int`, which cannot hold whatever is passed.

The class and its suite file move together, so the pair stays visible. Phase 3 records this rather
than fixing it.

`test.testng317.VerifyTest` stays where it is until phase 4. It asserts nothing -- it prints a
count -- so promoting it would add no coverage. Phase 4 decides whether to give it an assertion or
to delete it.

Phase 1 also carries two changes that are not about layout. It removes six `GITHUB-*` references
that pointed at the wrong issue, leaving those tests with no description at all. It also replaces
the #1362 assertion. Neither depends on the move.

Phase 1 additionally adds `exclude("org/testng/**/samples/**")` to the test task. With
`suites("src/test/resources/testng.xml")` still in place Gradle does not scan, so the exclude
changes nothing today — it is there so that #3446 step 5 flips one line rather than discovering the
problem then.

## The procedure for one phase

Phase 1 improvised and produced a bug that compiled cleanly and failed only in the suite loader.
Follow the order.

1. **Run `scripts/verify-issue-refs.sh` over every test class in this phase's scope, before
   anything moves.** Not only the classes that already carry a description: a class with no
   description may still deserve one, and a class that has one may be wrong. Do it first, because
   `git log --follow` cannot see an uncommitted rename, so a reference checked after the move needs
   its old path passed by hand.

   A description is written only when the script returns 0. Exit 1 means the provenance does not
   name that issue, or the number is a pull request; exit 2 means the path is ambiguous; exit 3
   means the API could not be reached, which is not a verdict. Record the outcome for the phase in
   `docs/test-issue-references.md`.
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
   feature=listeners   # the package this phase is moving
   grep -rl "test\.$feature\." testng-core/src/test/resources testng-yaml/src/test/resources \
     testng-core/src/test/java testng-test-kit/src testng-jcommander/src
   ```
7. **`autostyleApply`, then compile, then the phase's tests, then the full build.**
8. **Let the build check the execution set.** `verifyTestExecution` runs inside `check` and fails
   when a class named in `testng.xml` did not run, when anything under `.samples.` ran as a root
   test, or when the set of tests that ran differs from `testng-core/execution-inventory.txt`.
   A phase that deliberately changes what runs updates the baseline in the same commit:

   ```bash
   ./gradlew :testng-core:verifyTestExecution -PupdateExecutionInventory
   ```

   Read that diff. It is the review's evidence that the phase moved tests without losing any.

## The execution guard

Phase 1 proved the parity by hand and reported the numbers. That does not survive the next seven
phases, so `verifyTestExecution` in `testng-core-build.gradle.kts` now enforces three things on
every `check`:

1. **Every class named in `testng.xml` actually ran.** This is the GitHub issue #1362 failure mode:
   compiled, green, never executed, invisible for years.
2. **Nothing under `.samples.` ran as a root test.** Samples are TestNG input and several are meant
   to fail, so a sample running as a test is both a false failure and a sign the boundary leaked.
3. **Every test in `testng-core/execution-inventory.txt` still runs, with the same outcome.** Each
   line holds `class#method`, a status, and how many times it ran:

   ```text
   org.testng.memory.MemoryLeakTestNg#testMemoryLeak	PASS 1
   ```

   A test that stops running fails the build. So does a status change, which is how `PASS` turning
   into `SKIP` is caught. So does a drop in the count, which is how a lost `invocationCount` or
   data-provider row is caught.

   A test the file has never seen does **not** fail the build. New tests arrive from master as
   often as from a branch, and failing on them would turn every pull request red as soon as master
   gained a test.

   The invocation index and the data-provider arguments are dropped, so `m[3](arg)` counts as `m`.
   The counts are then divided by their greatest common divisor. Gradle starts one test fork per two
   CPUs and each fork runs the whole suite, so a raw count is the real count times the fork count.
   That number changes with the machine. Dividing removes it.

Check 3 is the one that catches a silent loss during a move. A class dropped from both the suite and
the code passes checks 1 and 2.

New tests are not guarded until the file is regenerated, and a contributor has no reason to know it
exists. `.github/workflows/refresh-execution-inventory.yml` regenerates it when test sources change
on master, and opens a pull request if it moved.

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

1. **Take the scaffolding out of the tree.** `docs/TEST_RELOCATION_PLAN.md` and
   `docs/test-issue-references.md` carry the plan and the evidence while the phases run. Delete them
   at the end.

   `scripts/verify-issue-refs.sh` goes too. Guessing an issue number from a package name is a
   bulk-conversion mistake, and bulk conversion ends with phase 8. After that the person adding a
   test knows the issue number, and review catches the rest -- it already did, finding all six wrong
   references and only those six.

   Its tests, `scripts/test/verify-issue-refs-test.sh`, and the workflow that runs them,
   `.github/workflows/scripts.yml`, go with it.

   `verifyTestExecution` is **not** scaffolding. It is the guard that makes GitHub issue #3446's
   central promise hold, and it matters more once the suite XML is gone. It stays.

   | File | Lifetime |
   | --- | --- |
   | `docs/TEST_RELOCATION_PLAN.md` | delete after phase 8 |
   | `docs/test-issue-references.md` | delete after phase 8 |
   | `scripts/verify-issue-refs.sh` | delete after phase 8 |
   | `scripts/test/verify-issue-refs-test.sh` | delete after phase 8 |
   | `.github/workflows/scripts.yml` | delete after phase 8 |
   | `testng-core/execution-inventory.txt` | **permanent** |
   | `testng-core/execution-known-silent.txt` | shrinks to two entries, then see below |

   `execution-inventory.txt` is not a migration artefact and does not go away. It matters more once
   the suite XML is deleted, not less: under classpath discovery a test can stop being discovered —
   renamed out of the pattern, moved into a `samples` package by mistake — and nothing else in the
   build would notice.

   `execution-known-silent.txt` cannot reach zero as written. Nine of its eleven entries are
   defects and should be fixed and deleted. The other two, `test.SerializationTest` and
   `test.thread.ThreadTest`, are in group `broken` which their `<test>` block excludes on purpose,
   so "named but never runs" is correct behaviour for them. Either the file keeps those two
   forever, or `verifyTestExecution` learns to read group filters and the file goes entirely. The
   second is better and is not hard; it was left out here to keep this PR to one subject.
2. **Decide what happens to `test.test111`.** It is the same shape as the packages this work
   removed, but it does not match `testng<number>` so it was never in scope. Nothing else in the
   tree is named that way now.

**The 452 `GITHUB-*` descriptions elsewhere in the tree are deliberately not audited.** Every
description checked so far that someone else wrote was correct -- 165, 182, 990, 1834, 1880, 2152
and 2232, seven for seven. The six wrong ones were all introduced by this migration, by inference
from package names. There is no evidence of a wider problem, and the tool that would audit them is
deleted at the end, so this is a decision rather than a deferred task.
