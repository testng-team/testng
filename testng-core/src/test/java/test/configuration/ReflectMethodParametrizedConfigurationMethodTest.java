package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** This class/interface */
public class ReflectMethodParametrizedConfigurationMethodTest {
  private Map<String, String> m_before = new HashMap<>();
  private Map<String, String> m_after = new HashMap<>();

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
    assertThat(m_before)
        .withFailMessage("@Test method should have been passed to @BeforeMethod")
        .containsKey("test1");
    assertThat(m_before)
        .withFailMessage("@Test method should have been passed to @BeforeMethod")
        .containsKey("test2");
    assertThat(m_after)
        .withFailMessage("@Test method should have been passed to @AfterMethod")
        .containsKey("test1");
    assertThat(m_before)
        .withFailMessage("@Test method should have been passed to @AfterMethod")
        .containsKey("test2");
  }
}
