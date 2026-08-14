package test.factory.issue3111;

import org.testng.annotations.Test;

/** A plain test class: no <code>@Factory</code> produced it, so it has no factory instance. */
public class NonFactorySample {

  @Test
  public void test() {}
}
