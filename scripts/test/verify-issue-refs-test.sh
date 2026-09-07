#!/usr/bin/env bash
# Tests for scripts/verify-issue-refs.sh.
#
# That script decides whether a GITHUB-<n> reference may be written into a @Test description. A
# wrong answer puts a false reference in the code, or drops a true one. Both are silent. So the
# rules it applies are checked here rather than by hand.
#
# Run it from the repository root:
#
#     scripts/test/verify-issue-refs-test.sh
#
# It needs the real git history. It does not call the GitHub API: every case below stops at the
# provenance step, which is the part with the rules worth testing.
set -u
cd "$(dirname "$0")/../.." || exit 1

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

# Runs the script and prints the provenance line, or the first word of an error line.
provenance() {
  local out
  out=$(bash scripts/verify-issue-refs.sh "$1" "${2:-}" 2>&1)
  if printf '%s' "$out" | grep -q AMBIGUOUS; then printf 'AMBIGUOUS'; return; fi
  if printf '%s' "$out" | grep -q 'OWN COMMIT'; then printf 'OWN COMMIT'; return; fi
  if printf '%s' "$out" | grep -q 'NOT PROVEN'; then printf 'NOT PROVEN'; return; fi
  if printf '%s' "$out" | grep -q '^provenance'; then printf 'PROVEN'; return; fi
  printf 'NO COMMIT'
}

R=testng-core/src/test/java/org/testng

# The commit that wrote the test names the issue outright.
check "commit names the issue" PROVEN \
  "$(provenance "$R/preserveorder/TestNG173Test.java" 173)"

# The commit says only "Fixing review comments". The merge names the branch krmahadevan-fix-765.
check "merge branch names the issue" PROVEN \
  "$(provenance "$R/reflect/ExcludeSyntheticMethodsFromTemplateCallsTest.java" 765)"

# This file moved twice before the migration: once when the modules were split, once when the
# tests were grouped by feature. Both moves must be walked back.
check "file moved twice still resolves" PROVEN \
  "$(provenance "$R/skip/github1632/IssueTest.java" 1632)"

# The same file, with git's rename detection turned off. Without -M on the queries, one file that
# moved three times looks like four files, and the ambiguity check rejects it.
check "resolves with diff.renames=false" PROVEN \
  "$(GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=diff.renames GIT_CONFIG_VALUE_0=false \
     provenance "$R/skip/github1632/IssueTest.java" 1632)"

# A number that appears in a path or a version is not evidence. Issue 3 exists, and the commit
# that added this file says nothing about it.
check "unrelated number is not provenance" "NOT PROVEN" \
  "$(provenance "$R/preserveorder/TestNG173Test.java" 3)"

# A file name alone is not enough when several files share it.
check "ambiguous file name is rejected" AMBIGUOUS \
  "$(provenance "TestClassSample.java" 1405)"

printf '\n%s passed, %s failed\n' "$pass" "$fail"
[ "$fail" -eq 0 ]
