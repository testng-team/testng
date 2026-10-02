package org.testng.configuration.samples;

import org.testng.annotations.Test;

public class BaseGroupsBSampleTest extends Base {
  @Test(groups = "foo")
  public void b() {}
}
