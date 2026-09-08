package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyDataProviderInvocationCountSample {

  @DataProvider
  public Object[][] emptyDp() {
    return new Object[0][];
  }

  @Test(dataProvider = "emptyDp", invocationCount = 3)
  public void testMultipleInvocations() {
    fail();
  }
}
