package test.skip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.Map;
import org.testng.CommandLineArgs;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.skip.github1967.TestClassSample;
import test.skip.issue2674.ConfigAwareTestNG;

public class ReasonForSkipTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1878")
  public void ensureSkipInfoHasFailedConfigDetails() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("testMethod", "beforeClass");
    runTest(expected, TestClassWithFailedConfig.class);
  }

  @Test(description = "GITHUB-1878")
  public void ensureSkipInfoHasFailedTestDetails() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("childMethod", "parentMethod");
    runTest(expected, TestClassWithFailedMethod.class);
  }

  @Test(description = "GITHUB-1878")
  public void ensureSkipInfoHasAllFailedTestDetails() {
    TestNG testng = create(TestClassWithMultipleFailures.class);
    ReasonReporter reporter = new ReasonReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.getSkippedInfo())
        .containsAnyOf(entry("child", "father,mother"), entry("child", "mother,father"));
  }

  @Test(description = "GITHUB-1878")
  public void ensureSkipInfoHasFailedConfigDetailsInBaseClass() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("testMethod", "beforeClass");
    expected.put("testMethodInChildClass", "beforeClass");
    runTest(expected, TestClassWithFailedConfigInParentClass.class);
  }

  @Test(description = "GITHUB-1878")
  public void ensureSkipInfoHasFailedTestDetailsInBaseClass() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("childMethod", "parentMethod");
    expected.put("anotherChild", "parentMethod");
    runTest(expected, TestClassWithFailedMethodInParentClass.class);
  }

  @Test(description = "GITHUB-1878")
  public void ensureSkipInfoHasGlobalConfigFailureDetails() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("testMethod", "beforeTest");
    runTest(expected, TestClassWithOnlyGlobalConfig.class, TestClassWithOnlyTestMethods.class);
  }

  @Test(description = "GITHUB-1878")
  public void ensureSkipInfoHasFailedTestDetailsWhenInvolvingGroups() {
    XmlSuite xmlSuite = createXmlSuite("sample_suite");
    createXmlTest(xmlSuite, "sample_test", TestClassWithGroupFailures.class);
    xmlSuite.getIncludedGroups().addAll(Arrays.asList("unit", "integration"));
    TestNG testng = create(xmlSuite);
    ReasonReporter reporter = new ReasonReporter();
    testng.addListener(reporter);
    testng.run();
    Map<String, String> expected = Maps.newHashMap();
    expected.put("integrationTests", "unitTests");
    assertThat(reporter.getSkippedInfo()).containsAllEntriesOf(expected);
  }

  @Test(description = "GITHUB-1878")
  public void ensureSkipInfoHasFailedTestDetailsWhenInvolvingMultipleGroups() {
    XmlSuite xmlSuite = createXmlSuite("sample_suite");
    createXmlTest(xmlSuite, "sample_test", TestClassWithMultipleGroupFailures.class);
    xmlSuite.getIncludedGroups().addAll(Arrays.asList("p1", "p2", "all"));
    TestNG testng = create(xmlSuite);
    ReasonReporter reporter = new ReasonReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.getSkippedInfo())
        .containsAnyOf(entry("child", "father,mother"), entry("child", "mother,father"));
  }

  @Test(description = "GITHUB-1878")
  public void testEnsureTestStatusIsSetProperlyForSkippedTests() {
    TestNG testng = create(TestClassSample.class);
    ReasonReporter reporter = new ReasonReporter();
    testng.addListener(reporter);
    testng.run();
    Map<String, Integer> actual = reporter.getResults();
    Map<String, Integer> expected = Maps.newHashMap();
    expected.put("test1min", ITestResult.SKIP);
    expected.put("test2min", ITestResult.SKIP);
    expected.put("setup", ITestResult.FAILURE);
    assertThat(actual).containsAllEntriesOf(expected);
  }

  @Test(description = "GITHUB-2674")
  public void ensureUpstreamFailuresTriggerSkipsForAllDataProviderValues() {
    TestNG testng = create(test.skip.issue2674.TestClassSample.class);
    testng.setReportAllDataDrivenTestsAsSkipped(true);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getSkippedMethodNames())
        .containsExactly("test2(iPhone,13)", "test2(iPhone-Pro,12)");
  }

  @Test(description = "GITHUB-2674")
  public void ensureUpstreamFailuresTriggerSkipsForAllDataProviderValuesViaCmdLineArgs() {
    CommandLineArgs cli = new CommandLineArgs();
    cli.includeAllDataDrivenTestsWhenSkipping = true;
    ConfigAwareTestNG testng = new ConfigAwareTestNG();
    testng.setTestClasses(new Class<?>[] {test.skip.issue2674.TestClassSample.class});
    testng.configure(cli);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getSkippedMethodNames())
        .containsExactly("test2(iPhone,13)", "test2(iPhone-Pro,12)");
  }

  private static void runTest(Map<String, String> expected, Class<?>... clazz) {
    TestNG testng = create(clazz);
    ReasonReporter reporter = new ReasonReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.getSkippedInfo()).containsAllEntriesOf(expected);
  }
}
