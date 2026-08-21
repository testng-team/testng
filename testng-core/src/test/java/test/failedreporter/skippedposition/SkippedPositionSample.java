package test.failedreporter.skippedposition;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Three data provider rows; only the middle one fails, so only position 1 is re-run. */
public class SkippedPositionSample {

  @DataProvider(name = "rows")
  public static Object[][] rows() {
    return new Object[][] {{"a"}, {"b"}, {"c"}};
  }

  @Test(dataProvider = "rows")
  public void f1(String row) {
    if ("b".equals(row)) {
      throw new RuntimeException("row " + row);
    }
  }
}
