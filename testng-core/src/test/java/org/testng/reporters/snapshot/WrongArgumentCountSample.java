package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** A data provider that supplies more values than the method it feeds declares. */
public class WrongArgumentCountSample {

  @DataProvider(name = "tooMany")
  public static Object[][] tooMany() {
    return new Object[][] {{"first", "second"}};
  }

  @Test(dataProvider = "tooMany")
  public void report(String only) {}
}
