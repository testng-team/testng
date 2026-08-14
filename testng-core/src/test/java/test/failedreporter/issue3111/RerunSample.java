package test.failedreporter.issue3111;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** Four factory instances; the odd ones fail. {@code f1} has no data provider of its own. */
public class RerunSample {

  private final int instance;

  @Factory(dataProvider = "instances")
  public RerunSample(int instance) {
    this.instance = instance;
  }

  @DataProvider(name = "instances")
  public static Object[][] instances() {
    return new Object[][] {{0}, {1}, {2}, {3}};
  }

  @Test
  public void f1() {
    ExecutedPairs.record(instance);
    if (instance % 2 == 1) {
      throw new RuntimeException("instance " + instance);
    }
  }
}
