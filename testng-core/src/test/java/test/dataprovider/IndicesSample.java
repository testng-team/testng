package test.dataprovider;

import java.util.Arrays;
import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IndicesSample {

  @DataProvider(indices = {2})
  public Object[][] dp1() {
    return new Object[][] {{1}, {2}, {3}};
  }

  @Test(dataProvider = "dp1")
  public void indicesShouldWork(int n) {
    if (n == 2) {
      throw new RuntimeException("This method should not have received a 2");
    }
  }

  @DataProvider(indices = {2})
  public Iterator<Object[]> dp2() {
    return Arrays.asList(new Object[] {1}, new Object[] {2}, new Object[] {3}).iterator();
  }

  @Test(dataProvider = "dp2")
  public void indicesShouldWorkWithIterator(int n) {
    if (n == 2) {
      throw new RuntimeException("This method should not have received a 2");
    }
  }
}
