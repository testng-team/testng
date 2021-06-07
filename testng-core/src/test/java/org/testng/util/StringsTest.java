package org.testng.util;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/** Unit tests for {@link Strings} */
public class StringsTest {
  @Test
  public void joinEmptyArray() {
    String[] emptyArray = new String[0];
    assertEquals(Strings.join(",", emptyArray), "");
  }

  @Test
  public void joinArrayWithOneElement() {
    String[] array = new String[] {"one"};
    assertEquals(Strings.join(",", array), "one");
  }

  @Test
  public void joinArrayWithTwoElements() {
    String[] array = new String[] {"one", "two"};
    assertEquals(Strings.join(",", array), "one,two");
  }
}
