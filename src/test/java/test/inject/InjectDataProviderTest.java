package test.inject;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import test.dataprovider.MyIterator;

import java.util.Iterator;

/**
 * Test that injection works for data providers.
 *
 * @author Cedric Beust, Mar 3, 2010
 *
 */
public class InjectDataProviderTest {

  @DataProvider
  public Object[][] dp1() {
    return new Object[][] {
      new Object[] { 1, "a" },
      new Object[] { 2, "b" },
    };
  }

  @Test(dataProvider = "dp1", enabled = true)
  public void dpObject1(Integer n, ITestContext ctx, String a) {
  }

  @Test(dataProvider = "dp1", enabled = true)
  public void dpObject2(ITestContext ctx, Integer n, String a) {
  }

  @Test(dataProvider = "dp1", enabled = true)
  public void dpObject3(Integer n, String a, ITestContext ctx) {
  }

  @DataProvider
  public Iterator<Object[]> dp2() {
    return new MyIterator(
    new Object[][] {
      new Object[] { 1, "a" },
      new Object[] { 2, "b" },
    });
  }

  @Test(dataProvider = "dp2", enabled = false)
  public void dpIterator1(Integer n, ITestContext ctx, String a) {
  }

  @Test(dataProvider = "dp2", enabled = false)
  public void dpIterator2(ITestContext ctx, Integer n, String a) {
  }

  @Test(dataProvider = "dp2", enabled = false)
  public void dpIterator3(Integer n, String a, ITestContext ctx) {
  }
}
