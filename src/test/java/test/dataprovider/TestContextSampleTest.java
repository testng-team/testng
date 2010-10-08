package test.dataprovider;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests that when a DataProvider is declared with an ITestContext,
 * this parameter is correctly passed.
 *
 * Created on Dec 28, 2006
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class TestContextSampleTest {

  /**
   * @return As many parameters as the name of the included group
   */
  @DataProvider(name = "testContext")
  public Object[][] createContext(ITestContext ctx) {
//    ppp("CONTEXT:" + ctx);
    String[] groups = ctx.getIncludedGroups();

    int n = groups.length > 0 ? new Integer(groups[0]): 0;
    Object[] result = new Object[n];
    for (int i = 0; i < n; i++) {
      result[i] = "foo";
    }

    return new Object[][] {
        new Object[] { result },
    };
  }

  private static void ppp(String s) {
    System.out.println("[TestContextSampleTest] " + s);
  }

  @Test(dataProvider = "testContext", groups="10")
  public void verifyTen(Object[] objects) {
    Assert.assertEquals(objects.length, 10);
  }

  @Test(dataProvider = "testContext", groups="5")
  public void verifyFive(Object[] objects) {
    Assert.assertEquals(objects.length, 5);
  }

}
