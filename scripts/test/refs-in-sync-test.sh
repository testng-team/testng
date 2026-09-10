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
  printf '# Verified issue references\n\n## Verified in phase 1\n\n| Ref | Title |\n| --- | --- |\n| `GITHUB-765` | something |\n' \
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

# A description belongs to a @Test. @DataProvider takes one too, and a field may be called
# description, so neither is the code claiming the reference.
d=$(new_case)
printf 'class T {\n  @DataProvider(description = "GITHUB-765")\n  public Object[][] dp() { return null; }\n}\n' \
  > "$d/src/org/testng/feature/ITest.java"
check "a data provider is not a test" missing "$(verdict "$d")"

d=$(new_case)
printf 'class T {\n  String description = "GITHUB-765";\n}\n' \
  > "$d/src/org/testng/feature/JTest.java"
check "a field called description is not one" missing "$(verdict "$d")"

# The description has to belong to the @Test, not merely sit near it. A window of lines cannot
# tell the difference, and these three shapes all satisfied one.
d=$(new_case)
printf 'class T {\n  @Test\n  @DataProvider(description = "GITHUB-765")\n  public Object[][] dp() { return null; }\n}\n' \
  > "$d/src/org/testng/feature/MTest.java"
check "a bare @Test above a data provider" missing "$(verdict "$d")"

d=$(new_case)
printf 'class T {\n  @Test\n  public void a() {}\n\n  @DataProvider(description = "GITHUB-765")\n  public Object[][] dp() { return null; }\n}\n' \
  > "$d/src/org/testng/feature/NTest.java"
check "a @Test method above a data provider" missing "$(verdict "$d")"

d=$(new_case)
printf 'class T {\n  @Test(enabled = true)\n  @DataProvider(description = "GITHUB-765")\n  public Object[][] dp() { return null; }\n}\n' \
  > "$d/src/org/testng/feature/OTest.java"
check "a @Test with its own members above one" missing "$(verdict "$d")"

# A description beside other members of the same @Test still counts.
d=$(new_case)
printf 'class T {\n  @Test(dataProvider = "dp", description = "GITHUB-765")\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/PTest.java"
check "a description beside other members" ok "$(verdict "$d")"

# A bracket inside the description text is text, not syntax. Counting brackets without reading
# string literals leaves the parser at the wrong depth, and a real description looks missing.
d=$(new_case)
printf 'class T {\n  @Test(description = "GITHUB-765: expected (")\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/QTest.java"
check "an open bracket inside the text" ok "$(verdict "$d")"

d=$(new_case)
printf 'class T {\n  @Test(\n      description =\n          "GITHUB-765: expected ( but saw nothing")\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/RTest.java"
check "an open bracket, over three lines" ok "$(verdict "$d")"

d=$(new_case)
printf 'class T {\n  @Test(description = "GITHUB-765: a closing ) alone")\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/STest.java"
check "a closing bracket inside the text" ok "$(verdict "$d")"

# A comment is not code. An annotation shown in a comment or a javadoc line is an example, not a
# description the code carries.
d=$(new_case)
printf 'class T {\n  // @Test(description = "GITHUB-765") was removed\n  @Test\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/TTest.java"
check "a commented-out annotation" missing "$(verdict "$d")"

d=$(new_case)
printf 'class T {\n  /**\n   * Like @Test(description = "GITHUB-765") but not.\n   */\n  @Test\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/UTest.java"
check "an annotation shown in javadoc" missing "$(verdict "$d")"

# A bracket in a comment inside the annotation is text too.
d=$(new_case)
printf 'class T {\n  @Test(\n      // the case is (unbalanced\n      description = "GITHUB-765")\n  public void a() {}\n}\n' \
  > "$d/src/org/testng/feature/VTest.java"
check "a bracket in a comment inside it" ok "$(verdict "$d")"

# Prose is not a claim. The document explains why GITHUB-3408 was dropped, and reading that
# sentence as a claim made the check demand the reference it says to remove.
d=$(new_case)
printf 'class T {\n  @Test\n  public void a() {}\n}\n' > "$d/src/org/testng/feature/KTest.java"
printf '\nGITHUB-765 was removed because it names a pull request.\n' >> "$d/doc.md"
python3 - "$d/doc.md" <<'PYEOF'
import sys
p=sys.argv[1]; s=open(p).read()
open(p,'w').write(s.replace('| `GITHUB-765` | something |\n',''))
PYEOF
check "prose is not a claim" ok "$(verdict "$d")"

# The exemption belongs to its own table. Prose after it must not exempt anything.
d=$(new_case)
printf 'class T {\n  @Test\n  public void a() {}\n}\n' > "$d/src/org/testng/feature/LTest.java"
printf '\n## Verified, description not yet written\n\n| `GITHUB-999` | test.elsewhere |\n' >> "$d/doc.md"
printf '\n## Notes\n\nGITHUB-765 is discussed here but not exempt.\n' >> "$d/doc.md"
check "prose after the table exempts nothing" missing "$(verdict "$d")"

printf '\n%s passed, %s failed\n' "$pass" "$fail"
[ "$fail" -eq 0 ]
