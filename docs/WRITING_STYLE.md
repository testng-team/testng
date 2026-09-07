# Writing style and how we check it

People read TestNG's javadoc far more often than they read the code behind it. This page explains
the writing rules we follow, where those rules live, what checks them, and what the checks miss.

The rules are not on this page. They are in [`AGENTS.md`](../AGENTS.md), under `## Writing`. This
page explains the tooling around them.

## Table of Contents

1. [Why we have this](#why-we-have-this)
2. [How the pieces fit together](#how-the-pieces-fit-together)
3. [Where the rules live](#where-the-rules-live)
4. [Running the check](#running-the-check)
5. [What each rule does](#what-each-rule-does)
6. [What Vale sees](#what-vale-sees)
7. [Turning a rule off](#turning-a-rule-off)
8. [Adding or changing a rule](#adding-or-changing-a-rule)
9. [What the checks miss](#what-the-checks-miss)
10. [Rules considered and rejected](#rules-considered-and-rejected)

## Why we have this

Contributors now draft javadoc, issues and pull request text with Claude, ChatGPT, Copilot and
other tools. Each tool writes in its own voice, and none of those voices is ours.

Our style is not written down anywhere. It lives in the habits of the people who review changes.
That works while every change passes a reviewer who knows the style. It does not work for a tool,
and it does not scale.

This change writes the style down once and points a linter at it. Two things follow from that:

- **These are house rules, not rules for AI.** The linter cannot tell who wrote a sentence, so a
  rule that applies only to generated text cannot be checked. Everything here applies to people
  too.
- **The written rules are the standard.** We do not tell a tool to "follow ASD-STE100". A tool
  cannot check itself against a specification it does not have, so it will say it complied and move
  on. Our rules are short and concrete, so a tool can actually follow them.

## How the pieces fit together

There are three parts:

| # | Part | What it is | Does it enforce? |
|---|---|---|---|
| 1 | The rules | `## Writing` in `AGENTS.md` | No. It is the source of truth |
| 2 | The pointers | One file per tool, pointing at the rules | No. Tools may skim or skip them |
| 3 | The check | `vale` | Yes |

Parts 1 and 2 set the intent. Part 3 checks the output.

## Where the rules live

The rules are in one file. Every other file points at it. If you copy the rules into a second file,
the two will disagree within a month.

| Path | What it is | Read by |
|---|---|---|
| `AGENTS.md` | The rules | Codex, Cursor, Copilot coding agent, Jules, Amp, Gemini CLI |
| `CLAUDE.md` | Points at `AGENTS.md` | Claude Code |
| `.github/copilot-instructions.md` | Symlink to `../AGENTS.md` | GitHub Copilot |
| `GEMINI.md` | Symlink to `AGENTS.md` | Gemini CLI |
| `.cursor/rules/house-style.mdc` | Three lines, points at `AGENTS.md` | Cursor |

The rules sit inside `AGENTS.md` rather than on this page. Tools read that file all the way
through, and do not reliably follow links out of it.

**The symlinks resolve** for anything that reads a working tree on disk:

- Claude Code, Cursor, and the Codex and Gemini command line tools.
- Copilot running inside VS Code or IntelliJ.
- CI, once `actions/checkout` has restored the tree.

**GitHub's Contents API resolves them too.** Measured against this repository: a request for
`.github/copilot-instructions.md` comes back as `type=file` with the full 23 KB of `AGENTS.md`, not
as a link. So a tool that reads repository files through that API sees the rules.

**Two things still do not resolve them:**

- `raw.githubusercontent.com` serves the blob as stored, which is 12 bytes reading `../AGENTS.md`.
  Anything fetching a raw URL gets that instead of the rules.
- A Windows clone without symlink support writes the target path into an ordinary text file.

If either starts to matter, generate the copies from `AGENTS.md` with a script and have CI fail
when they drift apart. Never keep a second copy by hand.

## Running the check

Two Gradle tasks, because there are two different questions:

```bash
# did I add a problem? Run this before committing.
./gradlew writingStyleCheckChanges

# what is left in the repository? A maintenance question.
./gradlew writingStyleCheck
```

`writingStyleCheckChanges` is the one you want day to day. It reports only problems your change
introduced, the same way CI does with `filter_mode: added`. It looks at:

- Files committed on the branch, compared against the base branch, renames included.
- Anything staged, unstaged or untracked in your working copy.

Within those files it keeps only findings that sit on lines the change added. Anything older is
counted and mentioned, so you know it is there, but it is not yours to fix.

It picks the base by comparing merge bases and taking the most recent. Taking the first remote it
finds would be wrong, because a fork's `origin/master` is often far behind the canonical
repository. On this branch that difference was 483 changed files against 20. Pass
`-PwritingStyleSince=<ref>` when you need a specific base.

Either task becomes a gate with `-PfailOnWritingStyle=true`, which is what you want in a
pre-commit hook. Gradle accepts camel case abbreviations, so `./gradlew wSCC` runs the first one.

The tasks are named for the question they answer rather than for Vale. Vale is the tool we happen
to run, and replacing it would not change either question. The tool-named tasks nearby,
`autostyleCheck` and `rewriteDryRun`, take their names from third-party plugins instead of from
this build.

Neither needs Vale installed. The task uses a `vale` already on your `PATH` when there is one.
Otherwise it falls back to `npx`, which downloads a pinned binary on first use and caches it.
The build pins two versions, not one. `valeCliVersion` is the Vale we want. `valeNpmWrapperVersion`
is the npm package used by the fallback, and npm does not publish a wrapper for every Vale release.
Both live in `testng.writing-style.gradle.kts`.
Installing Vale is still worth it for the speed. A warm `writingStyleCheckChanges` takes about eight
seconds, against a little over one for Vale on its own. The
[Vale install guide](https://vale.sh/docs/install) covers Homebrew, apt, Chocolatey, Scoop and a
plain binary download.

Findings also land in `build/reports/writing-style/`, in `changed.txt` and `all.txt`.

You do not have to read this page to find the settings. `./gradlew parameters` lists them all,
and `./gradlew tasks --group verification` lists the tasks.

| Setting | What it does |
|---|---|
| `-PfailOnWritingStyle=true` | Findings fail the build |
| `-PwritingStyleSince=<ref>` | Compare against this base branch |
| `-PwritingStyleBaseRefs=a,b,c` | Base branches to try, in order, when the one above is unset |
| `-PwritingStyleValePath=<path>` | Run this Vale binary, instead of `PATH` or `npx` |

The last one is worth setting if you run the whole-tree task often. On this machine a full
`writingStyleCheck` took 114 seconds through `npx` and 63 seconds with the binary named directly.
It is also the way to run offline.

Both tasks live in `build-logic/code-quality/`, beside the other quality plugins.
`testng.writing-style.gradle.kts` registers them, and `buildlogic/ValeCheck.kt` is the task itself.
The root build applies the plugin, because Vale reads the tree in one pass.

`writingStyleCheck` reports findings in text that predates these rules. Your change did not cause
them, and `writingStyleCheckChanges` is the task that answers whether it did. The whole-tree count
also moves with your working copy, because the task walks the tree rather than the index.

[`.github/workflows/prose.yml`](../.github/workflows/prose.yml) runs the same rules on pull
requests, through the Vale GitHub action rather than the Gradle task. Two settings in it matter:

- `filter_mode: added` limits it to lines the pull request added. Nobody is asked to fix text
  somebody else wrote.
- `fail_on_error: false` makes it comment without blocking. Turn this on once the number of
  warnings settles down.

## What each rule does

The rules are in `.vale/styles/TestNG/`. Each one was counted against this codebase before it went
in. None of them is here because it sounded like a good idea.

| Rule | What it catches |
|---|---|
| `Filler.yml` | Words that add nothing |
| `Spelling.yml` | British spellings. We use American |
| `Narration.yml` | Text about the change instead of about the code |
| `PlainWords.yml` | Long words that have short equivalents |
| `SalesWords.yml` | Marketing adjectives |
| `SentenceLength.yml` | Sentences over 25 words. Off unless you ask for it |
| `Vale.Spelling` | Real typos, filtered through a word list |

`./gradlew writingStyleCheck` prints how often each one fires today. The counts are not written
down here, because they go stale on the next merge.

These rules use American spelling, because the Java API already does, in names like
`Serializable`, `synchronized` and `Externalizable`.

`Vale.Spelling` depends on the word list at
`.vale/styles/config/vocabularies/TestNG/accept.txt`. Without that list there are well over a
thousand findings. Nearly every one is an identifier or a contributor name sitting in comment text.
With the list, the count drops by two orders of magnitude. What is left is mostly real typos. The
rest are identifiers added since the list was built, and those want `{@code}` around them rather
than a new entry.

The word list covers test sources as well as main sources. It is built from `git ls-files`, because
CI checks every changed `.java` file. Building it from `src/main` alone leaves 225 false positives
in the test tree.

`SentenceLength` is set to `suggestion`, and `.vale.ini` sets `MinAlertLevel = warning`, so it
never fires through the Gradle tasks or CI. To see it, call Vale yourself and lower the level:

```bash
npx --yes @vvago/vale --minAlertLevel=suggestion docs/WRITING_STYLE.md
```

See [What the checks miss](#what-the-checks-miss) for why it is not enforced.

## What Vale sees

Vale checks word choice, spelling and tone. It reads both `.java` and `.md` files. In Java it only
looks at comments, so code and string literals never produce a finding.

It does not check whether a javadoc block is well formed. It does not know what a `{@link}` points
at, whether an `@param` has a description, or whether a comment has a summary sentence. Those are
structural questions, and they belong to the compiler toolchain rather than to a prose linter.

## Turning a rule off

There are three ways, and they are for three different situations.

**A word is spelled correctly but the spell checker disagrees.** Add it to
`.vale/styles/config/vocabularies/TestNG/accept.txt`, one per line. Each line is a regular
expression, so `validators?` covers both the singular and the plural.

**A block of text quotes the banned words on purpose.** Wrap it:

```markdown
<!-- vale off -->
- No filler: simply, basically, essentially.
<!-- vale on -->
```

The `## Writing` section of `AGENTS.md` does this. Keep the wrapped block as small as you can. A
block that is too wide hides real findings, spelling mistakes included.

**An identifier shows up in javadoc.** Do not suppress it. Mark it up instead:

```java
/** Counts from zero. See {@code paramIndex} and {@link #getParameters()}. */
```

Vale ignores anything inside `{@code ...}`, `{@link ...}` and `<code>...</code>`, but flags a bare
`paramIndex`. So the spell checker reminds you to mark up identifiers, and the published page gets
better at the same time.

## Adding or changing a rule

`AGENTS.md` already asks you to measure before making an absolute claim. Do the same here, and
expect to throw some candidates away.

1. **Count the sites.**

   ```bash
   grep -rhoiE '\byour-pattern\b' --include='*.java' . | wc -l
   ```

2. **Read them in context.** The count on its own will mislead you. Take a rule that replaces
   `dataprovider` with `data provider`. At 27 hits it looks worth having. All 27 are identifiers:
   the `dataProvider` attribute, the `@DataProvider` annotation, the `dataproviderthreadcount`
   setting, and the `test.dataprovider` package. That rule would produce 27 false positives and
   nothing else.

3. **Write the rule** as a `.yml` file in `.vale/styles/TestNG/`. Vale's `existence`, `substitution`
   and `occurrence` types cover almost everything worth checking.

4. **Run it over the whole repository** and read every finding. A rule that is wrong one time in
   ten will be switched off within a month.

5. **Update `AGENTS.md`** in the same change. A rule the linter checks but the rules file does not
   mention will confuse the next person.

One YAML detail will cost you an afternoon. A token like `TODO: revisit` parses as a map, not a
string, and Vale then refuses to start. Put it in quotes.

## What the checks miss

We tested each of these rather than assuming it.

- **Vale cannot tell javadoc from a `//` comment.** `scope: comment`, `scope: text.comment` and no
  scope at all behave the same way on Java. This matters, because we write the two differently. We
  use "we" and "our" 156 times in `//` comments and 36 times in javadoc, and contractions 73 times
  against 35. That is fine in an implementation comment and wrong in published documentation. So
  `AGENTS.md` states those rules for reviewers, and no linter checks them.

- **`scope: raw` reaches into string literals.** It skips comment extraction altogether. Never use
  it on Java.

- **Sentence length does not work on Java at all.** This is not about line wrapping. A single
  unwrapped 60-word javadoc line reports nothing. On Markdown it does work, and it fires on most
  of the documentation we already have. Both reasons are why it is left at `suggestion`, which
  `MinAlertLevel` then filters out. Treat it as a drafting aid, not a check.

- **The word list silences every rule, not just the spell checker.** A word in `accept.txt` is
  invisible to our own rules too. So British spellings have to stay out of it, which means a word
  like `initialiser` gets reported twice until someone fixes it. Two places are affected.

- **`Vale.Terms` and `Vale.Repetition` are switched off.** Terms treats the capitalization of each
  `accept.txt` entry as the correct one, so it asked us to write `testng` instead of `TestNG`, and
  `sha` instead of `SHA`.

- **Nothing checks issues or pull request comments.** CI never sees them. We name the rules in the
  issue and pull request templates instead, so whoever, or whatever, is typing reads them first.
  That half of the standard is advice, and we are fine with that.

- **No check catches heavy writing.** The rules find single words. They miss a sentence that is
  correct but hard to follow. They miss a paragraph that makes the reader work out the point.
  Readability scores do not help either. Flesch-Kincaid counts word length and sentence length,
  nothing more. Text can score below grade 5, well under `BUILD_SYSTEM.md` at 9.38, and still make
  the reader stop and decode every other sentence. This one is on the reviewer.

## Rules considered and rejected

Each one below looks reasonable. The reason it is absent is written down so nobody adds it later
without checking.

- **Full ASD-STE100 compliance.** The specification comes with a dictionary of roughly 900 approved
  words, each with one permitted meaning, and the tools that check against it are commercial. What
  `AGENTS.md` carries is a subset, and it says so. Claiming full compliance would be untrue.

- **Banning technical jargon.** STE allows what it calls technical names and technical verbs, which
  are the words a reader in the field needs. So `annotation`, `listener`, `suite` and `data
  provider` all stay. What we remove is ordinary English that is longer or vaguer than it needs to
  be.

- **Banning "you".** Our javadoc addresses the reader as "you" 45 times, and our `//` comments never
  do. That is a choice somebody made, not an accident, so `AGENTS.md` says it is allowed. Without
  that line, a tool reading "name who acts" would rewrite all 45.

- **Terminology replacements.** See step 2 of
  [Adding or changing a rule](#adding-or-changing-a-rule). The measurement kills it.

- **Checking `CHANGES.txt`.** It holds twenty years of contributor names and issue numbers. On its
  own it produced more than nine tenths of every finding in the repository. So `.vale.ini` covers
  `*.md` and `*.java` only.

- **Keeping the rules on this page.** Tools do not reliably follow links, so the rules have to sit
  in the file the tool already reads. That is why they are in `AGENTS.md`.
