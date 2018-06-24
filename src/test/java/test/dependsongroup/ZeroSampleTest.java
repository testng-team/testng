package test.dependsongroup;

import org.testng.annotations.Test;

@Test(groups = {"zero"})
public class ZeroSampleTest {

  @Test
  public void zeroA() {}

  @Test
  public void zeroB() {}
}
