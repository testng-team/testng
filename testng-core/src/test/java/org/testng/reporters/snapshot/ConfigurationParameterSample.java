package org.testng.reporters.snapshot;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A configuration method that is handed the values its test method will run with, mutates them, and
 * then fails -- so a reporter has to have kept what it was announced with to report it.
 */
public class ConfigurationParameterSample {

  private static final MutableParameter SHARED = new MutableParameter("before-configuration");

  @DataProvider(name = "shared")
  public static Object[][] shared() {
    return new Object[][] {{SHARED}};
  }

  @BeforeMethod
  public void prepare(Object[] parameters) {
    ((MutableParameter) parameters[0]).set("mutated");
    throw new IllegalStateException("this configuration method fails on purpose");
  }

  @Test(dataProvider = "shared")
  public void report(MutableParameter parameter) {}
}
