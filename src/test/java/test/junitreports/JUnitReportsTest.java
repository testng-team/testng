package test.junitreports;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.TestHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class JUnitReportsTest extends SimpleBaseTest {

    private static String clazz = SimpleTestSample.class.getName();
    private static List<Testcase> testcaseList = Arrays.asList(
        Testcase.newInstance("childTest", clazz, "skipped"),
        Testcase.newInstance("masterTest", clazz, "error"),
        Testcase.newInstance("masterTest", clazz, "failure"),
        Testcase.newInstance("iShouldNeverBeExecuted", clazz, "skipped"),
        Testcase.newInstance("iShouldNeverBeExecuted", clazz, "ignored")
    );

    @Test
    public void testJUnitXMLReporter() throws IOException {
        runTest(2, 0, 1, 1, 0, new LocalJUnitXMLReporter(), false);
    }

    @Test
    public void testJUnitReportReporter() throws IOException {
        runTest(3, 1, 0, 0, 2, new LocalJUnitReportReporter(), true);
    }

    private void runTest(int tests,
        int errors, int ignored, int failures, int skipped, ITestNGListener reporter, boolean useClazzAsSuiteName)
        throws IOException {
        Path outputDir = TestHelper.createRandomDirectory();
        XmlSuite xmlSuite = createXmlSuite("suite");
        XmlTest xmlTest = createXmlTest(xmlSuite, "test");
        createXmlClass(xmlTest, SimpleTestSample.class);
        TestNG tng = create(outputDir, xmlSuite);
        TestsuiteRetriever reportReporter = (TestsuiteRetriever) reporter;
        tng.addListener(reporter);
        tng.run();
        String suitename = SimpleTestSample.class.getName();
        if (! useClazzAsSuiteName) {
            suitename = xmlTest.getName();
        }
        Testsuite suite = reportReporter.getTestsuite(suitename);
        assertEquals(suite.getName(), suitename,"Suite Name validation.");
        assertEquals(suite.getTests(), tests, "<test> count validation.");
        assertEquals(suite.getErrors(), errors, "errored count validation.");
        assertEquals(suite.getIgnored(), ignored, "ignored count validation.");
        assertEquals(suite.getFailures(), failures, "failure count validation.");
        assertEquals(suite.getSkipped(), skipped, "skipped count validation.");
        assertEquals(suite.getTestcase().size(), 3, "test case count validation.");
        List<Testcase> actualTestcases = suite.getTestcase();
        for (Testcase actualTestcase : actualTestcases) {
            assertTrue(testcaseList.contains(actualTestcase), "Validation of " + actualTestcase.getName() + " " + "presence.");
        }
    }
}
