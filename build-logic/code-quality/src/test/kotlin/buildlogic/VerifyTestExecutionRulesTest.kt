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
            Evidence(
                mentioned = setOf("test.dependent.SomeOtherTest"),
                silent = setOf("test.dependent.MissingGroupTest"),
            ),
            "but no suite file mentions it",
        )
    }

    @Test(description = "a task that cannot see every suite does not judge the shared list")
    fun aPartialViewDoesNotJudgeTheList() {
        // execution-known-silent.txt is shared by the task that verifies testng.xml and the one
        // that verifies testng-memory.xml. The memory task sees one suite, which names none of the
        // entries. Judging the list from that view reports every entry of the other suite as dead.
        assertNoProblem(Evidence(silent = setOf("test.jar.JarTest"), mentioned = null))
    }

    @Test(description = "a commented-out class is still a mention, so its entry stands")
    fun aCommentedOutClassKeepsItsEntry() {
        // declared strips comments; mentioned does not. A parked class appears only in mentioned.
        assertNoProblem(
            Evidence(mentioned = setOf("test.jar.JarTest"), silent = setOf("test.jar.JarTest"))
        )
    }

    // --- the group-filter classification -----------------------------------------------------------

    @Test(description = "a class filtered by the suite's group exclusion is not reported as silent")
    fun aGroupFilteredClassIsNotReportedAsSilent() {
        assertNoProblem(
            Evidence(
                declared = setOf("test.SerializationTest"),
                mentioned = setOf("test.SerializationTest"),
                groupFiltered = setOf("test.SerializationTest"),
            )
        )
    }

    @Test(description = "a group-filtered class that ran is accepted because it produced results")
    fun aGroupFilteredClassThatRanIsAccepted() {
        assertNoProblem(
            Evidence(
                declared = setOf("test.SerializationTest"),
                executed = setOf("test.SerializationTest"),
                groupFiltered = setOf("test.SerializationTest"),
            )
        )
    }

    @Test(description = "groupFiltered does not cover a class outside the declared set")
    fun aGroupFilteredClassNotDeclaredIsNotAffected() {
        assertNoProblem(Evidence(groupFiltered = setOf("test.NotInSuite")))
    }

    @Test(description = "a class with a group matching regex exclusion is classified as filtered")
    fun aClassWithGroupExcludedByRegexIsFiltered() {
        val source = """
            package test;
            import org.testng.annotations.Test;
            public class SampleTest {
                @Test(groups = "brokenSerialization")
                public void testOne() {}
            }
        """.trimIndent()
        val filter = GroupFilter(excludePatterns = listOf(Regex("broken.*")))
        assert(isClassGroupFiltered("test.SampleTest", source, filter))
    }

    @Test(description = "class-level @Test groups contribute to effective groups of test methods")
    fun classLevelGroupAnnotationIsInheritedByMethods() {
        val source = """
            package test;
            import org.testng.annotations.Test;
            @Test(groups = "broken")
            public class InheritedGroupTest {
                public void testOne() {}
            }
        """.trimIndent()
        val filter = GroupFilter(excludePatterns = listOf(Regex("broken")))
        assert(isClassGroupFiltered("test.InheritedGroupTest", source, filter))
    }

    @Test(description = "a method with multiple groups is excluded when ANY group is in the exclude set")
    fun multiGroupTestIsExcludedWhenAnyGroupIsExcluded() {
        val source = """
            package test;
            import org.testng.annotations.Test;
            public class MultiGroupTest {
                @Test(groups = {"checkin", "broken"})
                public void testOne() {}
            }
        """.trimIndent()
        val filter = GroupFilter(excludePatterns = listOf(Regex(asRegexp("broken"))))
        assert(isClassGroupFiltered("test.MultiGroupTest", source, filter))
    }

    @Test(description = "outer class with no test methods is not classified as filtered even if nested class has tests")
    fun outerClassWithNoTestsAndNestedClassWithTestsIsNotFiltered() {
        val source = """
            package test;
            import org.testng.annotations.Test;
            public class OuterClass {
                public static class NestedClass {
                    @Test(groups = "broken")
                    public void testNested() {}
                }
            }
        """.trimIndent()
        val filter = GroupFilter(excludePatterns = listOf(Regex(asRegexp("broken"))))
        // OuterClass has no test methods of its own, so it MUST NOT be classified as group-filtered.
        assert(!isClassGroupFiltered("test.OuterClass", source, filter))
        // NestedClass has a test method with group "broken", so it IS classified as group-filtered.
        assert(isClassGroupFiltered("test.OuterClass${'$'}NestedClass", source, filter))
    }

    @Test(description = "unescaped dollar in exclude pattern is treated as literal dollar sign")
    fun unescapedDollarInExcludePatternIsTreatedAsLiteralDollar() {
        val sourceWithBroken = """
            package test;
            import org.testng.annotations.Test;
            public class SampleTest {
                @Test(groups = "broken")
                public void testOne() {}
            }
        """.trimIndent()

        val sourceWithDollar = """
            package test;
            import org.testng.annotations.Test;
            public class SampleTest {
                @Test(groups = "broken$")
                public void testOne() {}
            }
        """.trimIndent()

        // TestNG asRegexp("broken$") converts "broken$" -> "broken\$"
        val filter = GroupFilter(excludePatterns = listOf(Regex(asRegexp("broken$"))))

        // "broken$" regex must NOT match group "broken"
        assert(!isClassGroupFiltered("test.SampleTest", sourceWithBroken, filter))

        // "broken$" regex MUST match group "broken$"
        assert(isClassGroupFiltered("test.SampleTest", sourceWithDollar, filter))
    }

    @Test(description = "include-only group filter excludes methods matching no included group")
    fun includeOnlyGroupFilterClassifiesClassWithNoMatchingGroupsAsFiltered() {
        val source = """
            package test;
            import org.testng.annotations.Test;
            public class SampleTest {
                @Test(groups = "slow")
                public void testOne() {}
            }
        """.trimIndent()
        // Filter includes ONLY "fast"
        val filter = GroupFilter(includePatterns = listOf(Regex(asRegexp("fast"))))
        // SampleTest has group "slow" which matches no include pattern, so it is filtered out.
        assert(isClassGroupFiltered("test.SampleTest", source, filter))
    }

    @Test(description = "an ungrouped test method is not excluded by an exclude-only group filter")
    fun ungroupedMethodIsNotExcludedByExcludeFilter() {
        val source = """
            package test;
            import org.testng.annotations.Test;
            public class UngroupedClass {
                @Test
                public void plainTest() {}
            }
        """.trimIndent()
        val filter = GroupFilter(excludePatterns = listOf(Regex(asRegexp("broken"))))
        // Plain @Test has no groups, so exclude filter on "broken" does NOT exclude it.
        assert(!isClassGroupFiltered("test.UngroupedClass", source, filter))
    }

    @Test(description = "method with inheritGroups = false opts out of class-level excluded group")
    fun methodWithInheritGroupsFalseOptOutFromClassLevelGroup() {
        val source = """
            package test;
            import org.testng.annotations.Test;
            @Test(groups = "broken")
            public class OptOutTest {
                @Test(inheritGroups = false, groups = "checkin")
                public void testOne() {}
            }
        """.trimIndent()
        val filter = GroupFilter(excludePatterns = listOf(Regex(asRegexp("broken"))))
        // The method opts out of "broken", so it belongs only to "checkin" and is NOT excluded.
        assert(!isClassGroupFiltered("test.OptOutTest", source, filter))
    }

    @Test(description = "suite-level group filter applies to test block classes")
    fun suiteLevelGroupFilterAppliesToTestBlockClasses() {
        val tempDir = java.nio.file.Files.createTempDirectory("test-suite-groups").toFile()
        try {
            val rootXml = tempDir.resolve("root.xml")
            val sourcesDir = tempDir.resolve("src")
            sourcesDir.mkdirs()

            rootXml.writeText("""
                <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
                <suite name="SuiteWithGroups">
                    <groups>
                        <run>
                            <exclude name="broken"/>
                        </run>
                    </groups>
                    <test name="TestWithoutGroups">
                        <classes><class name="test.SuiteExcludedTest"/></classes>
                    </test>
                </suite>
            """.trimIndent())

            val pkgDir = sourcesDir.resolve("test")
            pkgDir.mkdirs()
            pkgDir.resolve("SuiteExcludedTest.java").writeText("""
                package test;
                import org.testng.annotations.Test;
                public class SuiteExcludedTest {
                    @Test(groups = "broken")
                    public void testMethod() {}
                }
            """.trimIndent())

            val filtered = findGroupFilteredClasses(rootXml, sourcesDir)
            assert(filtered.contains("test.SuiteExcludedTest")) {
                "expected test.SuiteExcludedTest to be filtered via suite-level group exclusion, got $filtered"
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test(description = "findGroupFilteredClasses identifies SerializationTest and ThreadTest from repository suite and sources")
    fun integrationTestGroupFilteredClassesFromRepository() {
        val rootSuite = java.io.File("../../testng-core/src/test/resources/testng.xml").canonicalFile
        val sourcesDir = java.io.File("../../testng-core/src/test/java").canonicalFile
        assert(rootSuite.isFile) { "expected root suite at ${rootSuite.absolutePath}" }
        assert(sourcesDir.isDirectory) { "expected sources dir at ${sourcesDir.absolutePath}" }

        val filtered = findGroupFilteredClasses(rootSuite, sourcesDir)
        assert(filtered.contains("test.SerializationTest")) {
            "expected SerializationTest in filtered set, got $filtered"
        }
        assert(filtered.contains("org.testng.concurrency.ThreadTest")) {
            "expected ThreadTest in filtered set, got $filtered"
        }
    }

    @Test(description = "suite-file traversal is supported when computing groupFilteredClasses")
    fun suiteFileTraversalIsSupportedByGroupFilteredClasses() {
        val tempDir = java.nio.file.Files.createTempDirectory("test-suite-file").toFile()
        try {
            val rootXml = tempDir.resolve("root.xml")
            val childXml = tempDir.resolve("child.xml")
            val sourcesDir = tempDir.resolve("src")
            sourcesDir.mkdirs()

            rootXml.writeText("""
                <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
                <suite name="Root">
                    <suite-file path="child.xml"/>
                </suite>
            """.trimIndent())

            childXml.writeText("""
                <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
                <suite name="Child">
                    <test name="ChildTest">
                        <groups><run><exclude name="broken"/></run></groups>
                        <classes><class name="test.ChildTest"/></classes>
                    </test>
                </suite>
            """.trimIndent())

            val pkgDir = sourcesDir.resolve("test")
            pkgDir.mkdirs()
            pkgDir.resolve("ChildTest.java").writeText("""
                package test;
                import org.testng.annotations.Test;
                public class ChildTest {
                    @Test(groups = "broken")
                    public void testMethod() {}
                }
            """.trimIndent())

            val filtered = findGroupFilteredClasses(rootXml, sourcesDir)
            assert(filtered.contains("test.ChildTest")) {
                "expected test.ChildTest to be filtered via suite-file, got $filtered"
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test(description = "a class declared in multiple test blocks is not filtered if one block runs it")
    fun multiTestBlockDeclarationIsNotFilteredIfOneBlockRunsIt() {
        val tempDir = java.nio.file.Files.createTempDirectory("test-multi-block").toFile()
        try {
            val rootXml = tempDir.resolve("root.xml")
            val sourcesDir = tempDir.resolve("src")
            sourcesDir.mkdirs()

            rootXml.writeText("""
                <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
                <suite name="MultiBlock">
                    <test name="FilteredBlock">
                        <groups><run><exclude name="broken"/></run></groups>
                        <classes><class name="test.SharedTest"/></classes>
                    </test>
                    <test name="UnfilteredBlock">
                        <classes><class name="test.SharedTest"/></classes>
                    </test>
                </suite>
            """.trimIndent())

            val pkgDir = sourcesDir.resolve("test")
            pkgDir.mkdirs()
            pkgDir.resolve("SharedTest.java").writeText("""
                package test;
                import org.testng.annotations.Test;
                public class SharedTest {
                    @Test(groups = "broken")
                    public void testMethod() {}
                }
            """.trimIndent())

            val filtered = findGroupFilteredClasses(rootXml, sourcesDir)
            assert(!filtered.contains("test.SharedTest")) {
                "expected test.SharedTest NOT to be filtered because UnfilteredBlock runs it, got $filtered"
            }
        } finally {
            tempDir.deleteRecursively()
        }
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

    @Test(description = "a duplicate key is refused, and the message names both lines")
    fun aDuplicateKeyIsRefused() {
        // toMap() kept the last value. A second, lower count for the same test then became the
        // baseline, and a later loss down to that count went unreported.
        val message = refused("test.A#one\tPASS 3", "test.B#two\tPASS 1", "test.A#one\tPASS 1")
        assert(message.contains(":1") && message.contains(":3")) { "expected both lines, got: $message" }
        assert(message.contains("test.A#one")) { "expected the key, got: $message" }
    }

    // --- rewriting the inventory -------------------------------------------------------------------
    //
    // The write used to happen before the rules ran. A run that should fail on the suite, the
    // silent list or the factory list still replaced the baseline, and the next run compared
    // against a file written by a broken build.

    @Test(description = "a failing update leaves the baseline untouched")
    fun aFailingUpdateDoesNotWrite() {
        var written = false
        val problems = updateOrFail(
            Evidence(executed = setOf("org.testng.factory.samples.Leaked"), updatingInventory = true)
        ) { written = true }
        assert(problems.isNotEmpty()) { "expected the leak to be reported" }
        assert(!written) { "the baseline was written by a run that failed" }
    }

    @Test(description = "a clean update writes the baseline")
    fun aCleanUpdateWrites() {
        var written = false
        val problems = updateOrFail(Evidence(updatingInventory = true)) { written = true }
        assert(problems.isEmpty()) { "expected no problem, got: $problems" }
        assert(written) { "a clean update did not write the baseline" }
    }

    // --- what counts as a count ---------------------------------------------------------------------
    //
    // A generated count is always one or more. Each key is created with its first status, and the
    // fork count is the greatest common divisor of every raw size, which divides each of them. So
    // zero or less can only arrive by damage, and it is damage that switches a rule off: a baseline
    // of 0 makes "now.invocations < was.invocations" false for every real count.

    @Test(description = "a count of zero is refused, because it hides every later count")
    fun aZeroCountIsRefused() {
        assert(Outcome.parse("PASS 0") == null) { "PASS 0 was accepted" }
    }

    @Test(description = "a negative count is refused")
    fun aNegativeCountIsRefused() {
        assert(Outcome.parse("PASS -1") == null) { "PASS -1 was accepted" }
    }

    @Test(description = "one is a real count, so it is read")
    fun aCountOfOneIsRead() {
        assert(Outcome.parse("PASS 1") == Outcome("PASS", 1)) { "PASS 1 was not read" }
    }

    @Test(description = "an inventory line with a zero count fails the build")
    fun aZeroCountInTheFileIsRefused() {
        val message = refused("test.A#one\tPASS 0")
        assert(message.contains(":1")) { "expected the line number, got: $message" }
    }

    @Test(description = "the Outcome itself refuses a count below one, whoever builds it")
    fun theOutcomeRefusesACountBelowOne() {
        for (bad in listOf(0, -1)) {
            try {
                Outcome("PASS", bad)
                throw AssertionError("Outcome accepted a count of $bad")
            } catch (expected: IllegalArgumentException) {
                // the point of the test
            }
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
