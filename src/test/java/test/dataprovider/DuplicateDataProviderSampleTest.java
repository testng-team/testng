package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DuplicateDataProviderSampleTest {

  @Test(dataProvider = "duplicate")
  public void f() {
  }

  @DataProvider(name = "duplicate")
  public Object[] dp1() {
    return new Object[0][];
  }

  @DataProvider(name = "duplicate")
  public Object[] dp2() {
    return new Object[0][];
  }
}
