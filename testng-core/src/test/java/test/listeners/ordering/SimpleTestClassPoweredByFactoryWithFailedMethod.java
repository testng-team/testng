package test.listeners.ordering;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SimpleTestClassPoweredByFactoryWithFailedMethod {

  @Factory
  public static Object[] create() {
    return new Object[] {
      new SimpleTestClassPoweredByFactoryWithFailedMethod(),
      new SimpleTestClassPoweredByFactoryWithFailedMethod(),
    };
  }

  @Test
  public void testWillFail() {
    fail();
  }
}
