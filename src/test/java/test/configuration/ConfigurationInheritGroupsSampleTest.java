package test.configuration;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "group1" })
public class ConfigurationInheritGroupsSampleTest {
  private boolean m_ok = false;

  @BeforeMethod
  public void setUp() {
    m_ok = true;
  }

  public void test1() {
    Assert.assertTrue(m_ok);
  }

  private void ppp(String s) {
    System.out.println("[ConfigurationInheritGroupsSampleTest] " + s);
  }
}
