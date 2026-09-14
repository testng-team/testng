# Verified issue references

Every `@Test(description = "GITHUB-<n>")` this reorganization relies on, and the evidence behind it.
Regenerate with `scripts/verify-issue-refs.sh`.

`GITHUB-182` and `GITHUB-1496` were found by running the check over **every** class in a phase's
scope rather than only the ones that already carried a description. Both tests had none and both
deserved one. `GITHUB-1461` was verified in an earlier round and then lost when this branch was
rebuilt from master; the same sweep caught that too.

## The rule

A reference is written only when **both** ends check out:

1. **Provenance** — the commit that introduced the test names the issue, or the merge that brought
   it in does. A pull request number does not count. Pull requests and issues share one number
   space, so "Merge pull request #765" and "Some fix (#765)" say nothing about issue #765.
2. **The issue exists and fits** — `#<n>` on GitHub is a real *issue* and not a pull request, and
   its subject is what the test asserts. The state is reported, never enforced; a regression test
   may reference an issue that is still open.

Package names are not evidence. `test.testng173` and `test.testng317` look identical; one is a
GitHub issue and the other is nothing.

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
same weight as the rest.

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

## Verified, description not yet written

A row above is a claim that the code carries that description. The rows below are the exception.
Phase 1 proved them by sweeping every class in reach. Their classes still sit in top-level `test.*`
packages that no phase has moved. The description goes in when the owning feature moves.

<!-- vale off -->

| Ref | Class still at |
| --- | --- |
| `GITHUB-565` | `test.issue565` |
| `GITHUB-1231` | `test.testng1231` |
| `GITHUB-1232` | `test.testng1232` |
| `GITHUB-1490` | `test.github1490` |

<!-- vale on -->

`scripts/refs-in-sync.sh` reads this list. Move a reference out of it when the
description is written, and the check starts requiring it.

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
