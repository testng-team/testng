package org.testng.reporters.jq

import org.assertj.core.api.Assertions.assertThat

import org.testng.IReporter
import org.testng.ISuite
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.testng.internal.TestResult
import org.testng.internal.Utils
import org.testng.internal.paramhandler.FakeSuite
import org.testng.reporters.XMLStringBuffer
import org.testng.xml.XmlClass
import org.testng.xml.XmlSuite
import org.testng.xml.XmlSuite.ParallelMode
import test.SimpleBaseTest

class TimesPanelTest : SimpleBaseTest() {

    companion object {
        const val GITHUB_1931 = "GITHUB-1931 [NPE] TimesPanel.maxTime(ISuite suite)"

        /** Two results that ran side by side, for the same 100 ms. */
        private val SAME_WINDOW = listOf(1_000L to 1_100L, 1_000L to 1_100L)
    }

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
                listOf(listOf(1_000L to 1_100L, 1_050L to 1_150L)),
                "150 ms",
            ),
            // Two <test>s run one after the other, even when the methods in each run in parallel.
            arrayOf(
                ParallelMode.METHODS,
                listOf(SAME_WINDOW, listOf(1_200L to 1_300L, 1_200L to 1_300L)),
                "200 ms",
            ),
            // One after another, with a gap that no test ran in: the sum, as before.
            arrayOf(ParallelMode.NONE, listOf(listOf(1_000L to 1_100L, 1_150L to 1_250L)), "200 ms"),
        )

    @Test(description = "GITHUB-1423", dataProvider = "runningTimes")
    fun theSuiteTotalCountsOverlappingResultsOnce(
        mode: ParallelMode,
        intervalsPerTest: List<List<Pair<Long, Long>>>,
        expected: String,
    ) {
        val suite = suiteWithResults(mode, intervalsPerTest)
        val content = TimesPanel(Model(listOf(suite))).getContent(suite, XMLStringBuffer(""))

        assertThat(totalRunningTimeOf(content)).isEqualTo("Total running time: $expected")
    }

    @Test(description = "GITHUB-1423: parallel=\"tests\" still reports its longest <test>")
    fun testsInParallelReportTheLongestTest() {
        val suite = suiteWithResults(ParallelMode.TESTS, listOf(SAME_WINDOW, SAME_WINDOW))
        val longest = suite.results.values.maxOf { Utils.durationOf(it.testContext) }
        val content = TimesPanel(Model(listOf(suite))).getContent(suite, XMLStringBuffer(""))

        assertThat(longest).`as`("the longest <test>, in ms").isLessThan(1_000)
        assertThat(totalRunningTimeOf(content)).isEqualTo("Total running time: $longest ms")
    }

    /**
     * Runs one <test> of [TwoMethodsSample] per entry of [intervalsPerTest], then swaps the two
     * results of each for ones that started and ended at the given millis, so the totals do not
     * depend on how long anything really took.
     */
    private fun suiteWithResults(
        mode: ParallelMode,
        intervalsPerTest: List<List<Pair<Long, Long>>>,
    ): ISuite {
        val xmlSuite = createXmlSuite("GITHUB-1423").apply { setParallel(mode) }
        intervalsPerTest.indices.forEach {
            createXmlTest(xmlSuite, "test-$it").apply {
                xmlClasses = listOf(XmlClass(TwoMethodsSample::class.java))
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
            val passed = context.passedTests
            val ran = passed.allResults.sortedBy { it.method.methodName }
            assertThat(ran).hasSameSizeAs(intervals)
            ran.zip(intervals).forEach { (result, interval) ->
                passed.removeResult(result)
                passed.addResult(
                    TestResult.newEndTimeAwareTestResult(
                            result.method,
                            context,
                            null,
                            interval.first,
                        )
                        .apply { endMillis = interval.second }
                )
            }
        }
        return suite
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

    class TwoMethodsSample {
        @Test fun first() {}

        @Test fun second() {}
    }

    /** Sleeps so the durations the panel sums are not all zero. */
    class SleepingSample {
        @Test fun first() = Thread.sleep(20)

        @Test fun second() = Thread.sleep(20)
    }
}
