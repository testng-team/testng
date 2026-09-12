package buildlogic

import buildlogic.VerifyTestExecution.Outcome
import org.testng.annotations.Test

/**
 * Tests the rules [problemsIn] applies.
 *
 * These rules decide whether a build passes, and a wrong answer is silent both ways. A real defect
 * is waved through, or a green branch turns red for nothing. The `knownSilent` list already proved
 * the first half: an entry only ever failed the build when the class it names RAN, so two entries
 * whose classes were renamed away sat in the file through a green merge and nobody saw them.
 *
 * So the cases that must be REJECTED come first in each block. An accepted case is the one anybody
 * would write anyway.
 */
class VerifyTestExecutionRulesTest {

    private fun problems(e: Evidence) = problemsIn(e)

    private fun assertNoProblem(e: Evidence) {
        val found = problems(e)
        assert(found.isEmpty()) { "expected no problem, got:\n${found.joinToString("\n")}" }
    }

    private fun assertProblem(e: Evidence, holds: String) {
        val found = problems(e)
        assert(found.any { it.contains(holds) }) {
            "expected a problem holding <$holds>, got:\n${found.joinToString("\n") { "  $it" }}"
        }
    }

    // --- the samples boundary, and the exemption that opens it ------------------------------------

    @Test(description = "a sample that ran with no exemption is a leak")
    fun aSampleThatRanIsReported() {
        assertProblem(
            Evidence(executed = setOf("org.testng.factory.samples.Leaked")),
            "Ran but lives under .samples.",
        )
    }

    @Test(description = "the exemption is what lets a factory-produced sample run")
    fun anExemptSampleMayRun() {
        assertNoProblem(
            Evidence(
                executed = setOf("org.testng.factory.samples.FactoryTest2"),
                byFactory = setOf("org.testng.factory.samples.FactoryTest2"),
            )
        )
    }

    @Test(description = "an exemption the suite also names is the leak it was meant to catch")
    fun anExemptSampleTheSuiteNamesIsReported() {
        assertProblem(
            Evidence(
                declared = setOf("org.testng.factory.samples.FactoryTest2"),
                executed = setOf("org.testng.factory.samples.FactoryTest2"),
                byFactory = setOf("org.testng.factory.samples.FactoryTest2"),
            ),
            "AND named in the suite",
        )
    }

    @Test(description = "a class outside samples needs no exemption, so listing one is a mistake")
    fun anExemptionOutsideSamplesIsReported() {
        assertProblem(
            Evidence(
                executed = setOf("org.testng.factory.FactoryTest"),
                byFactory = setOf("org.testng.factory.FactoryTest"),
            ),
            "but not under .samples.",
        )
    }

    @Test(description = "an exemption that stops running means the factory or the class has gone")
    fun anExemptionThatStoppedRunningIsReported() {
        assertProblem(
            Evidence(byFactory = setOf("org.testng.factory.samples.FactoryTest2")),
            "but no longer running",
        )
    }

    @Test(description = "the exemption covers the class it names, not its neighbours")
    fun anExemptionDoesNotCoverAnotherSample() {
        assertProblem(
            Evidence(
                executed = setOf(
                    "org.testng.factory.samples.FactoryTest2",
                    "org.testng.factory.samples.Leaked",
                ),
                byFactory = setOf("org.testng.factory.samples.FactoryTest2"),
            ),
            "org.testng.factory.samples.Leaked",
        )
    }

    @Test(description = "a name that only starts like an exemption is a different class")
    fun aLongerNameIsNotExempt() {
        assertProblem(
            Evidence(
                executed = setOf("org.testng.factory.samples.FactoryTest22"),
                byFactory = setOf("org.testng.factory.samples.FactoryTest2"),
            ),
            "org.testng.factory.samples.FactoryTest22",
        )
    }

    // --- the known-silent list ---------------------------------------------------------------------

    @Test(description = "a declared class that never ran is the GitHub issue #1362 failure mode")
    fun aDeclaredClassThatNeverRanIsReported() {
        assertProblem(
            Evidence(declared = setOf("test.Parked"), mentioned = setOf("test.Parked")),
            "Named in the suite but never ran",
        )
    }

    @Test(description = "an entry excuses a declared class that does not run")
    fun anEntryExcusesADeclaredClass() {
        assertNoProblem(
            Evidence(
                declared = setOf("test.Parked"),
                mentioned = setOf("test.Parked"),
                silent = setOf("test.Parked"),
            )
        )
    }

    @Test(description = "an entry whose class runs again is stale")
    fun anEntryThatRunsAgainIsReported() {
        assertProblem(
            Evidence(
                declared = setOf("test.Woken"),
                mentioned = setOf("test.Woken"),
                executed = setOf("test.Woken"),
                silent = setOf("test.Woken"),
            ),
            "but running now",
        )
    }

    @Test(description = "an entry no suite file mentions is dead text")
    fun anEntryNoSuiteMentionsIsReported() {
        assertProblem(
            Evidence(silent = setOf("test.dependent.MissingGroupTest")),
            "but no suite file mentions it",
        )
    }

    @Test(description = "a commented-out class is still a mention, so its entry stands")
    fun aCommentedOutClassKeepsItsEntry() {
        // declared strips comments; mentioned does not. A parked class appears only in mentioned.
        assertNoProblem(
            Evidence(mentioned = setOf("test.jar.JarTest"), silent = setOf("test.jar.JarTest"))
        )
    }

    // --- the inventory ------------------------------------------------------------------------------

    @Test(description = "a test that stops running fails, however it stopped")
    fun aTestThatStoppedRunningIsReported() {
        assertProblem(
            Evidence(expected = mapOf("test.A#one" to Outcome("PASS", 1))),
            "These tests no longer run",
        )
    }

    @Test(description = "a status change fails, even a change for the better")
    fun aStatusChangeIsReported() {
        assertProblem(
            Evidence(
                ranNow = mapOf("test.A#one" to Outcome("SKIP", 1)),
                expected = mapOf("test.A#one" to Outcome("PASS", 1)),
            ),
            "no longer match their recorded outcome",
        )
    }

    @Test(description = "a lost invocation fails, which is how a dropped data provider row shows")
    fun aLostInvocationIsReported() {
        assertProblem(
            Evidence(
                ranNow = mapOf("test.A#one" to Outcome("PASS", 2)),
                expected = mapOf("test.A#one" to Outcome("PASS", 3)),
            ),
            "no longer match their recorded outcome",
        )
    }

    @Test(description = "a new test is not a defect: it arrives from master as often as a branch")
    fun aNewTestIsAccepted() {
        assertNoProblem(Evidence(ranNow = mapOf("test.New#one" to Outcome("PASS", 1))))
    }

    @Test(description = "an extra invocation is accepted, which the inventory rule states")
    fun anExtraInvocationIsAccepted() {
        assertNoProblem(
            Evidence(
                ranNow = mapOf("test.A#one" to Outcome("PASS", 4)),
                expected = mapOf("test.A#one" to Outcome("PASS", 3)),
            )
        )
    }

    @Test(description = "rewriting the inventory has nothing to compare, so those rules stand down")
    fun rewritingTheInventorySkipsTheComparison() {
        assertNoProblem(
            Evidence(
                expected = mapOf("test.Gone#one" to Outcome("PASS", 1)),
                updatingInventory = true,
            )
        )
    }

    @Test(description = "rewriting the inventory does not stand the samples rule down")
    fun rewritingTheInventoryKeepsTheSamplesRule() {
        assertProblem(
            Evidence(
                executed = setOf("org.testng.factory.samples.Leaked"),
                updatingInventory = true,
            ),
            "Ran but lives under .samples.",
        )
    }

    // --- reading the inventory ----------------------------------------------------------------------
    //
    // A line that does not parse used to be dropped in silence. The entry then looked like a test
    // that never existed, so the rule above could not report its loss. That is the one rule the
    // class doc calls "the one that survives a package migration", switched off by a bad merge or
    // by an editor that turned a tab into spaces.

    private fun refused(vararg lines: String): String {
        try {
            readInventory("execution-inventory.txt", lines.toList())
        } catch (e: Exception) {
            return e.message ?: ""
        }
        throw AssertionError("expected a refusal for ${lines.toList()}")
    }

    @Test(description = "a line with no tab is refused, and the message says which line")
    fun aLineWithNoTabIsRefused() {
        val message = refused("test.A#one\tPASS 1", "test.B#two PASS 1")
        assert(message.contains(":2")) { "expected the line number, got: $message" }
        assert(message.contains("test.B#two")) { "expected the line text, got: $message" }
    }

    @Test(description = "a tab with no count is refused")
    fun aStatusWithNoCountIsRefused() {
        refused("test.A#one\tPASS")
    }

    @Test(description = "a count that is not a number is refused")
    fun aCountThatIsNotANumberIsRefused() {
        refused("test.A#one\tPASS many")
    }

    @Test(description = "extra fields are refused, because the shape is not the one that was written")
    fun extraFieldsAreRefused() {
        refused("test.A#one\tPASS 1 2")
    }

    @Test(description = "an empty outcome is refused")
    fun anEmptyOutcomeIsRefused() {
        refused("test.A#one\t")
    }

    @Test(description = "a well-formed file is read, and blank lines are skipped")
    fun aWellFormedFileIsRead() {
        val read = readInventory(
            "execution-inventory.txt",
            listOf("test.A#one\tPASS 1", "", "test.B#two\tSKIP 3", "   "),
        )
        assert(read == mapOf("test.A#one" to Outcome("PASS", 1), "test.B#two" to Outcome("SKIP", 3))) {
            "read the wrong thing: $read"
        }
    }

    // --- the whole set together ---------------------------------------------------------------------

    @Test(description = "the shape this repository is in today passes")
    fun aHealthyRepositoryPasses() {
        assertNoProblem(
            Evidence(
                declared = setOf("org.testng.factory.FactoryTest", "org.testng.factory.ObjectIdTest"),
                mentioned = setOf(
                    "org.testng.factory.FactoryTest",
                    "org.testng.factory.ObjectIdTest",
                    "test.jar.JarTest",
                ),
                executed = setOf("org.testng.factory.ObjectIdTest", "org.testng.factory.samples.FactoryTest2"),
                ranNow = mapOf("org.testng.factory.ObjectIdTest#testId" to Outcome("PASS", 1)),
                expected = mapOf("org.testng.factory.ObjectIdTest#testId" to Outcome("PASS", 1)),
                silent = setOf("org.testng.factory.FactoryTest", "test.jar.JarTest"),
                byFactory = setOf("org.testng.factory.samples.FactoryTest2"),
            )
        )
    }

    @Test(description = "every problem is reported, not just the first")
    fun everyProblemIsReported() {
        val found = problems(
            Evidence(
                declared = setOf("test.Parked"),
                mentioned = setOf("test.Parked"),
                executed = setOf("org.testng.factory.samples.Leaked"),
                silent = setOf("test.Renamed"),
                byFactory = setOf("org.testng.factory.Outside"),
            )
        )
        assert(found.size == 5) { "expected five problems, got ${found.size}:\n${found.joinToString("\n")}" }
    }
}
