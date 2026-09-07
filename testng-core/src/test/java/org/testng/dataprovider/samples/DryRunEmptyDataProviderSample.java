package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DryRunEmptyDataProviderSample {

  private final List<String> data = new ArrayList<>();

  @BeforeClass
  public void beforeClass() {
    data.add("one");
    data.add("two");
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return data.stream().map(d -> new Object[] {d}).toArray(Object[][]::new);
  }

  @Test(dataProvider = "dp")
  public void testMethod(String val) {
    fail("Should not execute test body");
  }
}
