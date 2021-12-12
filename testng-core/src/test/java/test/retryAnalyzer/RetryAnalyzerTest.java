package test.retryAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.retryAnalyzer.dataprovider.issue2163.TestClassPoweredByDataProviderSample;
import test.retryAnalyzer.github1519.MyListener;
import test.retryAnalyzer.github1519.TestClassSample;
import test.retryAnalyzer.github1600.Github1600Listener;
import test.retryAnalyzer.github1600.Github1600TestSample;
import test.retryAnalyzer.github1706.DataDrivenSample;
import test.retryAnalyzer.github1706.NativeInjectionSample;
import test.retryAnalyzer.github1706.ParameterInjectionSample;
import test.retryAnalyzer.github2669.RetryTestSample;
import test.retryAnalyzer.issue1241.GitHub1241Sample;
import test.retryAnalyzer.issue1538.TestClassSampleWithTestMethodDependencies;
import test.retryAnalyzer.issue1697.DatadrivenSample;
import test.retryAnalyzer.issue1697.LocalReporter;
import test.retryAnalyzer.issue1697.SampleTestclass;
import test.retryAnalyzer.issue1946.RetryAnalyzer;
import test.retryAnalyzer.issue1946.TestclassSample1;
import test.retryAnalyzer.issue1946.TestclassSample2;
import test.retryAnalyzer.issue2684.SampleTestClassWithGroupConfigs;

public class RetryAnalyzerTest extends SimpleBaseTest {
  @Test
  public void testInvocationCounts() {
    TestNG tng = create(InvocationCountTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(new TestResultPruner());
    tng.addListener(tla);

    tng.run();

    assertThat(tla.getFailedTests()).isEmpty();

    List<ITestResult> fsp = tla.getFailedButWithinSuccessPercentageTests();
    assertThat(fsp).hasSize(1);
    assertThat(fsp.get(0).getName()).isEqualTo("failAfterThreeRetries");

    List<ITestResult> skipped = tla.getSkippedTests();
    assertThat(skipped).hasSize(InvocationCountTest.invocations.size() - fsp.size());
  }

  @Test
  public void testIfRetryIsInvokedBeforeListener() {
    TestNG tng = create(TestClassSample.class);
    tng.addListener(new MyListener());
    tng.run();
    assertThat(TestClassSample.messages)
        .containsExactly("afterInvocation", "retry", "afterInvocation");
  }

  @Test(description = "GITHUB-1600")
  public void testIfRetryIsInvokedBeforeListenerButHasToConsiderFailures() {
    TestNG tng = create(Github1600TestSample.class);
    Github1600Listener listener = new Github1600Listener();
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.addListener(listener);
    tng.run();
    assertThat(tla.getFailedTests()).hasSize(1);
    assertThat(tla.getSkippedTests()).hasSize(1);
  }

  @Test(description = "GITHUB-1706", dataProvider = "1706")
  public void testIfRetryIsInvokedWhenTestMethodHas(
      Class<?> clazz, int size, Map<String, String> parameters) {
    XmlSuite xmlsuite = createXmlSuite("suite");
    XmlTest xmlTest = createXmlTest(xmlsuite, "test", clazz);
    if (!parameters.isEmpty()) {
      xmlTest.setParameters(parameters);
    }
    TestNG tng = create();
    tng.setXmlSuites(Collections.singletonList(xmlsuite));
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getSkippedMethodNames().size()).isEqualTo(size);
  }

  @DataProvider(name = "1706")
  public Object[][] getData() {
    return new Object[][] {
      {NativeInjectionSample.class, 2, Maps.newHashMap()},
      {DataDrivenSample.class, 4, Maps.newHashMap()},
      {ParameterInjectionSample.class, 2, constructParameterMap()}
    };
  }

  @Test(description = "GITHUB-1538")
  public void testIfDependentMethodsAreInvokedWhenRetrySucceeds() {
    TestNG testng = create(TestClassSampleWithTestMethodDependencies.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    assertThat(
            tla.getPassedTests().stream()
                .map(RetryAnalyzerTest::methodName)
                .collect(Collectors.toList()))
        .containsExactly("a", "b");
    assertThat(tla.getFailedTests()).isEmpty();
    assertThat(
            tla.getSkippedTests().stream()
                .map(RetryAnalyzerTest::methodName)
                .collect(Collectors.toList()))
        .containsExactly("a");
  }

  @Test(description = "GITHUB-1241")
  public void testToEnsureNewRetryAnalyzerInstanceUsedPerTest() {
    XmlSuite suite = createXmlSuite("Test Suite", "Test One", GitHub1241Sample.class);
    createXmlTest(suite, "Test Two", GitHub1241Sample.class);

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("test1", "test2", "test2", "test1", "test2", "test2");
  }

  @Test(description = "GITHUB-1697")
  public void ensureRetriedMethodsAreDistinguishable() {
    XmlSuite xmlsuite = createXmlSuite("1697_suite");
    createXmlTest(xmlsuite, "1697_test", SampleTestclass.class);
    TestNG testng = create(xmlsuite);
    LocalReporter reporter = new LocalReporter();
    testng.addListener(reporter);
    testng.run();
    runAssertions(reporter.getRetried(), "dataDrivenTest");
    runAssertions(reporter.getSkipped(), "child");
  }

  @Test
  public void ensureRetriedMethodsAreDistinguishableInDataDrivenTests() {
    XmlSuite xmlsuite = createXmlSuite("1697_suite");
    createXmlTest(xmlsuite, "1697_test", DatadrivenSample.class);
    TestNG testng = create(xmlsuite);
    LocalReporter reporter = new LocalReporter();
    testng.addListener(reporter);
    testng.run();
    ITestResult firstResult = runAssertions(reporter.getRetried(), "testMethod");
    assertThat(firstResult.getParameters()).containsAll(Collections.singletonList(1));
  }

  @Test(description = "GITHUB-1946")
  public void ensureRetriesHappenForDataDrivenTests() {
    List<String> expected =
        Arrays.asList(
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample1.class.getName()
                + ".test1(), Parameters : [param1, value1]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample1.class.getName()
                + ".test1(), Parameters : [param1, value1]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample1.class.getName()
                + ".test1(), Parameters : [param2, value2]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample1.class.getName()
                + ".test1(), Parameters : [param2, value2]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample1.class.getName()
                + ".test2(), Parameters : [param1, value1]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample1.class.getName()
                + ".test2(), Parameters : [param1, value1]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample1.class.getName()
                + ".test2(), Parameters : [param2, value2]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample1.class.getName()
                + ".test2(), Parameters : [param2, value2]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample2.class.getName()
                + ".test1(), Parameters : [param1, value1]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample2.class.getName()
                + ".test1(), Parameters : [param1, value1]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample2.class.getName()
                + ".test1(), Parameters : [param2, value2]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample2.class.getName()
                + ".test1(), Parameters : [param2, value2]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample2.class.getName()
                + ".test3(), Parameters : [param1, value1]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample2.class.getName()
                + ".test3(), Parameters : [param1, value1]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample2.class.getName()
                + ".test3(), Parameters : [param2, value2]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample2.class.getName()
                + ".test3(), Parameters : [param2, value2]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample2.class.getName()
                + ".test4(), Parameters : [param1, value1]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample2.class.getName()
                + ".test4(), Parameters : [param1, value1]",
            "Attempt #0. Retry :true  Test method : "
                + TestclassSample2.class.getName()
                + ".test4(), Parameters : [param2, value2]",
            "Attempt #1. Retry :false Test method : "
                + TestclassSample2.class.getName()
                + ".test4(), Parameters : [param2, value2]");
    XmlSuite xmlsuite = createXmlSuite("1946_suite");
    createXmlTest(xmlsuite, "1946_test", TestclassSample1.class, TestclassSample2.class);
    TestNG testng = create(xmlsuite);
    testng.run();
    assertThat(RetryAnalyzer.logs).containsExactlyElementsOf(expected);
  }

  @Test(description = "GITHUB-2163 & GITHUB-2280", timeOut = 5000)
  public void ensureRetryDoesntRunEndlesslyForDataDrivenTests() {
    XmlSuite xmlsuite = createXmlSuite("2163_suite");
    createXmlTest(xmlsuite, "2163_test", TestClassPoweredByDataProviderSample.class);
    TestNG testng = create(xmlsuite);
    testng.run();
    assertThat(test.retryAnalyzer.dataprovider.issue2163.RetryAnalyzer.logs).hasSize(21);
  }

  @Test(description = "GITHUB-2669")
  public void testFailedRetryWithParameters() {
    Map<String, String> params = new HashMap<>();
    params.put("id", "1111");
    params.put("name", "qa");
    params.put("age", "30");
    XmlSuite suite = createXmlSuite("GITHUB_2669", params);

    createXmlTest(suite, "2669_Test", RetryTestSample.class);
    TestNG testng = create(suite);
    testng.run();
    Assert.assertEquals(RetryTestSample.count, 3);
  }

  @Test(description = "GITHUB-2684")
  public void testAfterConfigurationsInvokedAfterRetriedMethod() {
    XmlSuite xmlSuite = createXmlSuite("2684_suite");
    createXmlTest(xmlSuite, "2684_test", SampleTestClassWithGroupConfigs.class);
    createXmlGroups(xmlSuite, "2684_group");
    TestNG testng = create(xmlSuite);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();

    String[] expected = {
      "beforeSuite",
      "beforeTest",
      "beforeClass",
      "beforeGroups",
      "beforeMethod",
      "testMethod",
      "afterMethod",
      "beforeMethod",
      "testMethod",
      "afterMethod",
      "afterGroups",
      "afterClass",
      "afterTest",
      "afterSuite"
    };
    assertThat(listener.getInvokedMethodNames()).containsExactly(expected);
  }

  private ITestResult runAssertions(Set<ITestResult> results, String methodName) {
    assertThat(results).hasSize(1);
    ITestResult firstResult = results.iterator().next();
    assertThat(firstResult.getMethod().getMethodName()).isEqualToIgnoringCase(methodName);
    return firstResult;
  }

  private static String methodName(ITestResult result) {
    return result.getMethod().getMethodName();
  }

  private static Map<String, String> constructParameterMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put("counter", "3");
    return map;
  }
}
