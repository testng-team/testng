package test.skip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class GitHubIssue1878Test extends SimpleBaseTest {

  @Test
  public void ensureSkipInfoHasFailedConfigDetails() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("testMethod", "beforeClass");
    runTest(expected, TestClassWithFailedConfig.class);
  }

  @Test
  public void ensureSkipInfoHasFailedTestDetails() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("childMethod", "parentMethod");
    runTest(expected, TestClassWithFailedMethod.class);
  }

  @Test
  public void ensureSkipInfoHasAllFailedTestDetails() {
    TestNG testng = create(TestClassWithMultipleFailures.class);
    ReasonReporter reporter = new ReasonReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.getSkippedInfo()).containsAnyOf(entry("child", "father,mother"),
        entry("child", "mother,father"));
  }

  @Test
  public void ensureSkipInfoHasFailedConfigDetailsInBaseClass() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("testMethod", "beforeClass");
    expected.put("testMethodInChildClass", "beforeClass");
    runTest(expected, TestClassWithFailedConfigInParentClass.class);
  }

  @Test
  public void ensureSkipInfoHasFailedTestDetailsInBaseClass() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("childMethod", "parentMethod");
    expected.put("anotherChild", "parentMethod");
    runTest(expected, TestClassWithFailedMethodInParentClass.class);
  }

  @Test
  public void ensureSkipInfoHasGlobalConfigFailureDetails() {
    Map<String, String> expected = Maps.newHashMap();
    expected.put("testMethod", "beforeTest");
    runTest(expected, TestClassWithOnlyGlobalConfig.class, TestClassWithOnlyTestMethods.class);
  }

  @Test
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

  @Test
  public void ensureSkipInfoHasFailedTestDetailsWhenInvolvingMultipleGroups() {
    XmlSuite xmlSuite = createXmlSuite("sample_suite");
    createXmlTest(xmlSuite, "sample_test", TestClassWithMultipleGroupFailures.class);
    xmlSuite.getIncludedGroups().addAll(Arrays.asList("p1", "p2", "all"));
    TestNG testng = create(xmlSuite);
    ReasonReporter reporter = new ReasonReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.getSkippedInfo()).containsAnyOf(entry("child", "father,mother"),
        entry("child", "mother,father"));

  }
  private static void runTest(Map<String, String> expected, Class<?>... clazz) {
    TestNG testng = create(clazz);
    ReasonReporter reporter = new ReasonReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.getSkippedInfo()).containsAllEntriesOf(expected);
  }

}
