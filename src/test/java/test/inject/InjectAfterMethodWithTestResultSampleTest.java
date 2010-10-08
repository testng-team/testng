package test.inject;

import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class InjectAfterMethodWithTestResultSampleTest {
  static int m_success;

  @Test
  public void pass() {}

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
    System.out.println("Before result: " + r);
  }

  @AfterMethod
  public void after(Method m, ITestResult r) {
    String name = m.getName();
    if (("pass".equals(name) && r.getStatus() == ITestResult.SUCCESS)
        || ("fail".equals(name) && r.getStatus() == ITestResult.FAILURE)
        || ("skip".equals(name) && r.getStatus() == ITestResult.SKIP)) {
          m_success--;
        }
  }
}
