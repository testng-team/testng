package org.testng;

import java.util.Collection;
import java.util.Date;


/**
 * This class/interface 
 */
public class DefaultTestContext implements ITestContext {

  /**
   * @see org.testng.ITestContext#getAllTestMethods()
   */
  public ITestNGMethod[] getAllTestMethods() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getEndDate()
   */
  public Date getEndDate() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getExcludedGroups()
   */
  public String[] getExcludedGroups() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getExcludedMethods()
   */
  public Collection<ITestNGMethod> getExcludedMethods() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getFailedButWithinSuccessPercentageTests()
   */
  public IResultMap getFailedButWithinSuccessPercentageTests() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getFailedConfigurations()
   */
  public IResultMap getFailedConfigurations() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getFailedTests()
   */
  public IResultMap getFailedTests() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getHost()
   */
  public String getHost() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getIncludedGroups()
   */
  public String[] getIncludedGroups() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getName()
   */
  public String getName() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getOutputDirectory()
   */
  public String getOutputDirectory() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getPassedConfigurations()
   */
  public IResultMap getPassedConfigurations() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getPassedTests()
   */
  public IResultMap getPassedTests() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getSkippedConfigurations()
   */
  public IResultMap getSkippedConfigurations() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getSkippedTests()
   */
  public IResultMap getSkippedTests() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getStartDate()
   */
  public Date getStartDate() {
    return null;
  }

  /**
   * @see org.testng.ITestContext#getSuite()
   */
  public ISuite getSuite() {
    return null;
  }

}
