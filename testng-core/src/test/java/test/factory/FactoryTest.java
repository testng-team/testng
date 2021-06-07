package test.factory;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

public class FactoryTest {

  static boolean isInvoked = false;

  @Parameters({"factory-param"})
  @Factory
  public Object[] createObjects(String param) {
    assertEquals(param, "FactoryParam");
    assertFalse(isInvoked, "Should only be invoked once");
    isInvoked = true;

    return new Object[] {new FactoryTest2(42), new FactoryTest2(43)};
  }

  @AfterSuite
  public void afterSuite() {
    isInvoked = false;
  }
}
