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
frag=${1:?usage: verify-issue-refs.sh <path-fragment> [issue-number]}
num=${2:-}
rc=0

# Enumerate every historical path the fragment matches before picking a commit. Choosing the first
# match would tie a test to an unrelated commit, and the output reads exactly like the real thing.
reject_if_ambiguous() {
  local pattern=$1
  local paths
  paths=$(git log --all --diff-filter=A --format= --name-only -- "$pattern" | grep -E '\.java$|\.kt$|\.groovy$' | sort -u)
  if [ "$(printf '%s\n' "$paths" | grep -c .)" -gt 1 ]; then
    echo "AMBIGUOUS   '$pattern' was added at more than one path; pass the full original path:"
    printf '%s\n' "$paths" | sed 's/^/              /'
    exit 2
  fi
  printf '%s' "$paths"
}

sha=""
# A path in the worktree is followed through renames first.
if [ -e "$frag" ]; then
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
    found=$(git log --all --reverse --diff-filter=A --format=%H -- "*$short" | head -1)
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
  sha=$(git log --all --reverse --diff-filter=A --format=%H -- "*/$(basename "$frag")" | head -1)
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

# Provenance must name the number being checked. Any issue marker is not enough: a commit that says
# "#123" does not prove anything about issue 765.
# Accepts "#765", "TESTNG-765", "issues/765" and a branch name such as "fix-765". The last form
# is common in merge commits and is real evidence, so the matched text is printed for a human to
# judge rather than hidden behind a yes or no.
names_num() { printf '%s' "$1" | grep -qE "(TESTNG-|#|issues/|[-/])${num}([^0-9]|$)"; }
matched_text() { printf '%s' "$1" | grep -oE "[A-Za-z/#-]*${num}([^0-9]|$)" | head -1; }
if names_num "$msg"; then
  printf 'provenance  the introducing commit names it: %s\n' "$(matched_text "$msg")"
else
  merge=$(git log --merges --ancestry-path --format=%H "$sha".."$BASE" 2>/dev/null | tail -1)
  mmsg=$([ -n "$merge" ] && git log -1 --format='%s | %b' "$merge" | tr '\n' ' ' | sed 's/  */ /g')
  if [ -n "${mmsg:-}" ] && names_num "$mmsg"; then
    printf 'provenance  the merge names it (%s): %s\n' "$(matched_text "$mmsg")" "$mmsg"
  else
    printf 'provenance  NOT PROVEN -- neither the commit nor its merge names #%s\n' "$num"
    [ -n "${mmsg:-}" ] && printf '            merge was: %s\n' "$mmsg"
    rc=1
  fi
fi

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
