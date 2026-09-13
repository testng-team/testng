#!/usr/bin/env bash
# Check an issue reference from both ends before writing it into a @Test description:
#
#   1. provenance -- the commit that introduced the test names THIS issue number, or its merge does
#   2. the issue  -- #<n> is a real GitHub issue, not a pull request
#
# Package names are not evidence: test.testng173 and test.testng317 look identical, and only one of
# them is a GitHub issue.
#
# The issue's state is reported but never enforced. A regression test can legitimately reference an
# issue that is still open.
#
#   scripts/verify-issue-refs.sh <path-fragment> [issue-number]
#   scripts/verify-issue-refs.sh github765/ExcludeSyntheticMethodsFromTemplateCallsTest.java 765
set -u
REPO=${REPO:-testng-team/testng}
# The branch that provenance is judged against. Overridable for a fork or a release branch.
BASE=${BASE:-master}
# Set to 1 to stop after step 1 and skip step 2. Step 2 needs the network and a GitHub token.
# The tests use this: they check the provenance rules, which is the part with logic in it.
PROVENANCE_ONLY=${PROVENANCE_ONLY:-0}
# Set to 1 when the description is already in the code, and the file holds more than one of them.
# The commit that wrote the file then proves nothing about one method in it. This finds the commit
# that wrote the text "GITHUB-<n>" instead. ParameterTest.java carries four such references, and
# ListenerTest.java carries seventeen.
#
# The search covers every Java file, not the path given. Restricting it to one path repeats the
# fault this script was written to fix: the 2021 module split moved every file, so the text looks
# added at the new path, and the split becomes the answer for six references out of nine. The path
# is still read, to say which file is being checked.
#
# It searches for a regex, not a plain string. "GITHUB-182" is the start of "GITHUB-1827", so a
# plain string finds whichever came first. Ten such pairs already exist in this tree.
BY_DESCRIPTION=${BY_DESCRIPTION:-0}
# The command that prints a pull request body. It is given the pull request number.
# Overridable so the tests can run offline: they pass a script that prints a fixed body.
PR_BODY_CMD=${PR_BODY_CMD:-}
frag=${1:?usage: verify-issue-refs.sh <path-fragment> [issue-number]}
num=${2:-}
rc=0

# Enumerate every historical path the fragment matches before picking a commit. Choosing the first
# match would tie a test to an unrelated commit, and the output reads exactly like the real thing.
# Every path in history that ends with the segments of $1.
#
# Two pathspecs are given. The bare one matches a path written from the repository root. The
# "*/" one matches the same segments deeper in the tree, and the slash is what keeps the match on a
# segment boundary: "factory/X.java" must not answer for "objectfactory/X.java", which is a
# different file with a different commit.
#
# -M is passed on purpose. Git turns rename detection on by default, but a repository that sets
# diff.renames=false would report every rename of a file as a new add. One file that moved three
# times then looks like four different files, and this rejects it as ambiguous.
added_paths() {
  git log --all --diff-filter=A -M --format= --name-status -- "$1" "*/$1" \
    | awk '$1 == "A" { print $2 }' | sort -u
}

reject_if_ambiguous() {
  local paths
  paths=$(added_paths "$1")
  if [ "$(printf '%s\n' "$paths" | grep -c .)" -gt 1 ]; then
    # stderr, not stdout: callers redirect this function's stdout away, and a refusal that nobody
    # sees is worse than no check at all.
    echo "AMBIGUOUS   '$1' was added at more than one path; pass more of the original path:" >&2
    printf '%s\n' "$paths" | sed 's/^/              /' >&2
    exit 2
  fi
  printf '%s' "$paths"
}

sha=""
# The commit that first wrote this description into this file. -G reports every commit whose diff
# holds a line matching the pattern, so the oldest is the one that added it. The pattern ends the
# number, or a search for issue 182 answers with the commit that wrote GITHUB-1827.
#
# The candidate list is repository wide, but the answer must be a commit that added the text to
# THIS file. One issue number often sits in several files: GITHUB-1336 is in six and GITHUB-2830 in
# five. Taking the oldest of those would prove another file's reference, not this one.
#
# The file is named by its history, not by its name. 45 files here are called IssueTest.java and 60
# TestClassSample.java, so a name matches other people's files. --follow gives every path this one
# file has had, and a candidate counts only if it wrote the text to one of them.
if [ "$BY_DESCRIPTION" = 1 ]; then
  [ -n "$num" ] || { echo "BY_DESCRIPTION needs an issue number"; exit 1; }
  lineage=$(git log --follow --format= --name-only -- "$frag" 2>/dev/null | grep -v '^$' | sort -u)
  if [ -z "$lineage" ]; then
    echo "no history for $frag; BY_DESCRIPTION needs a path git can follow"
    exit 1
  fi
  for candidate in $(git log -G "GITHUB-${num}([^0-9]|\$)" --all --reverse --format=%H -- '*.java')
  do
    for path in $lineage; do
      if git show "$candidate" -M --format= -- "$path" \
           | grep -qE "^\+.*GITHUB-${num}([^0-9]|\$)"; then
        sha=$candidate
        break 2
      fi
    done
  done
  if [ -z "$sha" ]; then
    echo "no commit wrote \"GITHUB-$num\" into $frag or any path it came from"
    exit 1
  fi
  printf 'wrote it    %s\n' "$(git log -1 --format=%h "$sha")  in $frag"
fi

# A path in the worktree is followed through renames first.
if [ -z "$sha" ] && [ -e "$frag" ]; then
  sha=$(git log --follow --reverse --diff-filter=A --format=%H -- "$frag" | head -1)
fi

# Then search history by the last two path segments, for example "github765/SomeTest.java".
# A full path is wrong here: the modules moved in 2021, so a modern path only matches history
# after that move, and the move itself then looks like the commit that wrote the test.
#
# The oldest "add" of a path is often a rename, not the original. Tests here have been moved twice
# already: once when the modules were split, and once when they were grouped by feature. So each
# time the add turns out to be a rename, take the old path and look again.
last_two() { printf '%s' "$1" | awk -F/ '{ if (NF>1) print $(NF-1)"/"$NF; else print $NF }'; }

# The shortest suffix of a path, two segments or more, that names one file in history.
#
# Two segments are usually enough, and they have to be the starting point: a full modern path only
# matches history after the 2021 module split, so the split itself becomes the answer. But two
# segments are not always enough. "github1131/GitHub1131Test.java" is one file under test/factory
# and another under test/objectfactory, added by two different commits. So the suffix grows a
# segment at a time until one file is left. The refusal above asks the caller for more of the path;
# this is what makes that advice work.
narrowest_suffix() {
  local path=$1 total n cand
  total=$(printf '%s' "$path" | awk -F/ '{ print NF }')
  [ "$total" -le 2 ] && { printf '%s' "$path"; return; }
  n=2
  while [ "$n" -le "$total" ]; do
    cand=$(printf '%s' "$path" | awk -F/ -v n="$n" \
      '{ s = $(NF - n + 1); for (j = NF - n + 2; j <= NF; j++) s = s "/" $j; print s }')
    [ "$(added_paths "$cand" | grep -c .)" -le 1 ] && { printf '%s' "$cand"; return; }
    n=$((n + 1))
  done
  # Nothing separated them. Report the two-segment ambiguity, which is the honest answer.
  printf '%s' "$(last_two "$path")"
}

if [ -z "$sha" ]; then
  short=$(narrowest_suffix "$frag")
  for _ in 1 2 3 4 5 6 7 8 9 10; do
    reject_if_ambiguous "$short" >/dev/null
    found=$(git log --all --reverse --diff-filter=A -M --format=%H -- "$short" "*/$short" | head -1)
    [ -z "$found" ] && break
    sha=$found
    # git log reports a rename as an add unless -M is given, so re-read the commit with it.
    # The suffix has to start at a segment here too. Matching "graph/IssueTest.java" inside
    # "dynamicgraph/IssueTest.java" sends the walk down another file's history.
    older=$(git show --name-status -M --format= "$sha" \
              | awk -v suffix="$short" \
                  '$1 ~ /^R/ && ($3 == suffix || index($3, "/" suffix) == length($3) - length(suffix)) \
                   { print $2 }' | head -1)
    [ -z "$older" ] && break
    short=$(narrowest_suffix "$older")
  done
fi

# The file name alone, and only when that is all the caller gave.
#
# A basename is the weakest key there is: 45 files here are called IssueTest.java. A caller who
# passes a path is asking about THAT file, so when no suffix of it matches anything, the answer is
# that the path has no history. Answering from the basename instead returns another file's commit,
# with an "introduced" line that reads exactly like the real thing.
if [ -z "$sha" ] && [ "$frag" = "$(basename "$frag")" ]; then
  reject_if_ambiguous "$frag" >/dev/null
  sha=$(git log --all --reverse --diff-filter=A -M --format=%H -- "$frag" "*/$frag" | head -1)
fi
if [ -z "$sha" ]; then echo "no introducing commit found for $frag"; exit 1; fi

# A file this migration has already moved looks "added" by the migration commit itself. That is our
# own work, not provenance, so refuse it and ask for the path the file had before the move.
if ! git merge-base --is-ancestor "$sha" "$BASE" 2>/dev/null; then
  echo "OWN COMMIT   the only commit that adds this path is $(git log -1 --format=%h "$sha"), which is not on $BASE."
  echo "             That is this migration moving the file, not the commit that wrote the test."
  echo "             Pass the path the file had before the move, or run this before moving it."
  exit 4
fi

printf 'introduced  %s  %s\n' "$(git log -1 --format=%h "$sha")" "$(git log -1 --format='%ad' --date=short "$sha")"
msg=$(git log -1 --format='%s %b' "$sha" | tr '\n' ' ' | sed 's/  */ /g')
printf 'message     %s\n' "${msg:-<EMPTY -- no provenance here>}"

[ -z "$num" ] && exit 0

# GitHub writes the pull request number into the subject of a merge commit and of a squash commit.
# Pull requests and issues share one number space, so "Merge pull request #765" and "Some fix
# (#765)" say nothing about issue #765. Remove those two forms before the text is searched.
#
# Removing them can only make this check stricter. A reference this drops was never proof. A
# reference it kept and should not have puts a false issue number into the code, which nobody sees.
without_pr_number() {
  printf '%s' "$1" | sed -e 's/Merge pull request #[0-9][0-9]*/Merge pull request/g' \
                         -e 's/(#[0-9][0-9]*)//g'
}

# Provenance must name the number being checked. Any issue marker is not enough: a commit that says
# "#123" does not prove anything about issue 765.
# A commit message must name the issue outright: "#765", "TESTNG-765" or "issues/765".
# Anything looser accepts text that proves nothing, such as "src/765/data.txt" or "release-765".
names_num() {
  printf '%s' "$(without_pr_number "$1")" | grep -qE "(TESTNG-|#|issues/)${num}([^0-9]|$)"
}

# A merge line may instead carry the issue in the branch it merges, such as
# "Merge pull request #1374 from krmahadevan/krmahadevan-fix-765". Only the branch is read, and
# only after "from", so an issue number elsewhere in the subject cannot stand in for it.
branch_names_num() {
  printf '%s' "$1" | sed -n 's/.*Merge pull request [^ ]* from \([^ |]*\).*/\1/p' \
    | grep -qE "(^|[^0-9])${num}([^0-9]|$)"
}

# Last source: the body of the pull request that carried the commit. GitHub closes an issue when a
# pull request body holds a closing word and the number, and neither the commit nor the merge has
# to repeat it. Issue #1307 was closed that way by pull request #1308, whose branch is called
# "feature/ignore-anonymous-tests". Phase 1 had to judge #765 and #1417 by hand for the same reason.
#
# Only the closing form counts. "See #765", "unlike #765" and "duplicate of #765" are mentions, and
# a mention proves nothing. These are GitHub's own closing words.
#
# The body is reached only through the pull request number that git already ties to this commit, so
# it cannot be some other pull request's body. That number comes from a merge subject GitHub wrote.
# A hand-written merge subject could name a pull request it did not come from; no merge in this
# repository is hand-written.
#
# GitHub reads its closing words without regard to case, so "FIXES #765" closes the issue as
# surely as "Fixes #765". The searches below pass -i for that reason.
#
# The word must stand alone. Without the boundary in front, "prefixes #765" holds "fixes" and
# "Still unresolved: #765" holds "resolved", and GitHub closes nothing on either. Both would have
# been written into the code as proof.
CLOSING='(close[sd]?|fix(e[sd])?|resolve[sd]?)'
CLOSES_NUM="(^|[^[:alnum:]_])${CLOSING}:? +(https://github\.com/${REPO}/issues/|#)${num}([^0-9]|$)"
body_closes_num() {
  printf '%s' "$1" | grep -qiE "$CLOSES_NUM"
}
# Reports the closing phrase that matched, so a reader can judge it.
matched_close() {
  printf '%s' "$1" | grep -oiE "$CLOSES_NUM" | head -1 | sed 's/^[^[:alnum:]_]*//'
}
# The pull request a commit arrived in. Two forms, and only these two:
#
#   "Merge pull request #<n> from <branch>"   -- the subject of a merge GitHub made
#   "... (#<n>)"                              -- the tail of a subject GitHub squashed
#
# Both are SUBJECTS. A commit body may name any pull request at all: "reverts (#1308)", "follows on
# from (#900)". Reading one of those bodies would prove nothing about this commit, and the rule
# above promises it cannot happen. So the squashed form is anchored to the end of the line, and
# callers pass a subject.
pr_number_of() {
  printf '%s' "$1" | sed -n -e 's/^Merge pull request #\([0-9][0-9]*\) from .*/\1/p' \
                            -e 's/.*(#\([0-9][0-9]*\))[[:space:]]*$/\1/p' | head -1
}
# Prints the body of pull request $1. Exits non-zero when it could not be read at all, which is not
# the same as a body that says nothing: the caller reports the two differently.
pr_body() {
  if [ -n "$PR_BODY_CMD" ]; then
    $PR_BODY_CMD "$1" 2>/dev/null
  else
    gh api "repos/$REPO/pulls/$1" --jq .body 2>/dev/null
  fi
}
# Reports the text that matched, so a reader can judge it. It searches the same cleaned string the
# rules did, never the raw message, or it would print a pull request number as the evidence.
matched_text() {
  # The same alternation names_num applies. A looser one printed "release-765" as the evidence for
  # a commit that was proven by "fix #765", which tells the reader the opposite of the truth.
  printf '%s' "$(without_pr_number "$1")" \
    | grep -oE "(TESTNG-|#|issues/)${num}([^0-9]|$)" | head -1
}
# Three sources may prove the reference, strongest first. The first that answers wins.
proven=""
if names_num "$msg"; then
  proven="the introducing commit names it: $(matched_text "$msg")"
else
  merge=$(git log --merges --ancestry-path --format=%H "$sha".."$BASE" 2>/dev/null | tail -1)
  mmsg=$([ -n "$merge" ] && git log -1 --format='%s | %b' "$merge" | tr '\n' ' ' | sed 's/  */ /g')
  if [ -n "${mmsg:-}" ] && names_num "$mmsg"; then
    proven="the merge names it ($(matched_text "$mmsg")): $mmsg"
  elif [ -n "${mmsg:-}" ] && branch_names_num "$mmsg"; then
    branch=$(printf '%s' "$mmsg" | sed -n 's/.*Merge pull request [^ ]* from \([^ |]*\).*/branch \1/p')
    proven="the merge names it ($branch): $mmsg"
  else
    # Subjects only. $msg and $mmsg hold the body too, and a body may name any pull request.
    pr=$([ -n "$merge" ] && pr_number_of "$(git log -1 --format=%s "$merge")")
    [ -n "$pr" ] || pr=$(pr_number_of "$(git log -1 --format=%s "$sha")")
    if [ -n "$pr" ]; then
      prbody=$(pr_body "$pr"); body_rc=$?
      if [ "$body_rc" = 0 ] && body_closes_num "$prbody"; then
        proven="pull request #$pr says it closes the issue: $(matched_close "$prbody")"
      fi
    fi
  fi
fi

if [ -n "$proven" ]; then
  printf 'provenance  %s\n' "$proven"
else
  printf 'provenance  NOT PROVEN -- the commit does not name #%s\n' "$num"
  [ -n "${mmsg:-}" ] && printf '            merge was: %s\n' "$mmsg"
  # A body that could not be read is not a body that says nothing. Saying so would let an offline
  # run, a rate limit or a missing token read as a verdict, and a true reference would be deleted.
  if [ -z "${pr:-}" ]; then
    printf '            no pull request number in the subject, so no body was read\n'
  elif [ "${body_rc:-0}" != 0 ]; then
    printf '            pull request #%s could not be read, so its body is NOT a verdict\n' "$pr"
  else
    printf '            pull request #%s does not say it closes the issue\n' "$pr"
  fi
  if [ "$BY_DESCRIPTION" = 1 ]; then
    # An earlier phase of this migration may have written the description itself. Its commit
    # names no issue, because the proof came from the file, not from the text. Do not delete a
    # reference on this answer alone.
    echo "            This mode judges the commit that wrote the text. If an earlier phase of"
    echo "            this migration wrote it, that commit names no issue. Check the same"
    echo "            reference without BY_DESCRIPTION, and check docs/test-issue-references.md,"
    echo "            before removing it."
  fi
  rc=1
fi

[ "$PROVENANCE_ONLY" = 1 ] && exit $rc

# Distinguish "no such issue" from an API that is unreachable, rate limited or unauthenticated.
# Treating those alike would delete valid references.
body=$(gh api "repos/$REPO/issues/$num" 2>/dev/null)
if [ -z "$body" ]; then
  status=$(gh api "repos/$REPO/issues/$num" 2>&1 | grep -oE 'HTTP [0-9]+' | head -1)
  case "$status" in
    "HTTP 404") echo "issue       #$num does not exist"; exit 1 ;;
    *)          echo "issue       CANNOT CHECK -- the API call failed (${status:-no status}). Not a verdict."; exit 3 ;;
  esac
fi

if printf '%s' "$body" | grep -q '"pull_request"'; then
  printf 'issue       #%s is a PULL REQUEST, not an issue\n' "$num"
  rc=1
else
  printf 'issue       #%s is an issue, %s (state is reported, never enforced)\n' \
    "$num" "$(printf '%s' "$body" | python3 -c 'import sys,json;print(json.load(sys.stdin)["state"])')"
fi
printf 'title       %s\n' "$(printf '%s' "$body" | python3 -c 'import sys,json;print(json.load(sys.stdin)["title"])')"

if gh api "repos/$REPO/issues/$num/timeline" --paginate \
     -q '.[] | select(.event=="referenced" or .event=="closed") | .commit_id // empty' 2>/dev/null | grep -qx "$sha"; then
  echo "timeline    links the introducing commit"
else
  echo "timeline    does not link the commit; the provenance line above is what counts"
fi
exit $rc
