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
  public void setReportResultsFalsePreservesUserRegisteredReporters() {
    File reportsDir = createDirInTempDir("issue3562_preserve_user_reports");
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
}
