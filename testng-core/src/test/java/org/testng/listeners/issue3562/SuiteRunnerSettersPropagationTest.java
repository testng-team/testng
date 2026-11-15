package org.testng.listeners.issue3562;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.SuiteRunner;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class SuiteRunnerSettersPropagationTest extends SimpleBaseTest {

  public static class FirstInvocationFailsSample {
    private static final AtomicInteger INVOCATION = new AtomicInteger(0);

    @Test(invocationCount = 3)
    public void testMethod() {
      if (INVOCATION.incrementAndGet() == 1) {
        throw new AssertionError("First invocation failed on purpose");
      }
    }
  }

  public static class SkipFailedInvocationsListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
      if (suite instanceof SuiteRunner) {
        ((SuiteRunner) suite).setSkipFailedInvocationCounts(true);
      }
    }
  }

  public static class DoNotSkipFailedInvocationsListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
      if (suite instanceof SuiteRunner) {
        ((SuiteRunner) suite).setSkipFailedInvocationCounts(false);
      }
    }
  }

  public static class DisableReportResultsListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
      if (suite instanceof SuiteRunner) {
        ((SuiteRunner) suite).setReportResults(false);
      }
    }
  }

  public static class EnableReportResultsListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
      if (suite instanceof SuiteRunner) {
        ((SuiteRunner) suite).setReportResults(true);
      }
    }
  }

  public static class ToggleReportResultsListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
      if (suite instanceof SuiteRunner) {
        SuiteRunner sr = (SuiteRunner) suite;
        sr.setReportResults(false);
        sr.setReportResults(true);
        sr.setReportResults(true); // check idempotence against duplicate registration
      }
    }
  }

  @Test
  public void setSkipFailedInvocationCountsTruePropagatesToTestRunners() {
    FirstInvocationFailsSample.INVOCATION.set(0);
    TestNG testng = create(FirstInvocationFailsSample.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.addListener(new SkipFailedInvocationsListener());
    testng.run();

    assertThat(tla.getPassedTests()).isEmpty();
    assertThat(tla.getFailedTests()).hasSize(1);
    assertThat(tla.getSkippedTests()).hasSize(2);
  }

  @Test
  public void setSkipFailedInvocationCountsFalsePropagatesToTestRunners() {
    FirstInvocationFailsSample.INVOCATION.set(0);
    XmlSuite suite = createXmlSuite("Suite");
    suite.setSkipFailedInvocationCounts(true);
    createXmlTest(suite, "Test", FirstInvocationFailsSample.class);

    TestNG testng = create(suite);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.addListener(new DoNotSkipFailedInvocationsListener());
    testng.run();

    assertThat(tla.getPassedTests()).hasSize(2);
    assertThat(tla.getFailedTests()).hasSize(1);
    assertThat(tla.getSkippedTests()).isEmpty();
  }

  @Test
  public void setSkipFailedInvocationCountsDoesNotOverrideExplicitTestSetting() {
    FirstInvocationFailsSample.INVOCATION.set(0);
    XmlSuite suite = createXmlSuite("Suite");
    XmlTest test = createXmlTest(suite, "Test", FirstInvocationFailsSample.class);
    test.setSkipFailedInvocationCounts(true);

    TestNG testng = create(suite);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.addListener(new DoNotSkipFailedInvocationsListener());
    testng.run();

    assertThat(tla.getPassedTests()).isEmpty();
    assertThat(tla.getFailedTests()).hasSize(1);
    assertThat(tla.getSkippedTests()).hasSize(2);
  }

  @Test
  public void setReportResultsFalseDisablesDefaultReporters() {
    File reportsDir = createDirInTempDir("issue3562_disable_reports");
    Path reportsPath = reportsDir.toPath();

    XmlSuite suite = createXmlSuite("Suite");
    createXmlTest(suite, "Test", FirstInvocationFailsSample.class);

    FirstInvocationFailsSample.INVOCATION.set(0);
    TestNG testng = create(reportsPath, suite);
    // Explicitly enable default listeners to ensure setReportResults(false) actively disables them
    testng.setUseDefaultListeners(true);
    testng.addListener(new DisableReportResultsListener());
    testng.run();

    File htmlReport = new File(reportsDir, "Suite" + File.separator + "Test.html");
    File xmlReport = new File(reportsDir, "Suite" + File.separator + "Test.xml");
    assertThat(htmlReport).doesNotExist();
    assertThat(xmlReport).doesNotExist();
  }

  @Test
  public void setReportResultsTrueEnablesDefaultReporters() {
    File reportsDir = createDirInTempDir("issue3562_enable_reports");
    Path reportsPath = reportsDir.toPath();

    XmlSuite suite = createXmlSuite("Suite");
    createXmlTest(suite, "Test", FirstInvocationFailsSample.class);

    FirstInvocationFailsSample.INVOCATION.set(0);
    TestNG testng = create(reportsPath, suite);
    testng.setUseDefaultListeners(false);
    testng.addListener(new EnableReportResultsListener());
    testng.run();

    File htmlReport = new File(reportsDir, "Suite" + File.separator + "Test.html");
    File xmlReport = new File(reportsDir, "Suite" + File.separator + "Test.xml");
    assertThat(htmlReport).exists();
    assertThat(xmlReport).exists();
  }

  @Test
  public void setReportResultsDisableAndReEnableWithoutDuplicates() {
    File reportsDir = createDirInTempDir("issue3562_toggle_reports");
    Path reportsPath = reportsDir.toPath();

    XmlSuite suite = createXmlSuite("Suite");
    createXmlTest(suite, "Test", FirstInvocationFailsSample.class);

    FirstInvocationFailsSample.INVOCATION.set(0);
    TestNG testng = create(reportsPath, suite);
    testng.setUseDefaultListeners(true);
    testng.addListener(new ToggleReportResultsListener());
    testng.run();

    File htmlReport = new File(reportsDir, "Suite" + File.separator + "Test.html");
    File xmlReport = new File(reportsDir, "Suite" + File.separator + "Test.xml");
    assertThat(htmlReport).exists();
    assertThat(xmlReport).exists();
  }

  @Test
  public void setReportResultsFalsePreservesUserRegisteredHTMLReporter() {
    File reportsDir = createDirInTempDir("issue3562_preserve_user_html_reports");
    Path reportsPath = reportsDir.toPath();

    XmlSuite suite = createXmlSuite("Suite");
    createXmlTest(suite, "Test", FirstInvocationFailsSample.class);

    FirstInvocationFailsSample.INVOCATION.set(0);
    TestNG testng = create(reportsPath, suite);
    testng.setUseDefaultListeners(false);
    testng.addListener(new TestHTMLReporter());
    testng.addListener(new DisableReportResultsListener());
    testng.run();

    File htmlReport = new File(reportsDir, "Suite" + File.separator + "Test.html");
    assertThat(htmlReport).exists();
  }

  @Test
  public void setReportResultsFalsePreservesUserRegisteredJUnitXMLReporter() {
    File reportsDir = createDirInTempDir("issue3562_preserve_user_junit_reports");
    Path reportsPath = reportsDir.toPath();

    XmlSuite suite = createXmlSuite("Suite");
    createXmlTest(suite, "Test", FirstInvocationFailsSample.class);

    FirstInvocationFailsSample.INVOCATION.set(0);
    TestNG testng = create(reportsPath, suite);
    testng.setUseDefaultListeners(false);
    testng.addListener(new JUnitXMLReporter());
    testng.addListener(new DisableReportResultsListener());
    testng.run();

    File xmlReport = new File(reportsDir, "Suite" + File.separator + "Test.xml");
    assertThat(xmlReport).exists();
  }

  @Test
  public void setReportResultsTrueEnablesReportsForMultipleParallelTestRunners() {
    File reportsDir = createDirInTempDir("issue3562_multiple_runners_reports");
    Path reportsPath = reportsDir.toPath();

    XmlSuite suite = createXmlSuite("Suite");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    createXmlTest(suite, "TestA", FirstInvocationFailsSample.class);
    createXmlTest(suite, "TestB", FirstInvocationFailsSample.class);

    FirstInvocationFailsSample.INVOCATION.set(0);
    TestNG testng = create(reportsPath, suite);
    testng.setUseDefaultListeners(false);
    testng.addListener(new EnableReportResultsListener());
    testng.run();

    File htmlReportA = new File(reportsDir, "Suite" + File.separator + "TestA.html");
    File xmlReportA = new File(reportsDir, "Suite" + File.separator + "TestA.xml");
    File htmlReportB = new File(reportsDir, "Suite" + File.separator + "TestB.html");
    File xmlReportB = new File(reportsDir, "Suite" + File.separator + "TestB.xml");

    assertThat(htmlReportA).exists();
    assertThat(xmlReportA).exists();
    assertThat(htmlReportB).exists();
    assertThat(xmlReportB).exists();
  }

  @Test
  public void setReportResultsFalseDisablesReportsForMultipleParallelTestRunners() {
    File reportsDir = createDirInTempDir("issue3562_multiple_runners_disable");
    Path reportsPath = reportsDir.toPath();

    XmlSuite suite = createXmlSuite("Suite");
    suite.setParallel(XmlSuite.ParallelMode.TESTS);
    createXmlTest(suite, "TestA", FirstInvocationFailsSample.class);
    createXmlTest(suite, "TestB", FirstInvocationFailsSample.class);

    FirstInvocationFailsSample.INVOCATION.set(0);
    TestNG testng = create(reportsPath, suite);
    testng.setUseDefaultListeners(true);
    testng.addListener(new DisableReportResultsListener());
    testng.run();

    File htmlReportA = new File(reportsDir, "Suite" + File.separator + "TestA.html");
    File xmlReportA = new File(reportsDir, "Suite" + File.separator + "TestA.xml");
    File htmlReportB = new File(reportsDir, "Suite" + File.separator + "TestB.html");
    File xmlReportB = new File(reportsDir, "Suite" + File.separator + "TestB.xml");

    assertThat(htmlReportA).doesNotExist();
    assertThat(xmlReportA).doesNotExist();
    assertThat(htmlReportB).doesNotExist();
    assertThat(xmlReportB).doesNotExist();
  }
}
