package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

public class FactoryTest {

  static boolean isInvoked = false;

  @Parameters({"factory-param"})
  @Factory
  public Object[] createObjects(String param) {
    assertThat(param).isEqualTo("FactoryParam");
    assertThat(isInvoked).withFailMessage("Should only be invoked once").isFalse();
    isInvoked = true;

    return new Object[] {new FactoryTest2(42), new FactoryTest2(43)};
  }

  @AfterSuite
  public void afterSuite() {
    isInvoked = false;
  }
}
