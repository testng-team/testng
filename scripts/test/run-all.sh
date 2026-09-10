#!/usr/bin/env bash
# Runs every test suite under scripts/test, and fails when a script has none.
#
#     scripts/test/run-all.sh
#
# The workflow used to name each suite by hand. A new script with no test then passed CI in
# silence, which is how verify-issue-refs.sh and refs-in-sync.sh both shipped untested. Between
# them they produced five review points, every one in the untested part.
#
# A script here decides whether a reference may be written into the code. A wrong answer is silent.
# So the rule is mechanical: scripts/x.sh needs scripts/test/x-test.sh.
set -u
# Overridable so the tests can point at a throwaway tree.
cd "${ROOT_DIR:-$(dirname "$0")/../..}" || exit 1
fail=0

missing=""
for f in scripts/*.sh; do
  [ -e "$f" ] || continue
  name=$(basename "$f" .sh)
  [ -f "scripts/test/$name-test.sh" ] || missing="$missing $name"
done
if [ -n "$missing" ]; then
  fail=1
  echo "These scripts have no test suite:"
  for m in $missing; do echo "  scripts/$m.sh  needs  scripts/test/$m-test.sh"; done
  echo
  echo "A script here decides what may be written into the code, and a wrong answer is silent."
  echo "Write the cases that must be REJECTED first, then check that reverting the code fails them."
fi

ran=0
for t in scripts/test/*-test.sh; do
  [ -e "$t" ] || continue
  ran=$((ran + 1))
  echo "=== $t"
  bash "$t" || fail=1
done
[ "$ran" -gt 0 ] || { echo "no test suites found under scripts/test"; fail=1; }

exit $fail
