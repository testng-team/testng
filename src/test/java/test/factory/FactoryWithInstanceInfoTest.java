package test.factory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.IInstanceInfo;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;
import org.testng.internal.InstanceInfo;

public class FactoryWithInstanceInfoTest {
  static boolean isInvoked = false;

  @Parameters({"factory-param"})
  @Factory
  public IInstanceInfo[] createObjectsWithInstanceInfo(String param) {
    assertEquals(param, "FactoryParam", "Incorrect param: " + param);
    assertFalse(isInvoked, "Should only be invoked once");
    isInvoked = true;

    return new IInstanceInfo[] {
      new InstanceInfo<>(
          FactoryWithInstanceInfo2Sample.class, new FactoryWithInstanceInfo2Sample(42)),
      new InstanceInfo<>(
          FactoryWithInstanceInfo2Sample.class, new FactoryWithInstanceInfo2Sample(43)),
    };
  }

  @BeforeSuite
  public void beforeSuite() {
    isInvoked = false;
  }
}
