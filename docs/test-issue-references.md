# Verified issue references

Every `@Test(description = "GITHUB-<n>")` this reorganization relies on, and the evidence behind it.
Regenerate with `scripts/verify-issue-refs.sh`.

## The rule

A reference is written only when **both** ends check out:

1. **Provenance** — the commit that introduced the test names the issue, or the merge that brought
   it in does.
2. **The issue exists and fits** — `#<n>` on GitHub is a real *issue* (not a pull request), it is
   closed, and its subject is what the test asserts.

Package names are not evidence. `test.testng173` and `test.testng317` look identical; one is a
GitHub issue and the other is nothing.

Where GitHub's own timeline for the issue links the introducing commit, that is recorded as
`timeline` below — the issue itself points at the code, which is as strong as this gets.

## Verified — 15 references

| Ref | Issue title on GitHub | Provenance | Timeline |
| --- | --- | --- | --- |
| `GITHUB-173` | Dependent methods executed out-of-order if method names match across classes | "Fix for the issue #173" | links commit |
| `GITHUB-565` | Deadlock when using group dependency (plus other factors) | "Add test for #565" | links commit |
| `GITHUB-674` | TestNG is not reporting any log for skip tests | "Inject config failure data into test results. Fixes #674" | links commit |
| `GITHUB-799` | @Factory with dataProvider changes order of iterations | "…Closes #799" | links commit |
| `GITHUB-1231` | Swap invocation order between IExecutionListener implementation and report generation | "…Fixes #1231" | links commit |
| `GITHUB-1232` | Prevent TestNG from adding duplicate instances of the same listener | "Ensure unique listener injection. Fixes #1232" | links commit |
| `GITHUB-1336` | Parallel test (parallel='tests') does not work when priority is used in Test | "…Fixes #1336" | links commit |
| `GITHUB-1362` | AfterGroups does not get executed when MethodInterceptor is involved | "Invoke AfterGroups when involving MethodInterceptors. Closes #1362" | links commit |
| `GITHUB-1396` | Order established by IMethodInterceptor not honored when running with parallel='instances' | "Fix https://github.com/cbeust/testng/issues/1396" | links commit |
| `GITHUB-1430` | Cannot load class from file XXX when using with ant and classfileset | "Issue #1430 : Fix loading class from file with ant and classfileset" | links commit |
| `GITHUB-1461` | Memory leak (TestNG seems to keep all test object in memory) | "Add test case for #1461" | links commit |
| `GITHUB-1490` | Add a listener for data provider interception | "…Closes #1490" | links commit |
| `GITHUB-765` | Test invoked twice when implements abstract method from parameterized parent | PR #1374, branch `krmahadevan-fix-765` | links PR 1374 |
| `GITHUB-1417` | Class param injection is not working with @BeforeClass | PR #1447, branch `krmahadevan-fix-1417` | links PR 1447 |
| `GITHUB-107` | TestNG printout wrong statistic number | "Improve Issue 107 test, add it to testng.xml" | **no link** |

All fifteen are closed issues, none is a pull request.

`GITHUB-107` is the weakest of the set: issue #107 was closed by hand in 2011 and its timeline links
no commit or PR at all. It rests on the commit saying "Issue 107" in words and on the issue title
matching what the test asserts — it counts passed tests. It is also **not something this work
added**: the description predates it. Left as it stands; flagged so nobody assumes it carries the
same weight as the rest.

## No reference — 6 tests

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
