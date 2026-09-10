#!/usr/bin/env bash
# Tests for scripts/test/run-all.sh.
#
#     scripts/test/run-all-test.sh
#
# run-all.sh is the gate that makes every other script here carry tests. It was checked by hand
# once and then trusted. A gate nobody tests is the thing it exists to prevent.
set -u
GATE=$(cd "$(dirname "$0")" && pwd)/run-all.sh
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

pass=0; fail=0
check() {
  if [ "$2" = "$3" ]; then pass=$((pass + 1)); else
    fail=$((fail + 1))
    printf 'FAIL  %s\n      expected: %s\n      actual:   %s\n' "$1" "$2" "$3"
  fi
}

# new_tree -- makes a throwaway repository layout and prints its path.
new_tree() {
  local d
  d=$(mktemp -d "$WORK/tree.XXXXXX")
  mkdir -p "$d/scripts/test"
  printf '%s' "$d"
}

# script <tree> <name>  -- adds scripts/<name>.sh
script() { printf '#!/usr/bin/env bash\necho ok\n' > "$1/scripts/$2.sh"; chmod +x "$1/scripts/$2.sh"; }

# suite <tree> <name> <exit code>  -- adds scripts/test/<name>-test.sh
suite() {
  printf '#!/usr/bin/env bash\nexit %s\n' "$3" > "$1/scripts/test/$2-test.sh"
  chmod +x "$1/scripts/test/$2-test.sh"
}

verdict() { (ROOT_DIR="$1" bash "$GATE" >/dev/null 2>&1) && printf 'ok' || printf 'fails'; }

# Every script has a suite, and every suite passes.
t=$(new_tree); script "$t" alpha; suite "$t" alpha 0
check "a tested script passes" ok "$(verdict "$t")"

# A script with no suite is the case that let both real scripts ship untested.
t=$(new_tree); script "$t" alpha; suite "$t" alpha 0; script "$t" beta
check "a script with no suite fails" fails "$(verdict "$t")"

# A suite that fails must fail the gate.
t=$(new_tree); script "$t" alpha; suite "$t" alpha 1
check "a failing suite fails" fails "$(verdict "$t")"

# One good suite must not cover for a failing one.
t=$(new_tree); script "$t" alpha; suite "$t" alpha 0; script "$t" beta; suite "$t" beta 1
check "one passing suite does not hide another" fails "$(verdict "$t")"

# A tree with no suites at all is a mistake, not a pass.
t=$(new_tree)
check "no suites at all fails" fails "$(verdict "$t")"

# A suite with no script of its own is fine. run-all-test.sh is one.
t=$(new_tree); script "$t" alpha; suite "$t" alpha 0; suite "$t" orphan 0
check "a suite without a script is allowed" ok "$(verdict "$t")"

printf '\n%s passed, %s failed\n' "$pass" "$fail"
[ "$fail" -eq 0 ]
