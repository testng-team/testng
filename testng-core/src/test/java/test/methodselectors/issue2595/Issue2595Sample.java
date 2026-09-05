package test.methodselectors.issue2595;

import org.testng.annotations.Test;

public class Issue2595Sample {

  @Test
  public void throwError() {
    throw new AssertionError("I shall fail");
  }

  @Test
  public void doNothing() {}
}
