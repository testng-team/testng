package test.invocationcount;

import java.util.Arrays;
import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderBase {
  @Test(dataProvider = "dp")
  public void f(Integer n) {}

  @DataProvider
  public Object[][] dp() {
    return new Integer[][] {new Integer[] {0}, new Integer[] {1}, new Integer[] {2}};
  }

  @Test(dataProvider = "dp2")
  public void f2(Integer n) {}

  @DataProvider
  public Iterator<Object[]> dp2() {
    return Arrays.asList(new Object[] {0}, new Object[] {1}, new Object[] {2}).iterator();
  }
}
