package test.dataprovider.issue217;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AnotherTestClassSample {
  public static final String DP_NAME = "dataProvider";

  @DataProvider(name = DP_NAME, propagateFailureAsTestFailure = true)
  public static Object[][] getData() {
    throw new IllegalStateException("guess me!");
  }

  @Test(dataProvider = DP_NAME)
  public void test() {}
}
