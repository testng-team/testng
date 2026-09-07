import buildlogic.ValeCheck

plugins {
    id("build-logic.build-params")
}

// Writing-style checks. The rules are in AGENTS.md under `## Writing`, and docs/WRITING_STYLE.md
// explains the tooling, the suppressions and the gaps.
//
// Applied to the root project only, unlike its neighbours in this directory. Vale reads the whole
// tree in one pass, so a per-module task would start the binary once per module and report the
// same shared Markdown every time.
//
// Two tasks, because the two questions are different. writingStyleCheckChanges answers "did I add
// a problem", which is what a contributor asks before committing. writingStyleCheck answers "what
// is left in the repository", which is a maintenance question.
//
// Both report only by default. -PfailOnWritingStyle=true turns either into a gate.
//
// The tasks are named for what they check, not for Vale. Vale is the tool we happen to run, and
// swapping it would not change the question either task answers. The neighbouring tool-named
// tasks, autostyleCheck and rewriteDryRun, are named by their third-party plugins, not by us.

// Two versions, not one. The first is the Vale we want. The second is the npm package used when
// Vale is not installed locally, and it is versioned on its own: 3.18.0 and 3.19.0 are real Vale
// releases with no npm wrapper. Bump them together when you can, apart when you have to.
val valeCliVersion = "3.20.0"

// Tried in order, closest merge base wins. A fork's origin/master is often behind the canonical
// repository, so upstream/master is listed first. Override the whole list with a comma separated
// -PwritingStyleBaseRefs, for a checkout whose remotes are named differently.
val valeBaseRefs =
    buildParameters.writingStyleBaseRefs
        .map { property -> property.split(",").map(String::trim).filter(String::isNotEmpty) }
        .orElse(listOf("upstream/master", "origin/master", "master"))
val valeHelpText = "See docs/WRITING_STYLE.md."
val valeNpmWrapperVersion = "3.20.0"

val valeSources =
    fileTree(layout.projectDirectory) {
        include("**/*.java", "**/*.md")
        // Generated output, vendored trees and scratch directories are not ours to fix.
        exclude(
            "**/build/**",
            "**/.git/**",
            "**/.gradle/**",
            "**/node_modules/**",
            "dont_check_in/**",
            "test-output/**",
        )
    }

val valeConfiguration =
    files(
        layout.projectDirectory.file(".vale.ini"),
        fileTree(layout.projectDirectory.dir(".vale")),
    )

tasks.register<ValeCheck>("writingStyleCheckChanges") {
    group = "verification"
    description = "Checks the javadoc, comments and Markdown this branch touched."
    sources.from(valeSources)
    configuration.from(valeConfiguration)
    valeVersion.set(valeCliVersion)
    valeNpmVersion.set(valeNpmWrapperVersion)
    valeExecutable.set(buildParameters.writingStyleValePath)
    baseRefs.set(valeBaseRefs)
    helpText.set(valeHelpText)
    failOnFindings.set(buildParameters.failOnWritingStyle)
    workingDirectory.set(layout.projectDirectory)
    report.set(layout.buildDirectory.file("reports/writing-style/changed.txt"))
    // -PwritingStyleSince=<ref> picks the base branch. Empty means "work it out".
    changedSince.set(buildParameters.writingStyleSince.orElse(""))
    // The file list comes from git, which Gradle does not track, so never skip this as up to date.
    outputs.upToDateWhen { false }
}

tasks.register<ValeCheck>("writingStyleCheck") {
    group = "verification"
    description =
        "Checks all javadoc, comments and Markdown against the writing rules in AGENTS.md."
    sources.from(valeSources)
    configuration.from(valeConfiguration)
    valeVersion.set(valeCliVersion)
    valeNpmVersion.set(valeNpmWrapperVersion)
    valeExecutable.set(buildParameters.writingStyleValePath)
    baseRefs.set(valeBaseRefs)
    helpText.set(valeHelpText)
    failOnFindings.set(buildParameters.failOnWritingStyle)
    workingDirectory.set(layout.projectDirectory)
    report.set(layout.buildDirectory.file("reports/writing-style/all.txt"))
    // What a reader wants from this task is the console output, and Gradle cannot see that. Left
    // to its own judgement it prints the findings once, then stays silent on every later run.
    outputs.upToDateWhen { false }
}
