package test.factory.issue3111;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A factory method returning two instances per data provider row, over two rows: the shape that
 * separates "which invocation produced this instance" from "which instance is this". All four
 * instances have distinct indexes; the two born of the same invocation share its parameters.
 */
public class MultiInstancePerRowFactorySample {

  private final String label;

  private MultiInstancePerRowFactorySample(String label) {
    this.label = label;
  }

  @DataProvider(name = "rows")
  public static Object[][] rows() {
    return new Object[][] {{"a"}, {"b"}};
  }

  @Factory(dataProvider = "rows")
  public static Object[] create(String row) {
    return new Object[] {
      new MultiInstancePerRowFactorySample(row + "1"),
      new MultiInstancePerRowFactorySample(row + "2")
    };
  }

  @Test
  public void test() {}

  @Override
  public String toString() {
    return label;
  }
}
