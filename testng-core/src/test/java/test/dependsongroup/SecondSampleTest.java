package test.dependsongroup;

import org.testng.annotations.Test;

@Test(
    groups = {"second"},
    dependsOnGroups = {"zero"})
public class SecondSampleTest {

  @Test
  public void secondA() {}

  @Test
  public void secondB() {}
}
