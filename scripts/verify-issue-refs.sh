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
frag=${1:?usage: verify-issue-refs.sh <path-fragment> [issue-number]}
num=${2:-}
rc=0

# Enumerate every historical path the fragment matches before picking a commit. Choosing the first
# match would tie a test to an unrelated commit, and the output reads exactly like the real thing.
reject_if_ambiguous() {
  local pattern=$1
  local paths
  # -M is passed on purpose. Git turns rename detection on by default, but a repository that sets
  # diff.renames=false would report every rename of a file as a new add. One file that moved three
  # times then looks like four different files, and this rejects it as ambiguous.
  paths=$(git log --all --diff-filter=A -M --format= --name-status -- "$pattern" \
            | awk '$1 == "A" { print $2 }' | grep -E '\.java$|\.kt$|\.groovy$' | sort -u)
  if [ "$(printf '%s\n' "$paths" | grep -c .)" -gt 1 ]; then
    # stderr, not stdout: callers redirect this function's stdout away, and a refusal that nobody
    # sees is worse than no check at all.
    echo "AMBIGUOUS   '$pattern' was added at more than one path; pass the full original path:" >&2
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
# The candidate list is repository wide, but the answer must be a commit that added the text to a
# file of this name. One issue number often sits in several files: GITHUB-1336 is in six and
# GITHUB-2830 in five. Taking the oldest of those would prove another file's reference, not this
# one. The pathspec is the file name alone, so a rename does not hide the commit.
if [ "$BY_DESCRIPTION" = 1 ]; then
  [ -n "$num" ] || { echo "BY_DESCRIPTION needs an issue number"; exit 1; }
  base=$(basename "$frag")
  for candidate in $(git log -G "GITHUB-${num}([^0-9]|\$)" --all --reverse --format=%H -- '*.java')
  do
    if git show "$candidate" -M --format= -- "*/$base" \
         | grep -qE "^\+.*GITHUB-${num}([^0-9]|\$)"; then
      sha=$candidate
      break
    fi
  done
  if [ -z "$sha" ]; then
    echo "no commit wrote \"GITHUB-$num\" into a file named $base"
    exit 1
  fi
  printf 'wrote it    %s\n' \
    "$(git show "$sha" -M --format= --name-only -- "*/$base" | tr '\n' ' ')"
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
if [ -z "$sha" ]; then
  short=$(last_two "$frag")
  for _ in 1 2 3 4 5 6 7 8 9 10; do
    reject_if_ambiguous "*$short" >/dev/null
    found=$(git log --all --reverse --diff-filter=A -M --format=%H -- "*$short" | head -1)
    [ -z "$found" ] && break
    sha=$found
    # git log reports a rename as an add unless -M is given, so re-read the commit with it.
    older=$(git show --name-status -M --format= "$sha" \
              | awk -v suffix="$short" '$1 ~ /^R/ && index($3, suffix) { print $2 }' | head -1)
    [ -z "$older" ] && break
    short=$(last_two "$older")
  done
fi

# Last resort: the file name alone.
if [ -z "$sha" ]; then
  reject_if_ambiguous "*/$(basename "$frag")" >/dev/null
  sha=$(git log --all --reverse --diff-filter=A -M --format=%H -- "*/$(basename "$frag")" | head -1)
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
# Reports the text that matched, so a reader can judge it. It searches the same cleaned string the
# rules did, never the raw message, or it would print a pull request number as the evidence.
matched_text() {
  printf '%s' "$(without_pr_number "$1")" | grep -oE "[A-Za-z/#-]*${num}([^0-9]|$)" | head -1
}
if names_num "$msg"; then
  printf 'provenance  the introducing commit names it: %s\n' "$(matched_text "$msg")"
else
  merge=$(git log --merges --ancestry-path --format=%H "$sha".."$BASE" 2>/dev/null | tail -1)
  mmsg=$([ -n "$merge" ] && git log -1 --format='%s | %b' "$merge" | tr '\n' ' ' | sed 's/  */ /g')
  if [ -n "${mmsg:-}" ] && { names_num "$mmsg" || branch_names_num "$mmsg"; }; then
    if names_num "$mmsg"; then evidence=$(matched_text "$mmsg")
    else evidence=$(printf '%s' "$mmsg" | sed -n 's/.*Merge pull request [^ ]* from \([^ |]*\).*/branch \1/p')
    fi
    printf 'provenance  the merge names it (%s): %s\n' "$evidence" "$mmsg"
  else
    printf 'provenance  NOT PROVEN -- neither the commit nor its merge names #%s\n' "$num"
    [ -n "${mmsg:-}" ] && printf '            merge was: %s\n' "$mmsg"
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
