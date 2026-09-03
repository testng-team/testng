# Label management

Labels provide a shared vocabulary for triaging issues and pull requests. Maintainers must keep label
names, descriptions, colors, and automation rules consistent so contributors can understand and
filter the backlog reliably.

## Classification rules

Every triaged issue must have:

1. Exactly one `type:` label.
2. At least one `area:` or `runner:` label when the affected scope is known.
3. Any applicable `status:`, `platform:`, `aspect:`, or `contribution:` labels.

Pull requests receive scope labels only. Automated dependency pull requests use `dependency:` labels
instead of `type:` labels.

Remove obsolete workflow labels when an issue changes state. Closed issues may retain classification
labels, but maintainers should replace temporary `status:` labels with a `resolution:` label when the
reason for closure is relevant.

## Label families

Each prefix represents one classification dimension.

| Prefix | Purpose | Color |
|---|---|---|
| `type:` | Nature of an issue | `#8250DF` |
| `status:` | Current triage or workflow state | `#BF8700` |
| `area:` | TestNG feature or code area | `#0969DA` |
| `runner:` | Runner or external execution integration | `#1B7C83` |
| `platform:` | Runtime or operating environment | `#1A7F37` |
| `aspect:` | Cross-cutting concern | `#BC4C00` |
| `contribution:` | Contributor accessibility | `#2DA44E` |
| `resolution:` | Reason an issue was closed | `#57606A` |
| `dependency:` | Automated dependency pull request | `#5A32A3` |

Use the exact family color unless a documented exception is necessary. Do not create a different
shade for every label in one family.

One prefix falls outside these families; see [Tooling markers](#tooling-markers).

## Type labels

Apply exactly one `type:` label to every triaged issue.

| Label | Description |
|---|---|
| `type: bug` | Reported behavior that differs from the intended behavior |
| `type: enhancement` | Request for new or improved behavior |
| `type: question` | Usage question redirected to community support and closed |
| `type: documentation` | Missing, incorrect, or unclear documentation, including testng.org |
| `type: maintenance` | Internal maintenance without a user-facing behavior change |
| `type: epic` | Umbrella issue coordinating several related changes |

A regression is still a bug. Apply `type: bug` together with `aspect: regression`.

A documentation request is `type: documentation`, not `type: enhancement`. The two never coexist.

## Status labels

Status labels describe the current action required to move an issue forward.

| Label | Description |
|---|---|
| `status: needs triage` | Awaiting initial review by a maintainer |
| `status: needs reproducer` | Awaiting a minimal reproducible example from the reporter |
| `status: waiting for reporter` | Awaiting additional information from the reporter |
| `status: needs discussion` | Awaiting a maintainer decision on scope or design |
| `status: confirmed` | Validated as reproducible or otherwise confirmed by a maintainer |
| `status: blocked` | Blocked by another issue, project, or external dependency |

Use no more than one waiting or blocking status at a time. `status: confirmed` may coexist with one
actionable status such as `status: needs discussion`.

Remove `status: needs triage` after assigning the initial type and scope labels.

## Scope labels

`area:` labels are the backbone of the taxonomy: they are what makes the backlog searchable years
later. Apply multiple area labels only when the issue genuinely crosses those boundaries.

| Label | Description |
|---|---|
| `area: annotations` | IAnnotationTransformer and annotation rewriting |
| `area: assertions` | Assert, assertEquals, and the assertion utilities |
| `area: configuration methods` | @BeforeX / @AfterX, alwaysRun, and configfailurepolicy |
| `area: data provider` | @DataProvider and data-driven tests |
| `area: dependency injection` | Guice and other third-party injection |
| `area: execution order` | priority, preserve-order, and group-by-instances |
| `area: factory` | @Factory and IObjectFactory instantiation |
| `area: groups` | Test groups, include and exclude |
| `area: inheritance` | Behavior involving class or method inheritance |
| `area: internal tests` | TestNG's own test suite |
| `area: invocation count` | invocationCount and repeated invocations |
| `area: junit` | JUnit mode and JUnit compatibility |
| `area: listeners` | Listeners, method interceptors, and invocation callbacks |
| `area: logging` | log4testng and internal logging |
| `area: method dependencies` | dependsOnMethods and dependsOnGroups ordering |
| `area: naming` | Test and method naming, ITest, and custom names |
| `area: parallel execution` | Parallel modes, thread pools, and custom executors |
| `area: parameters` | @Parameters and native injection of test parameters |
| `area: public api` | Public API changes, compatibility, and ITestContext attributes |
| `area: reporting` | Reporters, report generation, and testng-failed.xml |
| `area: retry analyzer` | IRetryAnalyzer retrying of failed tests |
| `area: skipped tests` | SkipException, skipped results, and @Ignore |
| `area: test selection` | Method selectors, filtering, and BeanShell scripts |
| `area: timeout` | Test and suite timeout handling |
| `area: xml suites` | testng.xml parsing and multi-suite runs |
| `area: yaml suites` | YAML suite file parsing |

`area: method dependencies` is deliberately not named `area: dependencies`: that name collides with
the `dependency:` family in GitHub's label autocompletion.

Use `runner:` when behavior depends on how TestNG is launched:

| Label | Description |
|---|---|
| `runner: ant` | Running TestNG via the Ant task |
| `runner: cli` | Running TestNG from the command line |
| `runner: custom` | Custom or programmatic TestNG runner |
| `runner: eclipse` | Eclipse plugin, tracked in the testng-eclipse repository |
| `runner: gradle` | Running TestNG via Gradle |
| `runner: maven` | Running TestNG via Maven Surefire |

Use `platform:` for environment-specific behavior:

| Label | Description |
|---|---|
| `platform: jdk 11` | Specific to JDK 11 |
| `platform: jdk 21` | Specific to JDK 21 |

Add a `platform:` label for a JDK version only once an issue actually turns on it.

## Cross-cutting labels

Use `aspect:` for concerns that can affect several areas. An aspect combines with any `type:`.

| Label | Description |
|---|---|
| `aspect: architecture` | Internal architecture or refactoring concern |
| `aspect: performance` | Execution time or memory behavior |
| `aspect: regression` | Worked in an earlier version and is now broken |
| `aspect: security` | Security-related concern |

Use contribution labels to advertise suitable work:

| Label | Description |
|---|---|
| `contribution: help wanted` | Ready for a contributor to investigate or implement |
| `contribution: good first issue` | Small, well-scoped issue suitable for a new contributor |

Apply `contribution: good first issue` only when the expected outcome, affected files, and
acceptance criteria are clear.

## Resolution labels

Resolution labels explain why an issue was closed without a code change.

| Label | Description |
|---|---|
| `resolution: external dependency` | Closed because the root cause belongs to another project |
| `resolution: not reproducible` | Closed because maintainers could not reproduce the behavior |
| `resolution: won't fix` | Closed because the behavior is intentional or out of scope |
| `resolution: duplicate` | Closed in favor of another issue |

Do not use a resolution label as an active workflow status.

## Tooling markers

| Label | Description |
|---|---|
| `triage: ai-reviewed` | Triage assisted by tooling, not part of the classification families |

Tooling markers record that a process ran. They are applied by tooling rather than by human triage,
they satisfy none of the [classification rules](#classification-rules), and they must never be
treated as a `status:`. Keep them to a minimum: a marker that no tool applies any more is dead
weight and should be deleted.

## Dependency pull requests

Dependabot assigns labels automatically. Custom labels must already exist before they are referenced
in `.github/dependabot.yml`; otherwise, Dependabot ignores them.

| Label | Description |
|---|---|
| `dependency: update` | Automated pull request updating a dependency |
| `dependency: java` | Automated update of a Gradle or Java dependency |
| `dependency: github actions` | Automated update of a GitHub Actions dependency |
| `dependency: npm` | Automated update of an npm dependency |

Configure every package ecosystem explicitly:

```yaml
version: 2
updates:
  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"
    labels:
      - "dependency: update"
      - "dependency: github actions"

  - package-ecosystem: "npm"
    directory: "/.github/workflows"
    schedule:
      interval: "weekly"
    labels:
      - "dependency: update"
      - "dependency: npm"

  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "daily"
      time: "00:00"
    labels:
      - "dependency: update"
      - "dependency: java"
```

Defining custom Dependabot labels replaces its default `dependencies` and ecosystem labels, and a
label that does not exist in the repository is silently ignored -- declaring only labels that have
yet to be created leaves the pull requests with no label at all. When renaming, list the old and the
new names together until the rename has landed, as Label Commenter also has to. Preserve
every classification required by repository filters or automation.

Dependabot cannot label per dependency, only per ecosystem. The Gradle wrapper is bumped by the
`gradle` ecosystem, so a wrapper pull request is labeled `dependency: java` like any other Gradle
update; it is recognizable by its branch and title. Do not add a label that nothing applies.

## Automated label behavior

The Label Commenter workflow reacts to exact label names configured in
`.github/label-commenter-config.yml`.

The following behaviors must remain covered:

| Label | Automated behavior |
|---|---|
| `status: needs reproducer` | Requests a minimal reproducible example and environment details |
| `type: question` | Redirects the reporter to community support and closes the issue |
| `contribution: help wanted` | Invites contributors to work on the issue |

Treat these label names as automation interfaces. A rename is incomplete until the configuration and
behavior have been updated and verified.

The main test workflow does not use labels to select or skip CI jobs. The Combine PRs workflow
selects bot pull requests by branch name (`dependabot/*`) rather than label.

## Writing label descriptions

Every label must have a description.

Descriptions must:

- Use English and sentence case.
- Explain when the label applies.
- Avoid repeating only the label name.
- Distinguish issues from pull requests when relevant.
- Remain concise, preferably under 80 characters.
- Omit a final period for consistency.

Prefer:

```text
Awaiting a minimal reproducible example from the reporter
```

Avoid:

```text
Needs reproducer
```

## Adding a label

Before adding a label:

1. Confirm that an existing label does not cover the same concept.
2. Select the correct family and its standard color.
3. Write a description that explains the application criteria.
4. Search workflows, configuration files, saved searches, and external bots for related names.
5. Document any automated behavior.
6. Add the label to this guide if it introduces a new rule or category.

Avoid creating labels for values that belong in issue content, milestones, projects, or assignees.

## Renaming a label

A GitHub label rename preserves its associations with existing issues and pull requests: one API
call moves every issue at once, whatever their number or state. Automation that matches the old text
does not update automatically.

Use this migration sequence:

1. Search the repository and GitHub settings for the exact old label name.
2. Update automation to recognize both the old and new names.
3. Create or rename the destination label.
4. Verify the new name on an issue or pull request.
5. Remove support for the old name after the migration is complete.

For automation-sensitive labels, merge the compatibility change before performing the rename.

## Merging labels

A merge requires migrating associations because GitHub does not provide a native label merge
operation. Unlike a rename, its cost is proportional to the number of issues and pull requests
carrying the source labels, closed ones included.

1. Create the destination label, or rename the most used source label to it.
2. Add it to every issue and pull request carrying a source label.
3. Remove the source labels.
4. Verify that no source label remains in use.
5. Update workflows, bots, templates, documentation, and saved searches.
6. Delete the source labels.

Do not delete labels before migrating their associations.

## Deleting a label

Delete a label only when:

- No open issue or pull request uses it.
- Its historical associations have been migrated when needed.
- No workflow or bot references it.
- No external repository setting depends on it.
- Another label or GitHub feature covers its purpose.

Repository files cannot reveal every external GitHub App or saved search. Check repository settings
before deleting labels previously created by bots.

## Validation checklist

Run this checklist once a label change is **complete**: the renames and merges have run, and the old
names have been dropped from the automation. Three of these items cannot hold while a compatibility
window is open, so do not treat them as failures before then.

- [ ] Every target label exists with the intended color and description.
- [ ] Existing issues and pull requests retain their classifications.
- [ ] No issue carries two `type:` labels.
- [ ] Label Commenter responds to `status: needs reproducer`.
- [ ] Label Commenter closes an issue labeled `type: question`.
- [ ] Label Commenter responds to `contribution: help wanted`.
- [ ] New Dependabot pull requests receive only the intended `dependency:` labels.
- [ ] Combine PRs still discovers Dependabot branches, checked in its log rather than its conclusion.
- [ ] No workflow or repository configuration references a retired label.
- [ ] Temporary test issues are closed or removed.

While a compatibility window is open -- both the old and the new names are live, see
[Renaming a label](#renaming-a-label) -- check instead that:

- [ ] The automation still answers to the old names, which are the ones issues actually carry.
- [ ] Automated pull requests still receive their labels, the old names included.
- [ ] Both configuration files already list the new names, ready for the rename.

## Periodic review

Review the label set at least twice a year.

During the review:

- Find labels with no open usage.
- Find open issues without a `type:` label.
- Find issues that still carry `status: needs triage`.
- Identify overlapping area labels.
- Check that every label has a description and the correct family color.
- Recheck workflow, Dependabot, and GitHub App dependencies.
- Update this document when the taxonomy or automation changes.
