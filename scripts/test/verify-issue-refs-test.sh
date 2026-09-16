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
# GitHub API either. PROVENANCE_ONLY=1 stops the script before the issue check, and each GitHub
# lookup the provenance step makes is given a local command. The cases about the issue check give
# the script a local "gh" instead.
set -u
SCRIPT=$(cd "$(dirname "$0")/../.." && pwd)/scripts/verify-issue-refs.sh
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

# The header above promises these tests reach no network. Promising it is not enough: a case that
# gives the script no local command makes it call "gh api" itself. So a "gh" that refuses and
# records the call goes first on PATH. A recorded call fails the run at the end, which turns the
# promise into a check.
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

# says <words> <text>  -- prints yes when the text holds the words, no when it does not.
# A helper, not an inline case: macOS /bin/bash 3.2 reads the ")" of a case pattern written straight
# inside $( ) as the end of the substitution.
says() { case "$2" in *"$1"*) printf yes ;; *) printf no ;; esac; }

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

# write <repo> <path> <message> <content>  -- writes the file with exactly this content, and commits.
write() {
  mkdir -p "$1/$(dirname "$2")"
  printf '%s\n' "$4" > "$1/$2"
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
#
# Every fake gets its own file from mktemp. The helpers run inside $( ), so a counter kept in the
# shell never moves, and each new fake would overwrite the one before it.
fake_pr_body() {
  local cmd
  cmd=$(mktemp "$WORK/prbody.XXXXXX")
  printf '%s' "$1" > "$cmd.txt"
  {
    printf '#!/bin/sh\n'
    if [ -n "${2:-}" ]; then
      printf 'case "$1" in %s) ;; *) exit 1 ;; esac\n' "$2"
    fi
    printf 'cat "%s"\n' "$cmd.txt"
  } > "$cmd"
  chmod +x "$cmd"
  printf '%s' "$cmd"
}

# add_method <repo> <path> <method> <message>  -- adds a method inside the class, and commits it.
# The file must exist. The class is written out again with the methods it already has, one per line,
# so a later commit adds exactly one line.
add_method() {
  { printf 'class X {\n'; grep '^  void .*() {}$' "$1/$2"; printf '  void %s() {}\n}\n' "$3"; } \
    > "$WORK/method.java"
  mv "$WORK/method.java" "$1/$2"
  git -C "$1" commit -q -am "$4"
}

# fake_prs <lines> [expected-sha]  -- makes a command that lists the pull requests holding a commit.
#
# Each line is "<number><TAB><branch><TAB><title>", the shape the script's own filter makes of
# GitHub's commits/{sha}/pulls. The title may be left out. The script runs it as "<cmd> <sha>". Give
# the second argument to answer only for that commit: any other commit gets an empty list, so asking
# about the wrong commit fails a test that expects a proof.
fake_prs() {
  local cmd
  cmd=$(mktemp "$WORK/prs.XXXXXX")
  printf '%s' "$1" > "$cmd.txt"
  {
    printf '#!/bin/sh\n'
    if [ -n "${2:-}" ]; then
      printf '[ "$1" = "%s" ] || exit 0\n' "$2"
    fi
    printf 'cat "%s"\n' "$cmd.txt"
  } > "$cmd"
  chmod +x "$cmd"
  printf '%s' "$cmd"
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
# The commit itself names no issue. The merge names one, and the script must not read it.
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

# A pull request number is not an issue number. Pull requests and issues share one number space.
# GitHub writes the pull request number at the end of a squash commit's subject. The body of a squash
# commit lists the subjects of the commits it holds, and those can be merges.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Speed up the parser. (#765)"
check "a squashed pull request number is not provenance" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765)"

r=$(new_repo)
add "$r" src/foo/AlphaTest.java "Speed up the parser. (#999)

* Merge pull request #765 from someone/unrelated"
check "a merged pull request number is not provenance" "NOT PROVEN" \
  "$(verdict "$r" src/foo/AlphaTest.java 765)"

# Removing the pull request number must not remove the issue number beside it.
r=$(new_repo); add "$r" src/foo/AlphaTest.java "Reject the empty name. Fixes #765 (#800)"
check "an issue beside a squashed number is still provenance" PROVEN \
  "$(verdict "$r" src/foo/AlphaTest.java 765)"

# --- without GitHub, only the commit's own message proves a reference ---------------------------
# Only GitHub knows which pull request holds a commit. git's oldest merge after the commit belongs to
# some other pull request whenever the real one was rebase-merged. So when the lookup fails, the
# script says CANNOT CHECK and exits 3, and a merge proves nothing. The cases below make the lookup
# fail by passing COMMIT_PRS_CMD=false.
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
check "the refusal is on stderr" yes "$(says AMBIGUOUS "$err")"
check "the refusal is not on stdout" no "$(says AMBIGUOUS "$out")"

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
check "resolves counts too" PROVEN "$(body_verdict 'Resolves #765' 765)"
check "resolved counts too" PROVEN "$(body_verdict 'resolved #765' 765)"
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

# --- the pull request title names the issue -------------------------------------------------------
# GitHub writes a pull request's title into the body of its merge commit. Pull request #1065 is titled
# "Fix issue #1009: Iterator<Object[]> DataProvider: indices not working". Its commit and its body
# name no issue, so only the title proves that reference. A title follows the rule for a commit
# message.
title_verdict() {
  local title=$1 num=$2 r sha
  r=$(new_repo)
  sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
  verdict "$r" src/foo/AlphaTest.java "$num" \
    "COMMIT_PRS_CMD=$(fake_prs "1065${tab}feature-x${tab}${title}" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')"
}

# The rejected titles come first. Each holds the number, and none names the issue.
for title in 'Speed up the parser (#765)' 'Cut release-765' 'Fix #7650' 'Fix #173' ''; do
  check "title <$title> is not provenance" "NOT PROVEN" "$(title_verdict "$title" 765)"
done

check "a title proves it" PROVEN \
  "$(title_verdict 'Fix issue #1009: Iterator<Object[]> DataProvider: indices not working' 1009)"
check "a TESTNG title proves it" PROVEN "$(title_verdict 'TESTNG-765 reject the empty name' 765)"

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

# A name that is not a Java identifier is refused, before any search can read it as a pattern.
r=$(new_repo)
add "$r" src/foo/MixedTest.java "Create the class"
add_method "$r" src/foo/MixedTest.java target "Fix #111"
for name in 'tar.et' 'tar get' 'target(' '1target'; do
  out=$(cd "$r" && METHOD="$name" PROVENANCE_ONLY=1 bash "$SCRIPT" src/foo/MixedTest.java 111 2>&1)
  check "method: <$name> is refused as a name" yes "$(says 'plain method name' "$out")"
done

# A matcher that fails gives no answer. Read as "no method here", it would refuse every method, or
# pick the wrong commit. A python3 that fails stands in for any failure of the matcher.
r=$(new_repo)
add "$r" src/foo/MixedTest.java "Create the class"
add_method "$r" src/foo/MixedTest.java target "Fix #111"
mkdir -p "$WORK/brokenpython"
printf '#!/bin/sh\nexit 1\n' > "$WORK/brokenpython/python3"
chmod +x "$WORK/brokenpython/python3"
out=$(cd "$r" && PATH="$WORK/brokenpython:$PATH" METHOD=target PROVENANCE_ONLY=1 \
        bash "$SCRIPT" src/foo/MixedTest.java 111 2>&1)
check "method: a failing matcher stops the script" yes "$(says 'matcher failed' "$out")"
check "method: a failing matcher gives no verdict" no "$(says provenance "$out")"

# The text "void target()" in a comment or a string is not the method. An older commit wrote both,
# and the method came later.
r=$(new_repo)
write "$r" src/foo/MixedTest.java "Create the class" 'class X {
  // call void target() later
  String s = "void target() {}";
}'
write "$r" src/foo/MixedTest.java "Unit tests for #222" 'class X {
  // call void target() later
  String s = "void target() {}";
  void target() {}
}'
check "method: a comment or a string is not the method" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"

# A nested class with a method of the same name is not this class's method.
r=$(new_repo)
write "$r" src/foo/MixedTest.java "Add a sample class" 'class X {
  static class Inner {
    void target() {}
  }
}'
check "method: a nested class's method alone is refused" "NO COMMIT" \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"
write "$r" src/foo/MixedTest.java "Unit tests for #222" 'class X {
  static class Inner {
    void target() {}
  }
  void target() {}
}'
check "method: a nested class's method is not the method" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"

# An edit to the declaration does not add the method.
r=$(new_repo)
write "$r" src/foo/MixedTest.java "Fix #111" 'class X {
  void target() {}
}'
write "$r" src/foo/MixedTest.java "Declare the exception. Closes #222" 'class X {
  void target() throws Exception {}
}'
check "method: an edit to the declaration is not the answer" "NOT PROVEN" \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"
check "method: the commit that added it still proves it" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"

# A method removed and written again belongs to the commit that wrote it again.
r=$(new_repo)
write "$r" src/foo/MixedTest.java "Fix #111" 'class X {
  void target() {}
}'
write "$r" src/foo/MixedTest.java "Remove the method" 'class X {
}'
write "$r" src/foo/MixedTest.java "Bring the check back. Fixes #222" 'class X {
  void target() {}
}'
check "method: the removed method's commit does not answer" "NOT PROVEN" \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"
check "method: the commit that wrote it again proves it" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"

# A branch that never merged holds an older commit that added the same method. It is not on the
# history of this file, so it must not answer. The dates are fixed: commits made in the same second
# come out of git in no set order, and the branch commit has to be the older one.
r=$(new_repo)
GIT_AUTHOR_DATE=2020-01-01T00:00:00 GIT_COMMITTER_DATE=2020-01-01T00:00:00 \
  write "$r" src/foo/MixedTest.java "Create the class" 'class X {
}'
git -C "$r" checkout -q -b github-111
GIT_AUTHOR_DATE=2020-02-01T00:00:00 GIT_COMMITTER_DATE=2020-02-01T00:00:00 \
  write "$r" src/foo/MixedTest.java "Fix #111" 'class X {
  void target() {}
}'
git -C "$r" checkout -q master
GIT_AUTHOR_DATE=2020-03-01T00:00:00 GIT_COMMITTER_DATE=2020-03-01T00:00:00 \
  write "$r" src/foo/MixedTest.java "Unit tests for #222" 'class X {
  void target() {}
}'
check "method: an older branch that never merged does not answer" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"

# The same, with the branch commit newer than the commit on master. A walk that read every branch
# would meet the branch commit first.
r=$(new_repo)
GIT_AUTHOR_DATE=2020-01-01T00:00:00 GIT_COMMITTER_DATE=2020-01-01T00:00:00 \
  write "$r" src/foo/MixedTest.java "Create the class" 'class X {
}'
GIT_AUTHOR_DATE=2020-02-01T00:00:00 GIT_COMMITTER_DATE=2020-02-01T00:00:00 \
  write "$r" src/foo/MixedTest.java "Unit tests for #222" 'class X {
  void target() {}
}'
git -C "$r" checkout -q -b github-111 HEAD~1
GIT_AUTHOR_DATE=2020-03-01T00:00:00 GIT_COMMITTER_DATE=2020-03-01T00:00:00 \
  write "$r" src/foo/MixedTest.java "Fix #111" 'class X {
  void target() {}
}'
git -C "$r" checkout -q master
check "method: a newer branch that never merged does not answer" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 222 METHOD=target)"

# A copied file is a new file. The commit that copied it added its methods, and the file it came
# from answers only for itself.
r=$(new_repo)
write "$r" src/a/OldTest.java "Regression test. Fix #111" 'class X {
  void target() {}
  void other() {}
  void third() {}
}'
mkdir -p "$r/src/b"
cp "$r/src/a/OldTest.java" "$r/src/b/NewTest.java"
git -C "$r" add -A && git -C "$r" commit -q -m "Copy the old test for a new bug. Fix #222"
check "method: a copy does not answer with the original's commit" "NOT PROVEN" \
  "$(verdict "$r" src/b/NewTest.java 111 METHOD=target)"
check "method: a copy answers with the commit that copied it" PROVEN \
  "$(verdict "$r" src/b/NewTest.java 222 METHOD=target)"

# A change on a side branch, merged later, does not break the file's history. The side branch
# changed the file after the method was added on master, but from a version without the method.
r=$(new_repo)
write "$r" src/foo/MixedTest.java "Create the class" 'class X {
  void one() {}
  // a
  // b
  // c
}'
write "$r" src/foo/MixedTest.java "Fix #111" 'class X {
  void one() {}
  // a
  // b
  // c
  void target() {}
}'
git -C "$r" checkout -q -b side HEAD~1
write "$r" src/foo/MixedTest.java "Tweak the first method" 'class X {
  void one() { }
  // a
  // b
  // c
}'
git -C "$r" checkout -q master
git -C "$r" merge -q --no-ff -m "Merge the side branch" side
check "method: a side branch merged later does not hide the commit" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"

# The formatter wraps a long declaration onto two lines. It is still the method.
r=$(new_repo)
write "$r" src/foo/MixedTest.java "Create the class" 'class X {
}'
write "$r" src/foo/MixedTest.java "Fix #111" 'class X {
  public void
      target() {}
}'
check "method: a wrapped declaration is found" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"

# A method that returns a value is a method too.
r=$(new_repo)
write "$r" src/foo/MixedTest.java "Create the class" 'class X {
}'
write "$r" src/foo/MixedTest.java "Fix #111" 'class X {
  public Object[][] target() {
    return null;
  }
}'
check "method: a method with a return value is found" PROVEN \
  "$(verdict "$r" src/foo/MixedTest.java 111 METHOD=target)"

# Run from a module directory, both modes still read the file's whole history. The file moved into
# the module after the method was added, the way the 2021 module split moved every test.
r=$(new_repo)
add "$r" src/foo/MixedTest.java "Create the class"
add_method "$r" src/foo/MixedTest.java target "Fix #111"
move "$r" src/foo/MixedTest.java core/src/foo/MixedTest.java
check "method: run from a module directory" PROVEN \
  "$(verdict "$r/core" src/foo/MixedTest.java 111 METHOD=target)"
r=$(new_repo)
add "$r" src/foo/AlphaTest.java "Fix #111"
move "$r" src/foo/AlphaTest.java core/src/foo/AlphaTest.java
check "a path given from a module directory" PROVEN "$(verdict "$r/core" src/foo/AlphaTest.java 111)"

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

# A name that ends with this one is a different method. The name must start where the type ends,
# not part of the way through a word.
r=$(new_repo)
write "$r" src/foo/MixedTest.java "Fix #111" 'class X {
  void nottarget() {}
}'
check "method: a name that ends with this one is a different method" "NO COMMIT" \
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

# --- which pull request holds the commit: ask GitHub, not git ------------------------------------
# git's oldest merge after a commit is the merge that brought it in only when a merge commit carried
# it. Pull request #2368 was rebase-merged, so the oldest merge after commit 839a01980 is #2375, a fix
# for a security advisory. GitHub lists #2368 for that commit.
#
# The rejected cases come first.

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
  "$(says "Not a verdict" "$out")"

# --- a pull request branch must name the issue, not merely hold its digits --------------------------
# Digits alone do not name an issue. They would put "java-17-support" behind issue 17, a dependabot
# branch ending in "assertj-core-3.27.3" behind 27, and "release-765" behind 765 -- a string the
# commit-message rule refuses. A branch names an issue only after a word that marks one, such as fix
# or issue, and never as part of a version.
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
            "tissue-765:765" \
            "issue-#7650:765"; do
  ref=${pair%:*}; n=${pair##*:}
  r=$(new_repo)
  sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
  check "branch: <$ref> does not name issue $n" "NOT PROVEN" \
    "$(verdict "$r" src/foo/AlphaTest.java "$n" \
        "COMMIT_PRS_CMD=$(fake_prs "1${tab}${ref}" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')")"
done

# Branches that do name the issue still prove it.
for pair in "krmahadevan-fix-765:765" "task/fix_3242:3242" "github-2321:2321" "issue-1234:1234" \
            "bugfix/fix_2587:2587" "GH-1234:1234" "issue-#1009-dp-indices-on-iterator:1009" \
            "issue-#809:809"; do
  ref=${pair%:*}; n=${pair##*:}
  r=$(new_repo)
  sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
  check "branch: <$ref> names issue $n" PROVEN \
    "$(verdict "$r" src/foo/AlphaTest.java "$n" \
        "COMMIT_PRS_CMD=$(fake_prs "1${tab}${ref}" "$sha")" "PR_BODY_CMD=$(fake_pr_body '')")"
done

# --- the script's own GitHub commands -----------------------------------------------------------
# Every case above gives the script a local lookup command. These give it none, so the script runs
# its own "gh api ... --jq" commands. A local "gh" answers with JSON in GitHub's shape, and applies
# the filter with jq, the way gh does. A wrong field in a filter, such as base.ref for head.ref,
# then fails here and not on real data.
#
# The local "gh" answers only the endpoints a case prepared. Any other call is recorded, and fails
# the run at the end.
mkdir -p "$WORK/api"
cat > "$WORK/api/gh" <<SHIM
#!/bin/sh
f="$WORK/api/\$(printf '%s' "\$2" | tr '/' '_').json"
if [ "\$1" != api ] || [ "\$3" != --jq ] || [ ! -f "\$f" ]; then
  echo "\$*" >> "$WORK/gh-calls"
  exit 1
fi
exec jq -r "\$4" "\$f"
SHIM
chmod +x "$WORK/api/gh"

# api_verdict <num> <pulls-json> [<pr-number> <pr-json>]  -- the verdict through the script's own
# commands. The pulls JSON is GitHub's answer for the commit. The pull request JSON is GitHub's answer
# for that one pull request, when a case reads its body.
api_verdict() {
  local num=$1 r sha
  r=$(new_repo)
  sha=$(rebased_then_merged "$r" src/foo/AlphaTest.java "Merge pull request #999 from someone/unrelated")
  printf '%s' "$2" > "$WORK/api/repos_testng-team_testng_commits_${sha}_pulls.json"
  [ -n "${3:-}" ] && printf '%s' "$4" > "$WORK/api/repos_testng-team_testng_pulls_$3.json"
  verdict "$r" src/foo/AlphaTest.java "$num" COMMIT_PRS_CMD= PR_BODY_CMD= "PATH=$WORK/api:$PATH"
}

if ! command -v jq >/dev/null 2>&1; then
  fail=$((fail + 1))
  printf 'FAIL  jq is needed to check the script'"'"'s own GitHub commands\n'
else
  check "api: the branch comes from head.ref" PROVEN "$(api_verdict 765 \
    '[{"number":1374,"title":"Guard the listener list","head":{"ref":"krmahadevan-fix-765"},"base":{"ref":"master"}}]')"
  check "api: the title comes from title" PROVEN "$(api_verdict 1009 \
    '[{"number":1065,"title":"Fix issue #1009: Iterator<Object[]> DataProvider: indices not working","head":{"ref":"feature-x"},"base":{"ref":"master"}}]')"
  check "api: the body comes from the pull request" PROVEN "$(api_verdict 1307 \
    '[{"number":1308,"title":"Ignore anonymous tests","head":{"ref":"feature/ignore-anonymous-tests"},"base":{"ref":"master"}}]' \
    1308 '{"number":1308,"title":"Ignore anonymous tests","body":"Fixes #1307"}')"
  check "api: a base branch that names the issue is not the pull request's branch" "NOT PROVEN" \
    "$(api_verdict 765 \
    '[{"number":1374,"title":"Guard the listener list","head":{"ref":"feature-x"},"base":{"ref":"fix-765"}}]' \
    1374 '{"number":1374,"title":"Guard the listener list","body":"No closing word here"}')"
fi

# --- the issue check: a failed call is not an answer --------------------------------------------
# The issue check runs only without PROVENANCE_ONLY. When a call fails, gh exits non-zero but still
# prints GitHub's error message, as JSON, on stdout. Read as an issue, a bad token passes a pull
# request number as an issue, with exit 0. So each case here gives the script a local "gh".
#
# issue_check <stdout> <stderr> <exit>  -- runs the whole script against a commit that names #765,
# with a "gh" that answers the issue call this way. Prints "<exit code> <output>".
issue_check() {
  local r d
  r=$(new_repo)
  add "$r" src/foo/AlphaTest.java "Reject the empty name. Fixes #765"
  d=$(mktemp -d "$WORK/issuegh.XXXXXX")
  printf '%s' "$1" > "$d/stdout"
  printf '%s\n' "$2" > "$d/stderr"
  cat > "$d/gh" <<SHIM
#!/bin/sh
case "\$2" in
  */issues/765/timeline) exit 0 ;;
  */issues/765) cat "$d/stdout"; cat "$d/stderr" >&2; exit $3 ;;
esac
echo "\$*" >> "$WORK/gh-calls"
exit 1
SHIM
  chmod +x "$d/gh"
  out=$(cd "$r" && PATH="$d:$PATH" bash "$SCRIPT" src/foo/AlphaTest.java 765 2>&1)
  printf '%s %s' "$?" "$out"
}

out=$(issue_check '{"message":"Bad credentials","documentation_url":"https://docs.github.com/rest","status":"401"}' \
        'gh: Bad credentials (HTTP 401)' 1)
check "issue: a rejected token exits 3" 3 "${out%% *}"
check "issue: a rejected token is not a verdict" yes "$(says "CANNOT CHECK" "$out")"

out=$(issue_check '{"message":"Not Found","documentation_url":"https://docs.github.com/rest","status":"404"}' \
        'gh: Not Found (HTTP 404)' 1)
check "issue: a missing issue exits 1" 1 "${out%% *}"
check "issue: a missing issue says so" yes "$(says "does not exist" "$out")"

out=$(issue_check '{"number":765,"state":"closed","title":"Some change","pull_request":{"url":"x"}}' '' 0)
check "issue: a pull request exits 1" 1 "${out%% *}"
check "issue: a pull request says so" yes "$(says "PULL REQUEST" "$out")"

out=$(issue_check '{"number":765,"state":"open","title":"Some bug"}' '' 0)
check "issue: a real issue exits 0" 0 "${out%% *}"
check "issue: a real issue says so" yes "$(says "is an issue, open" "$out")"

if [ -s "$WORK/gh-calls" ]; then
  fail=$((fail + 1))
  printf 'FAIL  the tests reached the network\n      gh was called with:\n'
  sed 's/^/        /' "$WORK/gh-calls"
fi

printf '\n%s passed, %s failed\n' "$pass" "$fail"
[ "$fail" -eq 0 ]
