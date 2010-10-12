package test.configuration;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class BeforeMethodTest {
  private Method m_method;
  private ITestContext m_context;

  @BeforeMethod
  public void before(Method m, ITestContext ctx) {
    m_method = m;
    m_context = ctx;
  }

  @Test
  public void mainTest() {
    Assert.assertEquals(m_method.getName(), "mainTest");
    ITestNGMethod[] methods = m_context.getAllTestMethods();
    Assert.assertEquals(1, methods.length);
    Assert.assertEquals(methods[0].getMethod().getName(), "mainTest");

  }
}
