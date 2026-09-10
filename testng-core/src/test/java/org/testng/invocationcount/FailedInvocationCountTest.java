package org.testng.invocationcount;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.assertj.core.api.SoftAssertions;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.invocationcount.issue1719.IssueTest;
import org.testng.invocationcount.samples.FailedInvocationCount;
import org.testng.invocationcount.samples.FailedInvocationCount2;
import org.testng.invocationcount.samples.issue3170.DataDrivenWithSuccessPercentageAndInvocationCountDefinedSample;
import org.testng.invocationcount.samples.issue3170.DataDrivenWithSuccessPercentageDefinedSample;
import org.testng.invocationcount.samples.issue3180.SampleTestContainer;
import org.testng.reporters.FailedReporter;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import test.SimpleBaseTest;

public class FailedInvocationCountTest extends SimpleBaseTest {

  private void runTest(boolean skip, int passed, int failed, int skipped) {
    TestNG testng = create(FailedInvocationCount.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.setSkipFailedInvocationCounts(skip);
    testng.addListener(tla);
    testng.run();

    assertThat(tla.getPassedTests().size()).isEqualTo(passed);
    assertThat(tla.getFailedTests().size()).isEqualTo(failed);
    assertThat(tla.getSkippedTests().size()).isEqualTo(skipped);
  }

  @Test
  public void verifyGloballyShouldStop() {
    runTest(true, 4, 1, 5);
  }

  @Test
  public void verifyGloballyShouldNotStop() {
    runTest(false, 4, 6, 0);
  }

  @Test
  public void verifyAttributeShouldStop() {
    TestNG testng = create(FailedInvocationCount2.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();

    assertThat(tla.getPassedTests().size()).isEqualTo(8);
    assertThat(tla.getFailedTests().size()).isEqualTo(7);
    assertThat(tla.getSkippedTests().size()).isEqualTo(5);
  }

  @Test(dataProvider = "dp")
  public void ensureSuccessPercentageWorksFineWith(Class<?> clazz, IssueTest.Expected expected) {
    TestNG testng = create(clazz);
    AtomicInteger failed = new AtomicInteger(0);
    AtomicInteger passed = new AtomicInteger(0);
    AtomicInteger failedWithInSuccessPercentage = new AtomicInteger(0);
    testng.addListener(
        new IInvokedMethodListener() {
          @Override
          public void afterInvocation(IInvokedMethod method, ITestResult testResult) {

            switch (testResult.getStatus()) {
              case ITestResult.SUCCESS:
                passed.incrementAndGet();
                break;
              case ITestResult.FAILURE:
                failed.incrementAndGet();
                break;
              case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                failedWithInSuccessPercentage.incrementAndGet();
                break;
              default:
            }
          }
        });
    testng.run();
    assertThat(passed.get()).isEqualTo(expected.success());
    assertThat(failed.get()).isEqualTo(expected.failures());
    assertThat(failedWithInSuccessPercentage.get())
        .isEqualTo(expected.failedWithinSuccessPercentage());
  }

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {
      {
        DataDrivenWithSuccessPercentageAndInvocationCountDefinedSample.class,
        new IssueTest.Expected().failures(10)
      },
      {
        DataDrivenWithSuccessPercentageDefinedSample.class,
        new IssueTest.Expected().failures(3).success(1)
      }
    };
  }

  @DataProvider(name = "github-3180")
  public Object[][] getTestData() {
    Map<String, String> nothing = Collections.emptyMap();
    boolean noXml = false;
    boolean xmlSeen = true;
    return new Object[][] {
      // Test has flaky iterations which pass eventually. So no failed xml should be seen.
      {SampleTestContainer.TestContainsFlakyDataDrivenTest.class, noXml, nothing},
      // Repetitive test that eventually passes. So no failed xml should be seen.
      {SampleTestContainer.TestContainsPercentageDrivenTest.class, noXml, nothing},
      // Not a test that repeats. So no invocation count attribute should be seen
      {
        SampleTestContainer.TestWithNormalFailingTest.class,
        xmlSeen,
        Map.of("alwaysFailingRegularTest", "")
      },
      // Flaky test. So ensure only flaky invocations are referenced
      {
        SampleTestContainer.TestWithSomeFailingIterations.class,
        xmlSeen,
        Map.of("failsForOddNumbersOnly", "0 2")
      },
      // Flaky test. So ensure only flaky invocations are referenced
      {
        SampleTestContainer.TestContainsAlwaysFailingDataDrivenTest.class,
        xmlSeen,
        Map.of("alwaysFailingDataDrivenTest", "0")
      },
      // This is a combination of all the earlier permutations.
      // So we should see an xml that contains only the true cases from earlier
      {
        SampleTestContainer.TestContainsAllCombinations.class,
        xmlSeen,
        Map.of(
            "alwaysFailingRegularTest", "",
            "failsForOddNumbersOnly", "0 2",
            "alwaysFailingDataDrivenTest", "0")
      }
    };
  }

  @Test(description = "GITHUB-3180", dataProvider = "github-3180")
  public void ensureInvocationCountHonoursRetriesWhenUsingDataProviders(
      Class<?> cls, boolean isXmlGenerated, Map<String, String> expected) throws Exception {
    String reportsDir = createDirInTempDir("3180").getAbsolutePath();
    TestNG testng = create(Paths.get(reportsDir), cls);
    testng.setUseDefaultListeners(false);
    testng.addListener(new FailedReporter());
    testng.run();
    Path xml = Paths.get(reportsDir, "testng-failed.xml");
    assertThat(xml.toFile().exists()).isEqualTo(isXmlGenerated);
    if (!isXmlGenerated) {
      // Do not validate anything if the xml is NOT generated
      return;
    }
    assertIncludes(xml, expected, 1);
  }

  @DataProvider(name = "github-3180-test-tags")
  public Object[][] getTestData1() {
    // The same class runs under two <test> tags, so every method below is expected twice.
    return getTestData();
  }

  @Test(description = "GITHUB-3180", dataProvider = "github-3180-test-tags")
  public void ensureInvocationCountHonoursRetriesWhenUsingMultipleTestTags(
      Class<?> cls, boolean isXmlGenerated, Map<String, String> expected) throws Exception {
    String reportsDir = createDirInTempDir("3180").getAbsolutePath();
    XmlSuite xmlSuite = createXmlSuite("sample_suite");
    createXmlTest(xmlSuite, "sample_test1", cls);
    createXmlTest(xmlSuite, "sample_test2", cls);
    TestNG testng = create(Paths.get(reportsDir), xmlSuite);
    testng.setUseDefaultListeners(false);
    testng.addListener(new FailedReporter());
    testng.run();
    Path xml = Paths.get(reportsDir, "testng-failed.xml");
    assertThat(xml.toFile().exists()).isEqualTo(isXmlGenerated);
    if (!isXmlGenerated) {
      // Do not validate anything if the xml is NOT generated
      return;
    }
    assertIncludes(xml, expected, 2);
  }

  /**
   * Checks the {@code <include>} elements of a failed-suite file against the invocation numbers
   * each method is expected to record. {@code timesEachMethodRuns} says how many {@code <test>}
   * tags ran the class, because a method appears once under each of them.
   *
   * <p>The order of the elements is not checked. TestNG collects the methods of a class through a
   * hash-based set, so their order follows {@code Method#hashCode()}, which includes the name of
   * the declaring class and changes when that class is renamed. GITHUB-3180 is about the value each
   * method records, not about the order they are written in.
   */
  private static void assertIncludes(
      Path xml, Map<String, String> expected, int timesEachMethodRuns) throws Exception {
    Document document = document(Files.readString(xml));
    XPathExpression xPathExpression =
        XPathFactory.newInstance().newXPath().compile("//methods/include");
    NodeList nodeList = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);

    SoftAssertions softly = new SoftAssertions();
    Map<String, Integer> seen = new HashMap<>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      assertThat(node).isNotNull();
      assertThat(node.getNodeType()).isEqualTo(Node.ELEMENT_NODE);
      Element element = (Element) node;
      String name = element.getAttribute("name");
      seen.merge(name, 1, Integer::sum);
      softly
          .assertThat(element.getAttribute("invocation-numbers"))
          .describedAs("invocation-numbers of %s", name)
          .isEqualTo(expected.get(name));
    }
    // Count each name on its own. A total plus a set of names would let one method lose an
    // occurrence while another gains it, which is the miscount GITHUB-3180 is about.
    Map<String, Integer> wanted = new HashMap<>();
    expected.keySet().forEach(name -> wanted.put(name, timesEachMethodRuns));
    softly.assertThat(seen).describedAs("how often each method is listed").isEqualTo(wanted);
    softly.assertAll();
  }

  private static Document document(String xmlContent) throws Exception {
    return DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(new InputSource(new StringReader(xmlContent)));
  }
}
