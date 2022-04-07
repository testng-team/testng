package org.testng.reporters.jq

import org.assertj.core.api.Assertions.assertThat

import org.testng.annotations.Test
import org.testng.internal.paramhandler.FakeSuite
import org.testng.reporters.XMLStringBuffer
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
}
