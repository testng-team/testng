package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IndicesTest {

  @DataProvider(indices = { 0, 2 })
  public Object[][] dp1() {
    return new Object[][] {
      new Object[] { 1 },  
      new Object[] { 2 },  
      new Object[] { 3 },  
    };
  }

  @Test(dataProvider = "dp1")
  public void indicesShouldWork(int n) {
    if (n == 2) throw new RuntimeException("This method should not have received a 1");
  }
}
