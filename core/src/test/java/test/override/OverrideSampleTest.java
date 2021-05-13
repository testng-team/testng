package test.override;

import org.testng.annotations.Test;

public class OverrideSampleTest {

  @Test(groups = "goodGroup")
  public void good() {
  }

  @Test(groups = "badGroup")
  public void bad() {
    throw new RuntimeException("Should not happen");
  }
}
