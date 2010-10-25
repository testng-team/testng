package test.annotationtransformer;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigurationSampleTest {
  private static String m_before = "uninitialized";

  @BeforeClass
  public void beforeClass() {
    m_before = "correct";
  }

  @BeforeMethod(enabled = false)
  public void testingEnabledOnConfiguration() {
    m_before = "this method is not enabled, we should not be here";
  }

  // will be disabled by the configuration transformer
  @BeforeMethod
  public void beforeMethod() {
    m_before = "incorrect";
  }

  @Test
  public void f() {}

  public static String getBefore() {
    return m_before;
  }
}
