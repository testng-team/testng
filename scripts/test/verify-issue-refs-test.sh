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

# The script asks GitHub which pull request holds a commit. Here GitHub answers "none" by default, so
# a case reaches the rules without the network. A case about one answer passes its own command. A
# case about a lookup that fails passes COMMIT_PRS_CMD=false.
export COMMIT_PRS_CMD=true

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

# Prints yes when the text says the answer is not a verdict. A helper, not an inline case: macOS
# /bin/bash 3.2 reads the ")" of a case pattern written straight inside $( ) as the end of the
# substitution.
says_not_a_verdict() { case "$1" in *"Not a verdict"*) printf yes ;; *) printf no ;; esac; }

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

# fake_pr_body <text> [expected-pr-number]  -- makes a command that prints <text> as a body.
#
# The script under test runs it as "<cmd> <pr-number>". Give the second argument when the test is
# about WHICH pull request gets read: the reader then prints nothing for any other number, so
# choosing the wrong pull request fails the test instead of passing it by accident.
#
# Leave it out when the test is about the body TEXT. The number is then irrelevant, and pinning it
# would make an unrelated change to the pull request lookup fail a body-matching test.
fakes=0
fake_pr_body() {
  fakes=$((fakes + 1))
  printf '%s' "$1" > "$WORK/body.$fakes"
  {
    printf '#!/bin/sh\n'
    if [ -n "${2:-}" ]; then
      printf 'case "$1" in %s) ;; *) exit 1 ;; esac\n' "$2"
    fi
    printf 'cat "%s"\n' "$WORK/body.$fakes"
  } > "$WORK/prbody.$fakes.sh"
  chmod +x "$WORK/prbody.$fakes.sh"
  printf '%s' "$WORK/prbody.$fakes.sh"
}

# add_method <repo> <path> <method> <message>  -- appends a method to a file and commits it.
# The file must exist. Each method sits on its own line, so a later commit adds exactly one.
add_method() {
  printf 'void %s() {}\n' "$3" >> "$1/$2"
  git -C "$1" commit -q -am "$4"
}

# fake_prs <lines> [expected-sha]  -- makes a command that lists the pull requests holding a commit.
#
# Each line is "<number><TAB><branch>", the shape GitHub's commits/{sha}/pulls gives. The script runs
# it as "<cmd> <sha>". Give the second argument to answer only for that commit: any other commit gets
# an empty list, so asking about the wrong commit fails a test that expects a proof.
prfakes=0
fake_prs() {
  prfakes=$((prfakes + 1))
  printf '%s' "$1" > "$WORK/prs.$prfakes"
  {
    printf '#!/bin/sh\n'
    if [ -n "${2:-}" ]; then
      printf '[ "$1" = "%s" ] || exit 0\n' "$2"
    fi
    printf 'cat "%s"\n' "$WORK/prs.$prfakes"
  } > "$WORK/prs.$prfakes.sh"
  chmod +x "$WORK/prs.$prfakes.sh"
  printf '%s' "$WORK/prs.$prfakes.sh"
}

# rebased_then_merged <repo> <path> <later-merge-subject>  -- the pull request #2368 shape.
# The commit lands on master directly, the way a rebase-merge leaves it, so no merge commit carries
# it. An unrelated pull request is merged afterwards. That later merge is the oldest one on the
# commit's ancestry path, which is exactly the merge git would wrongly pick. Prints the commit's sha.
rebased_then_merged() {
  add "$1" README.md "First commit" >/dev/null
  add "$1" "$2" "Fixing review comments" >/dev/null
  local sha
  sha=$(git -C "$1" rev-parse HEAD)
  git -C "$1" checkout -q -b other
  add "$1" docs/notes.txt "Unrelated work" >/dev/null
  git -C "$1" checkout -q master
  git -C "$1" merge -q --no-ff -m "$3" other
  printf '%s' "$sha"
}

tab=$(printf '\t')

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
    *"CANNOT CHECK"*) printf 'CANNOT CHECK' ;;
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

# --- a merge is not asked when GitHub cannot be ------------------------------------------------
# The script used to fall back to git's oldest merge after a commit whenever GitHub could not be
# asked. That merge belongs to some other pull request whenever the real one was rebase-merged, which
# is the fault the GitHub lookup was written to fix. So a failed lookup now refuses. It says CANNOT
# CHECK, exits 3, and never proves through a merge. The harness makes the lookup fail by default.
#
# The rejected cases come first.

# A merge whose branch names the issue. Without GitHub it proves nothing.
r=$(new_repo)
add "$r" README.md "First commit"
git -C "$r" checkout -q -b krmahadevan-fix-765
add "$r" src/foo/BetaTest.java "Fixing review comments"
git -C "$r" checkout -q master
git -C "$r" merge -q --no-ff -m "Merge pull request #1374 from krmahadevan/krmahadevan-fix-765" \
  krmahadevan-fix-765
check "no GitHub: a merge branch is not a verdict" "CANNOT CHECK" \
  "$(verdict "$r" src/foo/BetaTest.java 765 COMMIT_PRS_CMD=false)"

# A merge whose body closes the issue. Without GitHub it proves nothing either.
r=$(new_repo)
add "$r" README.md "First commit"
git -C "$r" checkout -q -b some-work
add "$r" src/foo/EpsilonTest.java "Fixing review comments"
git -C "$r" checkout -q master
git -C "$r" merge -q --no-ff -m "Merge pull request #765 from someone/some-work" -m "Closes #1632" \
  some-work
check "no GitHub: a merge body is not a verdict" "CANNOT CHECK" \
  "$(verdict "$r" src/foo/EpsilonTest.java 1632 COMMIT_PRS_CMD=false)"

# A caller reads the exit code, not the words. 3 is what the issue check already uses for this.
(cd "$r" && COMMIT_PRS_CMD=false PROVENANCE_ONLY=1 bash "$SCRIPT" src/foo/EpsilonTest.java 1632 >/dev/null 2>&1)
check "no GitHub: the refusal exits 3" 3 "$?"

# The commit's own message needs no pull request, so it still proves the issue without GitHub.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Reject the empty name. Fixes #765"
check "no GitHub: the commit's own message still proves it" PROVEN \
  "$(verdict "$r" src/foo/AlphaTest.java 765 COMMIT_PRS_CMD=false)"

# With GitHub, the branch still counts even when the pull request number matches the issue.
r=$(new_repo)
sha=$(rebased_then_merged "$r" src/foo/DeltaTest.java "Merge pull request #999 from someone/unrelated")
check "the branch counts beside a matching pull request number" PROVEN \
  "$(verdict "$r" src/foo/DeltaTest.java 765 \
      "COMMIT_PRS_CMD=$(fake_prs "765${tab}fix-765" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')")"

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
# The body read is the body of a pull request GitHub lists for the commit, never one parsed from a
# subject. Each case below gets its own repository whose commit sits in pull request #1308.

body_verdict() {
  local body=$1 num=$2 r sha
  r=$(new_repo)
  sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
  verdict "$r" src/foo/AlphaTest.java "$num" \
    "COMMIT_PRS_CMD=$(fake_prs "1308${tab}feature-x" "$sha")" "PR_BODY_CMD=$(fake_pr_body "$body" 1308)"
}

check "the body closes the issue" PROVEN "$(body_verdict 'Fixes #765' 765)"
check "closes counts too" PROVEN "$(body_verdict 'Closes #765' 765)"
# GitHub ignores the case of its closing words.
check "an upper case closing word counts" PROVEN "$(body_verdict 'FIXES #765' 765)"

# --- bodies that must be REJECTED --------------------------------------------------------------
# A mention is not a claim. These five bodies all hold "765" and none of them says this pull
# request fixes issue 765.
for body in 'See #765 for details' \
            'This is not a fix for #765' \
            'Fixes #7650' \
            'Fixes #173' \
            ''; do
  check "body <$body> is not provenance" "NOT PROVEN" "$(body_verdict "$body" 765)"
done

# A closing word must be a whole word. Every body below holds a closing word inside a longer one,
# and GitHub closes nothing on any of them. "Still unresolved" is ordinary pull request English.
for body in 'Still unresolved: #765' \
            'This prefixes #765 onto the name' \
            'Nothing disclosed #765 here' \
            'Fixed: #100. Unfixed: #765'; do
  check "body <$body> is not a closing word" "NOT PROVEN" "$(body_verdict "$body" 765)"
done

# A body that could not be read is not a body that says nothing. When no source proves the reference
# and some body was not read, the answer is CANNOT CHECK with exit 3, never NOT PROVEN. A NOT PROVEN
# here would let a rate limit or a missing token delete a true reference.
two_prs_verdict() {
  local prs=$1 reader=$2 r sha
  r=$(new_repo)
  sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
  verdict "$r" src/foo/AlphaTest.java 765 \
    "COMMIT_PRS_CMD=$(fake_prs "$prs" "$sha")" "PR_BODY_CMD=$reader"
}
one_pr="1308${tab}feature-x"
two_prs="1308${tab}feature-x
900${tab}other"

check "a failing body reader is not a verdict" "CANNOT CHECK" \
  "$(two_prs_verdict "$one_pr" /nonexistent/reader)"

# The fault this guards: each body read overwrote the result of the one before. Only #900 is
# readable below, so the body of #1308 was never read, in either order.
check "an unreadable body before a silent one is not a verdict" "CANNOT CHECK" \
  "$(two_prs_verdict "$two_prs" "$(fake_pr_body 'Some cleanup' 900)")"
check "an unreadable body after a silent one is not a verdict" "CANNOT CHECK" \
  "$(two_prs_verdict "900${tab}other
1308${tab}feature-x" "$(fake_pr_body 'Some cleanup' 900)")"

# The rejected case stays rejected: two bodies, both read, and neither closes the issue.
check "two silent bodies that were both read are not provenance" "NOT PROVEN" \
  "$(two_prs_verdict "$two_prs" "$(fake_pr_body 'Some cleanup')")"

# A proof still wins over a body that could not be read.
check "a closing body proves it beside an unreadable one" PROVEN \
  "$(two_prs_verdict "$two_prs" "$(fake_pr_body 'Fixes #765' 900)")"
check "a branch proves it though its body is unreadable" PROVEN \
  "$(two_prs_verdict "1308${tab}fix-765" /nonexistent/reader)"

# A caller reads the exit code.
r=$(new_repo)
sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
(cd "$r" && COMMIT_PRS_CMD="$(fake_prs "$two_prs" "$sha")" PR_BODY_CMD="$(fake_pr_body 'Some cleanup' 900)" \
   PROVENANCE_ONLY=1 bash "$SCRIPT" src/foo/AlphaTest.java 765 >/dev/null 2>&1)
check "an unreadable body exits 3" 3 "$?"

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

# --- METHOD: prove a reference from the commit that added one method --------------------------
# A file's first commit proves nothing about a method added to it years later. ParallelTestTest
# holds a method from "Parallel test run is not working in 6.13.1" and two from "Unit tests for
# #2532", and the file itself came from a 2006 commit that names neither.
#
# The rejected cases come first. A refusal must never fall back to the file's own commit, because
# that answer reads exactly like a real one.

# The method is not in the file at all.
r=$(new_repo)
add "$r" src/foo/MixedTest.java "Fix #111"
check "method: a method the file never had is refused" "NO COMMIT" \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"

# Only a longer name exists. "target" must not match "targetMore".
r=$(new_repo)
add "$r" src/foo/MixedTest.java "Create the class"
add_method "$r" src/foo/MixedTest.java targetMore "Fix #111"
check "method: a longer name is a different method" "NO COMMIT" \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"

# "avoid target()" holds "void target()". It is not a declaration.
r=$(new_repo)
add "$r" src/foo/MixedTest.java "Create the class"
printf '// avoid target() here\n' >> "$r/src/foo/MixedTest.java"
git -C "$r" commit -q -am "Fix #111"
check "method: a word ending in void is not a declaration" "NO COMMIT" \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"

# Asking two modes at once is a mistake, not a choice. The file carries a real GITHUB-111 written by
# a commit that names #111, so BY_DESCRIPTION alone would prove it. Only the refusal fails this.
r=$(new_repo)
add "$r" src/foo/MixedTest.java "Create the class"
printf '/* GITHUB-111 */ void target() {}\n' >> "$r/src/foo/MixedTest.java"
git -C "$r" commit -q -am "Fix #111"
check "method: METHOD with BY_DESCRIPTION is refused" "NO COMMIT" \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target BY_DESCRIPTION=1)"

# The file's first commit names one issue and the method's commit names another.
r=$(new_repo)
add "$r" src/foo/MixedTest.java "Fix #111"
add_method "$r" src/foo/MixedTest.java target "Unit tests for #222"
check "method: the method's own commit proves it" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"
check "method: the file's commit does not prove the method" "NOT PROVEN" \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"

# The same method name in another file belongs to that file. The other file's method comes FIRST, so
# a lookup that leaves this file's history finds it as the oldest, and proves the wrong issue.
r=$(new_repo)
add "$r" src/bar/OtherTest.java "Create the other class"
add_method "$r" src/bar/OtherTest.java target "Fix #222"
add "$r" src/foo/MixedTest.java "Create the class"
add_method "$r" src/foo/MixedTest.java target "Fix #111"
check "method: another file's method does not answer" "NOT PROVEN" \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"

# The file moved after the method was added. The method still has its commit.
r=$(new_repo)
add "$r" old/place/MixedTest.java "Create the class"
add_method "$r" old/place/MixedTest.java target "Fix #111"
move "$r" old/place/MixedTest.java new/place/MixedTest.java
check "method: a moved file keeps its method's commit" PROVEN \
  "$(verdict "$r" new/place/MixedTest.java 111 METHOD=target)"

# --- which pull request holds the commit: ask GitHub, do not guess from git --------------------------
# The script took the oldest merge on the commit's ancestry path as "the merge that brought it in".
# That holds only when a merge commit carried it. Pull request #2368 was rebase-merged, so git's
# oldest merge after commit 839a01980 was #2375, a CVE fix. GitHub lists #2368 for that commit.
#
# The rejected cases come first.

tab=$(printf '\t')

# An unrelated later merge's branch names 765. GitHub says the commit is in #2368, which does not.
r=$(new_repo)
sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/fix-765")
check "prs: an unrelated later merge does not prove it" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765 \
      "COMMIT_PRS_CMD=$(fake_prs "2368${tab}github-2321" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')")"

# GitHub lists no pull request for the commit. The later merge still must not answer.
r=$(new_repo)
sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/fix-765")
check "prs: no pull request means no merge is used" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765 "COMMIT_PRS_CMD=$(fake_prs '' "$sha")")"

# A branch holding a longer number is not the issue.
r=$(new_repo)
sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
check "prs: a branch with a longer number does not prove it" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765 \
      "COMMIT_PRS_CMD=$(fake_prs "1374${tab}fix-7650" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')")"

# The pull request's own number is not the issue, even when the two match.
r=$(new_repo)
sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
check "prs: the pull request number is not the issue" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765 \
      "COMMIT_PRS_CMD=$(fake_prs "765${tab}cleanup" "$sha")" "PR_BODY_CMD=$(fake_pr_body '' 765)")"

# GitHub's pull request says it closes the issue. This is the #2321 case. The branch holds 23210, so
# the branch rule cannot answer: 2321 followed by a digit is a longer number. Only the body proves it.
r=$(new_repo)
sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
check "prs: the real pull request's body proves it" PROVEN \
  "$(verdict "$r" src/foo/AlphaTest.java 2321 \
      "COMMIT_PRS_CMD=$(fake_prs "2368${tab}github-23210" "$sha")" \
      "PR_BODY_CMD=$(fake_pr_body 'Closes #2321' 2368)")"

# GitHub's pull request branch names the issue.
r=$(new_repo)
sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
check "prs: the real pull request's branch proves it" PROVEN \
  "$(verdict "$r" src/foo/AlphaTest.java 765 \
      "COMMIT_PRS_CMD=$(fake_prs "1374${tab}krmahadevan-fix-765" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')")"

# GitHub cannot be asked. The merge git would pick is not used, and the answer says it is no verdict.
r=$(new_repo); merged_pr "$r" src/foo/BetaTest.java "Merge pull request #1374 from krmahadevan/krmahadevan-fix-765"
out=$(cd "$r" && COMMIT_PRS_CMD=false PROVENANCE_ONLY=1 bash "$SCRIPT" src/foo/BetaTest.java 765 2>&1)
check "prs: without GitHub the merge is not used" "CANNOT CHECK" \
  "$(verdict "$r" src/foo/BetaTest.java 765 COMMIT_PRS_CMD=false)"
check "prs: without GitHub the answer is not a verdict" yes \
  "$(says_not_a_verdict "$out")"

# --- a pull request branch must name the issue, not merely hold its digits --------------------------
# The branch rule took any digit-bounded number. So "java-17-support" proved issue 17, a dependabot
# branch ending in "assertj-core-3.27.3" proved 27, and "release-765" proved 765 -- a string the
# commit-message rule has always refused. A branch names an issue only after a word that marks one,
# such as fix or issue, and never as part of a version.
#
# The rejected branches come first. "prefix-765" and "tissue-765" hold a marking word inside a longer
# one, and only a word that stands alone marks an issue.

for pair in "java-17-support:17" \
            "dependabot/gradle/org.assertj-assertj-core-3.27.3:27" \
            "upgrade-guava-2019.1:2019" \
            "release-765:765" \
            "v0.765:765" \
            "fix-765.1:765" \
            "prefix-765:765" \
            "tissue-765:765"; do
  ref=${pair%:*}; n=${pair##*:}
  r=$(new_repo)
  sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
  check "branch: <$ref> does not name issue $n" "NOT PROVEN" \
    "$(verdict "$r" src/foo/AlphaTest.java "$n" \
        "COMMIT_PRS_CMD=$(fake_prs "1${tab}${ref}" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')")"
done

# Branches that do name the issue still prove it.
for pair in "krmahadevan-fix-765:765" "task/fix_3242:3242" "github-2321:2321" "issue-1234:1234" \
            "bugfix/fix_2587:2587" "GH-1234:1234"; do
  ref=${pair%:*}; n=${pair##*:}
  r=$(new_repo)
  sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
  check "branch: <$ref> names issue $n" PROVEN \
    "$(verdict "$r" src/foo/AlphaTest.java "$n" \
        "COMMIT_PRS_CMD=$(fake_prs "1${tab}${ref}" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')")"
done

if [ -s "$WORK/gh-calls" ]; then
  fail=$((fail + 1))
  printf 'FAIL  the tests reached the network\n      gh was called with:\n'
  sed 's/^/        /' "$WORK/gh-calls"
fi

printf '\n%s passed, %s failed\n' "$pass" "$fail"
[ "$fail" -eq 0 ]
