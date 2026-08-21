package org.testng.reporters.snapshot;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A configuration method that is handed the values its test method will run with, mutates them, and
 * passes -- so a reporter which prints a configuration as it succeeds has to have kept what it was
 * announced with, and the snapshot has to still be there when it does.
 */
public class PassingConfigurationParameterSample {

  @DataProvider(name = "shared")
  public static Object[][] shared() {
    return new Object[][] {{new MutableParameter("before-configuration")}};
  }

  @BeforeMethod
  public void prepare(Object[] parameters) {
    ((MutableParameter) parameters[0]).set("mutated");
  }

  @Test(dataProvider = "shared")
  public void report(MutableParameter parameter) {}
}
