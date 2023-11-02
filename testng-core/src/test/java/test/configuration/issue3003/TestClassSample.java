package test.configuration.issue3003;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"GROUP 1"})
public class TestClassSample extends BaseClassSample {

  @BeforeClass(
      alwaysRun = true,
      dependsOnMethods = {"setupMethod2"})
  public void setupMethod3() {
    logs.add("setupMethod3");
  }

  public void testMethod1() {
    logs.add("testMethod1");
  }
}
