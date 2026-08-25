package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Every shape the two reporting representations format differently: a string, an empty string, a
 * number, a null, arrays, and an object that is none of those.
 *
 * <p>The declared types matter as much as the values. Only a parameter declared {@code String} is
 * quoted in the text representation, and only the declaration tells a {@code null} apart from the
 * string {@code "null"} -- which is why the null here is declared as an {@link Object} rather than
 * left to be inferred.
 */
public class ParameterShapesSample {

  @DataProvider(name = "shapes")
  public static Object[][] shapes() {
    return new Object[][] {
      {"text", "", 42, null, new int[] {1, 2}, new String[] {"a", "b"}, new Shape()}
    };
  }

  @Test(dataProvider = "shapes")
  public void report(
      String text,
      String empty,
      int number,
      Object nothing,
      int[] numbers,
      String[] words,
      Shape shape) {}

  /** An ordinary custom parameter: not an array, not a string, and with something to say. */
  public static final class Shape {

    @Override
    public String toString() {
      return "shape";
    }
  }
}
