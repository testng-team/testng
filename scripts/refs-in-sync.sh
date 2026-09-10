#!/usr/bin/env bash
# Checks that every reference docs/test-issue-references.md records is really in the code.
#
#     scripts/refs-in-sync.sh
#
# Phase 3 verified five new references, wrote them into the document, and claimed them in the pull
# request. None of them reached the code. The document said the work was done and nothing checked.
#
# What it promises, and what it does not.
#
# It answers one question: does a description this document records exist in the code at all. That
# is the whole contract. Everything below is outside it, on purpose:
#
#   - It checks one direction. The document records what this migration verified or added, not
#     every reference in a moved package, so a reference in the code without a row is not a fault.
#   - It does not count occurrences. GITHUB-2238 sits on four methods, and dropping one still
#     passes. A count in a document goes stale, so there is nothing to check it against.
#   - It does not judge whether the description is the right one for that test. Only the commit
#     history proves that, and scripts/verify-issue-refs.sh is the tool for it.
#
# This is frozen. It has taken four rounds of review, every one a hole in reading Java source and
# markdown as text. Patching the next hole buys less than it costs. If a case gets past it, record
# the case here and leave the code alone. A reviewer reading the diff catches what this cannot, and
# in practice caught what this did not.
#
# If a fifth case turns up and someone judges it worth fixing, move the evidence out of markdown
# rather than patching a pattern. One row per reference in a CSV, with the document generated from
# it, and this script reads columns.
#
# Be clear about what that buys. Two of the four faults here came from reading a document written
# for people: a sentence read as a table row, and an exemption that ran past its own table. A CSV
# removes both. The other two came from reading Java as text: a marker matched anywhere, and a
# window of lines mistaken for an annotation. A CSV does nothing for those. Removing them needs a
# real Java parser, which is a bigger job than the migration it serves.
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
# It reads the @Test annotation rather than a window of lines. A window says only that @Test and
# description both appeared near the reference, which is not the same claim: an @Test followed by
# @DataProvider(description = "GITHUB-765") satisfied a window and means nothing.
#
# So it tracks the parentheses of @Test( and collects the text until they close. A bare @Test
# opens nothing and carries no description. Any other annotation is outside that text.
#
#     @Test(description = "GITHUB-980")                        one line
#     @Test(dataProvider = "dp", description = "GITHUB-949")   with other members
#     @Test(                                                   over three lines
#         description =
#             "GITHUB-3408: whether the data provider ...")
has_description() {
  local ref=$1
  grep -rlE "\"${ref}([^0-9]|\")" "$root" --include='*.java' 2>/dev/null | while read -r f; do
    awk -v ref="$ref" '
      # Counts brackets that are syntax. A bracket inside a string literal is text: a description
      # reading "expected (" would otherwise leave the depth wrong, and a real one look missing.
      # in_string carries across lines, because a Java string cannot span them but the annotation
      # can, and this is called once per line.
      function depth_of(t,   i, c, d) {
        d = 0
        for (i = 1; i <= length(t); i++) {
          c = substr(t, i, 1)
          if (in_string) {
            if (c == "\\") i++            # an escape hides the next character, quote included
            else if (c == "\"") in_string = 0
          }
          else if (c == "\"") in_string = 1
          else if (c == "(") d++
          else if (c == ")") d--
        }
        return d
      }
      function claims(t) {
        return t ~ /description[[:space:]]*=/ && t ~ ("\"" ref "([^0-9]|\")")
      }
      !collecting && /@Test[[:space:]]*\(/ {
        in_string = 0
        buf = substr($0, index($0, "@Test"))
        depth = depth_of(buf)
        if (depth <= 0) { if (claims(buf)) { found = 1; exit } }
        else collecting = 1
        next
      }
      collecting {
        buf = buf " " $0
        depth += depth_of($0)
        if (depth <= 0) {
          collecting = 0
          if (claims(buf)) { found = 1; exit }
        }
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
