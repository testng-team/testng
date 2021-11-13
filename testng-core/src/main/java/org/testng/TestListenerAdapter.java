package org.testng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.testng.collections.Objects;
import org.testng.internal.IResultListener2;

/**
 * A simple ITestListener adapter that stores all the tests that were run. You can retrieve these
 * results with the following methods: getPassedTests() getFailedTests() getSkippedTests()
 *
 * <p>If you extend this class in order to override any of these methods, remember to call their
 * super equivalent if you want this list of tests to be maintained.
 *
 * @author Cedric Beust, Aug 6, 2004
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class TestListenerAdapter implements IResultListener2 {
  private Collection<ITestNGMethod> m_allTestMethods = new ConcurrentLinkedQueue<>();
  private Collection<ITestResult> m_passedTests = new ConcurrentLinkedQueue<>();
  private Collection<ITestResult> m_failedTests = new ConcurrentLinkedQueue<>();
  private Collection<ITestResult> m_skippedTests = new ConcurrentLinkedQueue<>();
  private Collection<ITestResult> m_failedButWSPerTests = new ConcurrentLinkedQueue<>();
  private final Collection<ITestContext> m_testContexts = new ConcurrentLinkedQueue<>();
  private final Collection<ITestResult> m_failedConfs = new ConcurrentLinkedQueue<>();
  private final Collection<ITestResult> m_skippedConfs = new ConcurrentLinkedQueue<>();
  private final Collection<ITestResult> m_passedConfs = new ConcurrentLinkedQueue<>();
  private final Collection<ITestResult> m_timedOutTests = new ConcurrentLinkedQueue<>();

  @Override
  public void onTestSuccess(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_passedTests.add(tr);
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_failedTests.add(tr);
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_skippedTests.add(tr);
  }

  @Override
  public void onTestFailedWithTimeout(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_timedOutTests.add(tr);
    onTestFailure(tr);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_failedButWSPerTests.add(tr);
  }

  protected ITestNGMethod[] getAllTestMethods() {
    return m_allTestMethods.toArray(new ITestNGMethod[0]);
  }

  @Override
  public void onStart(ITestContext testContext) {
    m_testContexts.add(testContext);
  }

  @Override
  public void onFinish(ITestContext testContext) {}

  /** @return Returns the failedButWithinSuccessPercentageTests. */
  public List<ITestResult> getFailedButWithinSuccessPercentageTests() {
    return new ArrayList<>(m_failedButWSPerTests);
  }
  /** @return Returns the failedTests. */
  public List<ITestResult> getFailedTests() {
    return new ArrayList<>(m_failedTests);
  }
  /** @return Returns the passedTests. */
  public List<ITestResult> getPassedTests() {
    return new ArrayList<>(m_passedTests);
  }
  /** @return Returns the skippedTests. */
  public List<ITestResult> getSkippedTests() {
    return new ArrayList<>(m_skippedTests);
  }

  /** @return Returns the tests that failed due to a timeout */
  public Collection<ITestResult> getTimedoutTests() {
    return new ArrayList<>(m_timedOutTests);
  }

  /** @param allTestMethods The allTestMethods to set. */
  public void setAllTestMethods(List<ITestNGMethod> allTestMethods) {
    m_allTestMethods = allTestMethods;
  }
  /**
   * @param failedButWithinSuccessPercentageTests The failedButWithinSuccessPercentageTests to set.
   */
  public void setFailedButWithinSuccessPercentageTests(
      List<ITestResult> failedButWithinSuccessPercentageTests) {
    m_failedButWSPerTests = failedButWithinSuccessPercentageTests;
  }
  /** @param failedTests The failedTests to set. */
  public void setFailedTests(List<ITestResult> failedTests) {
    m_failedTests = failedTests;
  }
  /** @param passedTests The passedTests to set. */
  public void setPassedTests(List<ITestResult> passedTests) {
    m_passedTests = passedTests;
  }
  /** @param skippedTests The skippedTests to set. */
  public void setSkippedTests(List<ITestResult> skippedTests) {
    m_skippedTests = skippedTests;
  }

  @Override
  public void onTestStart(ITestResult result) {}

  public List<ITestContext> getTestContexts() {
    return new ArrayList<>(m_testContexts);
  }

  public List<ITestResult> getConfigurationFailures() {
    return new ArrayList<>(m_failedConfs);
  }

  /** @see org.testng.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult) */
  @Override
  public void onConfigurationFailure(ITestResult itr) {
    m_failedConfs.add(itr);
  }

  public List<ITestResult> getConfigurationSkips() {
    return new ArrayList<>(m_skippedConfs);
  }

  @Override
  public void beforeConfiguration(ITestResult tr) {}

  /** @see org.testng.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult) */
  @Override
  public void onConfigurationSkip(ITestResult itr) {
    m_skippedConfs.add(itr);
  }

  /** @see org.testng.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult) */
  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    m_passedConfs.add(itr);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass())
        .add("passed", getPassedTests().size())
        .add("failed", getFailedTests().size())
        .add("skipped", getSkippedTests().size())
        .toString();
  }
}
