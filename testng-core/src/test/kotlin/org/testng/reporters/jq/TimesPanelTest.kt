package org.testng.reporters.jq

import org.assertj.core.api.Assertions.assertThat

import java.time.Instant
import java.util.Date
import org.testng.IReporter
import org.testng.ISuite
import org.testng.ISuiteResult
import org.testng.ITestContext
import org.testng.ITestResult
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.testng.internal.TestResult
import org.testng.internal.paramhandler.FakeSuite
import org.testng.reporters.XMLStringBuffer
import org.testng.xml.XmlClass
import org.testng.xml.XmlInclude
import org.testng.xml.XmlSuite
import org.testng.xml.XmlSuite.ParallelMode
import test.SimpleBaseTest

class TimesPanelTest : SimpleBaseTest() {

    companion object {
        const val GITHUB_1931 = "GITHUB-1931 [NPE] TimesPanel.maxTime(ISuite suite)"

        /** Two results that ran side by side, for the same 100 ms. */
        private val SAME_WINDOW = listOf(passed(1_000, 1_100), passed(1_000, 1_100))

        private fun passed(start: Long, end: Long) = Ran(start, end, ITestResult.SUCCESS)

        private fun failed(start: Long, end: Long) = Ran(start, end, ITestResult.FAILURE)

        private fun skipped(start: Long, end: Long) = Ran(start, end, ITestResult.SKIP)
    }

    /** A test result that ran from [start] to [end], in millis, and ended with [status]. */
    data class Ran(val start: Long, val end: Long, val status: Int)

    @Test(description = GITHUB_1931)
    fun generateReportTimesPanelContentForSuiteWithoutStartedTests() {
        val xmlTest = createXmlTest("GITHUB_1931", "NPE", Object::class.java)
        val iSuite = FakeSuite(xmlTest)
        val suites = listOf(iSuite)
        val model = Model(suites)
        val panel = TimesPanel(model)
        val buffer = XMLStringBuffer()
        panel.getContent(iSuite, buffer)
        assertThat(panel.getContent(iSuite, buffer))
            .withFailMessage { "TimesPanel contains total running time" }
            .contains("Total running time: 0 ms")
    }

    @Test(description = "getContent reports the same total however often it is called")
    fun theSuiteTotalIsNotAccumulatedAcrossCalls() {
        // js() used to add each result's duration into m_totalTime, which maxTime then read back,
        // so every extra call reported the suite as having run that much longer. The test above
        // calls it twice and could not see it: its FakeSuite starts nothing, and twice zero is
        // zero.
        val suites = runAndCaptureSuites()
        val panel = TimesPanel(Model(suites))

        val totals =
            (1..3).map { totalRunningTimeOf(panel.getContent(suites[0], XMLStringBuffer(""))) }

        assertThat(totals[1]).`as`("second call").isEqualTo(totals[0])
        assertThat(totals[2]).`as`("third call").isEqualTo(totals[0])
    }

    @Test(description = "Each result gets its own row, from a builder reused across the loop")
    fun everyResultGetsOneRowOfItsOwn() {
        // The row builder is hoisted out of the loop and reset per iteration; forget the reset and
        // each row carries every row before it, which no assertion on the suite total would see.
        val suites = runAndCaptureSuites()
        val content = TimesPanel(Model(suites)).getContent(suites[0], XMLStringBuffer(""))

        val methodCells = Regex("""data\.setCell\((\d+), 1, '([^']*)'\)""").findAll(content).toList()
        assertThat(methodCells.map { it.groupValues[2] })
            .containsExactlyInAnyOrder("first", "second")
        // One cell per index, so nothing was carried over from the previous iteration.
        assertThat(methodCells.map { it.groupValues[1] }).containsExactly("0", "1")
    }

    @DataProvider
    fun runningTimes(): Array<Array<Any>> =
        arrayOf(
            arrayOf(ParallelMode.CLASSES, listOf(SAME_WINDOW), "100 ms"),
            arrayOf(ParallelMode.METHODS, listOf(SAME_WINDOW), "100 ms"),
            arrayOf(ParallelMode.INSTANCES, listOf(SAME_WINDOW), "100 ms"),
            // Partly overlapping: from the first start to the last end.
            arrayOf(
                ParallelMode.METHODS,
                listOf(listOf(passed(1_000, 1_100), passed(1_050, 1_150))),
                "150 ms",
            ),
            // One result inside another: the outer one.
            arrayOf(
                ParallelMode.METHODS,
                listOf(listOf(passed(1_000, 1_300), passed(1_100, 1_200))),
                "300 ms",
            ),
            // A chain: the third result overlaps the second but not the first, and still joins
            // their span.
            arrayOf(
                ParallelMode.METHODS,
                listOf(listOf(passed(1_000, 1_100), passed(1_050, 1_150), passed(1_120, 1_220))),
                "220 ms",
            ),
            // Failed and skipped results count as well as passed ones.
            arrayOf(
                ParallelMode.METHODS,
                listOf(listOf(passed(1_000, 1_100), failed(1_050, 1_150), skipped(1_100, 1_200))),
                "200 ms",
            ),
            // Two <test>s run one after the other, even when the methods in each run in parallel.
            arrayOf(
                ParallelMode.METHODS,
                listOf(SAME_WINDOW, listOf(passed(1_200, 1_300), passed(1_200, 1_300))),
                "200 ms",
            ),
            // One after another, with a gap that no test ran in: the sum, as before. The gap does
            // not count, so this is less than the 250 ms from the first start to the last end.
            arrayOf(
                ParallelMode.NONE,
                listOf(listOf(passed(1_000, 1_100), passed(1_150, 1_250))),
                "200 ms",
            ),
        )

    @Test(description = "GITHUB-1423", dataProvider = "runningTimes")
    fun theSuiteTotalCountsOverlappingResultsOnce(
        mode: ParallelMode,
        intervalsPerTest: List<List<Ran>>,
        expected: String,
    ) {
        val suite = suiteWithResults(mode, intervalsPerTest)
        val content = TimesPanel(Model(listOf(suite))).getContent(suite, XMLStringBuffer(""))

        assertThat(totalRunningTimeOf(content)).isEqualTo("Total running time: $expected")
    }

    @Test(description = "GITHUB-1423: parallel=\"tests\" still reports its longest <test>")
    fun testsInParallelReportTheLongestTest() {
        // The results of both <test>s overlap into 100 ms, while the <test>s themselves took 300
        // and 500 ms. So 500 ms can only come from the longest <test>: the overlap of the results
        // would give 100 ms, and the shorter <test> 300 ms.
        val ran = suiteWithResults(ParallelMode.TESTS, listOf(SAME_WINDOW, SAME_WINDOW))
        val suite = withTestDurations(ran, mapOf("test-0" to 300L, "test-1" to 500L))
        val content = TimesPanel(Model(listOf(suite))).getContent(suite, XMLStringBuffer(""))

        assertThat(totalRunningTimeOf(content)).isEqualTo("Total running time: 500 ms")
    }

    /**
     * Runs one <test> of [ThreeMethodsSample] per entry of [intervalsPerTest], with one of its
     * methods per interval, then swaps their results for ones that started and ended at the given
     * millis with the given status, so the totals do not depend on how long anything really took.
     */
    private fun suiteWithResults(mode: ParallelMode, intervalsPerTest: List<List<Ran>>): ISuite {
        val xmlSuite = createXmlSuite("GITHUB-1423").apply { setParallel(mode) }
        intervalsPerTest.forEachIndexed { index, intervals ->
            createXmlTest(xmlSuite, "test-$index").apply {
                xmlClasses =
                    listOf(
                        XmlClass(ThreeMethodsSample::class.java).apply {
                            includedMethods =
                                ThreeMethodsSample.METHODS.take(intervals.size).map(::XmlInclude)
                        }
                    )
            }
        }
        val captured = mutableListOf<ISuite>()
        val tng = create(xmlSuite)
        tng.addListener(
            object : IReporter {
                override fun generateReport(
                    xmlSuites: List<XmlSuite>,
                    suites: List<ISuite>,
                    outputDirectory: String,
                ) {
                    captured.addAll(suites)
                }
            }
        )
        tng.run()

        val suite = captured.single()
        intervalsPerTest.forEachIndexed { index, intervals ->
            val context = suite.results.getValue("test-$index").testContext
            val passedTests = context.passedTests
            val ran = passedTests.allResults.sortedBy { it.method.methodName }
            assertThat(ran).hasSameSizeAs(intervals)
            ran.zip(intervals).forEach { (result, interval) ->
                passedTests.removeResult(result)
                val resultsWithStatus =
                    when (interval.status) {
                        ITestResult.FAILURE -> context.failedTests
                        ITestResult.SKIP -> context.skippedTests
                        else -> passedTests
                    }
                resultsWithStatus.addResult(
                    TestResult.newEndTimeAwareTestResult(result.method, context, null, interval.start)
                        .apply {
                            endMillis = interval.end
                            status = interval.status
                        }
                )
            }
        }
        return suite
    }

    /**
     * [suite], with each <test> having taken the given millis, instead of however long it really
     * took to run.
     */
    private fun withTestDurations(suite: ISuite, millisPerTest: Map<String, Long>): ISuite {
        val start = Instant.ofEpochMilli(1_000)
        val results: Map<String, ISuiteResult> =
            suite.results.mapValues { (name, result) ->
                val end = start.plusMillis(millisPerTest.getValue(name))
                val context: ITestContext =
                    object : ITestContext by result.testContext {
                        override fun getStartDate(): Date = Date.from(start)

                        override fun getEndDate(): Date = Date.from(end)

                        override fun getStartInstant(): Instant = start

                        override fun getEndInstant(): Instant = end
                    }
                object : ISuiteResult by result {
                    override fun getTestContext(): ITestContext = context
                }
            }
        return object : ISuite by suite {
            override fun getResults(): Map<String, ISuiteResult> = results
        }
    }

    private fun totalRunningTimeOf(content: String): String =
        Regex("""<span class="suite-total-time">([^<]*)</span>""").find(content)!!.groupValues[1]

    private fun runAndCaptureSuites(): List<ISuite> {
        val captured = mutableListOf<ISuite>()
        val tng = create(SleepingSample::class.java)
        tng.addListener(
            object : IReporter {
                override fun generateReport(
                    xmlSuites: List<XmlSuite>,
                    suites: List<ISuite>,
                    outputDirectory: String,
                ) {
                    captured.addAll(suites)
                }
            }
        )
        tng.run()
        return captured
    }

    class ThreeMethodsSample {
        companion object {
            val METHODS = listOf("first", "second", "third")
        }

        @Test fun first() {}

        @Test fun second() {}

        @Test fun third() {}
    }

    /** Sleeps so the durations the panel sums are not all zero. */
    class SleepingSample {
        @Test fun first() = Thread.sleep(20)

        @Test fun second() = Thread.sleep(20)
    }
}
