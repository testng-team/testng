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

# Prints the references a table claims, one per line. $1 is the section heading to read, or an
# empty string for everything before the first heading given in $2.
#
# It reads table rows only: a row starts the line with the reference in a cell. Prose is not a
# claim. The document explains that GITHUB-3408 was removed because #3408 is a pull request, and
# reading that sentence as a claim made the check demand the very reference it says to drop.
rows_in() {
  awk -v want="$1" '
    /^## / { section = substr($0, 4) }
    section == want && /^\| `GITHUB-[0-9]+`/ {
      match($0, /GITHUB-[0-9]+/)
      print substr($0, RSTART, RLENGTH)
    }
  ' "$doc" | sort -u
}

# True when a @Test in the test sources carries this reference in its description.
#
# A bare match is not enough. A comment, a javadoc line or an unrelated string literal all mention
# an issue number without a test claiming it. Neither is a description on its own: @DataProvider
# takes one too, and a field may simply be called description. So both @Test and description have
# to sit in the window with the reference.
#
# The window is four lines, because a description often spans them:
#
#     @Test(
#         description =
#             "GITHUB-3408: whether the data provider was parallel ...")
has_description() {
  local ref=$1
  grep -rlE "\"${ref}([^0-9]|\")" "$root" --include='*.java' 2>/dev/null | while read -r f; do
    awk -v ref="$ref" '
      { w4=w3; w3=w2; w2=w1; w1=$0 }
      $0 ~ "\"" ref "([^0-9]|\")" {
        joined = w4 " " w3 " " w2 " " w1
        if (joined ~ /@Test/ && joined ~ /description[[:space:]]*=/) { found=1; exit }
      }
      END { exit found ? 0 : 1 }
    ' "$f" && { printf 'yes'; return; }
  done | grep -q yes
}

# Each phase records what it verified in a table of its own. Those are the claims.
# read, not for: a heading has spaces in it, and word splitting would pass each word separately.
required=$(grep -oE '^## Verified in phase [0-9]+' "$doc" | sed 's/^## //' \
             | while IFS= read -r h; do rows_in "$h"; done | sort -u)
# This table is the exception: the reference is proven, but its class has not moved yet.
waiting=$(rows_in "Verified, description not yet written")

missing=""
for ref in $required; do
  printf '%s\n' "$waiting" | grep -qx "$ref" && continue
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
