package test.junitreports;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.TestHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static test.junitreports.TestClassContainerForGithubIssue1265.*;

public class JUnitReportsTest extends SimpleBaseTest {

    private static final String TESTS = "tests";
    private static final String ERRORS = "errors";
    private static final String FAILURES = "failures";
    private static final String IGNORED = "ignored";
    private static final String SKIPPED = "skipped";
    private static String clazz = SimpleTestSample.class.getName();
    private static List<Testcase> testcaseList = Arrays.asList(
        Testcase.newInstance("childTest", clazz, SKIPPED),
        Testcase.newInstance("masterTest", clazz, "error"),
        Testcase.newInstance("masterTest", clazz, "failure"),
        Testcase.newInstance("iShouldNeverBeExecuted", clazz, SKIPPED),
        Testcase.newInstance("iShouldNeverBeExecuted", clazz, IGNORED)
    );

    @Test
    public void testJUnitXMLReporter() throws IOException {
        runTest(2, 0, 1, 1, 0, new LocalJUnitXMLReporter(), false);
    }

    @Test
    public void testJUnitReportReporter() throws IOException {
        runTest(3, 1, 0, 0, 2, new LocalJUnitReportReporter(), true);
    }

    @Test
    public void testJUnitReportReporterWithMultipleClasses() throws IOException {
        Path outputDir = TestHelper.createRandomDirectory();
        Class<?>[] classes = new Class<?>[] {FirstTest.class, SecondTest.class, ThirdTest.class};
        Map<Class<?>, Map<String, Integer>> mapping = Maps.newHashMap();
        mapping.put(FirstTest.class, createMapFor(2,0,0,0,1));
        mapping.put(SecondTest.class, createMapFor(1,0,0,0,0));
        mapping.put(ThirdTest.class, createMapFor(1,0,0,0,0));

        TestNG tng = createTests(outputDir, "suite", classes);
        LocalJUnitReportReporter reportReporter = new LocalJUnitReportReporter();
        tng.addListener((ITestNGListener) reportReporter);
        tng.run();
        for (Class<?> clazz : classes) {
            Testsuite suite = reportReporter.getTestsuite(clazz.getName());
            Map<String, Integer> attributes = mapping.get(clazz);
            assertEquals(suite.getName(), clazz.getName(),"Suite Name validation.");
            assertEquals(suite.getTests(), attributes.get(TESTS).intValue(), "<test> count validation.");
            assertEquals(suite.getErrors(), attributes.get(ERRORS).intValue(), "errored count validation.");
            assertEquals(suite.getIgnored(), attributes.get(IGNORED).intValue(), "ignored count validation.");
            assertEquals(suite.getFailures(), attributes.get(FAILURES).intValue(), "failure count validation.");
            assertEquals(suite.getSkipped(), attributes.get(SKIPPED).intValue(), "skipped count validation.");
        }
    }

    private static Map<String, Integer> createMapFor(int testCount, int errors, int ignored, int failures, int skipped) {
        Map<String, Integer> map = Maps.newHashMap();
        map.put(TESTS, testCount);
        map.put(ERRORS, errors);
        map.put(IGNORED, ignored);
        map.put(FAILURES, failures);
        map.put(SKIPPED, skipped);
        return map;
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
