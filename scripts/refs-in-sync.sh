#!/usr/bin/env bash
# Checks that every reference docs/test-issue-references.md records is really in the code.
#
#     scripts/refs-in-sync.sh
#
# Phase 3 verified five new references, wrote them into the document, and claimed them in the pull
# request. None of them reached the code. The document said the work was done and nothing checked.
#
# It checks one direction only. The document records what this migration verified or added, not
# every reference that happens to sit in a moved package, so a reference in the code without a row
# is not a fault.
#
# It asks whether a reference is in the code at all. It does not count the methods that carry one.
# GITHUB-2238 sits on four methods, and dropping one of them still passes here. The document
# records which issue a test covers, not how many of its methods say so, and a count in a document
# goes stale. Review covers that part.
set -u
# Overridable so the tests can point at a throwaway tree.
doc=${DOC:-docs/test-issue-references.md}
root=${ROOT:-testng-core/src/test/java}

# The document's "Verified, description not yet written" section lists references whose class has
# not moved yet. Those are not required in the code.
waiting=$(sed -n '/## Verified, description not yet written/,$p' "$doc" \
            | grep -oE '`GITHUB-[0-9]+`' | tr -d '`' | sort -u)

# True when a description attribute in the test sources carries this reference.
#
# A bare match is not enough. A comment, a javadoc line or an unrelated string literal all mention
# an issue number without the code claiming it. The document claims that a @Test carries the
# description, so that is what this looks for.
#
# A description often spans lines:
#
#     @Test(
#         description =
#             "GITHUB-3408: whether the data provider was parallel ...")
#
# so it reads a window of the three lines before the match.
has_description() {
  local ref=$1
  grep -rlE "\"${ref}([^0-9]|\")" "$root" --include='*.java' 2>/dev/null | while read -r f; do
    awk -v ref="$ref" '
      { w3=w2; w2=w1; w1=$0 }
      $0 ~ "\"" ref "([^0-9]|\")" {
        joined = w3 " " w2 " " w1
        if (joined ~ /description[[:space:]]*=/) { found=1; exit }
      }
      END { exit found ? 0 : 1 }
    ' "$f" && { printf 'yes'; return; }
  done | grep -q yes
}

missing=""
for ref in $(sed '/## Verified, description not yet written/,$d' "$doc" \
               | grep -oE '`GITHUB-[0-9]+`' | tr -d '`' | sort -u); do
  printf '%s\n' "$waiting" | grep -qx "$ref" && continue
  # The number must end here. GITHUB-182 is the start of GITHUB-1827.
  has_description "$ref" || missing="$missing $ref"
done

if [ -n "$missing" ]; then
  echo "In the document but not in the code:"
  for r in $missing; do echo "  $r"; done
  echo
  echo "A row in that document is a claim that the code carries the description."
  echo "Add the description, or drop the row."
  exit 1
fi
echo "every reference the document records is in the code"
