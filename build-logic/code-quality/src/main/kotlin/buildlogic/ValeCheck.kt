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
     * diff from 20 files to 483. So compare merge bases and take the most recent.
     */
    private fun discoverBaseRef(): String? =
        DEFAULT_BASE_REFS
            .filter(::exists)
            .mapNotNull { ref ->
                val mergeBase = git("merge-base", "HEAD", ref)?.trim() ?: return@mapNotNull null
                val committedAt =
                    git("show", "-s", "--format=%ct", mergeBase)?.trim()?.toLongOrNull()
                        ?: return@mapNotNull null
                ref to committedAt
            }
            .maxByOrNull { it.second }
            ?.first

    /** Paths this branch touched: committed against the base, plus anything not committed yet. */
    private fun changedPaths(requested: String): Set<String>? {
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
            git("diff", "--name-only", "--diff-filter=ACM", "$base...HEAD")?.let {
                paths.addAll(it.toLines())
            }
        } else {
            logger.lifecycle("$name: no base branch found, comparing against the working tree only")
        }
        git("diff", "--name-only", "--diff-filter=ACM", "HEAD")?.let { paths.addAll(it.toLines()) }
        git("ls-files", "--others", "--exclude-standard")?.let { paths.addAll(it.toLines()) }
        return paths.map { File(top, it).absolutePath }.toSet()
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

    @TaskAction
    fun check() {
        // The pointer files are symlinks to AGENTS.md. Linting them would report every AGENTS.md
        // finding once per pointer.
        var files = sources.files.filter { it.isFile && !Files.isSymbolicLink(it.toPath()) }

        // Narrow to what this branch touched, intersecting so the exclusions above still hold.
        if (changedSince.isPresent) {
            changedPaths(changedSince.get())?.let { changed ->
                files = files.filter { it.absolutePath in changed }
            }
        }

        if (files.isEmpty()) {
            // Truncate, so the report never disagrees with the console.
            writeReport(emptyList())
            logger.lifecycle("$name: nothing to check")
            return
        }

        val launcher = launcher()
        val findings = mutableListOf<String>()
        for (batch in batches(files)) {
            // --no-exit keeps the exit code meaning "Vale itself failed" rather than "Vale found
            // something", which tells a broken rule file apart from a style finding.
            val stdout = ByteArrayOutputStream()
            val stderr = ByteArrayOutputStream()
            val result =
                execOperations.exec {
                    workingDir = workingDirectory.get().asFile
                    commandLine(
                        launcher +
                            listOf("--no-exit", "--output=line") +
                            batch.map { it.absolutePath }
                    )
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
            findings += stdout.toString(Charsets.UTF_8.name()).toLines()
        }

        writeReport(findings)

        if (findings.isEmpty()) {
            logger.lifecycle("$name: no findings in ${files.size} files")
            return
        }
        findings.forEach { logger.lifecycle(it) }
        val summary =
            "$name: ${findings.size} findings in ${files.size} files. " +
                "See docs/WRITING_STYLE.md."
        if (failOnFindings.get()) {
            throw GradleException(summary)
        }
        logger.lifecycle("$summary Pass -PfailOnWritingStyle=true to make this fail the build.")
    }
}
