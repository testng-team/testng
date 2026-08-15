package test.factory.issue3111;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** Four instances meant to be run with <code>parallel="instances"</code>. */
public class ParallelFactorySample {

  private final int index;

  @Factory(dataProvider = "indices")
  public ParallelFactorySample(int index) {
    this.index = index;
  }

  @DataProvider(name = "indices")
  public static Object[][] indices() {
    return new Object[][] {{0}, {1}, {2}, {3}};
  }

  @Test
  public void test() {}

  @Override
  public String toString() {
    return "instance-" + index;
  }
}
