package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class VarArgsDataProviderSample {

  @DataProvider
  public Object[][] data() {
    return new Object[][] {new String[] {"a", "b", "c"}};
  }

  @Test(dataProvider = "data")
  public void testWithTwoEntriesInTestToolWindow(String... o) {}
}
