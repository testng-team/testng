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

## Verified, description not yet written

A row above is a claim that the code carries that description. These five are the exception. Phase 1
proved them by sweeping every class in reach, but their classes still sit in top-level `test.*`
packages that no phase has moved. The description goes in when the owning feature moves.

<!-- vale off -->

| Ref | Class still at |
| --- | --- |
| `GITHUB-565` | `test.issue565` |
| `GITHUB-799` | `test.github799` |
| `GITHUB-1231` | `test.testng1231` |
| `GITHUB-1232` | `test.testng1232` |
| `GITHUB-1490` | `test.github1490` |

<!-- vale on -->

`scripts/refs-in-sync.sh` in the phase skill reads this list. Move a reference out of it when the
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
