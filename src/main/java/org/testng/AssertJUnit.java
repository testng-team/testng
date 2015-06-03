package org.testng;

import org.testng.internal.junit.ArrayAsserts;


/**
 * A set of assert methods.  Messages are only displayed when an assert fails.
 * Renamed from <CODE>junit.framework.Assert</CODE>.
 */
public class AssertJUnit extends ArrayAsserts {

  /**
   * Protect constructor since it is a static only class
   */
  protected AssertJUnit() {
  }

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionFailedError with the given message.
   */
  static public void assertTrue(String message, boolean condition) {
    if(!condition) {
      fail(message);
    }
  }

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionFailedError.
   */
  static public void assertTrue(boolean condition) {
    assertTrue(null, condition);
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionFailedError with the given message.
   */
  static public void assertFalse(String message, boolean condition) {
    assertTrue(message, !condition);
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionFailedError.
   */
  static public void assertFalse(boolean condition) {
    assertFalse(null, condition);
  }

  /**
   * Fails a test with the given message.
   */
  static public void fail(String message) {
    if (null == message) {
      message = "";
    }
    throw new AssertionError(message);
  }

  /**
   * Fails a test with no message.
   */
  static public void fail() {
    fail(null);
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(String message, Object expected, Object actual) {
    if((expected == null) && (actual == null)) {
      return;
    }
    if((expected != null) && expected.equals(actual)) {
      return;
    }
    failNotEquals(message, expected, actual);
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionFailedError is thrown.
   */
  static public void assertEquals(Object expected, Object actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two Strings are equal.
   */
  static public void assertEquals(String message, String expected, String actual) {
    if((expected == null) && (actual == null)) {
      return;
    }
    if((expected != null) && expected.equals(actual)) {
      return;
    }
    throw new AssertionError(format(message, expected, actual));
  }

  /**
   * Asserts that two Strings are equal.
   */
  static public void assertEquals(String expected, String actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two doubles are equal concerning a delta.  If they are not
   * an AssertionFailedError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(String message, double expected, double actual, double delta) {

    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if(Double.isInfinite(expected)) {
      if(!(expected == actual)) {
        failNotEquals(message, expected, actual);
      }
    }
    else if(!(Math.abs(expected - actual) <= delta)) { // Because comparison with NaN always returns false
      failNotEquals(message, expected, actual);
    }
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(double expected, double actual, double delta) {
    assertEquals(null, expected, actual, delta);
  }

  /**
   * Asserts that two floats are equal concerning a delta. If they are not
   * an AssertionFailedError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(String message, float expected, float actual, float delta) {

    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if(Float.isInfinite(expected)) {
      if(!(expected == actual)) {
        failNotEquals(message, expected, actual);
      }
    }
    else if(!(Math.abs(expected - actual) <= delta)) {
      failNotEquals(message, expected, actual);
    }
  }

  /**
   * Asserts that two floats are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(float expected, float actual, float delta) {
    assertEquals(null, expected, actual, delta);
  }

  /**
   * Asserts that two longs are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(String message, long expected, long actual) {
    assertEquals(message, Long.valueOf(expected), Long.valueOf(actual));
  }

  /**
   * Asserts that two longs are equal.
   */
  static public void assertEquals(long expected, long actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two booleans are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(String message, boolean expected, boolean actual) {
    assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
  }

  /**
   * Asserts that two booleans are equal.
   */
  static public void assertEquals(boolean expected, boolean actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two bytes are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(String message, byte expected, byte actual) {
    assertEquals(message, Byte.valueOf(expected), Byte.valueOf(actual));
  }

  /**
     * Asserts that two bytes are equal.
   */
  static public void assertEquals(byte expected, byte actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two chars are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(String message, char expected, char actual) {
    assertEquals(message, Character.valueOf(expected), Character.valueOf(actual));
  }

  /**
   * Asserts that two chars are equal.
   */
  static public void assertEquals(char expected, char actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two shorts are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(String message, short expected, short actual) {
    assertEquals(message, Short.valueOf(expected), Short.valueOf(actual));
  }

  /**
  * Asserts that two shorts are equal.
  */
  static public void assertEquals(short expected, short actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that two ints are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(String message, int expected, int actual) {
    assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
  }

  /**
   * Asserts that two ints are equal.
  */
  static public void assertEquals(int expected, int actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that an object isn't null.
   */
  static public void assertNotNull(Object object) {
    assertNotNull(null, object);
  }

  /**
   * Asserts that an object isn't null. If it is
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertNotNull(String message, Object object) {
    assertTrue(message, object != null);
  }

  /**
   * Asserts that an object is null.
   */
  static public void assertNull(Object object) {
    assertNull(null, object);
  }

  /**
   * Asserts that an object is null.  If it is not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertNull(String message, Object object) {
    assertTrue(message, object == null);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertSame(String message, Object expected, Object actual) {
    if(expected == actual) {
      return;
    }
    failNotSame(message, expected, actual);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionFailedError is thrown.
   */
  static public void assertSame(Object expected, Object actual) {
    assertSame(null, expected, actual);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertNotSame(String message, Object expected, Object actual) {
    if(expected == actual) {
      failSame(message);
    }
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionFailedError is thrown.
   */
  static public void assertNotSame(Object expected, Object actual) {
    assertNotSame(null, expected, actual);
  }

  static public void assertEquals(final byte[] expected, final byte[] actual) {
    assertEquals("", expected, actual);
  }

  static public void assertEquals(final String message, final byte[] expected, final byte[] actual) {
    if(expected == actual) {
        return;
    }
    if(null == expected) {
      fail("expected a null array, but not null found. " + message);
    }
    if(null == actual) {
        fail("expected not null array, but null found. " + message);
    }

    assertEquals("arrays don't have the same size. " + message, expected.length, actual.length);

    for(int i= 0; i < expected.length; i++) {
        if(expected[i] != actual[i]) {
            fail("arrays differ firstly at element [" + i +"]; "
                + format(message, expected[i], actual[i]));
        }
    }
  }

  static private void failSame(String message) {
    String formatted = "";
    if(message != null) {
      formatted = message + " ";
    }
    fail(formatted + "expected not same");
  }

  static private void failNotSame(String message, Object expected, Object actual) {
    String formatted = "";
    if(message != null) {
      formatted = message + " ";
    }
    fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
  }

  static private void failNotEquals(String message, Object expected, Object actual) {
    fail(format(message, expected, actual));
  }

  static String format(String message, Object expected, Object actual) {
    String formatted = "";
    if(message != null) {
      formatted = message + " ";
    }

    return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
  }
}
