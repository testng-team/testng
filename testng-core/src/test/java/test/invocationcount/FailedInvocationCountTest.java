package test.invocationcount;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.FailedReporter;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import test.SimpleBaseTest;
import test.invocationcount.issue1719.IssueTest;
import test.invocationcount.issue3170.DataDrivenWithSuccessPercentageAndInvocationCountDefinedSample;
import test.invocationcount.issue3170.DataDrivenWithSuccessPercentageDefinedSample;
import test.invocationcount.issue3180.SampleTestContainer;

public class FailedInvocationCountTest extends SimpleBaseTest {

  private void runTest(boolean skip, int passed, int failed, int skipped) {
    TestNG testng = create(FailedInvocationCount.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.setSkipFailedInvocationCounts(skip);
    testng.addListener(tla);
    testng.run();

    Assert.assertEquals(tla.getPassedTests().size(), passed);
    Assert.assertEquals(tla.getFailedTests().size(), failed);
    Assert.assertEquals(tla.getSkippedTests().size(), skipped);
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

    Assert.assertEquals(tla.getPassedTests().size(), 8);
    Assert.assertEquals(tla.getFailedTests().size(), 7);
    Assert.assertEquals(tla.getSkippedTests().size(), 5);
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
    String[] nothing = new String[] {""};
    boolean noXml = false;
    boolean xmlSeen = true;
    return new Object[][] {
      // Test has flaky iterations which pass eventually. So no failed xml should be seen.
      {SampleTestContainer.TestContainsFlakyDataDrivenTest.class, noXml, nothing},
      // Repetitive test that eventually passes. So no failed xml should be seen.
      {SampleTestContainer.TestContainsPercentageDrivenTest.class, noXml, nothing},
      // Not a test that repeats. So no invocation count attribute should be seen
      {SampleTestContainer.TestWithNormalFailingTest.class, xmlSeen, nothing},
      // Flaky test. So ensure only flaky invocations are referenced
      {SampleTestContainer.TestWithSomeFailingIterations.class, xmlSeen, new String[] {"0 2"}},
      // Flaky test. So ensure only flaky invocations are referenced
      {
        SampleTestContainer.TestContainsAlwaysFailingDataDrivenTest.class,
        xmlSeen,
        new String[] {"0"}
      },
      // This is a combination of all the earlier permutations.
      // So we should see an xml that contains only the true cases from earlier
      {
        SampleTestContainer.TestContainsAllCombinations.class,
        xmlSeen,
        new String[] {"", "0 2", "0"}
      }
    };
  }

  @Test(description = "GITHUB-3180", dataProvider = "github-3180")
  public void ensureInvocationCountHonoursRetriesWhenUsingDataProviders(
      Class<?> cls, boolean isXmlGenerated, String[] invocationCountValue) throws Exception {
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
    String xmlContent = Files.readString(xml);
    Document document = document(xmlContent);
    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath xPath = xPathFactory.newXPath();
    XPathExpression xPathExpression = xPath.compile("//methods/include");

    NodeList nodeList = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
    assertThat(nodeList.getLength()).isEqualTo(invocationCountValue.length);
    SoftAssertions softly = new SoftAssertions();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      assertThat(node).isNotNull();
      assertThat(node.getNodeType()).isEqualTo(Node.ELEMENT_NODE);
      Element element = (Element) node;
      String value = element.getAttribute("invocation-numbers");
      softly.assertThat(value).isEqualTo(invocationCountValue[i]);
    }
    softly.assertAll();
  }

  @DataProvider(name = "github-3180-test-tags")
  public Object[][] getTestData1() {
    String[] nothing = new String[] {"", ""};
    boolean noXml = false;
    boolean xmlSeen = true;
    return new Object[][] {
      // Test has flaky iterations which pass eventually. So no failed xml should be seen.
      {SampleTestContainer.TestContainsFlakyDataDrivenTest.class, noXml, nothing},
      // Repetitive test that eventually passes. So no failed xml should be seen.
      {SampleTestContainer.TestContainsPercentageDrivenTest.class, noXml, nothing},
      // Not a test that repeats. So no invocation count attribute should be seen
      {SampleTestContainer.TestWithNormalFailingTest.class, xmlSeen, nothing},
      // Flaky test. So ensure only flaky invocations are referenced
      {
        SampleTestContainer.TestWithSomeFailingIterations.class,
        xmlSeen,
        new String[] {"0 2", "0 2"}
      },
      // Flaky test. So ensure only flaky invocations are referenced
      {
        SampleTestContainer.TestContainsAlwaysFailingDataDrivenTest.class,
        xmlSeen,
        new String[] {"0", "0"}
      },
      // This is a combination of all the earlier permutations.
      // So we should see an xml that contains only the true cases from earlier
      {
        SampleTestContainer.TestContainsAllCombinations.class,
        xmlSeen,
        new String[] {"", "0 2", "0", "", "0 2", "0"}
      }
    };
  }

  @Test(description = "GITHUB-3180", dataProvider = "github-3180-test-tags")
  public void ensureInvocationCountHonoursRetriesWhenUsingMultipleTestTags(
      Class<?> cls, boolean isXmlGenerated, String[] invocationCountValue) throws Exception {
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
    String xmlContent = Files.readString(xml);
    Document document = document(xmlContent);
    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath xPath = xPathFactory.newXPath();
    XPathExpression xPathExpression = xPath.compile("//methods/include");

    NodeList nodeList = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
    assertThat(nodeList.getLength()).isEqualTo(invocationCountValue.length);
    SoftAssertions softly = new SoftAssertions();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      assertThat(node).isNotNull();
      assertThat(node.getNodeType()).isEqualTo(Node.ELEMENT_NODE);
      Element element = (Element) node;
      String value = element.getAttribute("invocation-numbers");
      softly.assertThat(value).isEqualTo(invocationCountValue[i]);
    }
    softly.assertAll();
  }

  private static Document document(String xmlContent) throws Exception {
    return DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(new InputSource(new StringReader(xmlContent)));
  }
}
