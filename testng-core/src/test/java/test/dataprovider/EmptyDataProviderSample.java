package test.dataprovider;

import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyDataProviderSample {

  @DataProvider
  public Object[][] dp1() {
    return new Object[0][];
  }

  @Test(dataProvider = "dp1")
  public void test1() {
    fail();
  }

  @DataProvider(indices = {2})
  public Iterator<Object[]> dp2() {
    return Collections.emptyIterator();
  }

  @Test(dataProvider = "dp2")
  public void test2() {
    fail();
  }
}
