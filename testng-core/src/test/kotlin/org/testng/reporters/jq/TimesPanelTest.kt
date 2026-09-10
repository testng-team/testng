package org.testng.reporters.jq

import org.assertj.core.api.Assertions.assertThat

import org.testng.IReporter
import org.testng.ISuite
import org.testng.annotations.Test
import org.testng.internal.paramhandler.FakeSuite
import org.testng.reporters.XMLStringBuffer
import org.testng.xml.XmlSuite
import test.SimpleBaseTest

class TimesPanelTest : SimpleBaseTest() {

    companion object {
        const val GITHUB_1931 = "GITHUB-1931 [NPE] TimesPanel.maxTime(ISuite suite)"
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

    /** Sleeps so the durations the panel sums are not all zero. */
    class SleepingSample {
        @Test fun first() = Thread.sleep(20)

        @Test fun second() = Thread.sleep(20)
    }
}
