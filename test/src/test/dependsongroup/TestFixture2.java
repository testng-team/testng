package test.dependsongroup;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class TestFixture2 {
  @BeforeTest(groups={"test"}, dependsOnGroups={"testgroup"})
  public void setup() {
  }

  @Test(groups={"test"})  //@@
  public void testMethod() {
  }
}
