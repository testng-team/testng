package test.inject;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class InjectBeforeAndAfterMethodsWithTestResultSampleTest {
  static int m_success;
  private ITestResult m_testResult;

  @Test
  public void pass() {
    Assert.assertEquals(m_testResult.getAttribute("before"), 10);
  }

  @Test
  public void fail() {
    throw new RuntimeException();
  }

  @Test
  public void skip() {
    throw new SkipException("Skipped");
  }

  @BeforeClass
  public void init() {
    m_success = 3;
  }

  @BeforeMethod
  public void before(Method m, ITestResult r) {
    m_testResult = r;
    r.setAttribute("before", 10);
  }

  @AfterMethod
  public void after(Method m, ITestResult r) {
    String name = m.getName();
    Assert.assertEquals(r, m_testResult);
    if (("pass".equals(name) && r.getStatus() == ITestResult.SUCCESS)
        || ("fail".equals(name) && r.getStatus() == ITestResult.FAILURE)
        || ("skip".equals(name) && r.getStatus() == ITestResult.SKIP)) {
          m_success--;
        }
  }
}
