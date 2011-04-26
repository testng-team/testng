package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class VarArgsDataProviderTest {
  private static final String[] S = new String[] { "a", "b", "c" };

  @DataProvider
  public Object[][] data() {
    return new Object[][] { S };
  }

  @Test(dataProvider = "data")
  public void testWithTwoEntriesInTestToolWindow(String... o) {
    Assert.assertEquals(o, S);
  }
}
