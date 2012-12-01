package test.factory;

import static org.testng.Assert.assertFalse;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

public class FactoryTest {
  static boolean m_invoked = false;

  @Parameters({ "factory-param" })
  @Factory
  public Object[] createObjects(String param) {
    Assert.assertEquals(param, "FactoryParam");
    assertFalse(m_invoked, "Should only be invoked once");
    m_invoked = true;

    return new Object[] {
        new FactoryTest2(42),
        new FactoryTest2(43)
    };
  }

  @AfterSuite
  public void afterSuite() {
    m_invoked = false;
  }
}