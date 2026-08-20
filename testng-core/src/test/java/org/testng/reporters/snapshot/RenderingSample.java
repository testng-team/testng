package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** The ordinary parameter shapes a reporter renders: a quoted string, a number, and a null. */
public class RenderingSample {

  @DataProvider(name = "values")
  public static Object[][] values() {
    return new Object[][] {{"text", 42, null}};
  }

  @Test(dataProvider = "values")
  public void report(String text, int number, Object nothing) {}
}
