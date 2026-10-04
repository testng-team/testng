# Verified issue references

Every `@Test(description = "GITHUB-<n>")` this reorganization relies on, and the evidence behind it.
Regenerate with `scripts/verify-issue-refs.sh`.

`GITHUB-182` and `GITHUB-1496` were found by running the check over **every** class in a phase's
scope rather than only the ones that already carried a description. Both tests had none and both
deserved one. `GITHUB-1461` was verified in an earlier round and then lost when this branch was
rebuilt from master; the same sweep caught that too.

## The rule

A reference is written only when **both** ends check out:

1. **Provenance** — the commit that introduced the test names the issue, or a pull request that
   GitHub lists for that commit does. A pull request number does not count. Pull requests and issues
   share one number space, so "Merge pull request #765" and "Some fix (#765)" say nothing about issue
   #765.
2. **The issue exists and fits** — `#<n>` on GitHub is a real *issue* and not a pull request, and
   its subject is what the test asserts. The state is reported, never enforced; a regression test
   may reference an issue that is still open.

   A number that no issue and no pull request holds may still name a **discussion**, and that
   counts. GitHub gives discussions a number space of their own, so the script asks about one only
   after the issue lookup answers 404, which it does for a pull request too. The row then says
   `discussion:` in front of the title. Phase 8 records `GITHUB-2916` that way.

Package names are not evidence. `test.testng173` and `test.testng317` look identical; one is a
GitHub issue and the other is nothing.

### The one exception

A reference can stay, or go on a method beside others that already carry it, when rule 2 holds and
rule 1 fails. Three things must be true:

1. The phase section names every method that carries it.
2. It names the commit that added each method, and says what that commit says about the issue.
3. It says why the issue is what those methods assert, and quotes the words that show it.

A maintainer decides each case, in the pull request. The script never reports such a reference as
proven, and the section says which rule failed. `GITHUB-2110` in phase 6 and `GITHUB-2432` in phase 7
are the two of these.

Where GitHub's own timeline for the issue links the introducing commit, that is recorded as
`timeline` below — the issue itself points at the code, which is as strong as this gets.

Both columns quote other people's words. The title is the issue title on GitHub. The provenance is
the introducing commit, subject first, then the line of its body that names the issue. Quote them
as they are written. Do not correct the spelling, the grammar or the tense to suit this repository.
That is why Vale is turned off over the two tables and nowhere else in this file.

## Verified in phase 1

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-182` | Inherited test methods do not get expected group behavior | "Inherited test methods do not inherit groups. Closes #182" | links commit |
| `GITHUB-1496` | If method contains "$", run only one method, all methods will be run. | "Add test case for #1496" | links commit |
| `GITHUB-173` | Dependent methods executed out-of-order if method names match across classes | "Fix for the issue #173" | links commit |
| `GITHUB-565` | Deadlock when using group dependency (plus other factors) | "Add test for #565" | links commit |
| `GITHUB-674` | TestNG is not reporting any log for skip tests. | "Inject config failure data into test results. Fixes #674" | links commit |
| `GITHUB-799` | @Factory with dataProvider changes order of iterations | "…Closes #799" | links commit |
| `GITHUB-1231` | Swap invocation order between IExecutionListener implementation and report generation. | "…Fixes #1231" | links commit |
| `GITHUB-1232` | Prevent TestNG from adding duplicate instances of the same listener | "Ensure unique listener injection in TestNG. Fixes #1232" | links commit |
| `GITHUB-1336` | Parallel test (parallel='tests') does not work when priority is used in Test | "…Fixes #1336" | links commit |
| `GITHUB-1362` | AfterGroups does not get executed when MethodInterceptor is involved. | "Invoke AfterGroups when involving MethodInterceptors. Closes #1362" | links commit |
| `GITHUB-1396` | Order established by IMethodInterceptor not honored when running with parallel='instances' | "Fix https://github.com/cbeust/testng/issues/1396" | links commit |
| `GITHUB-1430` | Cannot load class from file  XXX when using with ant and classfileset | "Issue #1430 : Fix loading class from file with ant and classfileset" | links commit |
| `GITHUB-1461` | Memory leak (TestNG seems to keep all test object in memory) | "Add test case for #1461" | links commit |
| `GITHUB-1490` | Add a listener for data provider interception | "…Closes #1490" | links commit |
| `GITHUB-765` | Test invoked twice when implements abstract method from parameterized parent. | branch `krmahadevan-fix-765`, merged by PR #1374 | links PR 1374 |
| `GITHUB-1417` | Class param injection is not working with @BeforeClass | branch `krmahadevan-fix-1417`, merged by PR #1447 | links PR 1447 |
| `GITHUB-107` | TestNG printout wrong statistic number | "Improve Issue 107 test, add it to testng.xml" | **no link** |

<!-- vale on -->

Every one is an issue rather than a pull request. All happen to be closed. The check reports the state but does not require it.

`GITHUB-107` is the weakest of the set: issue #107 was closed by hand in 2011 and its timeline links
no commit or PR at all. It rests on the commit saying "Issue 107" in words and on the issue title
matching what the test asserts — it counts passed tests. It is also **not something this work
added**: the description predates it. Left as it stands; flagged so nobody assumes it carries the
same weight as the rest. Phase 8 finds a stronger proof for it, under "Verified in phase 8", so
read that row beside this one.

## Verified in phase 2

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance |
| --- | --- | --- |
| `GITHUB-392` | AfterClass method isn't fired when an IMethodInterceptor removes test methods | "Fix #392" |
| `GITHUB-521` | If test methods are filtered using IMethodInterceptor, beforeclass method is not executed | "Add test case for #521" |
| `GITHUB-1480` | Parallel=methods not working when tests have different priorities set | "Fix #1480: Priority/parallel=methods issue. (#1910)" |
| `GITHUB-1632` | throwing SkipException sets iTestResult status to Failure instead of Skip | "Streamline skipped test results in listeners. Closes #1632" |
| `GITHUB-1726` | Need a way to exclude built-in interceptors from being added to alter method order | "Re-order to ensure built-in interceptor added first. Closes #1726" |
| `GITHUB-3437` | ClassHelper.getAvailableMethods is uncached and re-derived per lookup, including once per data-provider invocation | "perf: cache ClassHelper.getAvailableMethods per class" |

<!-- vale on -->

`GITHUB-173`, `GITHUB-674`, `GITHUB-765`, `GITHUB-1336`, `GITHUB-1396` and `GITHUB-1430` also belong
to phase 2 packages. Phase 1 verified them and the table above records them already.

## Verified in phase 3

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-426` | firstTimeOnly ignored for threadPoolSize > 1 | "firstTimeOnly ignored for threadPoolSize > 1. Closes #426" | links commit |
| `GITHUB-564` | Optional always requires Parameters is this really needed? | "Support using @Optional without needing @Parameters. Closes #564" | links commit |
| `GITHUB-581` | Parameters of nested test suites are overriden | "Dont honour params specified in suite-file tag. Closes #581" | links commit |
| `GITHUB-949` | dependsOnMethods with alwaysRun = true and inheritance fails to find method | "Fix for Github-949. Closes #949" | links commit |
| `GITHUB-980` | TestNG run inherited method twice | "TestNG run inherited method twice. Closes #980" | links commit |
| `GITHUB-1061` | Feature request: Have a way to change timeout dynamically at runtime | "Ensure TestResult contains proper test method. Closes #1061" | links commit |
| `GITHUB-1105` | Test skipped instead failed if incorrect enum value is passed as parameter in testNG xml | "Fix #1105 Test skipped instead failed if incorrect enum value" | links commit |
| `GITHUB-1554` | @Parameters and parameter injection not wroking when used on the same method in version 6.12 | "Cant use @Parameters and parameter injection on the same method. Closes #1554" | links commit |
| `GITHUB-1719` | successPercentage does not work correctly for tests with dataProvider | "Streamline success%age & Data driven method combo. Closes #1719" | links commit |
| `GITHUB-2238` | Parameter values should be overridable from JVM arguments | "Streamline parameter initialization. Closes #2238" | links commit |
| `GITHUB-2489` | Hierarchical base- and test-class @AfterClass methods out of order using groups | "Fix Config invocation order for inheritance (#2503). Closes #2489" | links commit |
| `GITHUB-3180` | TestNG testng-failed.xml 'invocation-numbers' values are not calculated correctly with retry and dataproviders | "Streamline invocation numbers in failed xml file. Closes #3180" | links commit |

<!-- vale on -->

`GITHUB-1417` also belongs to a phase 3 package. Phase 1 verified it and the table above records it.

Eight of these descriptions were already in the code. Five are new: `GITHUB-949`, `GITHUB-980`,
`GITHUB-1417`, `GITHUB-1719` and `GITHUB-2238`. Each names a registered test that carried no
description.

Phase 3 also removes one. `CancelledInvocationReportingTest` described two methods as
`GITHUB-3408`. GitHub #3408 is a pull request, not an issue. The commit that wrote those
descriptions names no issue at all, and no commit ties that test to an issue. The prefix is gone
and the prose stays.

`TESTNG-37`, `TESTNG-57` and `TESTNG-387` name the old JIRA tracker. They are left as they are.

## Verified in phase 4

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-141` | regular expression in "dependsOnMethods" not work | "Honour regex in dependsOnMethods. Closes: #141" | links commit |
| `GITHUB-550` | Weird @BeforeMethod and @AfterMethod behaviour with dependsOnMethods | "Streamline dependsOnMethods for configurations. Closes #550" | links commit |
| `GITHUB-893` | TestNG should provide an Api which allow to find all dependent of a specific test | "Support getting dependencies info for a test. Closes #893" | links commit |
| `GITHUB-1156` | test execution dependant upon class name order and fails with TestNGException:  No free nodes found | "Detect circular dependencies asap #1156" | links commit |
| `GITHUB-1380` | DynamicGraph should manage transitive dependencies | "Add test cases for #1380" | links commit |
| `GITHUB-1648` | Depends on method is not respected on the sequential run on second test that extends same base testClass | "DependsOnMethods not respected on the sequential run on 2nd test with same base testClass. Closes #1648" | links commit |
| `GITHUB-2658` | Inheritance + dependsOnMethods | "Streamline Inheritance + dependsOnMethods. Closes #2658" | links commit |
| `GITHUB-3222` | Failing to detect test dependsOn methods in static nested class | "fix(depends): resolve dependsOnMethods in static nested classes. Fixes #3222" | links commit |

<!-- vale on -->

Every one was already in the code. Phase 4 adds no new reference. The 19 registered classes in
`test.dependent` that carry no description have introducing commits that name no issue at all, so
there is nothing to prove.

Phase 4 brings three tests back into the suite instead. None had run for years.

- `DependsOnMethodsWithSharedNamesTest`, which was `test.testng317.VerifyTest`, was in no suite file
  and asserted nothing. It printed a count. It now asserts that `dependsOnMethods` finds the method
  in its own class when another class in the run declares one of the same name.
- `MissingGroupTest` and `MissingMethodTest` were commented out in `testng.xml`. Both expected a
  skip. TestNG refuses the run instead, which is what their own method names say. Their bodies had
  drifted from their names, and both now assert the exception.

`test.testng317` is renamed rather than kept. GitHub #317 is a pull request, so the number points at
nothing a reader can open.

## Verified in phase 5

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-876` | NullPointerException creating tests with parameters by a factory | "Add test case for #876: NPE when a factory method is not static" | links commit |
| `GITHUB-1030` | Parameterized test class crashes when data provider returns empty array | "#1030 Add test cases for empty data provider and factory" | links commit |
| `GITHUB-1083` | Having indices on Factory | "Fix #1083 Factory supports indices" | links commit |
| `GITHUB-1131` | IObjectFactory not being called for factory test instances with constructor-injected data provider | "#1131 Step1: Fix inconsistency between factory constructor" | links commit |
| `GITHUB-1307` | TestNGException when using an anonymous class in Factory | pull request #1308: "Fixes #1307" | **no link** |
| `GITHUB-1631` | [Feature Request] Implicitly inject dataProviderClass into Factory meta-data | "Data provider class injection into Factory meta-data.. There was no way to extract data provider class name from IAnnotationTransformer2 listener's meta-data, used for constructor-level Factory interception with missing dataProviderClass arg. See #1631 for details." | links commit |
| `GITHUB-1745` | java.lang.IllegalArgumentException: wrong number of arguments | "Support native injection for @Factory methods.. Closes #1745" | links commit |
| `GITHUB-1924` | Unable to run testcases with testng 7.0.0-beta1 in eclipse / maven test | "Streamline test-class instantiation. Closes #1924" | links commit |
| `GITHUB-1041` | Factory dataprovider parameters not displayed in testresult | "Support params in Factory in ITestResult. Closes #1041" | links commit |
| `GITHUB-2428` | Configuration methods have the same test class instance when @Factory is being used | "Fix #2428 Configuration methods have the same test class instance when @Factory is being used" | links commit |
| `GITHUB-3344` | Feature Request: Lazy (just-in-time) instantiation for `@Factory` powered test classes | "feat(factory): lazy (just-in-time) instantiation for @Factory powered tests (#3345). Closes #3344" | links commit |
| `GITHUB-799` | @Factory with dataProvider changes order of iterations | "@Factory with dataProvider changes order of iterations. Closes #799" | links commit |

<!-- vale on -->

`GITHUB-1307` is the one to read twice. Neither its commit nor its merge names the issue. The merge
says "Merge pull request #1308 from michaelcowan/feature/ignore-anonymous-tests". The proof sits in
the body of pull request #1308, which says "Fixes #1307", and that pull request holds exactly the
commit that added the test. `scripts/verify-issue-refs.sh` now reads the pull request body as a
third source of provenance. Only a GitHub closing word counts. A body that says "see #1307" is a
mention and proves nothing.

Five references were already in the code and are re-proven here: `GITHUB-326`, `GITHUB-553`,
`GITHUB-1770`, `GITHUB-1953` and `GITHUB-3111`. `GITHUB-3079` is re-proven from its commit.
`GITHUB-876` was written as a full GitHub URL and now uses the same `GITHUB-<n>` form as the rest.

`test.factory.github328` is renamed and gets no description. GitHub #328 is a pull request, and its
own author wrote on it that the change was not acceptable. The commit that added the test says "Add
test case for #328", which is that pull request. The test asserts that a factory does not run when
its group is excluded, so it is `FactoryWithExcludedGroupTest` now. Phase 4 renamed `test.testng317`
for the same reason.

Phase 5 also brings two tests into the suite. `GitHub1083Test` and `GitHub1131Test` are ordinary
tests that were in no suite file, so neither had ever run. Both pass, and both are registered now.

Two samples go the other way. `NestedFactorySample` and `NestedStaticFactorySample` hold a `@Test`
and nothing in the tree names them, so TestNG never sees them. Commit `d8a8caa09` orphaned them in
2016. Their comments are identical and say "Should have three instances", yet one asserts three and
the other asserts two, so at least one is wrong. Moving them under `samples` would make that
permanent and invisible, so phase 5 deletes them. `leftovers.sh` now reports this shape, because the
suspect sweep in the plan only looks at classes named `*Test`.

## Verified in phase 6

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-1066` | Regression is in priority. It broke parallel mode | "Add test for #1066: Priority does not honor single threaded class" | **no link** |
| `GITHUB-188` | suite parallel="methods" does not work when there are multiple &lt;test&gt; tags in the testng.xml | "Support parallelism at suite level. Closes #188" | links commit |
| `GITHUB-2361` | No way to enforce @Test(singleThreaded = true) when test defined in base class | "Streamline honoring of “singleThreaded” attribute. Closes #2361" | links commit |
| `GITHUB-1636` | Parallel test run is not working in 6.13.1 | "Parallel test run is not working in 6.13.1. Closes #1636" | links commit |
| `GITHUB-2532` | -parallel -threadcount CLI switches has no effect on test jar | "Unit tests for #2532" | **no link** |
| `GITHUB-2321` | -Dtestng.thread.affinity=true do not work when running multiple instance of test in parallel | branch `github-2321`, in PR #2368 | **no link** |
| `GITHUB-2110` | NPE is thrown when running with Thread affinity | "Fix NPE in Thread affinity mode. Closes #2110" | links commit |

<!-- vale on -->

Two of these needed the tool to change first, and both are worth reading twice.

`GITHUB-2532` and `GITHUB-1636` sit on methods of `ParallelTestTest`, a file from a 2006 commit that
names neither issue. The script used to judge only the commit that added a file, so it refused both.
`METHOD=<name>` now judges the commit that added that one method. One method came from the #1636 fix,
and two from "Unit tests for #2532".

`GITHUB-2321` was refused because the script picked the wrong pull request. Its method's commit,
`839a01980`, reached master through pull request #2368, which was rebase-merged. With no merge commit
for #2368, the oldest merge after the commit was #2375, a CVE fix. The script now asks GitHub which
pull request holds a commit, and GitHub names #2368, whose branch is `github-2321`.

No earlier row was wrong. The three rows proven through a merge or a branch -- `GITHUB-765`,
`GITHUB-1417` and `GITHUB-1307` -- came from pull requests merged with a merge commit, and GitHub
names the same pull request for each.

Four references already in the code are re-proven: `GITHUB-3066`, `GITHUB-2019`, `GITHUB-3242` and
`GITHUB-3179`.

`GITHUB-2110` on `ThreadAffinityTest#ensureNoNPEIsThrown` falls under "The one exception" above. It
is proven through the file the test came from. `METHOD` mode answers with `839a01980`, in pull request #2368, which closes only #2321. That
commit did not write the test. It deleted `test/thread/parallelization/issue2110/IssueTest.java` and
moved the only test of that file into `ThreadAffinityTest`, under a new name. The script follows the
history of one file, so it cannot see a method move from one file to another.

The file the test came from is proven by the script:

    PROVENANCE_ONLY=1 scripts/verify-issue-refs.sh parallelization/issue2110/IssueTest.java 2110

It prints `introduced  da1fa3317` and `provenance  the introducing commit names it: #2110`, and it
exits 0. The row above is the output of `evidence-row.sh` for that same file. The moved test runs the
same suite with the same settings, and it asserts the same thing. Compare the two versions:

    git show da1fa3317:src/test/java/test/thread/parallelization/issue2110/IssueTest.java
    git show 839a01980:src/test/java/test/thread/parallelization/ThreadAffinityTest.java

Phase 6 also brings three tests into the suite. None of them had ever run.

`SingleThreadForParallelMethodsTest` was written in 2016 for #1066 and sat in no suite file. It is
registered now and it passes.

`TestThreadCountTest` and `SuiteThreadCountTest` assert a thread count, which holds only under the
settings of their own suite file. That file sat inside the Java source tree, where nothing loaded it.
It moves to `src/test/resources/concurrency/thread-count.xml`, and `ThreadCountSuiteTest` runs it. The
assertions sit in `@AfterClass`, so a failure there counts as a configuration failure, not a failed
test. The driver checks configuration failures as well, or a wrong count would pass unseen.

No description is written for `GITHUB-3028`. Its samples came from a commit that names it, but the one
test that runs them came from pull request #3289, which closes only #3242. `GITHUB-1773` is refused:
its package name is the only thing that names it.

## Verified in phase 7

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-1753` | TestResult for an SKIP test lose attributes contributed by @BeforeMethod's or @AfterMethod's | "Streamline TestResult sharing in Native Injection. Closes #1753" | links commit |
| `GITHUB-1622` | BUG: Parameter alwaysRun=true for before-methods forces execution of those methods | "fix(config): stop alwaysRun on @Before* from bypassing a failure. Fix #1622" | links commit |
| `GITHUB-2209` |  @Before and @After are not executed as expected when a combination of class and method level groupping is applied | "Streamline config invocation when coupled with groups. Closes #2209" | links commit |
| `GITHUB-1625` | Null fields in parallel method tests | "Null fields in parallel method tests. Closes #1625" | links commit |
| `GITHUB-2426` | New feature TestNG - getFactoryMethodParamsInfo on ConfigurationMethod | "Expose Factory params on config methods. Closes #2426" | links commit |
| `GITHUB-1338` | BeforeClass doesn't work when BeforeGroup run on Class with excluded tests | "Add test case for #1338" | links commit |
| `GITHUB-2729` | beforeConfiguration() listener method should be invoked for skipped configurations as well | "beforeConfiguration() listener method should be invoked for skipped configurations as well Fixes #2729" | links commit |
| `GITHUB-3239` | @BeforeClass methods in base class with dependsOnGroups and groups are not executed in the expected order. | pull request #3471: "Fixes #3239" | **no link** |
| `GITHUB-2714` | dependsOnGroups on @AfterMethod seems to behave incorrectly | pull request #3471: "Fixes #2714" | **no link** |
| `GITHUB-549` | Groups in @BeforeMethod and @AfterMethod don't work as expected | pull request #1738: "fixes #549" | **no link** |
| `GITHUB-549` | Groups in @BeforeMethod and @AfterMethod don't work as expected | pull request #1741: "Fixes #549" | **no link** |
| `GITHUB-3435` | A @Factory with @BeforeMethod/@AfterMethod runs quadratically in the instance count | pull request #3535: "Fixes #3435" | **no link** |
| `GITHUB-3539` | Scope pooled firstTimeOnly/lastTimeOnly configurations when instance id is unavailable | pull request #3541: "Fixes #3539." | **no link** |

<!-- vale on -->

The commit that added each method proves its row, with `METHOD=<name>`. The code already carries
these references, and the script proves them the same way:

- `GITHUB-1035`, `GITHUB-1346` and `GITHUB-1700`
- `GITHUB-2400`, `GITHUB-2432` on `issue2432.IssueTest`, `GITHUB-2663` and `GITHUB-2664`
- `GITHUB-2726`, `GITHUB-2743` and `GITHUB-2961`
- `GITHUB-3000`, `GITHUB-3003` and `GITHUB-3006`
- `GITHUB-3358` and `GITHUB-3359`

The sweep over methods with no description writes three references. The script proves each one, and
each issue asks for what its method asserts:

- `GITHUB-3239`, on `beforeClassInheritanceSurvivesUnrelatedChildGroups`,
  `alignedHardDependencyKeepsInheritanceOrder` and
  `regexpDependsOnGroupsUsesSameMatchingAsExecutionGraph`, in `issue3239.IssueTest`. Each one pins the
  order of an inherited configuration method when groups are in play. Pull request #3471 closes #3239.
- `GITHUB-2714`, on `issue3239.IssueTest#afterMethodInheritanceSurvivesUnrelatedChildGroups`. The same
  pull request closes #2714. That method drives the same samples as its two neighbours, and they
  already carry `GITHUB-2714`.
- `GITHUB-549`, on `BeforeMethodWithGroupFiltersTest` and `AfterMethodWithGroupFiltersTest`. Pull
  request #1738 added the first method, and #1741 added the second. Each one says it fixes #549.

`GroupsTest#verifyIteratorDataProviderAfterGroups` gets no reference. The script proves `GITHUB-1009`
for it, through the branch of pull request #1065. The issue asks for `indices` on an `Iterator` data
provider. The method asserts the order of `@BeforeGroups` and `@AfterGroups` around such a provider,
and it touches no index. Rule 2 is what decides, so the reference is not written.

`GITHUB-3435` and `GITHUB-3539` came in with the rebase onto master. Master added
`issue3435.IssueTest` and `issue3539.IssueTest` to `test.configuration` while this phase was open, and
both already carried their reference. They move with the rest of the feature, and the script proves
each one from the commit that added its file.

`GITHUB-1338` and `GITHUB-2729` were already in the code, written as a URL and as `github 2729`. They
now use the same `GITHUB-<n>` form as the rest.

Each method of `issue1753.IssueTest` now carries the issue its own commit names:

- `testToEnsureProperTestResultIsReferredInNativeInjection` came with the fix for #1753. It carries
  `GITHUB-1753`.
- `testToEnsureAFailingParentConfigurationStillContributesItsAttributes` came with the fix for #1622,
  in pull request #3453. It carries `GITHUB-1622`. It runs the case that #1753 first reported, in
  which the parent `@BeforeMethod` fails. The fix for #1622 changed the samples of the other method, so
  that its parent passes and its child fails.

`issue2254.IssueTest` now carries `GITHUB-2209`, not `GITHUB-2254`. The commit that added it closes
#2209. The maintainer closed #2254 as a duplicate of #2209, in a comment on #2254.

Seven methods of `issue3239.IssueTest` carry `GITHUB-2432` as a recorded exception:

- `inheritanceEdgeDoesNotCycleWhenAgnosticMethodIsTransitivelyUpstream`, added by `f90a16bd2`
- `inheritanceEdgeDoesNotCycleForAfterClass`, added by `eff8d53b1`
- `inheritanceEdgeDoesNotCycleOnPureDependsOnGroupsChain`, added by `eff8d53b1`
- `twoHierarchiesDoNotFormACycleOnBeforeSuite`, added by `eff8d53b1`
- `twoHierarchiesDoNotFormACycleOnAfterSuite`, added by `a5bb1290d`
- `threeLevelInheritanceSelectivelyRejectsCyclingEdges`, added by `a5bb1290d`
- `oppositeHardDependencyWinsWithoutCycle`, added by `a5bb1290d`

Each one asserts that an inheritance edge gives way. Six check that TestNG adds no edge that would
close a cycle. The seventh checks that an explicit `dependsOnMethods` wins over the inherited order.
That is what #2432 asks for: "Rework MethodInheritance.fixMethodInheritance to \"soft\" dependencies".
Pull request #3471 says the same in its own words: the edge is left out when it would close a cycle,
"so a group pipeline such as GITHUB-2432 still wins".

The script proves `GITHUB-2432` on none of them. `f90a16bd2` names the issue only as "GITHUB-2432",
and the script does not read that form as proof. This project's commits use it to label the
descriptions they write, so a commit would prove its own label. `eff8d53b1` and `a5bb1290d` do not
name the issue at all. Rule 1 fails and rule 2 holds. "The one exception" above covers this, and the
maintainer asked for these seven in pull request #3536.

These methods get no reference:

- `BeforeClassTest#beforeClassMethodsShouldRunInParallel` and
  `BeforeClassTest#afterClassShouldRunEvenWithDisabledMethods`. They moved into the class with the fix
  for #1035, but they are older. The first came from a 2009 test. The second came from a 2011 fix that
  names no issue.
- `ConfigurationGroupsTest#multipleBeforeGroupTest` and `ConfigurationGroupsTest#runTest`. They came
  with the fix for #2152, but they only drive samples from 2006 and 2007.
- `ConfigurationTest#testConfiguration`, `ConfigurationTest#testMethodCallOrder` and
  `ConfigurationTest#testSuite`. The commit that added them names only "(#2626)", and #2626 is a pull
  request.
- `issue3239.IssueTest#missingDependsOnMethodsStillFailsFromInheritanceWalk` and
  `issue3239.IssueTest#missingDependsOnGroupsKeepsExistingErrorBehavior`. Pull request #3471 proves
  `GITHUB-3239` for both. Each one guards the error TestNG already reported for a dependency that does
  not exist, which is not what #3239 asks for.

`OnlyOnceConfigurationTest` and `ParentTestClass`, under `issue2961`, move to `samples` like any other
class that no suite file names. They came with the fix for #2961 in 2023, beside the samples in
`org.testng.listeners.samples.issue2961` that `ConfigurationTest` runs. Nothing runs them, and
`OnlyOnceConfigurationTest` asserts nothing.

## Verified in phase 8

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-107` | TestNG printout wrong statistic number | branch `issue-107-test`, in PR #308 | **no link** |
| `GITHUB-154` | MethodInterceptor will be called twice in test ng 6.3.1 | "Add test case for #154" | links commit |
| `GITHUB-169` | beforeInvocation and afterInvocation methods (IInvokedMethodListener) executed several times before/after each test method | "Add test case for #169" | links commit |
| `GITHUB-171` | ISuiteListener.onStart method called mulitple times if i have multiple test elements in xml configuration file | "Add test case for #171" | links commit |
| `GITHUB-551` | BeforeMethod has wrong value of endTime | "Add test for github issue #551" | **no link** |
| `GITHUB-767` | different behavior depending if Listener is declared with @Listeners annotation or in the suite XML file. | "Add test case for #767" | links commit |
| `GITHUB-776` | Add BeforeClass/AfterClass like on ITestListener | "Fix #776 Add IClassListener, a @BeforeClass/@AfterClass alternative" | links commit |
| `GITHUB-795` | "@Listeners" defined listeners applies to whole suite even if repeated. | "Add test case for #795" | links commit |
| `GITHUB-895` | Changing status of test by setStatus of ITestResult | "Add test case for #895 Changing status of test by setStatus of ITestResult From a sample provided by @rajsrivastav1919" | links commit |
| `GITHUB-911` | If a configuration fails, the onTestStart method of a TestListener is invoked once | "Fix #911: TestListener#onTestStart should not be invoked if a suite configuration method fails" | links commit |
| `GITHUB-1029` | Issue with getting XmlTest from test method | "XmlTest is null when read via IInvokedMethodListener. Closes #1029" | links commit |
| `GITHUB-1084` | testng 6.9.12 start to post 3 org.testng.ITestListener#onTestStart events for one test method | "Fix #1084 Using deprecated addListener methods should not register any times" | links commit |
| `GITHUB-1130` | Listener in multi-test context | "Fix #1130 IClassListener should only be instantiated once" | links commit |
| `GITHUB-1284` | Listeners on the child suites are not applied | pull request #1285: "Fixes https://github.com/cbeust/testng/issues/1284" | **no link** |
| `GITHUB-1296` | Listeners method run multiple times - testng 6.10 | "Add test case for #1296" | links commit |
| `GITHUB-1319` | ITestResult#getInstance() method returns null for IConfigurationListener | "Ensure instance is not null in IConfigurationListener implementations. Fixes #1319" | links commit |
| `GITHUB-1393` | How to fail a test from onTestStart method | "#1393: add test case" | links commit |
| `GITHUB-1465` | Version 6.11 - Failure policy CONTINUE handling is broken for tests that are skipped in @BeforeMethod method | "IInvokedMethodListener implementation called for skipped methods. Closes #1465" | links commit |
| `GITHUB-1602` | beforeInvocation method don't get called for skipped tests | "Streamline listener invocation with config methods. Closes #1602" | links commit |
| `GITHUB-1735` | IExecutionListener.onStart() running twice when used as annotation in mediumSuite file | "IExecutionListener.onStart() running multiple times. Closes #1735" | links commit |
| `GITHUB-1777` | ITestListener.onTestStart() not called after fail or skip from @BeforeMethod | "onTestStart() not called for skipped methods. Closes #1777" | links commit |
| `GITHUB-1863` | IMethodInterceptor will be invoked twice when listener implements both ITestListener and IMethodInterceptor via eclipse execution way. | "Fix #1863 patchset3: remove the blank methods since default methods already implemented from org.testng.ITestListener patchset2: fix the comments from juherr" | links commit |
| `GITHUB-1952` | Provide a TestNGListener that can be invoked when a test fails due to a timeout | "TestNGListener for test fails due to a timeout. Closes #1952" | links commit |
| `GITHUB-2043` | IConfigurationListener is not executed and IDataProviderListener is not added to the list of the listeners if a new listener is added at the runtime | "Streamline dynamic listener injection. Closes #2043" | links commit |
| `GITHUB-2055` | It's not possible to register a new ITestListener at the runtime | "Streamline dynamic ITestListener injection. Closes #2055" | links commit |
| `GITHUB-2061` | java.util.ConcurrentModificationException after registration of SuiteListener at the runtime | "Avoid ConcurrentModificationException. Closes #2061" | links commit |
| `GITHUB-2220` | ITestListener's methods get called multiple times for one test, when @Listeners annotation is used in multiple test classes | "Prevent duplicate wiring in of listeners. Closes #2220" | links commit |
| `GITHUB-2328` | Add ability to get test method for which configuration method was called | "Make ConfigListeners aware of Test Methods. Closes #2328" | links commit |
| `GITHUB-2381` | [FR] Controlling the inclusion of the listener at runtime | "Allow listeners to be disabled at runtime. Closes #2381" | links commit |
| `GITHUB-2385` | [BUG] @Listeners doesn't work for interfaces | "Features #2385 @Listeners doesn't work for interfaces" | links commit |
| `GITHUB-2456` | [Feature request] Add onDataProviderFailure listener? | "Add onFailure support for DataProvider Listener. Closes #2456" | links commit |
| `GITHUB-2469` | Parameters added in XmlTest during AlterSuiteListener not available in SuiteListener | "Issue #2469 (#2470)" | links commit |
| `GITHUB-2522` | TestNG 7.4.0  Can not skip test through listener | branch `github2522`, in PR #2528 | **no link** |
| `GITHUB-2558` | Make IExecutionListener, ITestListener, IInvokedMethodListener, IConfigurationListener, ISuiteListenerexecute in the order of insertion | "Honour Insertion order for listeners. Closes #2558" | links commit |
| `GITHUB-2578` | If testng fails to create an instance, it should say which class fails | "Task/bugfix 2578 (#3270). Closes #2578" | links commit |
| `GITHUB-2638` | "[WARN] Ignoring duplicate listener" appears when running .xml suite with &lt;listeners&gt; and &lt;suite-files&gt; | "Streamline adding listeners. Closes #2638" | links commit |
| `GITHUB-2685` | TestInvoker should clear Thread.interrupted flag before calling ITestListeners | pull request #2691: "Fixes #2685." | **no link** |
| `GITHUB-2752` | TestListener is being lost when implenting both IClassListener and ITestListener | "Wire-In listeners consistently. Closes #2752" | links commit |
| `GITHUB-2771` | After upgrading to TestNG 7.5.0, setting ITestResult.status to `FAILURE` doesn't fail the test anymore | pull request #2864: "Closes #2771" | **no link** |
| `GITHUB-2880` | before configuration and before invocation should be 'SKIP' when beforeMethod is 'skip' | "Skip config listener calls for skipped configs. Closes #2880 " | links commit |
| `GITHUB-2916` | discussion: Allow users to define ordering for TestNG listeners | "Support ordering of listeners. Closes #2916" | no timeline for a discussion |
| `GITHUB-3059` | Support the ability to inject custom listener factory | "Support ITestNGFactory customisation. Closes #3059" | links commit |
| `GITHUB-3064` | TestResult lost if failure creating RetryAnalyzer | "Copy test result attributes when unexpected failures. Closes #3064" | links commit |
| `GITHUB-3082` | IInvokedMethodListener Iinvoked method does not return correct instance during @BeforeMethod, @AfterMethod and @AfterClass | branch `fix_3082`, in PR #3089 | **no link** |
| `GITHUB-3095` | Super class annotated with ITestNGListenerFactory makes derived test class throw TestNGException on execution | "Honour inheritance when parsing listener factories. Closes #3095" | links commit |
| `GITHUB-3117` | ListenerComparator doesn't work | "Streaming working of listener comparator. Closes #3117" | links commit |
| `GITHUB-3120` | ITestNGListenerFactory is broken and never invoked | "Streamline Listener factory invocation (#3256). Closes #3120" | links commit |
| `GITHUB-3166` | Subsequent skipped configurations do not set a throwable | branch `fix-3166-skipped-config-throwable`, in PR #3276 | **no link** |
| `GITHUB-3238` | Tests never finish if helper throws exception while executing parallel tests | "fix(graph): hand the worker to the orchestrator when it completes exceptionally. Fix #3238" | links commit |

<!-- vale on -->

Every row is proven with `METHOD=<name>`, for the commit that added that one method. `ListenerTest`
comes from 2010 and carries more references than any other file in this scope, so its own first
commit answers for none of them. To count them, run `grep -oE 'GITHUB-[0-9]+'` over
`testng-core/src/test/java/org/testng/listeners/ListenerTest.java` and pipe it through `sort -u`.

Two of these needed `scripts/verify-issue-refs.sh` to learn something, and both changes come with
tests that reject the near misses:

- **`GITHUB-2916` is a discussion, not an issue.** The commit says "Support ordering of listeners.
  Closes #2916", as the row above quotes it. The branch is `feature/2916`, and the discussion is
  titled "Allow users to define ordering for TestNG listeners". GitHub keeps discussions in a number
  space of their own, so the script asks about one only after the issue lookup answers 404. That
  lookup covers a pull request as well as an issue, so a 404 means neither of those holds the
  number. The row says `discussion:` in front of
  the title.
- **`GITHUB-1284` is closed by a pull request body that links the old address.** Pull request #1285
  says "Fixes https://github.com/cbeust/testng/issues/1284". This repository was `cbeust/testng`
  until 2022, and GitHub closed the issue from that body. The script now accepts either name of this
  repository, and no other.

**`GITHUB-356` was the wrong issue.** Issue #356 is "Listener for XmlTest test start and stop".
`ListenerTest#classListenerShouldWork` and `#classListenerShouldWorkFromAnnotation` assert that
TestNG calls a class listener around `@BeforeClass` and `@AfterClass`. The commit that added both
says "Fix #776 Add IClassListener, a @BeforeClass/@AfterClass alternative", and #776 is titled
"Add BeforeClass/AfterClass like on ITestListener". Both methods carry `GITHUB-776` now.

**Three references are rewritten so the check can read them.** `ConfigurationListenerTest` said
`github 3166`, `GitHub1296Test` held the issue URL, and `issue1777.IssueTest` held the string in a
constant of its own. `refs-in-sync.sh` reads the source as text, so it sees none of those shapes.

**`GITHUB-107` gets a stronger proof than phase 1 gave it.** Phase 1 recorded it as the weakest
of the set, resting on a commit that said "Issue 107" in words. The script finds pull
request #308 for it, whose branch is `issue-107-test`. This is the second row for
`GITHUB-107`; the phase 1 section records the weaker proof and flags it. The description does not
change.

The sweep over methods with no description writes these references. Each one is proven for its own
method, and each issue asks for what the method asserts:

- `GITHUB-551` on `github551.Test551#testExecutionTimeOfFailedConfig`. Issue #551 says
  `@BeforeMethod` and the test uses a `@BeforeClass`, and the fix in pull request #1256 sets the end
  time in the shared configuration path, which both reach. `GITHUB-1130` on the second
  method of `github1130.GitHub1130Test`, and `GITHUB-1319` on
  `github1319.TestResultInstanceCheckTest#testInstances`
- `GITHUB-1284` on all three methods of `github1284.TestListeners`. Each one runs a suite file from
  `resources/listeners/github1284/`, and `github1284.xml` is the one that names a child suite
- `GITHUB-1465`, `GITHUB-1602` and `GITHUB-2220` on the one method of each `IssueTest` in those
  packages, `GITHUB-1735` on `github1735.ExecutionListenerTest`, and `GITHUB-2522` on both methods
  of `github2522.IssueTest`
- `GITHUB-2385` on every test method of `github2385.IssueTest`. Eight assert that a listener
  declared on a class or an interface reaches the test. The ninth, `testPackages`, asserts the
  opposite case: a listener declared on an interface that no test class implements must not be
  called. It also asserts the scanned sample ran, so the package name it holds in a string cannot
  go stale unnoticed.

`TESTNG-400` stays as prose in `ListenerTest`. It is a JIRA item, and `jira.opensymphony.com` is
dead, so the number points at nothing a reader can open.

## Verified for the numbered packages

GitHub issue #3551 covers the ten packages under `test.*` that are named after a tracker number.
No phase owned them, because the eight phases moved tests by feature.

<!-- vale off -->

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-565` | Deadlock when using group dependency (plus other factors) | "Add test for #565 (cherry picked from commit fddb95d)" | links commit |
| `GITHUB-111` | @BeforeClass method not executed if in parent class | pull request #112 names it in its title: "bug fixed #111" | **no link** |
| `GITHUB-1231` | Swap invocation order between IExecutionListener implementation and report generation. | "Make IExecutionListener implementation be the last reporter call before JVM exits Fixes #1231" | links commit |
| `GITHUB-1232` | Prevent TestNG from adding duplicate instances of the same listener | "Ensure unique listener injection in TestNG Fixes #1232" | links commit |
| `GITHUB-1490` | Add a listener for data provider interception | "Add a listener for data provider interception Closes #1490" | links commit |

<!-- vale on -->

`GITHUB-1231`, `GITHUB-1232` and `GITHUB-1490` were proved by phase 1 and waited in the list
below, because no phase owned their packages. This move writes them, so their rows come out of that
list. `GITHUB-1490` goes on all twelve methods of `github1490.VerifyDataProviderListener`, which is
the executable test in that package despite its name.

`GITHUB-111` is new. The package is called `test111`, not `testng111`, and the three JIRA-era
packages beside it make a package number look like weak evidence. The tool settles it: pull request
#112 is titled "bug fixed #111", and #111 is "@BeforeClass method not executed if in parent class",
which is what `test111.Test1` asserts. `test.testng195`, `test.testng249` and `test.testng285` get
no reference, for the reasons the "No reference" section gives. `test.bug90` and `test.bug92` get
none either, because the tool refuses their commits. GitHub issue #3555 covers that refusal.

`GITHUB-565` sits on `issue565.Issue565Test`, which had never run. `testng.xml` named it inside an
XML comment that read `TODO fix the random issue`, so no guard saw it: `verifyTestExecution` fails
on a class the suite names, and a commented line names nothing.

The random failure was the test's own guard. It gave every method in the inner suite 1000
milliseconds and called that "prevent real deadlock", which also made a slow run look like one. The
scenario does stall: over 1000 runs on an idle machine it took about 0.05 seconds most times, 6.6
seconds once in roughly 300, and 18.4 seconds once. The guard is now a wall clock on the whole run,
the inner suite carries no timeout, and a failure reports the threads the JVM finds blocked.

## Verified, description not yet written

A row above is a claim that the code carries that description. This list held the exception:
a reference phase 1 had proved, on a class in a top-level `test.*` package that no phase owned, so
the description waited for the owning feature to move.

**The list is empty.** GitHub issue #3551 moved the last of those packages and wrote the four
references that were waiting: `GITHUB-111`, `GITHUB-1231`, `GITHUB-1232` and `GITHUB-1490`.

<!-- vale off -->

| Ref | Class still at |
| --- | --- |

<!-- vale on -->

`scripts/refs-in-sync.sh` reads this list. Add a row only to record a reference that is proved and
whose description cannot go in yet. Take the row out when the description is written, and the check
starts requiring it.

`GITHUB-521` is the one worth reading twice. The test was written in 2015 and was in no suite file,
so it had never run. Phase 2 registers it.

Eight classes in phase 2 get no reference. No commit in their history names an issue:
`ExecutableCacheClassLoaderTest`, `InterningRegressionTest`, `ReflectionRecipesTest`,
`TestMethodMatcher`, `PreserveOrderTest`, `PriorityTest`, `MethodInterceptorTest` and
`MultipleInterceptorsTest`.

## No reference

These get **no** `description`. Their class and package names already say what they cover.

| Test | Why not |
| --- | --- |
| `testng106.TestNG106` | JIRA TESTNG-106. GitHub #106 is an unrelated issue about interleaved execution |
| `testng195.AfterMethodTest` | JIRA TESTNG-195. GitHub #195 is a pull request about ant resource collections |
| `testng249.VerifyTest` | JIRA TESTNG-249. GitHub #249 is a pull request adding `RetryAnalyzerCount.getCount` |
| `testng285.TestNG285Test` | JIRA TESTNG-285. GitHub #285 is an unrelated issue about log4testng log levels |
| `testng387.TestNG387` | JIRA TESTNG-387. GitHub #387 is an unrelated issue about `EmailableReporter2` |
| `testng317.VerifyTest` | Nothing at all. Added 2009-11-22 by a commit with an empty message |

The five JIRA items are genuine — the commits that fixed them quote their titles — but
`jira.opensymphony.com` is long dead, so a `TESTNG-<n>` in a description points at nothing a reader
can open. They are dropped rather than recorded.
