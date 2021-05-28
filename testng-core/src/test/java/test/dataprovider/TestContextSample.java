package test.dataprovider;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests that when a DataProvider is declared with an ITestContext, this parameter is correctly
 * passed.
 */
public class TestContextSample {

  /** @return As many parameters as the name of the included group */
  @DataProvider(name = "testContext")
  public Object[] createContext(ITestContext ctx) {
    String[] groups = ctx.getIncludedGroups();

    int n = groups.length > 0 ? Integer.parseInt(groups[0]) : 0;
    Object[] result = new Object[n];
    for (int i = 0; i < n; i++) {
      result[i] = "foo";
    }

    return new Object[] {result};
  }

  @Test(dataProvider = "testContext", groups = "10")
  public void verifyTen(Object[] objects) {
    Assert.assertEquals(objects.length, 10);
  }

  @Test(dataProvider = "testContext", groups = "5")
  public void verifyFive(Object[] objects) {
    Assert.assertEquals(objects.length, 5);
  }
}
