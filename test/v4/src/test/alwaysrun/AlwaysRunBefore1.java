package test.alwaysrun;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * Tests alwaysRun on a before Configuration method.  Invoke this test
 * by running group "A"
 * 
 * @author cbeust
 * @date Mar 11, 2006
 */
public class AlwaysRunBefore1 {
  private static boolean m_beforeSuiteSuccess = false;
  private static boolean m_beforeTestSuccess = false;
  private static boolean m_beforeTestClassSuccess = false;
  private static boolean m_beforeTestMethodSuccess = false;
  
  @Configuration(beforeSuite = true, alwaysRun = true)
  public void initSuite() {
    m_beforeSuiteSuccess = true;
  }
  
  @Configuration(beforeTest= true, alwaysRun = true)
  public void initTest() {
    m_beforeTestSuccess = true;
  }
  
  @Configuration(beforeTestClass = true, alwaysRun = true)
  public void initTestClass() {
    m_beforeTestClassSuccess = true;
  }
  
  @Configuration(beforeTestMethod = true, alwaysRun = true)
  public void initTestMethod() {
    m_beforeTestMethodSuccess = true;
  }
  
  @Test(groups = "A")
  public void foo() {
    Assert.assertTrue(m_beforeSuiteSuccess);
    Assert.assertTrue(m_beforeTestSuccess);
    Assert.assertTrue(m_beforeTestClassSuccess);
    Assert.assertTrue(m_beforeTestMethodSuccess);
  }

  public static boolean success() {
    return m_beforeSuiteSuccess && m_beforeTestSuccess &&
      m_beforeTestClassSuccess && m_beforeTestMethodSuccess;
  }

}
