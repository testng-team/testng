package test.failedreporter.issue3111;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A factory powered class whose test method has a data provider of its own: the shape in which the
 * factory index used to overwrite the data provider row index in {@code testng-failed.xml}.
 *
 * <p>The factory produces three instances (0, 1, 2) and the method runs on three rows (0, 1, 2).
 * {@code f1} fails on instance 1 for row 2, and on instance 2 for row 0.
 */
public class FactoryAndMethodDataProviderSample {

  private final int instance;

  @Factory(dataProvider = "instances")
  public FactoryAndMethodDataProviderSample(int instance) {
    this.instance = instance;
  }

  @DataProvider(name = "instances")
  public static Object[][] instances() {
    return new Object[][] {{0}, {1}, {2}};
  }

  @DataProvider(name = "rows")
  public static Object[][] rows() {
    return new Object[][] {{0}, {1}, {2}};
  }

  @Test(dataProvider = "rows")
  public void f1(int row) {
    if (instance == 1 && row == 2) {
      throw new RuntimeException("instance 1, row 2");
    }
    if (instance == 2 && row == 0) {
      throw new RuntimeException("instance 2, row 0");
    }
  }
}
