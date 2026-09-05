#!/usr/bin/env bash
# Check an issue reference from both ends before writing it into a @Test description:
#
#   1. provenance -- the commit that introduced the test names the issue, or its merge does
#   2. the issue  -- #<n> is a real, closed GitHub *issue*, and its subject is what the test asserts
#
# Package names are not evidence: test.testng173 and test.testng317 look identical, and only one of
# them is a GitHub issue.
#
#   scripts/verify-issue-refs.sh <path-fragment> [issue-number]
#   scripts/verify-issue-refs.sh github765/ExcludeSyntheticMethodsFromTemplateCallsTest.java 765
set -u
REPO=${REPO:-testng-team/testng}
frag=${1:?usage: verify-issue-refs.sh <path-fragment> [issue-number]}
num=${2:-}

# a path in the worktree is followed through renames; otherwise fall back to a history-wide glob
# A path in the worktree is followed through renames. That misses a rename that is still
# uncommitted, so fall back to searching all history for the basename.
sha=""
[ -e "$frag" ] && sha=$(git log --follow --reverse --diff-filter=A --format=%H -- "$frag" | head -1)
[ -z "$sha" ] && sha=$(git log --all --reverse --diff-filter=A --format=%H -- "*$frag" | head -1)
if [ -z "$sha" ]; then
  # Last resort: search history for the basename. Refuse when it is not unique -- names like
  # TestClassSample.java and IssueTest.java repeat all over the tree, and picking the first match
  # invents provenance that reads exactly like the real thing.
  base=$(basename "$frag")
  paths=$(git log --all --diff-filter=A --format= --name-only -- "*/$base" | grep -x ".*/$base" | sort -u)
  if [ "$(printf '%s\n' "$paths" | grep -c .)" -gt 1 ]; then
    echo "AMBIGUOUS   '$base' was added at more than one path; pass the original path instead:"
    printf '%s\n' "$paths" | sed 's/^/              /'
    exit 2
  fi
  sha=$(git log --all --reverse --diff-filter=A --format=%H -- "*/$base" | head -1)
fi
if [ -z "$sha" ]; then echo "no introducing commit found for $frag"; exit 1; fi
printf 'introduced  %s  %s\n' "$(git log -1 --format=%h "$sha")" "$(git log -1 --format='%ad' --date=short "$sha")"
msg=$(git log -1 --format='%s %b' "$sha" | tr '\n' ' ' | sed 's/  */ /g')
printf 'message     %s\n' "${msg:-<EMPTY -- no provenance here>}"

# an uninformative message usually means the reference is on the merge or the branch name
if ! printf '%s' "$msg" | grep -qiE '(TESTNG-|#|issues/)[0-9]+'; then
  m=$(git log --merges --ancestry-path --format=%H "$sha"..master 2>/dev/null | tail -1)
  [ -n "$m" ] && printf 'merge       %s\n' "$(git log -1 --format='%s | %b' "$m" | tr '\n' ' ' | sed 's/  */ /g')"
fi

[ -z "$num" ] && exit 0
if ! gh api "repos/$REPO/issues/$num" >/dev/null 2>&1; then
  echo "issue       #$num does not exist"; exit 1
fi
kind=$(gh api "repos/$REPO/issues/$num" -q 'if .pull_request then "PULL REQUEST -- not an issue" else "issue" end')
printf 'issue       #%s is a %s, %s\n' "$num" "$kind" "$(gh api "repos/$REPO/issues/$num" -q .state)"
printf 'title       %s\n' "$(gh api "repos/$REPO/issues/$num" -q .title)"
if gh api "repos/$REPO/issues/$num/timeline" --paginate \
     -q '.[] | select(.event=="referenced" or .event=="closed") | .commit_id // empty' 2>/dev/null \
     | grep -qx "$sha"; then
  echo "timeline    links the introducing commit"
else
  echo "timeline    does NOT link the commit -- check the merge PR before trusting the reference"
fi
