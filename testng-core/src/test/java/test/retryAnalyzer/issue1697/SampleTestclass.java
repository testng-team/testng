package test.retryAnalyzer.issue1697;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleTestclass {
  public static int counter = 0;

  @Test(retryAnalyzer = SimpleRetrier.class)
  public void dataDrivenTest() {
    if (counter++ != 1) {
      Assert.fail();
    }
  }

  @Test
  public void parent() {
    Assert.fail();
  }

  @Test(dependsOnMethods = "parent")
  public void child() {}
}
