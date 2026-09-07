package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyDataProviderParallelSample {

  @DataProvider(parallel = true)
  public Object[][] parallelEmptyDp() {
    return new Object[0][];
  }

  @Test(dataProvider = "parallelEmptyDp")
  public void testParallel() {
    fail();
  }
}
