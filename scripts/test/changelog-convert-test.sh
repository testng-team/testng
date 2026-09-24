#!/usr/bin/env bash
# Tests for scripts/changelog-convert.py --verify.
#
#     scripts/test/changelog-convert-test.sh
#
# --verify is what stands between CHANGELOG.md and the failures a hand review kept
# catching: a lost issue reference, a heading that is not one of the six Keep a
# Changelog categories, a section that repeats a version, an angle bracket the
# renderer swallows, a comparison link naming a tag that was never pushed. Each
# case below breaks the real file one way in a scratch copy and asserts that the
# check says so; the last one asserts it passes on the file as committed.
set -u
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
SCRIPT="$ROOT/scripts/changelog-convert.py"
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

pass=0; fail=0
check() {
  if [ "$2" = "$3" ]; then pass=$((pass + 1)); else
    fail=$((fail + 1))
    printf 'FAIL  %s\n      expected: %s\n      actual:   %s\n' "$1" "$2" "$3"
  fi
}

# break <name> <python expression on s> -- copies the repository, applies the edit to
# its CHANGELOG.md, and prints the exit status of --verify there.
#
# The copy is a git worktree of the real repository rather than a bare directory:
# --verify reads the source from git history and resolves tags, so it needs one.
break_and_verify() {
  local d="$WORK/$1" extra="${3:-}"
  git -C "$ROOT" worktree add -q --detach "$d" HEAD 2>/dev/null || return 99
  python3 -c "
import pathlib, sys
p = pathlib.Path(sys.argv[1]) / 'CHANGELOG.md'
s = p.read_text()
$2
p.write_text(s)" "$d"
  ( cd "$d" && python3 "$SCRIPT" --verify $extra >/dev/null 2>&1; echo $? )
}

cleanup_worktree() { git -C "$ROOT" worktree remove --force "$WORK/$1" 2>/dev/null || true; }

run_case() {
  local name=$1 edit=$2 expected=$3 extra="${4:-}"
  local actual
  actual=$(break_and_verify "$name" "$edit" "$extra")
  check "$name" "$expected" "$actual"
  cleanup_worktree "$name"
}

# --against-source reads CHANGES.txt out of history, so this case names the flag.
run_case "a lost issue reference is caught" \
  "s = s.replace('[GITHUB-', 'GONE-', 1)" 1 "--against-source"

run_case "a category outside the six is caught" \
  "s = s.replace('### Fixed', '### Bugfixes', 1)" 1

run_case "headings out of canonical order are caught" \
  "s = s.replace('### Changed', '@@T@@', 1).replace('### Fixed', '### Changed', 1).replace('@@T@@', '### Fixed', 1)" 1

run_case "a repeated version section is caught" \
  "s = s.replace('## [7.12.0]', '## [7.11.0]', 1)" 1

run_case "an unfenced angle bracket is caught" \
  "s = s.replace('## [Unreleased]', '## [Unreleased]\n\na bare <test> tag', 1)" 1

run_case "a link to a tag that does not exist is caught" \
  "s = s.replace('...7.12.0', '...v9.9.9', 1)" 1

# And the file as committed passes both ways, so the cases above fail for the reason
# named rather than because the check rejects everything. This is the case that caught
# a shallow CI checkout: the six above passed there while --verify was failing on
# every input, and only this one said so.
( cd "$ROOT" && python3 "$SCRIPT" --verify >/dev/null 2>&1 )
check "the committed CHANGELOG.md passes" 0 "$?"

( cd "$ROOT" && python3 "$SCRIPT" --verify --against-source >/dev/null 2>&1 )
check "it passes against the source too" 0 "$?"

printf '%s: %d passed, %d failed\n' "$(basename "$0")" "$pass" "$fail"
[ "$fail" -eq 0 ]
