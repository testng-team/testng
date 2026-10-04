package org.testng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.testng.collections.Objects;
import org.testng.internal.IResultListener2;

/**
 * A test listener that keeps the results of the test methods and configuration methods that run.
 *
 * <p>Read the results with methods such as {@link #getPassedTests()}, {@link #getFailedTests()} and
 * {@link #getSkippedTests()}.
 *
 * <p>When you extend this class and override a listener method, call the {@code super} method too.
 * If you do not, this class does not record that result.
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

  /**
   * Returns the test methods of the test results that this listener received.
   *
   * @return the test methods, in a new array.
   */
  protected ITestNGMethod[] getAllTestMethods() {
    return m_allTestMethods.toArray(new ITestNGMethod[0]);
  }

  @Override
  public void onStart(ITestContext testContext) {
    m_testContexts.add(testContext);
  }

  /**
   * Returns the results of the tests that failed, but stayed within the {@code successPercentage}
   * of their method.
   *
   * @return a copy of the list.
   */
  public List<ITestResult> getFailedButWithinSuccessPercentageTests() {
    return new ArrayList<>(m_failedButWSPerTests);
  }
  /**
   * Returns the results of the tests that failed. The tests that timed out are in this list too.
   *
   * @return a copy of the list.
   */
  public List<ITestResult> getFailedTests() {
    return new ArrayList<>(m_failedTests);
  }
  /**
   * Returns the results of the tests that passed.
   *
   * @return a copy of the list.
   */
  public List<ITestResult> getPassedTests() {
    return new ArrayList<>(m_passedTests);
  }
  /**
   * Returns the results of the tests that TestNG skipped.
   *
   * @return a copy of the list.
   */
  public List<ITestResult> getSkippedTests() {
    return new ArrayList<>(m_skippedTests);
  }

  /**
   * Returns the results of the tests that failed because they timed out.
   *
   * @return a copy of the list.
   */
  public Collection<ITestResult> getTimedoutTests() {
    return new ArrayList<>(m_timedOutTests);
  }

  /**
   * Replaces the list of test methods. This listener adds new test methods to the list you pass.
   *
   * @param allTestMethods the new list.
   */
  public void setAllTestMethods(List<ITestNGMethod> allTestMethods) {
    m_allTestMethods = allTestMethods;
  }
  /**
   * Replaces the list of tests that failed within their success percentage. This listener adds new
   * results to the list you pass.
   *
   * @param failedButWithinSuccessPercentageTests the new list.
   */
  public void setFailedButWithinSuccessPercentageTests(
      List<ITestResult> failedButWithinSuccessPercentageTests) {
    m_failedButWSPerTests = failedButWithinSuccessPercentageTests;
  }
  /**
   * Replaces the list of failed tests. This listener adds new results to the list you pass.
   *
   * @param failedTests the new list.
   */
  public void setFailedTests(List<ITestResult> failedTests) {
    m_failedTests = failedTests;
  }
  /**
   * Replaces the list of passed tests. This listener adds new results to the list you pass.
   *
   * @param passedTests the new list.
   */
  public void setPassedTests(List<ITestResult> passedTests) {
    m_passedTests = passedTests;
  }
  /**
   * Replaces the list of skipped tests. This listener adds new results to the list you pass.
   *
   * @param skippedTests the new list.
   */
  public void setSkippedTests(List<ITestResult> skippedTests) {
    m_skippedTests = skippedTests;
  }

  /**
   * Returns the contexts of the {@code <test>} tags that started.
   *
   * @return a copy of the list.
   */
  public List<ITestContext> getTestContexts() {
    return new ArrayList<>(m_testContexts);
  }

  /**
   * Returns the results of the configuration methods that failed.
   *
   * @return a copy of the list.
   */
  public List<ITestResult> getConfigurationFailures() {
    return new ArrayList<>(m_failedConfs);
  }

  /** Records the result of a configuration method that failed. */
  @Override
  public void onConfigurationFailure(ITestResult itr) {
    m_failedConfs.add(itr);
  }

  /**
   * Returns the results of the configuration methods that TestNG skipped.
   *
   * @return a copy of the list.
   */
  public List<ITestResult> getConfigurationSkips() {
    return new ArrayList<>(m_skippedConfs);
  }

  /** Records the result of a configuration method that TestNG skipped. */
  @Override
  public void onConfigurationSkip(ITestResult itr) {
    m_skippedConfs.add(itr);
  }

  /** Records the result of a configuration method that passed. */
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
