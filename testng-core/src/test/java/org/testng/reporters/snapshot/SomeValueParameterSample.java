package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * String parameters whose XML consumers read back as the element text of {@code <value>}.
 *
 * <p>Pretty-printing the CDATA onto its own indented line puts whitespace text nodes beside it. A
 * parser that asks for the element's text gets that padding as well as the parameter. One value has
 * leading and trailing spaces so a trim would fail.
 */
public class SomeValueParameterSample {

  @DataProvider(name = "value")
  public static Object[][] value() {
    return new Object[][] {{"Some Value"}, {"  padded  "}};
  }

  @Test(dataProvider = "value")
  public void report(String value) {}
}
