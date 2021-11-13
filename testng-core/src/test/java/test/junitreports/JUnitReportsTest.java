package test.junitreports;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;
import static test.junitreports.TestClassContainerForGithubIssue1265.*;

import com.beust.jcommander.internal.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import test.SimpleBaseTest;
import test.TestHelper;
import test.junitreports.issue2124.TestClassSample;
import test.junitreports.issue993.SampleTestClass;

public class JUnitReportsTest extends SimpleBaseTest {

  private static final String TESTS = "tests";
  private static final String ERRORS = "errors";
  private static final String FAILURES = "failures";
  private static final String IGNORED = "ignored";
  private static final String SKIPPED = "skipped";
  private static final String JUNIT_XSD = "jenkins-junit.xsd";
  private static String clazz = SimpleTestSample.class.getName();
  private static List<Testcase> testcaseList =
      Arrays.asList(
          Testcase.newInstance("childTest", clazz, SKIPPED),
          Testcase.newInstance("masterTest", clazz, "error"),
          Testcase.newInstance("masterTest", clazz, "failure"),
          Testcase.newInstance("iShouldNeverBeExecuted", clazz, SKIPPED),
          Testcase.newInstance("iShouldNeverBeExecuted", clazz, IGNORED));

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
    mapping.put(FirstTest.class, createMapFor(2, 1));
    mapping.put(SecondTest.class, createMapFor(1, 0));
    mapping.put(ThirdTest.class, createMapFor(1, 0));

    TestNG tng = createTests(outputDir, "suite", classes);
    LocalJUnitReportReporter reportReporter = new LocalJUnitReportReporter();
    tng.addListener(reportReporter);
    tng.run();
    for (Class<?> clazz : classes) {
      Testsuite suite = reportReporter.getTestsuite(clazz.getName());
      Map<String, Integer> attributes = mapping.get(clazz);
      assertEquals(suite.getName(), clazz.getName(), "Suite Name validation.");
      assertEquals(suite.getTests(), attributes.get(TESTS).intValue(), "<test> count validation.");
      assertEquals(
          suite.getErrors(), attributes.get(ERRORS).intValue(), "errored count validation.");
      assertEquals(
          suite.getIgnored(), attributes.get(IGNORED).intValue(), "ignored count validation.");
      assertEquals(
          suite.getFailures(), attributes.get(FAILURES).intValue(), "failure count validation.");
      assertEquals(
          suite.getSkipped(), attributes.get(SKIPPED).intValue(), "skipped count validation.");
    }
  }

  @Test
  public void testTestCaseOrderingInJUnitReportReporterWhenPrioritiesDefined() throws IOException {
    Path outputDir = TestHelper.createRandomDirectory();
    TestNG tng = createTests(outputDir, "suite", Issue1262TestSample.class);
    LocalJUnitReportReporter reportReporter = new LocalJUnitReportReporter();
    tng.addListener(reportReporter);
    tng.run();
    Testsuite suite = reportReporter.getTestsuite(Issue1262TestSample.class.getName());
    List<String> expected =
        new LinkedList<String>() {
          {
            add("testRoles001_Post");
            add("testRoles002_Post");
            add("testRoles003_Post");
            add("testRoles004_Post");
          }
        };
    List<String> actual = Lists.newLinkedList();
    for (Testcase testcase : suite.getTestcase()) {
      actual.add(testcase.getName().trim());
    }
    assertEquals(actual, expected);
  }

  @Test
  public void testEnsureTestnameDoesnotAcceptNullValues() throws IOException {
    Path outputDir = TestHelper.createRandomDirectory();
    TestNG tng = createTests(outputDir, "suite", SampleTestClass.class);
    LocalJUnitReportReporter reportReporter = new LocalJUnitReportReporter();
    tng.addListener(reportReporter);
    tng.run();
    Testsuite suite = reportReporter.getTestsuite(SampleTestClass.class.getName());
    Testcase testcase = suite.getTestcase().get(0);
    String actual = testcase.getName();
    assertEquals(actual, "Test_001");
  }

  @Test
  public void ensureTestReportContainsValidSysOutContent() throws Exception {
    Path outputDir = TestHelper.createRandomDirectory();
    TestNG tng = createTests(outputDir, "suite", TestClassSample.class);
    tng.setUseDefaultListeners(true);
    tng.run();
    DocumentBuilder builder = getJUnitDocumentBuilder();
    String name = "TEST-" + TestClassSample.class.getName();
    File file =
        new File(
            outputDir.toFile().getAbsolutePath()
                + File.separator
                + "junitreports"
                + File.separator
                + name
                + ".xml");
    Document doc = builder.parse(file);
    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//testsuite/system-out";
    String data = (String) xPath.compile(expression).evaluate(doc, XPathConstants.STRING);
    assertThat(data.trim()).isEqualTo(TestClassSample.MESSAGE_1 + "\n" + TestClassSample.MESSAGE_2);
  }

  private DocumentBuilder getJUnitDocumentBuilder()
      throws SAXException, ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    SchemaFactory xsdFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
    Schema schema = xsdFactory.newSchema(new File(getPathToResource(JUNIT_XSD)));
    factory.setSchema(schema);
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setErrorHandler(
        new DefaultHandler() {
          @Override
          public void error(SAXParseException e) {
            fail("Test Report Parse Error", e);
          }
        });
    return builder;
  }

  private static Map<String, Integer> createMapFor(int testCount, int skipped) {
    Map<String, Integer> map = Maps.newHashMap();
    map.put(TESTS, testCount);
    map.put(ERRORS, 0);
    map.put(IGNORED, 0);
    map.put(FAILURES, 0);
    map.put(SKIPPED, skipped);
    return map;
  }

  private void runTest(
      int tests,
      int errors,
      int ignored,
      int failures,
      int skipped,
      ITestNGListener reporter,
      boolean useClazzAsSuiteName)
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
    if (!useClazzAsSuiteName) {
      suitename = xmlTest.getName();
    }
    Testsuite suite = reportReporter.getTestsuite(suitename);
    assertEquals(suite.getName(), suitename, "Suite Name validation.");
    assertEquals(suite.getTests(), tests, "<test> count validation.");
    assertEquals(suite.getErrors(), errors, "errored count validation.");
    assertEquals(suite.getIgnored(), ignored, "ignored count validation.");
    assertEquals(suite.getFailures(), failures, "failure count validation.");
    assertEquals(suite.getSkipped(), skipped, "skipped count validation.");
    assertEquals(suite.getTestcase().size(), 3, "test case count validation.");
    List<Testcase> actualTestcases = suite.getTestcase();
    for (Testcase actualTestcase : actualTestcases) {
      assertTrue(
          testcaseList.contains(actualTestcase),
          "Validation of " + actualTestcase.getName() + " " + "presence.");
    }
  }
}
