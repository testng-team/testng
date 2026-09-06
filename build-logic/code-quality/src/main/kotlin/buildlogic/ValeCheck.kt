package buildlogic

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

private fun String.toLines() = lines().map(String::trim).filter(String::isNotEmpty)

private val isWindows = System.getProperty("os.name").startsWith("Windows")

private val HUNK = Regex("""^@@ -\S+ \+(\d+)(?:,(\d+))? @@""")

/** `<absolute path>:<line>:<col>:<Rule>:<message>`, as printed by `--output=line`. */
private val FINDING = Regex("""^(.*?):(\d+):\d+:""")

private val DEFAULT_BASE_REFS = listOf("upstream/master", "origin/master", "master")

/**
 * Windows caps a command line at 32767 characters, and the whole tree is roughly 278 KB of paths.
 * Chunk well under the cap so one run becomes several, on every platform rather than only the one
 * that would otherwise break.
 */
private const val MAX_ARGUMENT_CHARS = 8000

/**
 * Runs Vale over javadoc, code comments and Markdown.
 *
 * Named for the tool because that is what it wraps. The tasks built from it are named for the
 * question they answer: `writingStyleCheck` and `writingStyleCheckChanges`.
 *
 * The rules are in `AGENTS.md` under `## Writing`, and `docs/WRITING_STYLE.md` explains the
 * tooling. Vale is not a JVM tool and has no Gradle plugin, so this shells out to it.
 */
/** What a branch touched: which files, and which lines within them are new. */
private data class ChangedFiles(
    val paths: Set<String>,
    val addedLines: Map<String, Set<Int>>,
    val wholeFiles: Set<String>,
) {
    /** Matches CI's `filter_mode: added`: a finding counts only on a line this change introduced. */
    fun introduced(finding: String): Boolean {
        val match = FINDING.find(finding) ?: return true
        val path = match.groupValues[1]
        if (path in wholeFiles) return true
        return match.groupValues[2].toInt() in (addedLines[path] ?: emptySet())
    }
}

abstract class ValeCheck : DefaultTask() {
    @get:Inject abstract val execOperations: ExecOperations

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    /** `.vale.ini` and everything under `.vale/`, so editing a rule re-runs the task. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val configuration: ConfigurableFileCollection

    /** Used only when Vale is not already on `PATH`. */
    @get:Input abstract val valeVersion: Property<String>

    @get:Input abstract val failOnFindings: Property<Boolean>

    /**
     * When set, narrows the check to files this branch touched. The value is the base ref to
     * compare against, or an empty string to let the task find one. Unset means the whole tree.
     */
    @get:Input @get:Optional abstract val changedSince: Property<String>

    @get:OutputFile abstract val report: RegularFileProperty

    @get:Internal abstract val workingDirectory: DirectoryProperty

    /** Runs a process, returning stdout on success and null on any failure. */
    private fun run(command: List<String>): String? {
        val out = ByteArrayOutputStream()
        val result = runCatching {
            execOperations.exec {
                workingDir = workingDirectory.get().asFile
                commandLine(command)
                standardOutput = out
                errorOutput = ByteArrayOutputStream()
                isIgnoreExitValue = true
            }
        }
        return if (result.getOrNull()?.exitValue == 0) out.toString(Charsets.UTF_8.name()) else null
    }

    private fun git(vararg args: String) = run(listOf("git") + args)

    private fun exists(ref: String) = git("rev-parse", "--verify", "--quiet", ref) != null

    /**
     * The base to compare against.
     *
     * Taking the first ref that exists is wrong here. A fork's `origin/master` is often far behind
     * the canonical repository, and on one branch it was 130 commits behind, which widened the
     * diff from 20 files to 483.
     *
     * Ancestry decides it, not commit dates. A merge base that has every other merge base as an
     * ancestor is the closest one to HEAD, and unlike a timestamp that survives rebases, grafted
     * history and a rewritten committer date.
     */
    private fun discoverBaseRef(): String? {
        val bases =
            DEFAULT_BASE_REFS.filter(::exists).mapNotNull { ref ->
                git("merge-base", "HEAD", ref)?.trim()?.let { ref to it }
            }
        if (bases.isEmpty()) return null
        val closest =
            bases.firstOrNull { (_, candidate) ->
                bases.all { (_, other) ->
                    other == candidate ||
                        run(listOf("git", "merge-base", "--is-ancestor", other, candidate)) != null
                }
            }
        // No candidate dominates, so the refs have diverged. Fall back to declaration order.
        return (closest ?: bases.first()).first
    }

    /**
     * Which lines a diff added, keyed by absolute path.
     *
     * `git diff -U0` prints `@@ -a,b +c,d @@` before each hunk. Everything from c to c+d-1 is new.
     */
    private fun collectAddedLines(top: String, diff: String, into: MutableMap<String, MutableSet<Int>>) {
        var current: MutableSet<Int>? = null
        for (line in diff.lines()) {
            when {
                line.startsWith("+++ ") -> {
                    val path = line.removePrefix("+++ ").removePrefix("b/").trim()
                    current =
                        if (path == "/dev/null") null
                        else into.getOrPut(File(top, path).absolutePath) { mutableSetOf() }
                }
                line.startsWith("@@") -> {
                    val spec = HUNK.find(line)?.groupValues ?: continue
                    val start = spec[1].toInt()
                    val count = spec[2].takeIf { it.isNotEmpty() }?.toInt() ?: 1
                    if (count > 0) current?.addAll(start until start + count)
                }
            }
        }
    }

    /** Paths this branch touched: committed against the base, plus anything not committed yet. */
    private fun changedPaths(requested: String): ChangedFiles? {
        // git prints paths relative to the top level, which is not always the Gradle root.
        val top = git("rev-parse", "--show-toplevel")?.trim()
        if (top == null) {
            logger.warn("$name: not a git repository, checking the whole tree instead")
            return null
        }
        val base =
            if (requested.isNotBlank()) {
                // An explicitly requested ref that does not resolve must fail. Falling back would
                // silently shrink the check, and this task is documented as a pre-commit gate.
                requested.takeIf(::exists)
                    ?: throw GradleException(
                        "$name: -PwritingStyleSince=$requested does not resolve to a git ref."
                    )
            } else {
                discoverBaseRef()
            }
        val paths = linkedSetOf<String>()
        if (base != null) {
            git("diff", "--name-only", "--diff-filter=ACMR", "$base...HEAD")?.let {
                paths.addAll(it.toLines())
            }
        } else {
            logger.lifecycle("$name: no base branch found, comparing against the working tree only")
        }
        git("diff", "--name-only", "--diff-filter=ACMR", "HEAD")?.let { paths.addAll(it.toLines()) }

        // An untracked file is new in its entirety, so every line of it counts as added.
        val whole = mutableSetOf<String>()
        git("ls-files", "--others", "--exclude-standard")?.toLines()?.forEach {
            paths.add(it)
            whole.add(File(top, it).absolutePath)
        }

        // The same two diffs again with -U0, this time for the line numbers.
        val added = mutableMapOf<String, MutableSet<Int>>()
        if (base != null) {
            git("diff", "-U0", "--diff-filter=ACMR", "$base...HEAD")?.let {
                collectAddedLines(top, it, added)
            }
        }
        git("diff", "-U0", "--diff-filter=ACMR", "HEAD")?.let { collectAddedLines(top, it, added) }

        return ChangedFiles(
            paths = paths.map { File(top, it).absolutePath }.toSet(),
            addedLines = added,
            wholeFiles = whole,
        )
    }

    /**
     * A local install is faster and works offline. npx is the fallback, so nothing is required.
     * Windows npm ships `npx.cmd`, and `CreateProcess` only appends `.exe`, so name it in full.
     */
    private fun launcher(): List<String> {
        val names = if (isWindows) listOf("vale.exe", "vale.bat") else listOf("vale")
        val onPath =
            System.getenv("PATH")
                ?.split(File.pathSeparator)
                ?.asSequence()
                ?.flatMap { dir -> names.asSequence().map { File(dir, it) } }
                ?.firstOrNull { it.isFile && it.canExecute() }
                ?.absolutePath
        if (onPath != null) return listOf(onPath)
        val npx = if (isWindows) "npx.cmd" else "npx"
        return listOf(npx, "--yes", "@vvago/vale@${valeVersion.get()}")
    }

    /** Splits the paths so no single command line approaches the platform limit. */
    private fun batches(files: List<File>): List<List<File>> {
        val result = mutableListOf<List<File>>()
        var current = mutableListOf<File>()
        var length = 0
        for (file in files) {
            val cost = file.absolutePath.length + 1
            if (current.isNotEmpty() && length + cost > MAX_ARGUMENT_CHARS) {
                result += current
                current = mutableListOf()
                length = 0
            }
            current += file
            length += cost
        }
        if (current.isNotEmpty()) result += current
        return result
    }

    private fun writeReport(findings: List<String>) {
        report.get().asFile.let { file ->
            file.parentFile.mkdirs()
            file.writeText(findings.joinToString("\n"))
        }
    }

    /** Runs Vale once. Returns its findings, and fails the build if Vale itself broke. */
    private fun runVale(launcher: List<String>, paths: List<String>): List<String> {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        // --no-exit keeps the exit code meaning "Vale itself failed" rather than "Vale found
        // something", which tells a broken rule file apart from a style finding.
        val result =
            execOperations.exec {
                workingDir = workingDirectory.get().asFile
                commandLine(launcher + listOf("--no-exit", "--output=line") + paths)
                standardOutput = stdout
                // Kept out of the findings stream. npx writes update notices to stderr while
                // still exiting 0, and those would otherwise be counted as findings.
                errorOutput = stderr
                isIgnoreExitValue = true
            }
        if (result.exitValue != 0) {
            throw GradleException(
                "Vale failed to run (exit ${result.exitValue}):\n" +
                    stdout.toString(Charsets.UTF_8.name()) +
                    stderr.toString(Charsets.UTF_8.name())
            )
        }
        return stdout.toString(Charsets.UTF_8.name()).toLines()
    }

    /**
     * The npm wrapper is versioned separately from Vale and does not publish every Vale release,
     * so the requested version is a request, not a guarantee. Report what actually ran, and say so
     * when it differs, because a different Vale is the usual reason local and CI results diverge.
     */
    private fun reportVersion(launcher: List<String>) {
        val actual =
            run(launcher + "--version")?.trim()?.substringAfterLast(' ') ?: return
        val wanted = valeVersion.get()
        if (actual != wanted) {
            logger.warn(
                "$name: running Vale $actual, not the $wanted this build asks for. " +
                    "Results may differ from CI."
            )
        }
    }

    /**
     * Proves the rules still load when there is nothing else to lint.
     *
     * A change that only touches `.vale.ini`, or a rule file under `.vale`, leaves no `.java` or
     * `.md` file to check, and a broken rule would otherwise sail through. `vale ls-config` does
     * not notice one, so lint a throwaway file instead and keep only the exit code.
     */
    private fun verifyConfigurationLoads(launcher: List<String>) {
        val probe = File.createTempFile("writing-style-probe", ".md")
        try {
            probe.writeText("A short line of text.\n")
            runVale(launcher, listOf(probe.absolutePath))
            logger.lifecycle("$name: nothing to check; the rules still load")
        } finally {
            probe.delete()
        }
    }

    @TaskAction
    fun check() {
        // The pointer files are symlinks to AGENTS.md. Linting them would report every AGENTS.md
        // finding once per pointer.
        var files = sources.files.filter { it.isFile && !Files.isSymbolicLink(it.toPath()) }

        // Narrow to what this branch touched, intersecting so the exclusions above still hold.
        var changed: ChangedFiles? = null
        if (changedSince.isPresent) {
            changed = changedPaths(changedSince.get())
            changed?.let { c -> files = files.filter { it.absolutePath in c.paths } }
        }

        val launcher = launcher()
        reportVersion(launcher)

        if (files.isEmpty()) {
            // Truncate, so the report never disagrees with the console.
            writeReport(emptyList())
            verifyConfigurationLoads(launcher)
            return
        }

        var findings = batches(files).flatMap { batch ->
            runVale(launcher, batch.map { it.absolutePath })
        }

        // Match CI, which runs with filter_mode: added. Without this the task reports every
        // finding in a file the change happened to touch, including prose written years ago.
        val total = findings.size
        changed?.let { c -> findings = findings.filter(c::introduced) }
        val preexisting = total - findings.size

        writeReport(findings)

        if (findings.isEmpty()) {
            val note =
                if (preexisting > 0) {
                    ", and $preexisting on lines it did not touch"
                } else {
                    ""
                }
            logger.lifecycle("$name: no findings in ${files.size} files$note")
            return
        }
        findings.forEach { logger.lifecycle(it) }
        val note =
            if (preexisting > 0) " $preexisting more sit on lines this change did not touch." else ""
        val summary =
            "$name: ${findings.size} findings in ${files.size} files. " +
                "See docs/WRITING_STYLE.md.$note"
        if (failOnFindings.get()) {
            throw GradleException(summary)
        }
        logger.lifecycle("$summary Pass -PfailOnWritingStyle=true to make this fail the build.")
    }
}
