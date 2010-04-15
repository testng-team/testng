package test.priority;

import org.testng.annotations.Test;

public class Priority2SampleTest {
  @Test(priority = 1)
  public void cOne() {
  }

  @Test(priority = 2)
  public void bTwo() {
  }

  @Test(priority = 3)
  public void aThree() {
  }
}
