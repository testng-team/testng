package test.retryAnalyzer.issue1697;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class SampleTestclass {
  public static int counter = 0;

  @Test(retryAnalyzer = SimpleRetrier.class)
  public void dataDrivenTest() {
    if (counter++ != 1) {
      fail();
    }
  }

  @Test
  public void parent() {
    fail();
  }

  @Test(dependsOnMethods = "parent")
  public void child() {}
}
