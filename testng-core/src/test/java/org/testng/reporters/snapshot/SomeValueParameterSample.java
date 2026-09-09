package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A single string parameter whose XML consumers read back as the element text of {@code <value>}.
 *
 * <p>Pretty-printing the CDATA onto its own indented line puts whitespace text nodes beside it, so
 * a parser that asks for the element's text gets that padding as well as the parameter.
 */
public class SomeValueParameterSample {

  @DataProvider(name = "value")
  public static Object[][] value() {
    return new Object[][] {{"Some Value"}};
  }

  @Test(dataProvider = "value")
  public void report(String value) {}
}
