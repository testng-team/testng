package org.testng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Simple ITestListener adapter.
 *
 * @author Cedric Beust, Aug 6, 2004
 * 
 */
public class TestListenerAdapter implements ITestListener {
  private List<ITestNGMethod> m_allTestMethods = Collections.synchronizedList(new ArrayList<ITestNGMethod>());
  private List<ITestResult> m_passedTests = Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_failedTests = Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_skippedTests = Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_failedButWithinSuccessPercentageTests 
    = Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestContext> m_testContexts= Collections.synchronizedList(new ArrayList<ITestContext>());;

  public void onTestSuccess(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_passedTests.add(tr);
  }

  public void onTestFailure(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_failedTests.add(tr);
  }

  public void onTestSkipped(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_skippedTests.add(tr);
  }
  
  public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
    m_allTestMethods.add(tr.getMethod());
    m_failedButWithinSuccessPercentageTests.add(tr);
  }
  
  protected ITestNGMethod[] getAllTestMethods() {
    return m_allTestMethods.toArray(new ITestNGMethod[m_allTestMethods.size()]);
  }

  public void onStart(ITestContext testContext) {
	  m_testContexts.add(testContext);
  }

  public void onFinish(ITestContext testContext) {
  }
  
  /**
   * @return Returns the failedButWithinSuccessPercentageTests.
   */
  public List<ITestResult> getFailedButWithinSuccessPercentageTests() {
    return m_failedButWithinSuccessPercentageTests;
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
    m_failedButWithinSuccessPercentageTests = failedButWithinSuccessPercentageTests;
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

  public void onTestStart(ITestResult result) {
  }

	/**
	 * @return
	 */
	public List<ITestContext> getTestContexts() {
		return m_testContexts;
	}
}
