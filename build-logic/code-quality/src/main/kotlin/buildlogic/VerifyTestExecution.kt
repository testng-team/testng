package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Proves that the tests the suite claims to run actually ran.
 *
 * A test in `testng-core` runs only when `testng.xml` names it, so a class nobody registered is
 * compiled, reviewed, and skipped in silence. GitHub issue #1362 sat like that from 2017 until a
 * package move happened to disturb it. Knowing about the failure mode has not been enough to stop
 * it recurring, so the build checks three things instead:
 *
 *  1. every class the suite names produced results;
 *  2. nothing under `.samples.` ran, unless [factoryProduced] says a registered `@Factory` makes
 *     it -- those classes are TestNG input, and several are written to fail, so one running is
 *     both a false failure and a sign the boundary leaked;
 *  3. the set of tests that ran still matches [inventory].
 *
 * The third is the one that survives a package migration. The first two both read the suite file,
 * so neither can see a class that disappeared from the suite *and* the code in the same edit --
 * which is exactly what a mishandled move looks like.
 *
 * Both list files are guarded from rotting as well. An entry of [knownSilent] that no suite file
 * mentions is dead text, and an entry of [factoryProduced] must sit under `.samples.`, must not be
 * named in the suite, and must keep running.
 *
 * The rules themselves are [problemsIn], a pure function over [Evidence]. This class only gathers
 * the evidence, so `VerifyTestExecutionRulesTest` can put each case to the rules directly.
 */
abstract class VerifyTestExecution : DefaultTask() {

    /** The suite that decides what runs. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val suite: RegularFileProperty

    /** `class#method` for every test that ran, one per line. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inventory: RegularFileProperty

    /** Classes the suite names that are known not to run, with a reason each. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val knownSilent: RegularFileProperty

    /** Classes under `.samples.` that run because a registered `@Factory` creates them. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val factoryProduced: RegularFileProperty

    /** Where the test task wrote its JUnit XML. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val results: DirectoryProperty

    /** When true, rewrite [inventory] from this run instead of comparing against it. */
    @get:Internal
    abstract val update: Property<Boolean>

    @TaskAction
    fun verify() {
        val (executedClasses, ranNow) = actualExecution()
        val baseline = inventory.get().asFile
        val updating = update.getOrElse(false)

        val evidence = Evidence(
            declared = declaredClasses(),
            mentioned = mentionedClasses(),
            executed = executedClasses,
            ranNow = ranNow,
            expected = if (updating) emptyMap() else readInventory(baseline),
            silent = knownSilent.entries(),
            byFactory = factoryProduced.entries(),
            silentFile = knownSilent.get().asFile.name,
            factoryFile = factoryProduced.get().asFile.name,
            updatingInventory = updating,
        )

        if (updating) {
            // Sorted, so the file has a stable order and its diff is readable. Without this the
            // order follows the filesystem listing and can change between runs on its own.
            baseline.writeText(
                ranNow.entries.sortedBy { it.key }.joinToString("\n", postfix = "\n") { "${it.key}\t${it.value}" }
            )
            logger.lifecycle("Wrote ${ranNow.size} entries to ${baseline.name}. Read the diff before committing it.")
        }

        val problems = problemsIn(evidence)
        if (problems.isNotEmpty()) throw GradleException(problems.joinToString("\n\n"))
    }

    private fun readInventory(file: java.io.File): Map<String, Outcome> =
        file.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
            val name = line.substringBefore('\t')
            Outcome.parse(line.substringAfter('\t', ""))?.let { name to it }
        }.toMap()

    /** One entry per line, `#` starts a comment, blank lines are skipped. */
    private fun RegularFileProperty.entries(): Set<String> =
        get().asFile.readLines()
            .map { it.substringBefore('#').trim() }.filter { it.isNotEmpty() }.toSortedSet()

    /**
     * Every class name any suite file holds, comments included. Used only to tell a live entry
     * from a dead one: a commented-out `<class>` still names a class somebody parked on purpose,
     * while a name no suite file holds at all belongs to nothing.
     */
    private fun mentionedClasses(): Set<String> = collectClasses(stripComments = false)

    /**
     * The suite declares a DOCTYPE on testng.org, so it is read as text rather than letting a
     * parser reach for the network. Comments are stripped first: a commented-out `<class>` is not
     * registered, and matching inside one reports a parked class as silently missing.
     */
    private fun declaredClasses(): Set<String> = collectClasses(stripComments = true)

    private fun collectClasses(stripComments: Boolean): Set<String> {
        val found = sortedSetOf<String>()
        val seen = mutableSetOf<java.io.File>()
        fun visit(file: java.io.File) {
            val canonical = file.canonicalFile
            if (!seen.add(canonical) || !canonical.isFile) return
            val raw = canonical.readText()
            val text = if (stripComments) {
                raw.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            } else {
                raw
            }
            found += CLASS_ENTRY.findAll(text).map { it.groupValues[1] }
            // A suite pulls in others with <suite-file>. Classes named only in one of those are
            // still part of the run, so they belong under the same check.
            SUITE_FILE.findAll(text).forEach { visit(canonical.parentFile.resolve(it.groupValues[1])) }
        }
        visit(suite.get().asFile)
        return found
    }

    /** Returns the classes that ran, and `class#method` -> [Outcome] for the inventory. */
    private fun actualExecution(): Pair<Set<String>, Map<String, Outcome>> {
        val files = results.get().asFile
            .listFiles { f -> f.name.startsWith("TEST-") && f.extension == "xml" }
            ?: emptyArray()
        if (files.isEmpty()) {
            throw GradleException("No test results in ${results.get()}. Run the whole suite, not a --tests subset.")
        }
        val classes = sortedSetOf<String>()
        val runs = mutableMapOf<String, MutableList<String>>()
        files.forEach { file ->
            TEST_CASE.findAll(file.readText()).forEach { m ->
                // "m[3](arg)" is invocation 3 of method m. The index and the data-provider
                // arguments vary with ordering, so they are folded away -- but the number of
                // invocations is kept, because losing one is a regression worth catching.
                val method = m.groupValues[1].substringBefore('[')
                val cls = m.groupValues[2]
                val body = m.groupValues[4]
                classes += cls
                runs.getOrPut("$cls#$method") { mutableListOf() } += when {
                    body.contains("<failure") || body.contains("<error") -> "FAIL"
                    body.contains("<skipped") -> "SKIP"
                    else -> "PASS"
                }
            }
        }
        // Gradle starts one test fork per two CPUs, and each fork runs the whole suite. So every
        // test runs once per fork, and every raw count is the real count times the fork count.
        // That number changes with the machine: 6 on a 12 CPU laptop, 2 on a CI runner.
        //
        // The fork count is the GCD of all the raw counts, so dividing by it gives the real count
        // on any machine. If the build is ever changed to split classes across forks instead of
        // repeating them, the GCD becomes 1 and this does nothing.
        val forks = runs.values.map { it.size }.reduceOrNull(::gcd) ?: 1
        val inventory = runs.mapValues { (_, statuses) ->
            // Worst status wins: one failure among many invocations is still a failing test.
            val status = when {
                statuses.contains("FAIL") -> "FAIL"
                statuses.all { it == "SKIP" } -> "SKIP"
                else -> "PASS"
            }
            Outcome(status, statuses.size / forks)
        }
        return classes to inventory
    }

    /** What happened to one test method across all of its invocations. */
    data class Outcome(val status: String, val invocations: Int) {
        override fun toString() = "$status $invocations"

        companion object {
            fun parse(text: String): Outcome? {
                val parts = text.trim().split(Regex("\\s+"))
                return if (parts.size == 2) Outcome(parts[0], parts[1].toIntOrNull() ?: return null) else null
            }
        }
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    private companion object {
        val CLASS_ENTRY = Regex("""<class\s+name="([^"]+)"""")
        val SUITE_FILE = Regex("""<suite-file\s+path="([^"]+)"""")
        // Captures the body too, so a <skipped/> or <failure> inside can be seen.
        val TEST_CASE = Regex(
            """<testcase\s+name="([^"]*)"\s+classname="([^"]*)"[^>]*(/>|>(.*?)</testcase>)""",
            RegexOption.DOT_MATCHES_ALL,
        )
    }
}

/**
 * Everything the rules read, with no Gradle types in it.
 *
 * The rules decide whether a build passes, and a wrong answer is silent either way: a real defect
 * waved through, or a green branch turned red for nothing. So they live here, apart from the task,
 * where `VerifyTestExecutionRulesTest` can put each case to them directly.
 */
internal data class Evidence(
    /** Classes `testng.xml` names outside a comment. These must run. */
    val declared: Set<String> = emptySet(),
    /** Classes any suite file names, comments included. Used only to tell a live name from a dead one. */
    val mentioned: Set<String> = emptySet(),
    /** Classes that produced results in this run. */
    val executed: Set<String> = emptySet(),
    /** `class#method` to what happened, from this run. */
    val ranNow: Map<String, VerifyTestExecution.Outcome> = emptyMap(),
    /** `class#method` to what the inventory records. Empty while the inventory is being rewritten. */
    val expected: Map<String, VerifyTestExecution.Outcome> = emptyMap(),
    /** Entries of the known-silent list. */
    val silent: Set<String> = emptySet(),
    /** Entries of the factory-produced list. */
    val byFactory: Set<String> = emptySet(),
    val silentFile: String = "execution-known-silent.txt",
    val factoryFile: String = "execution-factory-produced.txt",
    /** True while the inventory is being rewritten, so the inventory rules have nothing to compare. */
    val updatingInventory: Boolean = false,
)

/** Every problem the evidence holds, one message each. Empty means the build passes. */
internal fun problemsIn(e: Evidence): List<String> {
    val problems = mutableListOf<String>()

    val neverRan = e.declared - e.executed - e.silent
    if (neverRan.isNotEmpty()) {
        problems += "Named in the suite but never ran -- compiled, and invisible:\n  " +
            neverRan.joinToString("\n  ") +
            "\n  Register it so it runs, or add it to ${e.silentFile} with a reason."
    }

    val staleEntries = e.silent intersect e.executed
    if (staleEntries.isNotEmpty()) {
        problems += "Listed in ${e.silentFile} but running now. Delete these entries:\n  " +
            staleEntries.joinToString("\n  ")
    }

    // An entry only ever failed the build when the class it names RAN. So an entry whose class was
    // renamed away matched nothing and was invisible. Two survived a package move and a green
    // merge that way. Comments count as a mention: a commented-out <class> is a decision somebody
    // made, not a dead name.
    val forgotten = e.silent - e.mentioned
    if (forgotten.isNotEmpty()) {
        problems += "Listed in ${e.silentFile} but no suite file mentions it:\n  " +
            forgotten.joinToString("\n  ") +
            "\n  The class was renamed or deleted, so the entry guards nothing. Delete it."
    }

    // An entry in the factory list says "a registered @Factory creates this sample". Two halves of
    // that are free to check, and without them one line in the file turns the samples rule off.
    val notASample = e.byFactory.filterNot { it.contains(".samples.") }
    if (notASample.isNotEmpty()) {
        problems += "Listed in ${e.factoryFile} but not under .samples.:\n  " +
            notASample.joinToString("\n  ") +
            "\n  This list exempts samples. A class outside .samples. needs no exemption."
    }
    // A sample a factory produces is never named in the suite. One that is named is a root test
    // under .samples. -- the leak the samples rule exists to catch, endorsed by the exemption.
    val exemptAndDeclared = e.byFactory intersect e.declared
    if (exemptAndDeclared.isNotEmpty()) {
        problems += "Listed in ${e.factoryFile} AND named in the suite:\n  " +
            exemptAndDeclared.joinToString("\n  ") +
            "\n  The suite runs it as a root test, so no @Factory is what makes it run."
    }

    val samplesThatRan = e.executed.filter { it.contains(".samples.") } - e.byFactory
    if (samplesThatRan.isNotEmpty()) {
        problems += "Ran but lives under .samples. -- these are TestNG input:\n  " +
            samplesThatRan.joinToString("\n  ") +
            "\n  Move it out of .samples., or add it to ${e.factoryFile} " +
            "if a registered @Factory creates it."
    }

    // An entry that stops running is as wrong as one that should not run. The factory that fed it
    // has gone, or the class has, and the list would go on claiming otherwise.
    val silentFactoryEntries = e.byFactory - e.executed
    if (silentFactoryEntries.isNotEmpty()) {
        problems += "Listed in ${e.factoryFile} but no longer running:\n  " +
            silentFactoryEntries.joinToString("\n  ") +
            "\n  The @Factory that created it is gone, or the class is."
    }

    if (e.updatingInventory) return problems

    // Only losses and regressions fail. A test that is new here is not a defect: it arrives from
    // master as often as from a branch, it is visible in the diff either way, and failing on it
    // would make every pull request red the moment master gained a test.
    val gone = e.expected.keys - e.ranNow.keys
    // Any status change is flagged, not only a worsening one: a skip that starts passing is good
    // news, but it still means the recorded outcome is stale.
    val changed = e.expected.filter { (name, was) ->
        val now = e.ranNow[name] ?: return@filter false
        now.status != was.status || now.invocations < was.invocations
    }

    if (gone.isNotEmpty()) {
        problems += "These tests no longer run:\n  " + gone.sorted().joinToString("\n  ") +
            "\n  A move that drops a class from both the suite and the code looks exactly like this."
    }
    if (changed.isNotEmpty()) {
        problems += "These tests no longer match their recorded outcome:\n" +
            changed.entries.sortedBy { it.key }.joinToString("\n") { (name, was) ->
                "  $name: was ${was}, now ${e.ranNow[name]}"
            } +
            "\n  If that is intended, rerun with -PupdateExecutionInventory and commit the diff."
    }
    return problems
}
