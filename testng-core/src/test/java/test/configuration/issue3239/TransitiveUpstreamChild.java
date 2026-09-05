package test.configuration.issue3239;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TransitiveUpstreamChild extends TransitiveUpstreamBase {

  @BeforeClass
  public void childAgnostic() {}

  @BeforeClass(groups = "g", dependsOnMethods = "childAgnostic")
  public void childGroup() {}

  @Test
  public void test() {}
}
