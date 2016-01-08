package org.testng;

import org.testng.collections.Lists;
import org.testng.collections.Objects;
import org.testng.internal.IResultListener2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple ITestListener adapter that stores all the tests
 * that were run.  You can retrieve these results with the
 * following methods:
 * getPassedTests()
 * getFailedTests()
 * getSkippedTests()
 *
 * If you extend this class in order to override any of these
 * methods, remember to call their super equivalent if you want
 * this list of tests to be maintained.
 *
 * @author Cedric Beust, Aug 6, 2004
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class TestListenerAdapter implements IResultListener2 {
  private List<ITestNGMethod> m_allTestMethods =
      Collections.synchronizedList(Lists.<ITestNGMethod>newArrayList());
  private List<ITestResult> m_passedTests = Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  private List<ITestResult> m_failedTests = Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  private List<ITestResult> m_skippedTests = Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  private List<ITestResult> m_failedButWSPerTests = Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  private List<ITestContext> m_testContexts= Collections.synchronizedList(new ArrayList<ITestContext>());
  private List<ITestResult> m_failedConfs= Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  private List<ITestResult> m_skippedConfs= Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  private List<ITestResult> m_passedConfs= Collections.synchronizedList(Lists.<ITestResult>newArrayList());

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
  public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_failedButWSPerTests.add(tr);
  }

  protected ITestNGMethod[] getAllTestMethods() {
    return m_allTestMethods.toArray(new ITestNGMethod[m_allTestMethods.size()]);
  }

  @Override
  public void onStart(ITestContext testContext) {
	  m_testContexts.add(testContext);
  }

  @Override
  public void onFinish(ITestContext testContext) {
  }

  /**
   * @return Returns the failedButWithinSuccessPercentageTests.
   */
  public List<ITestResult> getFailedButWithinSuccessPercentageTests() {
    return m_failedButWSPerTests;
  }
  /**
   * @return Returns the failedTests.
   */
  public List<ITestResult> getFailedTests() {
    return m_failedTests;
  }
  /**
   * @return Returns the passedTests.
   */
  public List<ITestResult> getPassedTests() {
    return m_passedTests;
  }
  /**
   * @return Returns the skippedTests.
   */
  public List<ITestResult> getSkippedTests() {
    return m_skippedTests;
  }

  private static void ppp(String s) {
    System.out.println("[TestListenerAdapter] " + s);
  }
  /**
   * @param allTestMethods The allTestMethods to set.
   */
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
  /**
   * @param failedTests The failedTests to set.
   */
  public void setFailedTests(List<ITestResult> failedTests) {
    m_failedTests = failedTests;
  }
  /**
   * @param passedTests The passedTests to set.
   */
  public void setPassedTests(List<ITestResult> passedTests) {
    m_passedTests = passedTests;
  }
  /**
   * @param skippedTests The skippedTests to set.
   */
  public void setSkippedTests(List<ITestResult> skippedTests) {
    m_skippedTests = skippedTests;
  }

  @Override
  public void onTestStart(ITestResult result) {
  }

  public List<ITestContext> getTestContexts() {
    return m_testContexts;
  }

  public List<ITestResult> getConfigurationFailures() {
    return m_failedConfs;
  }

  /**
   * @see org.testng.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult)
   */
  @Override
  public void onConfigurationFailure(ITestResult itr) {
    m_failedConfs.add(itr);
  }

  public List<ITestResult> getConfigurationSkips() {
    return m_skippedConfs;
  }

  @Override
  public void beforeConfiguration(ITestResult tr) {
  }

  /**
   * @see org.testng.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult)
   */
  @Override
  public void onConfigurationSkip(ITestResult itr) {
    m_skippedConfs.add(itr);
  }

  /**
   * @see org.testng.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult)
   */
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
