package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * @author Jacek Pulut <jacek.pulut@gmail.com>
 *
 * Make sure this class can run without causing a ConcurrentModificationException.
 */
public class ParallelDataProvider2Test {
  @DataProvider(parallel = true)
  Iterator<Integer[]> provide()
  {
    final List<Integer[]> ret = Lists.newArrayList();
    for (int i = 0; i < 1000; i++)
    {
      ret.add(new Integer[] { i });
    }
    return ret.iterator();
  }

  @Test(groups = "cme", dataProvider = "provide", invocationCount = 2, threadPoolSize = 2)
  public void checkCME(final Integer i)
  {
//    Reporter.log("" + i, true);
  }
}