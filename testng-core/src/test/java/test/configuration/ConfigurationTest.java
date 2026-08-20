package test.configuration;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.IConfigEavesdropper;
import org.testng.xml.XmlSuite;
import test.InvokedMethodNameListener;
import test.configuration.issue2726.TestClassSample;
import test.configuration.issue2743.SuiteRunnerIssueTestSample;
import test.configuration.issue3358.ChildFirstTimeOnlyInvocationSample;
import test.configuration.issue3358.ChildFirstTimeOnlyParallelDpFailingSample;
import test.configuration.issue3358.ChildFirstTimeOnlyParallelDpSample;
import test.configuration.issue3358.ChildFirstTimeOnlyParallelFailingSample;
import test.configuration.issue3358.ChildFirstTimeOnlyParallelSample;
import test.configuration.issue3358.ChildFirstTimeOnlySequentialDpSample;
import test.configuration.issue3358.ChildFirstTimeOnlyWithLastTimeSample;
import test.configuration.issue3358.FactoryFirstTimeOnlySample;
import test.configuration.issue3358.OverloadedFirstTimeOnlySample;
import test.configuration.sample.ConfigurationTestSample;
import test.configuration.sample.ExternalConfigurationClassSample;
import test.configuration.sample.MethodCallOrderTestSample;
import test.configuration.sample.SuiteTestSample;
import test.listeners.issue2961.OnlyOnceConfigurationThatFailsTestSample;
import test.listeners.issue2961.OnlyOnceConfigurationThatPassesTestSample;

/**
 * Test @Configuration
 *
 * @author cbeust
 */
public class ConfigurationTest extends ConfigurationBaseTest {
  @Test
  public void testConfiguration() {
    testConfiguration(ConfigurationTestSample.class);
  }

  @Test
  public void testMethodCallOrder() {
    testConfiguration(MethodCallOrderTestSample.class, ExternalConfigurationClassSample.class);
  }

  @Test
  public void testSuite() {
    testConfiguration(SuiteTestSample.class);
    assertThat(asList(1, 2, 3, 4, 5)).isEqualTo(SuiteTestSample.m_order);
  }

  @Test(description = "GITHUB-2743")
  public void testSuiteRunnerWithDefaultConfiguration() {
    TestNG testNG = create(SuiteRunnerIssueTestSample.class);
    testNG.run();

    assertThat(testNG.getStatus()).isEqualTo(0);
  }

  @Test(description = "GITHUB-2726")
  public void testAfterClassCalledOnlyOnceForParallelTestMethods() {
    TestNG testng = create(TestClassSample.class);
    testng.setParallel(XmlSuite.ParallelMode.METHODS);
    testng.setVerbose(2);
    testng.run();
    assertThat(TestClassSample.beforeLogs).hasSize(1);
    assertThat(TestClassSample.afterLogs).hasSize(1);
  }

  @Test(description = "GITHUB-2961")
  public void ensureFirstTimeOnlyConfigsHaveProperTestStatuses() {
    TestNG testng = create(OnlyOnceConfigurationThatPassesTestSample.class);
    testng.setVerbose(2);
    testng.run();
    assertThat(testng.getStatus()).isZero();
  }

  @Test(description = "GITHUB-3358")
  public void failingChildFirstTimeOnlyBeforeMethodIsInvoked() {
    TestNG testng = create(OnlyOnceConfigurationThatFailsTestSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(
            namesStartingWith(listener.getSucceedMethodNames(), "beforeMethodFirstTimeOnlyParent"))
        .hasSize(1);
    assertThat(
            namesStartingWith(
                listener.getFailedMethodNames(), "beforeMethodFirstTimeOnlyTestClass"))
        .hasSize(1);
    assertThat(listener.getSkippedMethodNames()).containsExactly("test");
    assertThat(
            namesStartingWith(listener.getInvokedMethodNames(), "beforeMethodFirstTimeOnlyParent"))
        .hasSize(1);
    assertThat(
            namesStartingWith(
                listener.getInvokedMethodNames(), "beforeMethodFirstTimeOnlyTestClass"))
        .hasSize(1);
    assertThat(testng.getStatus()).isNotZero();
  }

  @Test(description = "GITHUB-3358")
  public void parentAndChildFirstTimeOnlyRunOnceWithInvocationCount() {
    InvokedMethodNameListener listener = runWithListener(ChildFirstTimeOnlyInvocationSample.class);
    assertThat(listener.getInvokedMethodNames())
        .containsExactly("beforeParent", "beforeChild", "test", "test", "test");
    assertThat(listener.getSucceedMethodNames())
        .containsExactly("beforeParent", "beforeChild", "test", "test", "test");
  }

  @Test(description = "GITHUB-3358")
  public void parentAndChildFirstTimeOnlyCompleteBeforeParallelInvocations() {
    InvokedMethodNameListener listener = runWithListener(ChildFirstTimeOnlyParallelSample.class);
    List<String> invoked = listener.getInvokedMethodNames();
    assertThat(invoked).filteredOn("beforeParent"::equals).hasSize(1);
    assertThat(invoked).filteredOn("beforeChild"::equals).hasSize(1);
    assertThat(invoked).filteredOn("test"::equals).hasSize(3);
    int firstTest = invoked.indexOf("test");
    assertThat(invoked.indexOf("beforeParent")).isGreaterThanOrEqualTo(0).isLessThan(firstTest);
    assertThat(invoked.indexOf("beforeChild")).isGreaterThanOrEqualTo(0).isLessThan(firstTest);
  }

  @Test(description = "GITHUB-3358")
  public void failingChildFirstTimeOnlySkipsAllParallelInvocations() {
    InvokedMethodNameListener listener =
        runWithListener(ChildFirstTimeOnlyParallelFailingSample.class);
    assertThat(listener.getSucceedMethodNames()).containsExactly("beforeParent");
    assertThat(listener.getFailedMethodNames()).containsExactly("beforeChild");
    assertThat(listener.getSkippedMethodNames()).containsExactly("test", "test", "test");
    assertThat(listener.getInvokedMethodNames())
        .filteredOn(name -> name.equals("beforeParent") || name.equals("beforeChild"))
        .hasSize(2);
  }

  @Test(description = "GITHUB-3358")
  public void parentAndChildFirstTimeOnlyRunOnceWithParallelDataProvider() {
    ChildFirstTimeOnlyParallelDpSample.reset();
    InvokedMethodNameListener listener = runWithListener(ChildFirstTimeOnlyParallelDpSample.class);
    assertThat(listener.getInvokedMethodNames()).filteredOn("beforeParent"::equals).hasSize(1);
    assertThat(listener.getInvokedMethodNames()).filteredOn("beforeChild"::equals).hasSize(1);
    assertThat(listener.getSucceedMethodNames())
        .containsExactlyInAnyOrder(
            "beforeParent", "beforeChild", "test(0)", "test(1)", "test(2)", "test(3)", "test(4)");
    assertThat(ChildFirstTimeOnlyParallelDpSample.childFinishedAt.get()).isPositive();
    assertThat(ChildFirstTimeOnlyParallelDpSample.testStartedAt)
        .isNotEmpty()
        .allMatch(started -> started >= ChildFirstTimeOnlyParallelDpSample.childFinishedAt.get());
  }

  @Test(description = "GITHUB-3358")
  public void failingChildFirstTimeOnlySkipsParallelDataProviderBeforeTestBody() {
    ChildFirstTimeOnlyParallelDpFailingSample.reset();
    InvokedMethodNameListener listener =
        runWithListener(ChildFirstTimeOnlyParallelDpFailingSample.class);
    assertThat(listener.getSucceedMethodNames()).containsExactly("beforeParent");
    assertThat(listener.getFailedMethodNames()).containsExactly("beforeChild");
    assertThat(listener.getSkippedMethodNames())
        .containsExactlyInAnyOrder("test(0)", "test(1)", "test(2)", "test(3)", "test(4)");
    assertThat(ChildFirstTimeOnlyParallelDpFailingSample.testBodies.get()).isZero();
  }

  @Test(description = "GITHUB-3358")
  public void parentAndChildFirstTimeOnlyRunOnceWithSequentialDataProvider() {
    InvokedMethodNameListener listener =
        runWithListener(ChildFirstTimeOnlySequentialDpSample.class);
    assertThat(listener.getInvokedMethodNames())
        .containsExactly("beforeParent", "beforeChild", "test(0)", "test(1)", "test(2)");
  }

  @Test(description = "GITHUB-3358")
  public void firstTimeOnlyRemainsScopedPerFactoryInstance() {
    FactoryFirstTimeOnlySample.reset();
    InvokedMethodNameListener listener = runWithListener(FactoryFirstTimeOnlySample.class);
    assertThat(listener.getInvokedMethodNames()).filteredOn("before"::equals).hasSize(2);
    assertThat(listener.getInvokedMethodNames()).filteredOn("test"::equals).hasSize(4);
    assertThat(FactoryFirstTimeOnlySample.befores).hasSize(2);
    assertThat(FactoryFirstTimeOnlySample.befores.values()).allMatch(count -> count.get() == 1);
    assertThat(FactoryFirstTimeOnlySample.tests).hasSize(2);
    assertThat(FactoryFirstTimeOnlySample.tests.values()).allMatch(count -> count.get() == 2);
  }

  @Test(description = "GITHUB-3358")
  public void firstTimeOnlyRunsOncePerOverloadedTestMethod() {
    OverloadedFirstTimeOnlySample.reset();
    InvokedMethodNameListener listener = runWithListener(OverloadedFirstTimeOnlySample.class);
    assertThat(listener.getInvokedMethodNames()).filteredOn("before"::equals).hasSize(2);
    assertThat(listener.getSucceedMethodNames())
        .containsExactlyInAnyOrder("before", "before", "test", "test(a)", "test(b)");
    assertThat(OverloadedFirstTimeOnlySample.befores.get()).isEqualTo(2);
  }

  @Test(description = "GITHUB-3358")
  public void lastTimeOnlyStillRunsOnceAlongsideFirstTimeOnly() {
    InvokedMethodNameListener listener =
        runWithListener(ChildFirstTimeOnlyWithLastTimeSample.class);
    assertThat(listener.getInvokedMethodNames())
        .containsExactly("beforeParent", "beforeChild", "test", "test", "test", "afterLast");
  }

  @Test(description = "GITHUB-3359")
  public void firstTimeOnlyConfigsFireSuccessAndLeavePassedNotScheduled() {
    TestNG testng = create(OnlyOnceConfigurationThatPassesTestSample.class);
    FirstTimeOnlyConfigEvents listener = new FirstTimeOnlyConfigEvents();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.before)
        .contains(
            "beforeMethodFirstTimeOnlyParent",
            "beforeMethodFirstTimeOnlyTestClass",
            "afterMethodLastTimeOnly");
    assertThat(listener.success)
        .contains(
            "beforeMethodFirstTimeOnlyParent",
            "beforeMethodFirstTimeOnlyTestClass",
            "afterMethodLastTimeOnly");
    assertThat(listener.passed)
        .contains(
            "beforeMethodFirstTimeOnlyParent",
            "beforeMethodFirstTimeOnlyTestClass",
            "afterMethodLastTimeOnly");
    assertThat(listener.scheduled)
        .doesNotContain("beforeMethodFirstTimeOnlyParent", "beforeMethodFirstTimeOnlyTestClass");
    assertThat(testng.getStatus()).isZero();
  }

  private static InvokedMethodNameListener runWithListener(Class<?> cls) {
    TestNG testng = create(cls);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    return listener;
  }

  private static List<String> namesStartingWith(List<String> names, String prefix) {
    return names.stream().filter(name -> name.startsWith(prefix)).collect(Collectors.toList());
  }

  private static final class FirstTimeOnlyConfigEvents
      implements IConfigurationListener, ITestListener {
    private final List<String> before = new ArrayList<>();
    private final List<String> success = new ArrayList<>();
    private Set<String> passed = Set.of();
    private Set<String> scheduled = Set.of();

    @Override
    public void beforeConfiguration(ITestResult tr) {
      before.add(tr.getMethod().getMethodName());
    }

    @Override
    public void onConfigurationSuccess(ITestResult tr) {
      success.add(tr.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
      passed = methodNames(context.getPassedConfigurations().getAllResults());
      scheduled =
          methodNames(
              ((IConfigEavesdropper) context)
                  .getConfigurationsScheduledForInvocation()
                  .getAllResults());
    }

    private static Set<String> methodNames(Set<ITestResult> results) {
      return results.stream()
          .map(result -> result.getMethod().getMethodName())
          .collect(Collectors.toSet());
    }
  }

  @Test(description = "GITHUB-3003")
  public void ensureGroupInheritanceWorksForConfigMethods() {
    TestNG testng = create(test.configuration.issue3003.TestClassSample.class);
    testng.setVerbose(2);
    testng.run();
    List<String> expected =
        Arrays.asList("setupMethod1", "setupMethod2", "setupMethod3", "testMethod1");
    assertThat(test.configuration.issue3003.TestClassSample.logs).containsAll(expected);
  }

  @Test(description = "GITHUB-3006")
  public void ensureNativelyInjectedTestResultForAfterMethodMatchesTestMethod() {
    TestNG testng = create(test.configuration.issue3006.TestClassSample.class);
    testng.run();
    ITestResult actual = test.configuration.issue3006.TestClassSample.iTestResult;
    assertThat(actual.getStatus())
        .withFailMessage("The test method status should have been SKIPPED")
        .isEqualTo(ITestResult.SKIP);
    List<String> skippedDueTo =
        actual.getSkipCausedBy().stream()
            .map(ITestNGMethod::getQualifiedName)
            .collect(Collectors.toList());
    assertThat(skippedDueTo)
        .containsExactly("test.configuration.issue3006.TestClassSample.beforeMethod");
  }
}
