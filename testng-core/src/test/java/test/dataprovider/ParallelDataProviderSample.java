package test.dataprovider;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Data providers were not working properly with parallel=true */
public class ParallelDataProviderSample {

  @DataProvider(name = "test1", parallel = true)
  public Object[][] createData1() {
    return new Object[][] {
      {"Cedric", 36},
      {"Anne", 37},
      {"A", 36},
      {"B", 37}
    };
  }

  @Test(dataProvider = "test1", threadPoolSize = 5)
  public void verifyData1(ITestContext testContext, String n1, Integer n2) {}
}
