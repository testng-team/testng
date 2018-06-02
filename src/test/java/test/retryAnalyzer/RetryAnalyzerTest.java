package test.retryAnalyzer;

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
import test.retryAnalyzer.github1519.MyListener;
import test.retryAnalyzer.github1519.TestClassSample;
import test.retryAnalyzer.github1600.Github1600Listener;
import test.retryAnalyzer.github1600.Github1600TestSample;
import test.retryAnalyzer.github1706.DataDrivenSample;
import test.retryAnalyzer.github1706.NativeInjectionSample;
import test.retryAnalyzer.github1706.ParameterInjectionSample;
import test.retryAnalyzer.issue1241.GitHub1241Sample;
import test.retryAnalyzer.issue1538.TestClassSampleWithTestMethodDependencies;
import test.retryAnalyzer.issue1697.DatadrivenSample;
import test.retryAnalyzer.issue1697.LocalReporter;
import test.retryAnalyzer.issue1697.SampleTestclass;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(TestClassSample.messages).containsExactly("afterInvocation", "retry", "afterInvocation");
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
    public void testIfRetryIsInvokedWhenTestMethodHas(Class<?> clazz, int size, Map<String, String> parameters) {
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
        return new Object[][]{
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
        assertThat(tla.getPassedTests()
                .stream()
                .map(RetryAnalyzerTest::methodName)
                .collect(Collectors.toList())
        ).containsExactly("a", "b");
        assertThat(tla.getFailedTests()).isEmpty();
        assertThat(tla.getSkippedTests()
                .stream()
                .map(RetryAnalyzerTest::methodName)
                .collect(Collectors.toList())
        ).containsExactly("a");
    }

    @Test(description = "GITHUB-1241")
    public void testToEnsureNewRetryAnalyzerInstanceUsedPerTest() {
        XmlSuite suite = createXmlSuite("Test Suite", "Test One", GitHub1241Sample.class);
        createXmlTest(suite, "Test Two", GitHub1241Sample.class);

        TestNG tng = create(suite);

        InvokedMethodNameListener listener = new InvokedMethodNameListener();
        tng.addListener(listener);

        tng.run();

        assertThat(listener.getInvokedMethodNames()).containsExactly("test1", "test2", "test2", "test1", "test2", "test2");
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