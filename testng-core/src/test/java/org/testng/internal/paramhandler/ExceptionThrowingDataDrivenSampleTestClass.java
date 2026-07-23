package org.testng.internal.paramhandler;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExceptionThrowingDataDrivenSampleTestClass {
  @Test(dataProvider = "dp")
  public void testMethod(String i) {
    assertThat(i).isNotEmpty();
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    throw new UnsupportedOperationException("unsupported-operation");
  }
}
