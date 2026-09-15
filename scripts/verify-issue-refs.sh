#!/usr/bin/env bash
# Check an issue reference from both ends before writing it into a @Test description:
#
#   1. provenance -- the commit that introduced the test names THIS issue number, or a pull request
#                    GitHub lists for that commit does
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
# Set to 1 to stop after step 1 and skip step 2. Step 1 still asks GitHub which pull request holds
# the commit, whenever the commit's own message does not name the issue. The tests set this, and give
# each GitHub lookup of step 1 a local command.
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
# Set to a method name to judge the commit that added that one method, not the commit that added the
# file. A file's first commit proves nothing about a method added to it years later: ParallelTestTest
# came from a 2006 commit, and holds one method from a #1636 fix and two from "Unit tests for #2532".
#
# Bound to the file's own history, so a method of the same name in another file never answers. If no
# commit added the method here, the script refuses. It does not fall back to the file's commit,
# because that answer reads exactly like the real one.
METHOD=${METHOD:-}
# The command that lists the pull requests holding a commit, one "<number><TAB><branch><TAB><title>"
# per line. It is given the commit's full sha. Exit 0 with no lines means GitHub lists none. A non-zero exit means
# it could not be asked, which is not a verdict. Overridable so the tests can run offline.
COMMIT_PRS_CMD=${COMMIT_PRS_CMD:-}
frag=${1:?usage: verify-issue-refs.sh <path-fragment> [issue-number]}
num=${2:-}
rc=0

if [ -n "$METHOD" ]; then
  if [ "$BY_DESCRIPTION" = 1 ]; then
    echo "METHOD and BY_DESCRIPTION each pick the commit a different way; set only one"
    exit 1
  fi
  # A Java identifier, so the name is safe inside the patterns below.
  if ! printf '%s' "$METHOD" | grep -qE '^[A-Za-z_][A-Za-z0-9_]*$'; then
    echo "METHOD must be a plain method name, got <$METHOD>"
    exit 1
  fi
fi

# git reads a pathspec from the current directory, but prints the paths it reports from the top of the
# repository. The lookups below pass one into the other. Run from a module directory, they miss the
# 2021 module split and take the split as the commit that wrote the test. So a path on disk is made
# relative to the top first, and every lookup runs from the top.
if [ -e "$frag" ]; then
  full=$(git ls-files --full-name -- "$frag" 2>/dev/null | head -1)
  [ -n "$full" ] && frag=$full
fi
top=$(git rev-parse --show-toplevel 2>/dev/null) || { echo "not inside a git repository"; exit 1; }
cd "$top" || exit 1

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

# The commit that added one method to this file.
#
# The walk reads the versions of this one file, newest first, through its renames. A commit added
# the method when its version declares the method and no parent's version does. The newest such
# commit wins, so a method that was removed and written again answers with the commit that wrote it
# again.
#
# Whole versions are read, not changed lines. That keeps these right:
#   - a declaration the formatter wrapped onto two lines
#   - "void target()" in a comment, a string or a nested class, which is not this method
#   - an edit to the declaration line, which does not add the method
#
# The walk starts at HEAD and follows parents. A commit on a branch that never merged is not on that
# history, so it cannot answer. A copy starts a new file: the commit that copied it added every
# method in it. The file it was copied from answers only for itself.
#
# Reads one Java source on stdin. Exits 0 when the body of a top-level type declares a method named
# argv[1], and 10 when it does not. Python exits 1 when it fails, so 1 cannot mean "no".
#
# Comments, strings and character literals are blanked first, so their text declares nothing and
# their braces do not change the depth. A declaration is a type, the name, the parameters and a
# body, so a call such as "target();" does not count. A nested class sits deeper than depth 1.
#
# The bytes are read as Latin-1, which accepts any byte. Older files here are not all UTF-8, and a
# decoding error would stop the walk on them.
read -r -d '' DECLARES_METHOD <<'PYTHON'
import re
import sys

TEXT_BLOCK = r'"""(?:\\.|[^\\])*?"""'
STRING = r'"(?:\\.|[^"\\\n])*"'
CHAR = r"'(?:\\.|[^'\\\n])*'"
COMMENT = r'/\*.*?\*/|//[^\n]*'

src = sys.stdin.buffer.read().decode('latin-1')
src = re.sub('|'.join([TEXT_BLOCK, STRING, CHAR, COMMENT]), ' ', src, flags=re.S)
decl = re.compile(r'[\w>\]]\s+' + re.escape(sys.argv[1])
                  + r'\s*\((?:[^()]|\([^()]*\))*\)\s*(?:throws\s[^{;]*)?\{')
for match in decl.finditer(src):
    before = src[:match.start()]
    if before.count('{') - before.count('}') == 1:
        sys.exit(0)
sys.exit(10)
PYTHON

if [ -n "$METHOD" ]; then
  if ! git cat-file -e "HEAD:$frag" 2>/dev/null; then
    echo "no file $frag at HEAD; METHOD needs the path the file has now"
    exit 1
  fi
  # Answers already worked out, one " <blob>=yes" or " <blob>=no" each. A commit and its parent
  # usually hold the same version, so most versions are read once.
  declared=""
  # Succeeds when the version "<commit>:<path>" in $1 declares the method.
  declares() {
    local blob
    blob=$(git rev-parse -q --verify "$1" 2>/dev/null) || return 1
    case "$declared" in
      *" $blob=yes"*) return 0 ;;
      *" $blob=no"*) return 1 ;;
    esac
    git cat-file blob "$blob" | python3 -c "$DECLARES_METHOD" "$METHOD"
    case $? in
      0) declared="$declared $blob=yes"; return 0 ;;
      10) declared="$declared $blob=no"; return 1 ;;
    esac
    # Any other exit is the matcher failing, not an answer. Read as "no", it would refuse every
    # method, or pick the wrong commit.
    echo "the method matcher failed on $1" >&2
    exit 1
  }
  if ! declares "HEAD:$frag"; then
    echo "no method $METHOD is declared in the class in $frag"
    exit 1
  fi
  # One line per version: the commit, the path in it, and the path in its parents. The parents' path
  # is empty when the commit created this file, by adding it or by copying another file.
  versions=$(git log --follow -M --format='@%H' --name-status HEAD -- "$frag" 2>/dev/null \
    | awk -F'\t' '/^@/ { c = substr($0, 2); next }
                  NF >= 2 && c != "" {
                    s = substr($1, 1, 1)
                    if (s == "R") print c "\t" $3 "\t" $2
                    else if (s == "A" || s == "C") print c "\t" (s == "C" ? $3 : $2) "\t"
                    else print c "\t" $2 "\t" $2
                  }')
  while IFS="$(printf '\t')" read -r commit path before; do
    [ -n "$commit" ] || continue
    declares "$commit:$path" || continue
    added=1
    if [ -n "$before" ]; then
      for parent in $(git log -1 --format=%P "$commit"); do
        if declares "$parent:$before"; then
          added=0
          break
        fi
      done
    fi
    if [ "$added" = 1 ]; then
      sha=$commit
      break
    fi
  done <<< "$versions"
  if [ -z "$sha" ]; then
    echo "no commit added the method $METHOD to $frag or any path it came from"
    exit 1
  fi
  printf 'method      %s\n' "$(git log -1 --format=%h "$sha") added $METHOD in $frag"
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

# Last source: the body of the pull request that carried the commit. GitHub closes an issue when a
# pull request body holds a closing word and the number, and neither the commit nor the merge has
# to repeat it. Issue #1307 was closed that way by pull request #1308, whose branch is called
# "feature/ignore-anonymous-tests". Phase 1 had to judge #765 and #1417 by hand for the same reason.
#
# Only the closing form counts. "See #765", "unlike #765" and "duplicate of #765" are mentions, and
# a mention proves nothing. These are GitHub's own closing words.
#
# The body is read only for a pull request GitHub lists as holding this commit, so it cannot be some
# other pull request's body.
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
# Prints the body of pull request $1. Exits non-zero when it could not be read at all, which is not
# the same as a body that says nothing: the caller reports the two differently.
pr_body() {
  if [ -n "$PR_BODY_CMD" ]; then
    $PR_BODY_CMD "$1" 2>/dev/null
  else
    gh api "repos/$REPO/pulls/$1" --jq .body 2>/dev/null
  fi
}
# Prints the pull requests GitHub says hold commit $1, "<number><TAB><branch><TAB><title>" per line.
# A tab or a line break in a title becomes a space, so each pull request stays on one line.
#
# git cannot answer this, so there is no fallback to git. It can only find merges that came after the
# commit, and the oldest of them is the merge that carried it only when a merge commit carried it at
# all. Pull request #2368 was rebase-merged: GitHub's own merge commit for it has a single parent. So
# the oldest merge after its commit was #2375, a CVE fix that has nothing to do with it.
commit_prs() {
  if [ -n "$COMMIT_PRS_CMD" ]; then
    $COMMIT_PRS_CMD "$1" 2>/dev/null
  else
    gh api "repos/$REPO/commits/$1/pulls" \
      --jq '.[] | "\(.number)\t\(.head.ref)\t\(.title | gsub("[\t\n\r]"; " "))"' 2>/dev/null
  fi
}
# A pull request's branch names the issue, the way "fix-765", "github-2321" and "issue-#1009-dp" do.
#
# The number must follow a word that marks an issue: fix, issue, gh, github or bug. A "#" may stand
# before the number. Digits alone are not enough. They put "java-17-support" behind issue 17, a dependabot branch ending in
# "assertj-core-3.27.3" behind 27, and "release-765" behind 765, which the commit-message rule has
# always refused. The number must also end the name or be followed by something other than a digit or
# a dot, so "fix-7650" does not answer for 765 and neither does the version "fix-765.1".
ref_names_num() {
  printf '%s' "$1" \
    | grep -qiE "(^|[^[:alnum:]])(fix(es|ed)?|issues?|gh|github|bug(fix)?)[-_/]?#?${num}([^0-9.]|$)"
}
# Reports the text that matched, so a reader can judge it. It searches the same cleaned string the
# rules did, never the raw message, or it would print a pull request number as the evidence.
matched_text() {
  # The same alternation names_num applies. A looser one printed "release-765" as the evidence for
  # a commit that was proven by "fix #765", which tells the reader the opposite of the truth.
  printf '%s' "$(without_pr_number "$1")" \
    | grep -oE "(TESTNG-|#|issues/)${num}([^0-9]|$)" | head -1
}
# The sources that may prove the reference, strongest first: the commit's own message, then for each
# pull request GitHub lists, its branch, its title and its body. The first that answers wins.
proven=""
if names_num "$msg"; then
  proven="the introducing commit names it: $(matched_text "$msg")"
else
  prs=$(commit_prs "$(git rev-parse "$sha")"); prs_rc=$?
  if [ "$prs_rc" != 0 ]; then
    # Only GitHub knows which pull request holds the commit. A guess from git's merges would prove a
    # reference that some other pull request made, so an offline run, a rate limit or a missing token
    # must not read as a verdict either way.
    printf 'provenance  CANNOT CHECK -- GitHub could not say which pull request holds this commit, and\n'
    printf '            the commit does not name #%s. Not a verdict.\n' "$num"
    exit 3
  fi
  # Every body that could not be read, not only the last one. A later body that was read must not
  # hide an earlier one that was not.
  unread=""
  # Every pull request GitHub listed, for the refusal to name.
  listed=""
  while IFS="$(printf '\t')" read -r prn prref prtitle; do
    [ -n "$prn" ] || continue
    listed="$listed #$prn"
    if ref_names_num "$prref"; then
      proven="the pull request names it (branch $prref): PR #$prn"
      break
    fi
    # GitHub writes the title into the body of the merge commit, so a title is held to the rule for a
    # commit message. Pull request #1065 names #1009 only in its title.
    if names_num "$prtitle"; then
      proven="pull request #$prn names it in its title: $prtitle"
      break
    fi
    if ! prbody=$(pr_body "$prn"); then
      unread="$unread #$prn"
    elif body_closes_num "$prbody"; then
      proven="pull request #$prn says it closes the issue: $(matched_close "$prbody")"
      break
    fi
  done <<< "$prs"
  # A body that could not be read is not a body that says nothing. NOT PROVEN here would let an
  # offline run, a rate limit or a missing token delete a true reference.
  if [ -z "$proven" ] && [ -n "$unread" ]; then
    printf 'provenance  CANNOT CHECK -- the body of pull request%s could not be read, and nothing\n' "$unread"
    printf '            else names #%s. Not a verdict.\n' "$num"
    exit 3
  fi
fi

if [ -n "$proven" ]; then
  printf 'provenance  %s\n' "$proven"
else
  printf 'provenance  NOT PROVEN -- the commit does not name #%s\n' "$num"
  if [ -z "${listed:-}" ]; then
    printf '            GitHub lists no pull request for this commit\n'
  else
    printf '            GitHub lists pull request%s: no branch or title names it, and no body closes it\n' "$listed"
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
#
# The exit code decides, not an empty answer. On a failed call gh still prints GitHub's error
# message, as JSON, on stdout. Read as an issue, "Bad credentials" passes a pull request number as an
# issue, with exit 0.
body=$(gh api "repos/$REPO/issues/$num" 2>/dev/null); issue_rc=$?
if [ "$issue_rc" != 0 ] || [ -z "$body" ]; then
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
