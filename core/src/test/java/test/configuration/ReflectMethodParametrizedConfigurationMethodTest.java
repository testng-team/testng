package test.configuration;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * This class/interface
 */
public class ReflectMethodParametrizedConfigurationMethodTest {
  private Map<String, String> m_before= new HashMap<>();
  private Map<String, String> m_after= new HashMap<>();

  @BeforeMethod
  public void beforeMethod(Method tobeInvokedTestMethod) {
    m_before.put(tobeInvokedTestMethod.getName(), tobeInvokedTestMethod.getName());
  }

  @Test
  public void test1() {}

  @Test
  public void test2() {}

  @AfterMethod
  public void afterMethod(Method invokedTestMethod) {
    m_after.put(invokedTestMethod.getName(), invokedTestMethod.getName());
  }

  @AfterClass
  public void assertBeforeAfterMethodsInvocations() {
    Assert.assertTrue(m_before.containsKey("test1"), "@Test method should have been passed to @BeforeMethod");
    Assert.assertTrue(m_before.containsKey("test2"), "@Test method should have been passed to @BeforeMethod");
    Assert.assertTrue(m_after.containsKey("test1"), "@Test method should have been passed to @AfterMethod");
    Assert.assertTrue(m_before.containsKey("test2"), "@Test method should have been passed to @AfterMethod");
  }
}
