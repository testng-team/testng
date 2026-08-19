package test.configuration.issue3358;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ChildFirstTimeOnlySequentialDpSample extends ParentFirstTimeOnlySample {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeChild() {}

  @DataProvider(name = "rows")
  public Object[][] rows() {
    return new Object[][] {{0}, {1}, {2}};
  }

  @Test(dataProvider = "rows")
  public void test(int n) {}
}
