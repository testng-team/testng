#!/usr/bin/env bash
# Tests for scripts/refs-in-sync.sh.
#
#     scripts/test/refs-in-sync-test.sh
#
# That script decides whether the evidence document and the code agree. A row in the document is a
# claim that a @Test carries the description. Phase 3 wrote five rows and touched no code, and only
# a reviewer noticed. Each case below builds a throwaway document and source tree.
set -u
SCRIPT=$(cd "$(dirname "$0")/../.." && pwd)/scripts/refs-in-sync.sh
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

pass=0; fail=0
check() {
  if [ "$2" = "$3" ]; then pass=$((pass + 1)); else
    fail=$((fail + 1))
    printf 'FAIL  %s\n      expected: %s\n      actual:   %s\n' "$1" "$2" "$3"
  fi
}

# new_case <name> -- makes a document and a source root, and prints the case directory.
new_case() {
  local d
  d=$(mktemp -d "$WORK/case.XXXXXX")
  mkdir -p "$d/src/org/testng/feature"
  printf '# Verified issue references\n\n| Ref | Title |\n| --- | --- |\n| `GITHUB-765` | something |\n' \
    > "$d/doc.md"
  printf '%s' "$d"
}

# verdict <case dir>  -- prints ok or missing.
verdict() {
  if (cd "$1" && DOC=doc.md ROOT=src bash "$SCRIPT" >/dev/null 2>&1); then
    printf 'ok'
  else
    printf 'missing'
  fi
}

# A @Test description on one line is the ordinary shape.
d=$(new_case)
printf 'class T {\n  @Test(description = "GITHUB-765")\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/ATest.java"
check "a description on one line" ok "$(verdict "$d")"

# A description often spans lines. The reference still counts.
d=$(new_case)
printf 'class T {\n  @Test(\n      description =\n          "GITHUB-765: the name was empty")\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/BTest.java"
check "a description over three lines" ok "$(verdict "$d")"

# A comment naming the issue is not the code claiming it.
d=$(new_case)
printf 'class T {\n  // GITHUB-765 is what this covers\n  @Test\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/CTest.java"
check "a comment is not a description" missing "$(verdict "$d")"

# Nor is a javadoc line.
d=$(new_case)
printf 'class T {\n  /**\n   * Covers GITHUB-765.\n   */\n  @Test\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/DTest.java"
check "javadoc is not a description" missing "$(verdict "$d")"

# Nor is an unrelated string literal.
d=$(new_case)
printf 'class T {\n  String s = "GITHUB-765";\n  @Test\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/ETest.java"
check "a bare string is not a description" missing "$(verdict "$d")"

# Nothing at all in the code.
d=$(new_case)
printf 'class T {\n  @Test\n  public void a() {}\n}\n' > "$d/src/org/testng/feature/FTest.java"
check "no mention at all" missing "$(verdict "$d")"

# A longer number is a different issue.
d=$(new_case)
printf 'class T {\n  @Test(description = "GITHUB-7654")\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/GTest.java"
check "a longer number is not this one" missing "$(verdict "$d")"

# The section for references whose class has not moved yet is not required in the code.
d=$(new_case)
printf 'class T {\n  @Test\n  public void a() {}\n}\n' > "$d/src/org/testng/feature/HTest.java"
printf '\n## Verified, description not yet written\n\n| `GITHUB-765` | test.somewhere |\n' >> "$d/doc.md"
check "a reference still waiting is allowed" ok "$(verdict "$d")"

printf '\n%s passed, %s failed\n' "$pass" "$fail"
[ "$fail" -eq 0 ]
