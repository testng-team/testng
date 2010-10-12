package test.dependsongroup;

import org.testng.annotations.BeforeTest;


public class TestFixture1 {
  @BeforeTest(groups={"test", "testgroup"})
  public void setup() {
  }

}
