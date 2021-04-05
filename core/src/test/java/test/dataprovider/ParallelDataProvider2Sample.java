package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Make sure this class can run without causing a ConcurrentModificationException. */
public class ParallelDataProvider2Sample {

  @DataProvider(parallel = true)
  Iterator<Integer[]> provide() {
    List<Integer[]> ret = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      ret.add(new Integer[] {i});
    }
    return ret.iterator();
  }

  @Test(dataProvider = "provide", invocationCount = 2, threadPoolSize = 2)
  public void checkCME(Integer i) {
    Assert.assertNotNull(i);
  }
}
