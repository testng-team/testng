package test.cli.github2974;

import org.testng.annotations.Test;

public class TwoGroupsTest {

  @Test(groups = "override_group")
  public void overrideTest() {}

  @Test(groups = "default_group")
  public void defaultTest() {}
}
