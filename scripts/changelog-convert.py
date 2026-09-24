#!/usr/bin/env python3
"""Convert CHANGES.txt to a Keep a Changelog 1.1.0 CHANGELOG.md.

Every entry, sub-bullet, issue reference and attribution of the source is carried
over word for word. Four things are *not* in the source and are authored here,
in the tables below, each with its reasoning: the release dates CHANGES.txt never
recorded (DATES), one date it recorded impossibly (DATE_OVERRIDE), the note that
tells a reader where a packaging re-release's changes live (CARRIED), and the
7.5.1 section, which was released from a branch and never reached this file
(BACKPORT_7_5_1). Read those four before trusting the rest.

Run with --diagnose to see how each line was classified before generating.
"""
import argparse
import re
import subprocess
from collections import Counter

SRC = "CHANGES.txt"
DST = "CHANGELOG.md"
REPO = "https://github.com/testng-team/testng"

# Release dates for the sections CHANGES.txt never dated, reconstructed from tags,
# GitHub releases and Maven Central publication records. Consulted before the date
# the section carries, so an entry here also overrides one: 4.6 recorded
# "2006/27/02", and month 27 does not exist. In an era spelling dates YYYY/MM/DD
# the day and the month are swapped. The dates of 4.5 and 5.0 are chronologically
# impossible and are left alone, because correcting them would be inventing.
DATES = {
    "7.12.0": "2026-01-22", "7.11.0": "2025-02-13", "7.10.2": "2024-04-28",
    "7.10.1": "2024-04-09", "7.10.0": "2024-04-07", "7.9.0": "2023-12-26",
    "7.8.0": "2023-05-19", "7.5.1": "2023-04-26", "7.7.1": "2022-12-29",
    "7.7.0": "2022-12-09", "7.6.1": "2022-07-04", "7.6.0": "2022-05-18",
    "7.5": "2022-01-06", "7.4.0": "2021-02-27", "7.3.0": "2020-08-07",
    "7.1.0": "2019-12-24", "7.0.0": "2019-08-17", "6.14.3": "2018-02-23",
    "6.14.2": "2018-02-04", "6.13.1": "2017-11-27", "6.13": "2017-11-22",
    "6.12": "2017-07-25",
    # CHANGES.txt records "No official release" where this section's date belongs.
    # The tag gives the day it was published, and then withdrawn.
    "6.9.7": "2015-10-12",
    "4.6": "2006-02-27",
    # CHANGES.txt recorded 10/22/2011. The testng-6.3.1 tag and the .pom on
    # Maven Central both say 22 November, so the month in the old text is wrong.
    "6.3.1": "2011-11-22",
}

# Issue numbers the old text got wrong. Linking them would turn a typo into a
# working link to an unrelated issue, which is worse than the typo: GITHUB-2802
# carries the title of 2082, GITHUB-1173 that of 1773, the two in 6.8.1 are
# JCommander issue numbers, and GITHUB-2825 does not exist. The text is carried
# as written -- correcting it would be rewriting history -- and left unlinked,
# for the reason TESTNG-nnn is. Keyed by version: GITHUB-107 in 6.3.1 does name
# the TestNG issue.
UNLINKED = {("7.0.0", "2802"), ("7.0.0", "1173"), ("7.7.0", "2825"),
            ("6.8.1", "137"), ("6.8.1", "107")}

# The 6.9.13 line was published seven times in eight days to fix packaging, and
# CHANGES.txt files every entry under 6.9.13 itself. Authored, so that a reader
# landing on a re-release is not told the version changed nothing at all.
CARRIED = "Packaging only: it carries the changes listed under 6.9.13."
EMPTY_NOTE = {
    "6.9.13.6": f"Final release of the 6.9.13 line. {CARRIED}",
    "6.8.21": "No changes recorded.",
}

# 7.5.1 was released from the release_7.5 branch and never reached CHANGES.txt on
# master. Its single entry comes from the two commits between the 7.5 and 7.5.1
# tags: a release commit, and a cherry-pick of the Zip Slip fix.
BACKPORT_7_5_1 = [("Security",
                   "GITHUB-2852: [SECURITY] Fix Zip Slip Vulnerability, backported "
                   "to the 7.5 line (Jonathan Leitschuh)")]

# release_7.5 forked before the 7.5 tag, so this release is not 7.5 plus the fix
# above: `git rev-list --count 7.5.1..7.5` answers 20. Said outright, because a
# 7.5 user moving here for the security fix gives those up.
BACKPORT_7_5_1_NOTE = (
    "Cut from the `release_7.5` branch, which forked before the 7.5 tag. This "
    "release is not 7.5 plus the fix below: it is missing 20 commits that 7.5 "
    "has, among them GITHUB-2701 and GITHUB-2646, both listed under 7.5. Moving "
    "from 7.5 to 7.5.1 for the security fix gives those up.")

CATEGORY = {
    "Fixed": "Fixed", "Fix": "Fixed",
    "Added": "Added", "Add": "Added", "New": "Added",
    "Changed": "Changed", "Update": "Changed", "Improved behavior": "Changed",
    "Test": "Changed",
    "Removed": "Removed", "Remove": "Removed",
    "Deprecated": "Deprecated", "Security": "Security",
    # Also used as a standalone sub-heading. COMPONENT is tried first and only
    # matches the bare form, so what reaches PREFIX is an entry.
    "Documentation": "Changed", "Doc": "Changed",
}
ORDER = ["Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"]
BREAKING_HEADING = "#### Possible backward incompatible changes"

HEADING = re.compile(r"^(Current \(|\d+\.\d+)")
DATE_LINE = re.compile(r"^\s*(\d{1,4})[/-](\d{1,2})[/-](\d{1,4})\s*$")
PREFIX = re.compile(r"^(" + "|".join(sorted(CATEGORY, key=len, reverse=True)) + r")\s*:\s?(.*)$")
COMPONENT = re.compile(
    r"^(Eclipse( plug-?in| [\d.]+)?|IDEA plug-?in|Core|Doc(umentation)?):?\s*$")
BREAKING = re.compile(r"^Possible backward incompatible changes:\s*$")
SEPARATOR = re.compile(r"^=+\s*$")
BULLET = re.compile(r"^(\s*)-\s+(.*)$")
# Runs of adjacent tags are fenced as one code span. Two spans written back to back
# read as an empty one.
TAGLIKE = re.compile(r"(?:<[A-Za-z/!][^<>]*>)+|\b[A-Za-z_][\w.]*\[\]")


class Entry:
    def __init__(self, text, comp=""):
        self.text, self.subs, self.comp = text, [], comp


def git(*args):
    return subprocess.run(["git", *args], capture_output=True, text=True,
                          check=True).stdout


def split_sections(lines):
    heads = [i for i, l in enumerate(lines) if HEADING.match(l)]
    return [(lines[i], lines[i + 1:heads[k + 1] if k + 1 < len(heads) else len(lines)])
            for k, i in enumerate(heads)]


def parse_heading(head):
    if head.startswith("Current ("):
        return "Unreleased", re.sub(r"^Current \((.*)\)$", r"\1", head.strip())
    m = re.match(r"^([\d.]+?):?\s*(?:\((.*)\))?\s*$", head)
    return m.group(1), (m.group(2) or "").strip()


def iso(raw):
    a, b, c = DATE_LINE.match(raw).groups()
    if len(a) == 4:                                  # YYYY/MM/DD
        return f"{a}-{int(b):02d}-{int(c):02d}"
    return f"{c}-{int(a):02d}-{int(b):02d}"          # M/D/YYYY


def render(text, version=""):
    """Fence the tokens Markdown would swallow, then link the issue references.

    TESTNG-nnn is left as plain text on purpose: those are keys of the
    OpenSymphony JIRA the project used before 2011, and the same numbers name
    unrelated GitHub issues.
    """
    text = TAGLIKE.sub(lambda m: f"`{m.group(0)}`", text)
    # Outside the spans just created, escape what Markdown reads as syntax. The
    # file holds one tag that was never closed, which no fencing rule recognises.
    parts = re.split(r"(`[^`]*`)", text)
    for i, part in enumerate(parts):
        if not part.startswith("`"):
            parts[i] = part.replace("<", "&lt;").replace("*", "\\*")
    def link(m):
        if (version, m.group(1)) in UNLINKED:
            return m.group(0)
        return f"[GITHUB-{m.group(1)}]({REPO}/issues/{m.group(1)})"
    return re.sub(r"\bGITHUB-(\d+)\b", link, "".join(parts))


def parse_body(body, diag):
    """-> (notes, {category: [Entry]}, [Entry]) for the breaking-change block."""
    notes, buckets, breaking = [], {}, []
    component, in_breaking, current, pending = "", False, None, None

    def label():
        return component if component and component.lower() != "core" else ""

    for raw in body:
        line = raw.rstrip()
        if not line.strip() or SEPARATOR.match(line) or DATE_LINE.match(line):
            continue
        if BREAKING.match(line):
            in_breaking, current = True, None
            diag["breaking-heading"] += 1
            continue
        if COMPONENT.match(line):
            component, in_breaking, current = line.strip().rstrip(":"), False, None
            pending = None
            diag["component"] += 1
            continue
        m = PREFIX.match(line)
        if m:
            in_breaking = False
            if not m.group(2).strip():
                # A prefix with nothing after it heads a block whose items are
                # indented under it, rather than naming one entry.
                pending, current = CATEGORY[m.group(1)], None
                diag["category-heading"] += 1
                continue
            current = Entry(m.group(2).strip(), label())
            buckets.setdefault(CATEGORY[m.group(1)], []).append(current)
            pending = None
            diag["entry"] += 1
            continue
        b = BULLET.match(line)
        if b:
            indent, text = b.group(1), b.group(2).strip()
            if indent and current is not None:
                current.subs.append(text)       # a sub-bullet of the entry above
                diag["sub-bullet"] += 1
            elif in_breaking:
                current = Entry(text)
                breaking.append(current)
                diag["breaking-bullet"] += 1
            else:
                # The only ones are the four documentation items of 2.4, each
                # naming something the documentation gained.
                current = Entry(text, label())
                buckets.setdefault("Added", []).append(current)
                diag["loose-bullet"] += 1
            continue
        if pending and raw.startswith((" ", "\t")):
            current = Entry(line.strip(), label())
            buckets.setdefault(pending, []).append(current)
            diag["category-item"] += 1
            continue
        if current is not None:                 # a wrapped line
            if current.subs:
                current.subs[-1] += " " + line.strip()
            else:
                current.text += " " + line.strip()
            diag["continuation"] += 1
            continue
        notes.append(Entry(line.strip(), label()))
        diag["note"] += 1
    return notes, buckets, breaking


def emit(entries, version=""):
    out = []
    for e in entries:
        prefix = f"**{e.comp}:** " if e.comp else ""
        out.append(f"- {prefix}{render(e.text, version)}")
        out += [f"  - {render(s, version)}" for s in e.subs]
    return out


def is_ancestor(a, b):
    return subprocess.run(["git", "merge-base", "--is-ancestor",
                           f"{a}^{{commit}}", f"{b}^{{commit}}"],
                          capture_output=True).returncode == 0


def previous_tagged(versions, i, tags):
    """The release this one follows on its own line.

    Sections are ordered by release date, which is not the order of the commit
    graph: 7.5.1 was cut from release_7.5 and shipped after 7.7.1. Comparing
    against the section above it would answer with the whole divergence -- 150
    commits for 7.8.0 rather than its 25. So the previous release is the nearest
    earlier one this version descends from, or, for a maintenance release whose
    branch was cut before the release it patches, that release itself.
    """
    return next((p for p in versions[i + 1:]
                 if p in tags and (is_ancestor(tags[p], tags[versions[i]])
                                   or versions[i].startswith(p + "."))), None)


def build_tag_index(versions):
    """Map a version to its tag.

    Three naming conventions to cover: testng-6.9.5 and older, bare up to 7.12.0,
    and v-prefixed from the release that follows.
    """
    have = set(git("tag", "-l").split())
    def candidates(v):
        return (f"v{v}", v, f"testng-{v}")
    return {v: next(c for c in candidates(v) if c in have)
            for v in versions if have.intersection(candidates(v))}


def verify(source_lines, parsed):  # noqa: C901
    """Check CHANGELOG.md against its source and the rules the format imposes.

    Answers 0 when every check passes, and 1 having named the first failure of
    each kind. What it covers is what a hand review of this file kept re-checking:
    that the conversion lost nothing, and that the structure the format promises
    actually holds.
    """
    try:
        text = open(DST, encoding="utf-8").read()
    except FileNotFoundError:
        print(f"{DST} is missing")
        return 1

    failures = []

    def refs(t):
        return Counter(re.findall(r"\b(?:GITHUB|TESTNG)-\d+", t))

    # Nothing lost, counted as multisets across both trackers. Only with
    # --against-source: it reads CHANGES.txt out of history, which a shallow clone
    # does not carry, and a check that cannot run must not look like one that passed.
    if source_lines is not None:
        src = "\n".join(source_lines)
        if not src.strip():
            print("CHANGELOG.md: --against-source found no CHANGES.txt in this clone")
            return 1
        authored = Counter(r for _, t in BACKPORT_7_5_1
                           for r in re.findall(r"\b(?:GITHUB|TESTNG)-\d+", t))
        authored += refs(BACKPORT_7_5_1_NOTE)
        lost = refs(src) - (refs(text) - authored)
        if lost:
            failures.append(f"issue references lost: {dict(lost)}")

    sections = re.findall(r"^## \[([^\]]+)\]", text, re.M)
    if parsed is not None and len(sections) != len(parsed):
        failures.append(f"{len(parsed)} sections parsed, {len(sections)} written")
    if not sections or sections[0] != "Unreleased":
        failures.append("the first section is not [Unreleased]")
    duplicated = [v for v, n in Counter(sections).items() if n > 1]
    if duplicated:
        failures.append(f"a version has more than one section: {duplicated}")

    # Structure. Every heading is one of the six, none repeats inside a version,
    # and they keep the order the format lays down.
    current, seen = None, []
    for line in text.splitlines():
        if line.startswith("## ["):
            if seen != sorted(set(seen), key=ORDER.index):
                failures.append(f"{current}: headings out of order or repeated: {seen}")
            current, seen = line, []
        elif line.startswith("### "):
            name = line[4:].strip()
            if name not in ORDER:
                failures.append(f"{current}: '{name}' is not a Keep a Changelog category")
            else:
                seen.append(name)

    # Markdown. An unfenced angle bracket is swallowed by the renderer, and an odd
    # backtick count runs a code span into the rest of the line.
    outside = [part for part in re.split(r"(`[^`]*`)", text) if not part.startswith("`")]
    if any("<" in part for part in outside):
        failures.append("an angle bracket sits outside a code span")
    unbalanced = [l for l in text.splitlines()
                  if l.strip().startswith("- ") and l.count("`") % 2]
    if unbalanced:
        failures.append(f"{len(unbalanced)} entries have an unclosed code span")

    # Links. Every one names a tag that exists. Without tags there is nothing to
    # check them against, and saying so beats reporting every link as broken.
    have = set(git("tag", "-l").split())
    if not have:
        print("CHANGELOG.md: this clone has no tags, so the comparison links "
              "cannot be checked (fetch-tags)")
        return 1
    for version, base, tag in re.findall(
            r"^\[([^\]]+)\]: \S+/compare/(\S+)\.\.\.(\S+)$", text, re.M):
        for ref in (base, tag):
            if ref != "HEAD" and ref not in have:
                failures.append(f"[{version}] names a tag that does not exist: {ref}")

    for f in failures:
        print(f"CHANGELOG.md: {f}")
    if not failures:
        source = ", source compared" if source_lines is not None else ""
        print(f"{DST}: {len(sections)} sections, "
              f"{sum(refs(text).values())} issue references, structure and links OK{source}")
    return 1 if failures else 0


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--diagnose", action="store_true")
    ap.add_argument("--verify", action="store_true",
                    help="check the committed CHANGELOG.md against the format's rules "
                         "and against the tags its links name")
    ap.add_argument("--against-source", action="store_true",
                    help="with --verify, also compare every issue reference against "
                         "CHANGES.txt in git history; needs a clone that has it")
    ap.add_argument("--source-rev", default=None,
                    help="revision carrying CHANGES.txt; defaults to the commit "
                         "before the one that deleted it, so a re-run reads the "
                         "same input")
    args = ap.parse_args()

    # The structural half of --verify reads CHANGELOG.md and nothing else, so it
    # runs in a shallow clone. Everything below needs CHANGES.txt out of history.
    if args.verify and not args.against_source:
        raise SystemExit(verify(None, None))

    # CHANGES.txt is gone from the tree once the conversion is committed, so the
    # input always comes from git. A re-run therefore reproduces the conversion,
    # it does not fold in changelog entries written since.
    try:
        rev = args.source_rev or git("log", "--diff-filter=D", "--format=%H", "-1",
                                     "--", SRC).strip() + "~1"
        lines = git("show", f"{rev}:{SRC}").replace("﻿", "").splitlines()
    except subprocess.CalledProcessError:
        raise SystemExit(f"cannot read {SRC} from git history; this clone does not "
                         f"carry it (a shallow checkout does not)")

    parsed, diag = [], Counter()
    for head, body in split_sections(lines):
        version, note = parse_heading(head)
        if version == "Unreleased" and note:
            note = f"Next release: {note}"
        date = DATES.get(version) or next(
            (iso(l) for l in body[:3] if DATE_LINE.match(l)), None)
        notes, buckets, breaking = parse_body(body, diag)
        parsed.append({"version": version, "date": date, "note": note,
                       "notes": notes, "buckets": buckets, "breaking": breaking})

    # CHANGES.txt declares 5.0.1 twice, separated by a rule. One version, one section.
    merged = []
    for s in parsed:
        if merged and merged[-1]["version"] == s["version"]:
            prev = merged[-1]
            prev["notes"] += s["notes"]
            prev["breaking"] += s["breaking"]
            for cat, items in s["buckets"].items():
                prev["buckets"].setdefault(cat, []).extend(items)
            prev["date"] = prev["date"] or s["date"]
            diag["merged-duplicate"] += 1
            continue
        merged.append(s)
    parsed = merged

    at = next(i for i, s in enumerate(parsed) if s["version"] == "7.8.0")
    extra = {"version": "7.5.1", "date": DATES["7.5.1"], "note": "",
             "notes": [Entry(BACKPORT_7_5_1_NOTE)], "buckets": {}, "breaking": []}
    for cat, text in BACKPORT_7_5_1:
        extra["buckets"].setdefault(cat, []).append(Entry(text))
    parsed.insert(at + 1, extra)

    versions = [s["version"] for s in parsed if s["version"] != "Unreleased"]
    tags = build_tag_index(versions)

    if args.diagnose:
        print("classification:", dict(diag))
        print(f"{len(parsed)} sections")
        for s in parsed:
            cats = " ".join(f"{c}={len(v)}" for c, v in s["buckets"].items())
            subs = sum(len(e.subs) for v in s["buckets"].values() for e in v)
            print(f"  {s['version']:<10} {s['date'] or '-':<12} notes={len(s['notes'])} "
                  f"breaking={len(s['breaking'])} subs={subs} {cats}"
                  f"{'' if s['date'] else '  <undated>'}")
        untagged = [v for v in versions if v not in tags]
        print(f"\nuntagged ({len(untagged)}): {' '.join(untagged)}")
        return

    if args.verify:
        raise SystemExit(verify(lines if args.against_source else None, parsed))

    out = ["# Changelog", "",
           "All notable changes to this project are documented in this file.", "",
           "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).",
           "",
           "Version numbers do not follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html):",
           "a minor release may carry a breaking change. Each one is listed under *Possible backward",
           "incompatible changes*, in the section of the release that ships it.",
           ""]
    for s in parsed:
        # A version pulled after release says so in its own words, either in the
        # parenthesis of its heading or, for 6.9.7, where its date belongs.
        yanked = s["note"].startswith("Bad release") or s["version"] == "6.9.7"
        head = f"## [{s['version']}]"
        if s["date"]:
            head += f" - {s['date']}"
        out += [head + (" [YANKED]" if yanked else ""), ""]
        if s["note"]:
            out += [f"{render(s['note'], s['version'])}.", ""]
        for n in s["notes"]:
            out += [(f"**{n.comp}:** " if n.comp else "") + render(n.text, s["version"]), ""]
        if not s["buckets"] and not s["breaking"]:
            filler = EMPTY_NOTE.get(s["version"],
                                    CARRIED if s["version"].startswith("6.9.13.") else None)
            if filler:
                out += [filler, ""]
        for cat in ORDER:
            entries = s["buckets"].get(cat)
            # The breaking-change block hangs under Changed, and pulls the heading
            # in for a section that has the block but no Changed entry of its own.
            if not entries and not (cat == "Changed" and s["breaking"]):
                continue
            out += [f"### {cat}", ""]
            if entries:
                out += emit(entries, s["version"]) + [""]
            if cat == "Changed" and s["breaking"]:
                out += [BREAKING_HEADING, ""] + emit(s["breaking"], s["version"]) + [""]

    links, skipped = [], []
    newest = next((v for v in versions if v in tags), None)
    if newest:
        links.append(f"[Unreleased]: {REPO}/compare/{tags[newest]}...HEAD")
    for i, v in enumerate(versions):
        if v not in tags:
            continue
        prev = previous_tagged(versions, i, tags)
        if prev is None:
            links.append(f"[{v}]: {REPO}/releases/tag/{tags[v]}")
            continue
        # Releases between this one and its base carry no tag of their own, so the
        # comparison would answer with their changes too. 7.3.0 against 7.0.0
        # spans the 38 commits of the untagged 7.1.0. No link beats a wrong one.
        spanned = [p for p in versions[i + 1:versions.index(prev)] if p not in tags]
        if spanned:
            skipped.append((v, prev, spanned))
            continue
        links.append(f"[{v}]: {REPO}/compare/{tags[prev]}...{tags[v]}")
    out += links + [""]

    open(DST, "w", encoding="utf-8").write("\n".join(out))
    print(f"{DST}: {len(out)} lines, {len(parsed)} sections, {len(links)} links")


if __name__ == "__main__":
    main()
