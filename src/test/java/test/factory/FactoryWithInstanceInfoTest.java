package test.factory;

import static org.testng.Assert.assertFalse;

import org.testng.IInstanceInfo;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.internal.InstanceInfo;

public class FactoryWithInstanceInfoTest {
  static boolean m_invoked = false;

  @Parameters({ "factory-param" })
  @Factory
  public IInstanceInfo[] createObjectsWithInstanceInfo(String param)
  {
    assert "FactoryParam".equals(param) : "Incorrect param: " + param;

    assertFalse(m_invoked, "Should only be invoked once");
    m_invoked = true;

    return new IInstanceInfo[] {
        new InstanceInfo(FactoryWithInstanceInfoTest2.class,
            new FactoryWithInstanceInfoTest2(42)),
        new InstanceInfo(FactoryWithInstanceInfoTest2.class,
            new FactoryWithInstanceInfoTest2(43)),
    };
  }

  @BeforeSuite
  public void beforeSuite() {
    m_invoked = false;
  }

  private static void ppp(String s) {
    System.out.println("[FactoryWithInstanceInfoTest] " + s);
  }
}