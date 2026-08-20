package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A data provider that hands the same mutable object to every invocation, each of which leaves it
 * holding the value the next one is expected to report.
 *
 * <p>{@link MutableParameter} is not {@link Cloneable}, so the historical {@code
 * LegacyParameterSnapshotter} cannot help: by the time a reporter reads {@code
 * ITestResult#getParameters()}, every result points at the same object in its final state.
 */
public class NonCloneableParameterSample {

  private int invocation = 1;

  @DataProvider(name = "shared")
  public static Object[][] shared() {
    MutableParameter parameter = new MutableParameter("invocation-1");
    return new Object[][] {{parameter}, {parameter}, {parameter}};
  }

  @Test(dataProvider = "shared")
  public void report(MutableParameter parameter) {
    invocation++;
    parameter.set("invocation-" + invocation);
  }
}
