package test.testng195;

import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class AfterMethodSampleTest {
  static boolean m_success;

  @Test
  public void pass() {
  }

  @BeforeClass
  public void init() {
    m_success = false;
  }

  @AfterMethod
  public void afterMethod(ITestContext c, Method m) {
      IResultMap map = c.getPassedTests();
      m_success = map.size() == 1;
  }
}
