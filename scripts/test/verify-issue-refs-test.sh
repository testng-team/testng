#!/usr/bin/env bash
# Tests for scripts/verify-issue-refs.sh.
#
# That script decides whether a GITHUB-<n> reference may be written into a @Test description. A
# wrong answer puts a false reference in the code, or drops a true one. Both are silent. So the
# rules it applies are checked here rather than by hand.
#
# Run it from anywhere:
#
#     scripts/test/verify-issue-refs-test.sh
#
# Every case builds its own small git repository in a temporary directory. Nothing here reads the
# TestNG history, so moving a test file later cannot break these tests. Nothing here calls the
# GitHub API either: PROVENANCE_ONLY=1 stops the script after the provenance step, which is the
# step with the rules worth testing.
set -u
SCRIPT=$(cd "$(dirname "$0")/../.." && pwd)/scripts/verify-issue-refs.sh
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

# The header above promises these tests reach no network. Promising it is not enough: the script
# calls "gh api" on its own whenever a pull request number turns up and no reader was given, and
# two cases here do produce one. So a "gh" that refuses and records the call goes first on PATH.
# A recorded call fails the run at the end, which turns the promise into a check.
mkdir -p "$WORK/bin"
cat > "$WORK/bin/gh" <<SHIM
#!/bin/sh
echo "\$*" >> "$WORK/gh-calls"
exit 1
SHIM
chmod +x "$WORK/bin/gh"
PATH="$WORK/bin:$PATH"
export PATH

pass=0; fail=0

# Reports one result. $1 is what was tested, $2 is expected, $3 is what happened.
check() {
  if [ "$2" = "$3" ]; then
    pass=$((pass + 1))
  else
    fail=$((fail + 1))
    printf 'FAIL  %s\n      expected: %s\n      actual:   %s\n' "$1" "$2" "$3"
  fi
}

# Prints yes when the text holds the refusal word, no when it does not.
says_ambiguous() { case "$1" in *AMBIGUOUS*) printf yes ;; *) printf no ;; esac; }

# Makes an empty git repository and prints its path.
new_repo() {
  local dir
  dir=$(mktemp -d "$WORK/repo.XXXXXX")
  git -C "$dir" init -q -b master
  git -C "$dir" config user.email test@example.com
  git -C "$dir" config user.name "Test"
  git -C "$dir" config commit.gpgsign false
  printf '%s' "$dir"
}

# add <repo> <path> <message>  -- writes a file and commits it.
add() {
  mkdir -p "$1/$(dirname "$2")"
  printf 'class X {}\n' > "$1/$2"
  git -C "$1" add -A && git -C "$1" commit -q -m "$3"
}

# move <repo> <from> <to>  -- renames a file and commits it.
move() {
  mkdir -p "$1/$(dirname "$3")"
  git -C "$1" mv "$2" "$3" && git -C "$1" commit -q -m "Move the file"
}

# fake_pr_body <text>  -- makes a command that prints <text> as a pull request body.
# The script under test runs it as "<cmd> <pr-number>". This one ignores the number, because a
# test that needs two different bodies makes two commands.
fakes=0
fake_pr_body() {
  fakes=$((fakes + 1))
  printf '%s' "$1" > "$WORK/body.$fakes"
  { printf '#!/bin/sh\n'; printf 'cat "%s"\n' "$WORK/body.$fakes"; } > "$WORK/prbody.$fakes.sh"
  chmod +x "$WORK/prbody.$fakes.sh"
  printf '%s' "$WORK/prbody.$fakes.sh"
}

# merged_pr <repo> <path> <merge-subject>  -- adds a file on a branch and merges it.
# The commit itself names no issue, so only the merge and the pull request body can prove one.
merged_pr() {
  add "$1" README.md "First commit"
  git -C "$1" checkout -q -b work
  add "$1" "$2" "Adding a fix"
  git -C "$1" checkout -q master
  git -C "$1" merge -q --no-ff -m "$3" work
}

# Runs the script inside a repository and reduces its output to one word.
# $1 is the repository, $2 the path fragment, $3 the issue number.
# Extra arguments after that are passed to the environment, for example diff.renames=false.
verdict() {
  local repo=$1 frag=$2 num=$3 out
  shift 3
  out=$(cd "$repo" && env "$@" PROVENANCE_ONLY=1 bash "$SCRIPT" "$frag" "$num" 2>&1)
  case "$out" in
    *AMBIGUOUS*)    printf 'AMBIGUOUS' ;;
    *"OWN COMMIT"*) printf 'OWN COMMIT' ;;
    *"NOT PROVEN"*) printf 'NOT PROVEN' ;;
    *provenance*)   printf 'PROVEN' ;;
    *)              printf 'NO COMMIT' ;;
  esac
}

# --- the commit message names the issue ------------------------------------------------------
# These three forms are the only ones a commit message may use as proof.
for form in '#765' 'TESTNG-765' 'issues/765'; do
  r=$(new_repo)
  add "$r" src/foo/AlphaTest.java "Fix $form: something was broken"
  check "commit says $form" PROVEN "$(verdict "$r" src/foo/AlphaTest.java 765)"
done

# --- a number that is not an issue reference -------------------------------------------------
# Each of these carries "765" but proves nothing. The earlier matcher accepted all three.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Update src/765/data.txt"
check "src/765 is not provenance" "NOT PROVEN" "$(verdict "$r" src/foo/AlphaTest.java 765)"

r=$(new_repo); add "$r" src/foo/AlphaTest.java "Cut release-765"
check "release-765 is not provenance" "NOT PROVEN" "$(verdict "$r" src/foo/AlphaTest.java 765)"

r=$(new_repo); add "$r" src/foo/AlphaTest.java "Bump to 1.2-765"
check "a version is not provenance" "NOT PROVEN" "$(verdict "$r" src/foo/AlphaTest.java 765)"

# A different issue number is not proof either.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Fix #173"
check "another issue is not provenance" "NOT PROVEN" "$(verdict "$r" src/foo/AlphaTest.java 765)"

# --- the merge names the issue in its branch --------------------------------------------------
# The commit itself says nothing. The branch it merges carries the number.
r=$(new_repo)
add "$r" README.md "First commit"
git -C "$r" checkout -q -b krmahadevan-fix-765
add "$r" src/foo/BetaTest.java "Fixing review comments"
git -C "$r" checkout -q master
git -C "$r" merge -q --no-ff -m "Merge pull request #1374 from krmahadevan/krmahadevan-fix-765" \
  krmahadevan-fix-765
check "merge branch names the issue" PROVEN "$(verdict "$r" src/foo/BetaTest.java 765)"

# Pull requests and issues share one number space. "Merge pull request #765" says the pull request
# was numbered 765. It says nothing about issue 765, so it is not provenance.
r=$(new_repo)
add "$r" README.md "First commit"
git -C "$r" checkout -q -b some-other-work
add "$r" src/foo/GammaTest.java "Fixing review comments"
git -C "$r" checkout -q master
git -C "$r" merge -q --no-ff -m "Merge pull request #765 from someone/unrelated" some-other-work
# Both subjects below do name a pull request, so the script looks for its body. An empty body
# keeps that lookup offline and leaves the rule being tested as the only thing that decides.
check "a merged PR number is not the issue" "NOT PROVEN" \
  "$(verdict "$r" src/foo/GammaTest.java 765 "PR_BODY_CMD=$(fake_pr_body '')")"

# The same rule for a squashed pull request, where GitHub puts the number at the end of the subject.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Speed up the parser. (#765)"
check "a squashed PR number is not the issue" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body '')")"

# The branch still counts, even when the pull request number happens to match the issue number.
r=$(new_repo)
add "$r" README.md "First commit"
git -C "$r" checkout -q -b fix-765
add "$r" src/foo/DeltaTest.java "Fixing review comments"
git -C "$r" checkout -q master
git -C "$r" merge -q --no-ff -m "Merge pull request #765 from someone/fix-765" fix-765
check "the branch still counts" PROVEN "$(verdict "$r" src/foo/DeltaTest.java 765)"

# A real issue reference in the merge body still counts, next to a pull request number.
r=$(new_repo)
add "$r" README.md "First commit"
git -C "$r" checkout -q -b some-work
add "$r" src/foo/EpsilonTest.java "Fixing review comments"
git -C "$r" checkout -q master
git -C "$r" merge -q --no-ff -m "Merge pull request #765 from someone/some-work" -m "Closes #1632" \
  some-work
check "the merge body still counts" PROVEN "$(verdict "$r" src/foo/EpsilonTest.java 1632)"

# --- renames ----------------------------------------------------------------------------------
# A file that moved twice must be walked back to the commit that wrote it. The cases below pass
# "place/DeltaTest.java", which is not a file on disk. That skips the --follow branch, so the
# rename-walking search is the code under test.
renamed_repo() {
  local r
  r=$(new_repo)
  add "$r" old/place/DeltaTest.java "Streamline the listeners. Closes #1632"
  move "$r" old/place/DeltaTest.java middle/place/DeltaTest.java
  move "$r" middle/place/DeltaTest.java new/place/DeltaTest.java
  printf '%s' "$r"
}

r=$(renamed_repo)
check "file moved twice still resolves" PROVEN "$(verdict "$r" place/DeltaTest.java 1632)"

# The same repository with git's rename detection turned off. Without -M on the queries the three
# destinations look like three separate files, and the ambiguity check rejects them.
r=$(renamed_repo)
check "resolves with diff.renames=false" PROVEN \
  "$(verdict "$r" place/DeltaTest.java 1632 \
     GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=diff.renames GIT_CONFIG_VALUE_0=false)"

# --- ambiguity ---------------------------------------------------------------------------------
# Two unrelated files share a name. Picking either one would invent provenance, so the script
# refuses instead.
r=$(new_repo)
add "$r" one/TestClassSample.java "Fix #1405"
add "$r" two/TestClassSample.java "Fix #2674"
check "ambiguous file name is rejected" AMBIGUOUS "$(verdict "$r" TestClassSample.java 1405)"

# The refusal must go to stderr. Callers send this script's stdout to /dev/null, so a refusal
# written to stdout is a refusal nobody reads, and the script fails without saying why.
out=$(cd "$r" && PROVENANCE_ONLY=1 bash "$SCRIPT" TestClassSample.java 1405 2>/dev/null)
err=$(cd "$r" && PROVENANCE_ONLY=1 bash "$SCRIPT" TestClassSample.java 1405 2>&1 >/dev/null)
check "the refusal is on stderr" yes "$(says_ambiguous "$err")"
check "the refusal is not on stdout" no "$(says_ambiguous "$out")"

# Exit code 2 is what a caller checks for. Anything else reads as a plain failure.
(cd "$r" && PROVENANCE_ONLY=1 bash "$SCRIPT" TestClassSample.java 1405 >/dev/null 2>&1)
check "ambiguity exits 2" 2 "$?"

# --- BY_DESCRIPTION --------------------------------------------------------------------------
# One file can carry many descriptions, added by many commits. The commit that created the file
# proves nothing about a description added to it years later.
r=$(new_repo)
add "$r" src/foo/ManyTest.java "Create the test class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/src/foo/ManyTest.java"
git -C "$r" commit -q -am "Reject the empty name. Fixes #765"
printf 'class X { /* GITHUB-765 GITHUB-1632 */ }\n' > "$r/src/foo/ManyTest.java"
git -C "$r" commit -q -am "Report the skip reason. Closes #1632"

# Without the mode the script finds the commit that created the file, which names no issue.
check "the file's own commit proves nothing" "NOT PROVEN" \
  "$(verdict "$r" src/foo/ManyTest.java 1632)"

# With the mode it finds the commit that wrote that description.
check "by description finds the right commit" PROVEN \
  "$(verdict "$r" src/foo/ManyTest.java 1632 BY_DESCRIPTION=1)"
check "by description works for the earlier one" PROVEN \
  "$(verdict "$r" src/foo/ManyTest.java 765 BY_DESCRIPTION=1)"

# A rename after the description was written must not become the answer. Restricting the search to
# the path does exactly that: the text looks added at the new path. The 2021 module split moved
# every file in this repository, and it claimed six references out of nine.
r=$(new_repo)
add "$r" old/place/RenamedTest.java "Create the test class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/old/place/RenamedTest.java"
git -C "$r" commit -q -am "Reject the empty name. Fixes #765"
move "$r" old/place/RenamedTest.java new/place/RenamedTest.java
check "a later rename is not the answer" PROVEN \
  "$(verdict "$r" new/place/RenamedTest.java 765 BY_DESCRIPTION=1)"

# "GITHUB-182" is the start of "GITHUB-1827". A plain-string search answers with whichever came
# first. Ten such pairs already exist in the TestNG tree, and GITHUB-182 answered with the commit
# that wrote GITHUB-1827.
r=$(new_repo)
add "$r" src/foo/LongTest.java "Create the test class"
printf 'class X { /* GITHUB-7654 */ }\n' > "$r/src/foo/LongTest.java"
git -C "$r" commit -q -am "Speed up the parser. Closes #7654"
printf 'class X { /* GITHUB-7654 GITHUB-765 */ }\n' > "$r/src/foo/LongTest.java"
git -C "$r" commit -q -am "Reject the empty name. Closes #765"
check "a longer number is not this one" PROVEN \
  "$(verdict "$r" src/foo/LongTest.java 765 BY_DESCRIPTION=1)"

# One issue number often sits in several files. GITHUB-1336 is in six of them and GITHUB-2830 in
# five. The answer must be the commit that wrote the text into THIS file, not the oldest commit
# that wrote it anywhere.
r=$(new_repo)
add "$r" src/foo/OtherTest.java "Create the other class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/src/foo/OtherTest.java"
git -C "$r" commit -q -am "Reject the empty name. Closes #765"
add "$r" src/foo/MineTest.java "Create my class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/src/foo/MineTest.java"
git -C "$r" commit -q -am "Copy the check over here"
check "another file is not this file's proof" "NOT PROVEN" \
  "$(verdict "$r" src/foo/MineTest.java 765 BY_DESCRIPTION=1)"
check "the other file is still proven" PROVEN \
  "$(verdict "$r" src/foo/OtherTest.java 765 BY_DESCRIPTION=1)"

# A file name is not a file. 45 files here are called IssueTest.java and 60 TestClassSample.java.
# Two of them carrying the same reference must not prove each other.
r=$(new_repo)
add "$r" a/IssueTest.java "Create the first class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/a/IssueTest.java"
git -C "$r" commit -q -am "Reject the empty name. Closes #765"
add "$r" b/IssueTest.java "Create the second class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/b/IssueTest.java"
git -C "$r" commit -q -am "Copy the check over here"
check "the same file name is not the same file" "NOT PROVEN" \
  "$(verdict "$r" b/IssueTest.java 765 BY_DESCRIPTION=1)"
check "the first of the two is still proven" PROVEN \
  "$(verdict "$r" a/IssueTest.java 765 BY_DESCRIPTION=1)"

# The file's own history counts, including the paths it came from.
r=$(new_repo)
add "$r" old/place/MovedTest.java "Create the class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/old/place/MovedTest.java"
git -C "$r" commit -q -am "Reject the empty name. Closes #765"
move "$r" old/place/MovedTest.java new/place/MovedTest.java
check "a path it came from counts" PROVEN \
  "$(verdict "$r" new/place/MovedTest.java 765 BY_DESCRIPTION=1)"

# The mode changes how the commit is found. It does not soften the rules.
r=$(new_repo)
add "$r" src/foo/OneTest.java "Create the test class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/src/foo/OneTest.java"
git -C "$r" commit -q -am "Cut release-765"
check "by description still applies the rules" "NOT PROVEN" \
  "$(verdict "$r" src/foo/OneTest.java 765 BY_DESCRIPTION=1)"


# --- the pull request body closes the issue ---------------------------------------------------
# GitHub closes an issue when a pull request body says "Fixes #<n>". The commit and the merge may
# name nothing: issue #1307 was closed that way, by pull request #1308, whose branch was called
# "feature/ignore-anonymous-tests". Without this the true reference is lost.
#
# The body is read ONLY through the pull request the merge names, so it cannot be any other body.

r=$(new_repo); merged_pr "$r" src/foo/AlphaTest.java "Merge pull request #1308 from someone/feature-x"
check "the body closes the issue" PROVEN \
  "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body 'Fixes #765')")"

r=$(new_repo); merged_pr "$r" src/foo/AlphaTest.java "Merge pull request #1308 from someone/feature-x"
check "closes counts too" PROVEN \
  "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body 'Closes #765')")"

# GitHub ignores the case of its closing words.
r=$(new_repo); merged_pr "$r" src/foo/AlphaTest.java "Merge pull request #1308 from someone/feature-x"
check "an upper case closing word counts" PROVEN \
  "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body 'FIXES #765')")"

# A squash merge puts the pull request number in the subject instead of making a merge commit.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Adding a fix (#1308)"
check "a squashed subject names the pull request" PROVEN \
  "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body 'Resolves #765')")"

# --- bodies that must be REJECTED --------------------------------------------------------------
# A mention is not a claim. These five bodies all hold "765" and none of them says this pull
# request fixes issue 765.
for body in 'See #765 for details' \
            'This is not a fix for #765' \
            'Fixes #7650' \
            'Fixes #173' \
            ''; do
  r=$(new_repo); merged_pr "$r" src/foo/AlphaTest.java "Merge pull request #1308 from someone/feature-x"
  check "body <$body> is not provenance" "NOT PROVEN" \
    "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body "$body")")"
done

# A closing word must be a whole word. Every body below holds a closing word inside a longer one,
# and GitHub closes nothing on any of them. "Still unresolved" is ordinary pull request English.
for body in 'Still unresolved: #765' \
            'This prefixes #765 onto the name' \
            'Nothing disclosed #765 here' \
            'Fixed: #100. Unfixed: #765'; do
  r=$(new_repo); merged_pr "$r" src/foo/AlphaTest.java "Merge pull request #1308 from someone/feature-x"
  check "body <$body> is not a closing word" "NOT PROVEN" \
    "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body "$body")")"
done

# The pull request number comes from a merge subject GitHub wrote, or from the tail of a squashed
# subject. A "(#n)" anywhere else names some other pull request, and reading that body would prove
# nothing about this commit.
r=$(new_repo)
add "$r" src/foo/AlphaTest.java "Add a test
This re-adds coverage lost when we reverted (#1308)."
check "a (#n) in the body is not this pull request" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body 'Fixes #765')")"

# When a subject holds two, the one GitHub appended is the last, not the first.
r=$(new_repo)
add "$r" src/foo/BetaTest.java "Revert the change from (#900) (#1308)"
check "the appended number is the last one" PROVEN \
  "$(verdict "$r" src/foo/BetaTest.java 765 "PR_BODY_CMD=$(fake_pr_body 'Fixes #765')")"

# No pull request number means no body to read. The command must never run.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Adding a fix"
check "no pull request number, no body" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765 "PR_BODY_CMD=$(fake_pr_body 'Fixes #765')")"

# A body reader that fails proves nothing. It must not turn into a verdict either way.
r=$(new_repo); merged_pr "$r" src/foo/AlphaTest.java "Merge pull request #1308 from someone/feature-x"
check "a failing body reader is not provenance" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765 PR_BODY_CMD=/nonexistent/reader)"


# --- a longer fragment separates two files that end the same way --------------------------------
# GitHub1131Test.java exists under test/factory and under test/objectfactory, and the two were
# added by different commits. The refusal tells the caller to pass more of the path, so more of the
# path has to change the answer.

# Both files are moved away afterwards, so no fragment below still exists in the worktree. That
# is the real shape: the caller passes a path the file used to have, which is the only case where
# the suffix rules run at all.
r=$(new_repo)
add "$r" alpha/dir/IssueTest.java "Fix #111"
add "$r" beta/dir/IssueTest.java "Fix #222"
move "$r" alpha/dir/IssueTest.java kept/alpha/dir/IssueTest.java
move "$r" beta/dir/IssueTest.java kept/beta/dir/IssueTest.java

# Two segments are still not enough when two files share them.
check "two shared segments are ambiguous" AMBIGUOUS "$(verdict "$r" dir/IssueTest.java 111)"

# Three segments name one file, and it must be the right one.
check "three segments pick one file" PROVEN "$(verdict "$r" alpha/dir/IssueTest.java 111)"
check "three segments pick the right file" "NOT PROVEN" \
  "$(verdict "$r" alpha/dir/IssueTest.java 222)"
check "the other file keeps its own commit" PROVEN "$(verdict "$r" beta/dir/IssueTest.java 222)"

# A suffix matches whole path segments. "factory/X.java" is not part of "objectfactory/X.java".
r=$(new_repo)
add "$r" src/factory/IssueTest.java "Fix #111"
add "$r" src/objectfactory/IssueTest.java "Fix #222"
move "$r" src/factory/IssueTest.java kept/factory/IssueTest.java
move "$r" src/objectfactory/IssueTest.java kept/objectfactory/IssueTest.java
check "a suffix starts at a segment" PROVEN "$(verdict "$r" factory/IssueTest.java 111)"
check "objectfactory is a different file" "NOT PROVEN" "$(verdict "$r" factory/IssueTest.java 222)"
check "objectfactory keeps its own commit" PROVEN "$(verdict "$r" objectfactory/IssueTest.java 222)"


# --- a path with no history must not be answered from its name alone ---------------------------
# The basename is the weakest possible key: 45 files here are called IssueTest.java. A caller who
# gives a path is asking about THAT file. If no suffix of it matches, the honest answer is that the
# file has no history -- not the history of some other file with the same name.
r=$(new_repo)
add "$r" src/test/java/test/other/MySample.java "Unrelated work. Fix #999"
check "a path with no history is not answered by name" "NO COMMIT" \
  "$(verdict "$r" src/test/java/org/testng/factory/samples/MySample.java 999)"

# A bare name is still answered by name. That is what a bare name asks for.
r=$(new_repo)
add "$r" src/foo/LonelyTest.java "Fix #999"
check "a bare name is answered by name" PROVEN "$(verdict "$r" LonelyTest.java 999)"

# --- the ambiguity refusal must not depend on the file extension -------------------------------
# The count and the lookup have to see the same paths. A filter on one side made every non-Java
# file unambiguous, so the first of two matches was returned with no refusal.
r=$(new_repo)
add "$r" one/suites/case.xml "Fix #111"
add "$r" two/suites/case.xml "Fix #222"
check "two xml files are ambiguous too" AMBIGUOUS "$(verdict "$r" suites/case.xml 111)"

# --- the rename walk-back must match whole segments --------------------------------------------
# One commit renames two files. The suffix asked for is "graph/IssueTest.java". Matching it inside
# "dynamicgraph/IssueTest.java" sends the walk down the wrong file's history.
r=$(new_repo)
add "$r" a/dyn.java "Fix #222"
add "$r" b/gra.java "Fix #111"
mkdir -p "$r/new/dynamicgraph" "$r/new/graph"
git -C "$r" mv a/dyn.java new/dynamicgraph/IssueTest.java
git -C "$r" mv b/gra.java new/graph/IssueTest.java
git -C "$r" commit -q -m "Move both"
check "the walk-back follows the right file" PROVEN "$(verdict "$r" graph/IssueTest.java 111)"
check "the walk-back does not follow the other" "NOT PROVEN" \
  "$(verdict "$r" graph/IssueTest.java 222)"

# --- the evidence printed must be the text that proved it ---------------------------------------
# "release-765" is not provenance. Printing it as the evidence tells the reader the opposite.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Cut release-765 and fix #765"
out=$(cd "$r" && PROVENANCE_ONLY=1 bash "$SCRIPT" src/foo/AlphaTest.java 765 2>&1)
check "the evidence is the text that proved it" "#765" \
  "$(printf '%s' "$out" | sed -n 's/^provenance  the introducing commit names it: //p' | tr -d ' ')"

if [ -s "$WORK/gh-calls" ]; then
  fail=$((fail + 1))
  printf 'FAIL  the tests reached the network\n      gh was called with:\n'
  sed 's/^/        /' "$WORK/gh-calls"
fi

printf '\n%s passed, %s failed\n' "$pass" "$fail"
[ "$fail" -eq 0 ]
