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
check "a merged PR number is not the issue" "NOT PROVEN" "$(verdict "$r" src/foo/GammaTest.java 765)"

# The same rule for a squashed pull request, where GitHub puts the number at the end of the subject.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Speed up the parser. (#765)"
check "a squashed PR number is not the issue" "NOT PROVEN" "$(verdict "$r" src/foo/AlphaTest.java 765)"

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

# The mode changes how the commit is found. It does not soften the rules.
r=$(new_repo)
add "$r" src/foo/OneTest.java "Create the test class"
printf 'class X { /* GITHUB-765 */ }\n' > "$r/src/foo/OneTest.java"
git -C "$r" commit -q -am "Cut release-765"
check "by description still applies the rules" "NOT PROVEN" \
  "$(verdict "$r" src/foo/OneTest.java 765 BY_DESCRIPTION=1)"

printf '\n%s passed, %s failed\n' "$pass" "$fail"
[ "$fail" -eq 0 ]
