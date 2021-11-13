package test.failedreporter.issue2521;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class DataProviderWithFactoryMultiFailedReporterSample {
  private Integer data;

  @Factory(dataProvider = "dp")
  public DataProviderWithFactoryMultiFailedReporterSample(Integer data) {
    this.data = data;
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
      new Object[] {0}, new Object[] {1}, new Object[] {2},
    };
  }

  @Test
  public void f1() {
    if (data != 1) {
      throw new RuntimeException();
    }
  }

  @Test
  public void f2() {
    if (data != 0) {
      throw new RuntimeException();
    }
  }
}
