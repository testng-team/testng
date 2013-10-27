package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

public class VarArgsDataProviderTest {
  @DataProvider
  public Object[][] data() {
    return new Object[][] {
      new String[] { "query1", "query1#response0", "query1#response1" },
      new String[] { "query2", "query2#response0", "query2#response1" },
    };
  }

  public String getResponse(String query, int responseIndex) {
    return String.format("%s#response%d", query, responseIndex);
  }

  @Test(dataProvider = "data")
  public void testNoVararg(String query, String response0, String response1) {
    Assert.assertEquals(getResponse(query, 0), response0);
    Assert.assertEquals(getResponse(query, 1), response1);
  }

  @Test(dataProvider = "data")
  public void testVararg(String... data) {
    String query = data[0];
    String[] expectedResponses = Arrays.copyOfRange(data, 1, data.length);
    for (int responseIndex = 0; responseIndex < expectedResponses.length; ++responseIndex) {
      Assert.assertEquals(getResponse(query, responseIndex), expectedResponses[responseIndex]);
    }
  }

  @Test(dataProvider = "data")
  public void testMixed(String query, String... expectedResponses) {
    for (int responseIndex = 0; responseIndex < expectedResponses.length; ++responseIndex) {
      Assert.assertEquals(getResponse(query, responseIndex), expectedResponses[responseIndex]);
    }
  }
}
