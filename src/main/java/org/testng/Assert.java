package org.testng;

import static org.testng.internal.EclipseInterface.ASSERT_LEFT;
import static org.testng.internal.EclipseInterface.ASSERT_LEFT2;
import static org.testng.internal.EclipseInterface.ASSERT_MIDDLE;
import static org.testng.internal.EclipseInterface.ASSERT_RIGHT;
import static org.testng.internal.EclipseInterface.ASSERT_LEFT_INEQUALITY;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.collections.Lists;

/**
 * Assertion tool class. Presents assertion methods with a more natural parameter order. The order
 * is always <B>actualValue</B>, <B>expectedValue</B> [, message].
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class Assert {

  public static final String ARRAY_MISMATCH_TEMPLATE =
      "arrays differ firstly at element [%d]; " + "expected value is <%s> but was <%s>. %s";

  /** Protect constructor since it is a static only class */
  protected Assert() {
    // hide constructor
  }

  /**
   * Asserts that a condition is true. If it isn't, an AssertionError, with the given message, is
   * thrown.
   *
   * @param condition the condition to evaluate
   * @param message the assertion error message
   */
  public static void assertTrue(boolean condition, String message) {
    if (!condition) {
      failNotEquals(condition, Boolean.TRUE, message);
    }
  }

  /**
   * Asserts that a condition is true. If it isn't, an AssertionError is thrown.
   *
   * @param condition the condition to evaluate
   */
  public static void assertTrue(boolean condition) {
    assertTrue(condition, null);
  }

  /**
   * Asserts that a condition is false. If it isn't, an AssertionError, with the given message, is
   * thrown.
   *
   * @param condition the condition to evaluate
   * @param message the assertion error message
   */
  public static void assertFalse(boolean condition, String message) {
    if (condition) {
      failNotEquals(condition, Boolean.FALSE, message); // TESTNG-81
    }
  }

  /**
   * Asserts that a condition is false. If it isn't, an AssertionError is thrown.
   *
   * @param condition the condition to evaluate
   */
  public static void assertFalse(boolean condition) {
    assertFalse(condition, null);
  }

  /**
   * Fails a test with the given message and wrapping the original exception.
   *
   * @param message the assertion error message
   * @param realCause the original exception
   */
  public static void fail(String message, Throwable realCause) {
    AssertionError ae = new AssertionError(message);
    ae.initCause(realCause);

    throw ae;
  }

  /**
   * Fails a test with the given message.
   *
   * @param message the assertion error message
   */
  public static void fail(String message) {
    throw new AssertionError(message);
  }

  /** Fails a test with no message. */
  public static void fail() {
    fail(null);
  }

  /**
   * Asserts that two objects are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Object actual, Object expected, String message) {
    if (expected != null && expected.getClass().isArray()) {
      assertArrayEquals(actual, expected, message);
      return;
    }
    assertEqualsImpl(actual, expected, message);
  }

  /**
   * Differs from {@link #assertEquals(Object, Object, String)} by not taking arrays into special
   * consideration hence comparing them by reference. Intended to be called directly to test
   * equality of collections content.
   */
  private static void assertEqualsImpl(Object actual, Object expected, String message) {
    if ((expected == null) && (actual == null)) {
      return;
    }
    if (expected == null ^ actual == null) {
      failNotEquals(actual, expected, message);
    }
    if (expected.equals(actual) && actual.equals(expected)) {
      return;
    }
    fail(format(actual, expected, message, true));
  }

  private static void assertArrayEquals(Object actual, Object expected, String message) {
    if (expected == actual) {
      return;
    }
    if (null == expected) {
      fail("expected a null array, but not null found. " + message);
    }
    if (null == actual) {
      fail("expected not null array, but null found. " + message);
    }
    // is called only when expected is an array
    if (!actual.getClass().isArray()) {
      failNotEquals(actual, expected, message);
    }
    int expectedLength = Array.getLength(expected);
    if (expectedLength != Array.getLength(actual)) {
      failNotEquals(
          Array.getLength(actual),
          expectedLength,
          message == null ? "" : message + " (Array lengths are not the same)");
    }
    for (int i = 0; i < expectedLength; i++) {
      Object _actual = Array.get(actual, i);
      Object _expected = Array.get(expected, i);
      try {
        assertEquals(_actual, _expected);
      } catch (AssertionError ae) {
        failNotEquals(
            actual,
            expected,
            message == null ? "" : message + " (values at index " + i + " are not the same)");
      }
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(byte[] actual, byte[] expected) {
    assertEquals(actual, expected, "");
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(byte[] actual, byte[] expected, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) return;

    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        fail(
            String.format(
                ARRAY_MISMATCH_TEMPLATE,
                i,
                Byte.toString(expected[i]),
                Byte.toString(actual[i]),
                message));
      }
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(short[] actual, short[] expected) {
    assertEquals(actual, expected, "");
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(short[] actual, short[] expected, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) return;

    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        fail(
            String.format(
                ARRAY_MISMATCH_TEMPLATE,
                i,
                Short.toString(expected[i]),
                Short.toString(actual[i]),
                message));
      }
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(int[] actual, int[] expected) {
    assertEquals(actual, expected, "");
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(int[] actual, int[] expected, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) return;

    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        fail(
            String.format(
                ARRAY_MISMATCH_TEMPLATE,
                i,
                Integer.toString(expected[i]),
                Integer.toString(actual[i]),
                message));
      }
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(boolean[] actual, boolean[] expected) {
    assertEquals(actual, expected, "");
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(boolean[] actual, boolean[] expected, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) return;

    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        fail(
            String.format(
                ARRAY_MISMATCH_TEMPLATE,
                i,
                Boolean.toString(expected[i]),
                Boolean.toString(actual[i]),
                message));
      }
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(char[] actual, char[] expected) {
    assertEquals(actual, expected, "");
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(char[] actual, char[] expected, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) return;

    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        fail(
            String.format(
                ARRAY_MISMATCH_TEMPLATE,
                i,
                Character.toString(expected[i]),
                Character.toString(actual[i]),
                message));
      }
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(float[] actual, float[] expected) {
    assertEquals(actual, expected, "");
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(float[] actual, float[] expected, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) return;

    for (int i = 0; i < expected.length; i++) {
      assertEquals(actual[i], expected[i],
              String.format(
                      ARRAY_MISMATCH_TEMPLATE,
                      i,
                      Float.toString(expected[i]),
                      Float.toString(actual[i]),
                      message));
      }
  }

  /**
   * Asserts that two arrays contain the equal elements concerning a delta in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   */
  public static void assertEquals(float[] actual, float[] expected, float delta) {
    assertEquals(actual, expected, delta, "");
  }

  /**
   * Asserts that two arrays contain the equal elements concerning a delta in the same order. If
   * they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   * @param message the assertion error message
   */
  public static void assertEquals(float[] actual, float[] expected, float delta, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

    for (int i = 0; i < expected.length; i++) {
      assertEquals(actual[i], expected[i], delta,
              String.format(
                      ARRAY_MISMATCH_TEMPLATE,
                      i,
                      Float.toString(expected[i]),
                      Float.toString(actual[i]),
                      message));
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(double[] actual, double[] expected) {
    assertEquals(actual, expected, "");
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(double[] actual, double[] expected, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) return;

    for (int i = 0; i < expected.length; i++) {
      assertEquals(actual[i], expected[i],
              String.format(
                      ARRAY_MISMATCH_TEMPLATE,
                      i,
                      Double.toString(expected[i]),
                      Double.toString(actual[i]),
                      message));
    }
  }

  /**
   * Asserts that two arrays contain the equal elements concerning a delta in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   */
  public static void assertEquals(double[] actual, double[] expected, double delta) {
    assertEquals(actual, expected, delta, "");
  }

  /**
   * Asserts that two arrays contain the equal elements concerning a delta in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   * @param message the assertion error message
   */
  public static void assertEquals(double[] actual, double[] expected, double delta, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

    for (int i = 0; i < expected.length; i++) {
      assertEquals(actual[i], expected[i], delta,
              String.format(
                      ARRAY_MISMATCH_TEMPLATE,
                      i,
                      Double.toString(expected[i]),
                      Double.toString(actual[i]),
                      message));
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(long[] actual, long[] expected) {
    assertEquals(actual, expected, "");
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(long[] actual, long[] expected, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) return;

    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        fail(
            String.format(
                ARRAY_MISMATCH_TEMPLATE,
                i,
                Long.toString(expected[i]),
                Long.toString(actual[i]),
                message));
      }
    }
  }

  /**
   * This methods check referential equality of given arguments as well as references length
   * (assuming they are arrays). Successful execution of this method guaranties arrays length
   * equality.
   *
   * @param actualArray array of elements
   * @param expectedArray array of elements
   * @param message the assertion error message
   * @return {@code true} if {@code actualArray} and {@code expectedArray} are the same, {@code
   *     false} otherwise. If references are different and arrays length are different {@link
   *     AssertionError} is thrown.
   */
  private static boolean checkRefEqualityAndLength(
      Object actualArray, Object expectedArray, String message) {
    if (expectedArray == actualArray) {
      return true;
    }
    if (null == expectedArray) {
      fail("expectedArray a null array, but not null found. " + message);
    }
    if (null == actualArray) {
      fail("expectedArray not null array, but null found. " + message);
    }

    assertEquals(
        Array.getLength(actualArray),
        Array.getLength(expectedArray),
        "arrays don't have the same size. " + message);
    return false;
  }

  /**
   * Asserts that two objects are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Object actual, Object expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two Strings are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(String actual, String expected, String message) {
    assertEquals((Object) actual, (Object) expected, message);
  }

  /**
   * Asserts that two Strings are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(String actual, String expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If they are not, an AssertionError, with
   * the given message, is thrown. If the expected value is infinity then the delta value is
   * ignored.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   * @param message the assertion error message
   */
  public static void assertEquals(double actual, double expected, double delta, String message) {
    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if (Double.isInfinite(expected)) {
      if (!(expected == actual)) {
        failNotEquals(actual, expected, message);
      }
    } else if (Double.isNaN(expected)) {
      if (!Double.isNaN(actual)) {
        failNotEquals(actual, expected, message);
      }
    } else if (!(Math.abs(expected - actual) <= delta)) {
      failNotEquals(actual, expected, message);
    }
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If they are not, an AssertionError is
   * thrown. If the expected value is infinity then the delta value is ignored.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   */
  public static void assertEquals(double actual, double expected, double delta) {
    assertEquals(actual, expected, delta, null);
  }

  /**
   * Asserts that two doubles are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(double actual, double expected, String message) {
     if (Double.isNaN(expected)) {
      if (!Double.isNaN(actual)) {
        failNotEquals(actual, expected, message);
      }
    } else if (actual != expected) {
      failNotEquals(actual, expected, message);
    }
  }

  /**
   * Asserts that two doubles are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(double actual, double expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two floats are equal concerning a delta. If they are not, an AssertionError, with
   * the given message, is thrown. If the expected value is infinity then the delta value is
   * ignored.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   * @param message the assertion error message
   */
  public static void assertEquals(float actual, float expected, float delta, String message) {
    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if (Float.isInfinite(expected)) {
      if (!(expected == actual)) {
        failNotEquals(actual, expected, message);
      }
    } else if (Float.isNaN(expected)) {
      if (!Float.isNaN(actual)) {
        failNotEquals(actual, expected, message);
      }
    } else if (!(Math.abs(expected - actual) <= delta)) {
      failNotEquals(actual, expected, message);
    }
  }

  /**
   * Asserts that two floats are equal concerning a delta. If they are not, an AssertionError is
   * thrown. If the expected value is infinity then the delta value is ignored.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   */
  public static void assertEquals(float actual, float expected, float delta) {
    assertEquals(actual, expected, delta, null);
  }

  /**
   * Asserts that two floats are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(float actual, float expected, String message) {
    if (Float.isNaN(expected)) {
      if (!Float.isNaN(actual)) {
        failNotEquals(actual, expected, message);
      }
    } else if (actual != expected) {
      failNotEquals(actual, expected, message);
    }
  }

  /**
   * Asserts that two floats are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(float actual, float expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two longs are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(long actual, long expected, String message) {
    assertEquals(Long.valueOf(actual), Long.valueOf(expected), message);
  }

  /**
   * Asserts that two longs are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(long actual, long expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two booleans are equal. If they are not, an AssertionError, with the given
   * message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(boolean actual, boolean expected, String message) {
    assertEquals(Boolean.valueOf(actual), Boolean.valueOf(expected), message);
  }

  /**
   * Asserts that two booleans are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(boolean actual, boolean expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two bytes are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(byte actual, byte expected, String message) {
    assertEquals(Byte.valueOf(actual), Byte.valueOf(expected), message);
  }

  /**
   * Asserts that two bytes are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(byte actual, byte expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two chars are equal. If they are not, an AssertionFailedError, with the given
   * message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(char actual, char expected, String message) {
    assertEquals(Character.valueOf(actual), Character.valueOf(expected), message);
  }

  /**
   * Asserts that two chars are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(char actual, char expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two shorts are equal. If they are not, an AssertionFailedError, with the given
   * message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(short actual, short expected, String message) {
    assertEquals(Short.valueOf(actual), Short.valueOf(expected), message);
  }

  /**
   * Asserts that two shorts are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(short actual, short expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two ints are equal. If they are not, an AssertionFailedError, with the given
   * message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(int actual, int expected, String message) {
    assertEquals(Integer.valueOf(actual), Integer.valueOf(expected), message);
  }

  /**
   * Asserts that two ints are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(int actual, int expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that an object isn't null. If it is, an AssertionError is thrown.
   *
   * @param object the assertion object
   */
  public static void assertNotNull(Object object) {
    assertNotNull(object, null);
  }

  /**
   * Asserts that an object isn't null. If it is, an AssertionFailedError, with the given message,
   * is thrown.
   *
   * @param object the assertion object
   * @param message the assertion error message
   */
  public static void assertNotNull(Object object, String message) {
    if (object == null) {
      String formatted = "";
      if (message != null) {
        formatted = message + " ";
      }
      fail(formatted + "expected object to not be null");
    }
    assertTrue(object != null, message);
  }

  /**
   * Asserts that an object is null. If it is not, an AssertionError, with the given message, is
   * thrown.
   *
   * @param object the assertion object
   */
  public static void assertNull(Object object) {
    assertNull(object, null);
  }

  /**
   * Asserts that an object is null. If it is not, an AssertionFailedError, with the given message,
   * is thrown.
   *
   * @param object the assertion object
   * @param message the assertion error message
   */
  public static void assertNull(Object object, String message) {
    if (object != null) {
      failNotSame(object, null, message);
    }
  }

  /**
   * Asserts that two objects refer to the same object. If they do not, an AssertionFailedError,
   * with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertSame(Object actual, Object expected, String message) {
    if (expected == actual) {
      return;
    }
    failNotSame(actual, expected, message);
  }

  /**
   * Asserts that two objects refer to the same object. If they do not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertSame(Object actual, Object expected) {
    assertSame(actual, expected, null);
  }

  /**
   * Asserts that two objects do not refer to the same objects. If they do, an AssertionError, with
   * the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertNotSame(Object actual, Object expected, String message) {
    if (expected == actual) {
      failSame(actual, expected, message);
    }
  }

  /**
   * Asserts that two objects do not refer to the same object. If they do, an AssertionError is
   * thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertNotSame(Object actual, Object expected) {
    assertNotSame(actual, expected, null);
  }

  private static void failSame(Object actual, Object expected, String message) {
    String formatted = "";
    if (message != null) {
      formatted = message + " ";
    }
    fail(formatted + ASSERT_LEFT2 + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT);
  }

  private static void failNotSame(Object actual, Object expected, String message) {
    String formatted = "";
    if (message != null) {
      formatted = message + " ";
    }
    fail(formatted + ASSERT_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT);
  }

  private static void failNotEquals(Object actual, Object expected, String message) {
    fail(format(actual, expected, message));
  }

  static String format(Object actual, Object expected, String message) {
    return format(actual, expected, message, false);
  }

  static String format(Object actual, Object expected, String message, boolean equality) {
    String formatted = "";
    if (null != message) {
      formatted = message + " ";
    }
    if (equality) {
      return formatted + ASSERT_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT;
    }
    return formatted + ASSERT_LEFT_INEQUALITY + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT;
  }

  /**
   * Asserts that two collections contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Collection<?> actual, Collection<?> expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two collections contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Collection<?> actual, Collection<?> expected, String message) {
    if (actual == expected) {
      return;
    }

    if (actual == null || expected == null) {
      if (message != null) {
        fail(message);
      } else {
        fail("Collections not equal: expected: " + expected + " and actual: " + actual);
      }
    }

    assertEquals(
        actual.size(),
        expected.size(),
        (message == null ? "" : message + ": ") + "lists don't have the same size");

    Iterator<?> actIt = actual.iterator();
    Iterator<?> expIt = expected.iterator();
    int i = -1;
    while (actIt.hasNext() && expIt.hasNext()) {
      i++;
      Object e = expIt.next();
      Object a = actIt.next();
      String explanation = "Lists differ at element [" + i + "]: " + e + " != " + a;
      String errorMessage = message == null ? explanation : message + ": " + explanation;
      assertEqualsImpl(a, e, errorMessage);
    }
  }

  /**
   * Asserts that two iterators return the same elements in the same order. If they do not, an
   * AssertionError is thrown. Please note that this assert iterates over the elements and modifies
   * the state of the iterators.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Iterator<?> actual, Iterator<?> expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two iterators return the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown. Please note that this assert iterates over
   * the elements and modifies the state of the iterators.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Iterator<?> actual, Iterator<?> expected, String message) {
    if (actual == expected) {
      return;
    }
    if (actual == null || expected == null) {
      String msg =
          message != null
              ? message
              : "Iterators not equal: expected: " + expected + " and actual: " + actual;
      fail(msg);
    }

    int i = -1;
    while (actual.hasNext() && expected.hasNext()) {

      i++;
      Object e = expected.next();
      Object a = actual.next();
      String explanation = "Iterators differ at element [" + i + "]: " + e + " != " + a;
      String errorMessage = message == null ? explanation : message + ": " + explanation;

      assertEqualsImpl(a, e, errorMessage);
    }

    if (actual.hasNext()) {

      String explanation = "Actual iterator returned more elements than the expected iterator.";
      String errorMessage = message == null ? explanation : message + ": " + explanation;
      fail(errorMessage);

    } else if (expected.hasNext()) {

      String explanation = "Expected iterator returned more elements than the actual iterator.";
      String errorMessage = message == null ? explanation : message + ": " + explanation;
      fail(errorMessage);
    }
  }

  /**
   * Asserts that two iterables return iterators with the same elements in the same order. If they
   * do not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Iterable<?> actual, Iterable<?> expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two iterables return iterators with the same elements in the same order. If they
   * do not, an AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Iterable<?> actual, Iterable<?> expected, String message) {
    if (actual == expected) {
      return;
    }

    if (actual == null || expected == null) {
      if (message != null) {
        fail(message);
      } else {
        fail("Iterables not equal: expected: " + expected + " and actual: " + actual);
      }
    }

    Iterator<?> actIt = actual.iterator();
    Iterator<?> expIt = expected.iterator();

    assertEquals(actIt, expIt, message);
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Object[] actual, Object[] expected, String message) {
    if (actual == expected) {
      return;
    }

    if ((actual == null && expected != null) || (actual != null && expected == null)) {
      if (message != null) {
        fail(message);
      } else {
        fail("Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual));
      }
    }
    if (actual.length != expected.length) {
      failAssertNoEqual(
              "Arrays do not have the same size:" + actual.length + " != " + expected.length, message);
    }

    for(int i=0; i<expected.length; i++) {
      Object e = expected[i];
      Object a = actual[i];
      String explanation = "Arrays differ at element [" + i + "]: " + e + " != " + a;
      String errorMessage = message == null ? explanation : message + ": " + explanation;
      //Compare by value for multi-dimensional array.
      if(e.getClass().isArray()) {
        assertEquals(a, e, errorMessage);
      } else {
        assertEqualsImpl(a, e, errorMessage);
      }
    }

  }

  /**
   * Asserts that two arrays contain the same elements in no particular order. If they do not, an
   * AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEqualsNoOrder(Object[] actual, Object[] expected, String message) {
    if (actual == expected) {
      return;
    }

    if ((actual == null && expected != null) || (actual != null && expected == null)) {
      failAssertNoEqual(
          "Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual),
          message);
    }

    if (actual.length != expected.length) {
      failAssertNoEqual(
          "Arrays do not have the same size:" + actual.length + " != " + expected.length, message);
    }

    List<Object> actualCollection = Lists.newArrayList();
    actualCollection.addAll(Arrays.asList(actual));
    for (Object o : expected) {
      actualCollection.remove(o);
    }
    if (actualCollection.size() != 0) {
      failAssertNoEqual(
          "Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual),
          message);
    }
  }

  private static void failAssertNoEqual(String defaultMessage, String message) {
    if (message != null) {
      fail(message);
    } else {
      fail(defaultMessage);
    }
  }

  /**
   * Asserts that two arrays contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Object[] actual, Object[] expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two arrays contain the same elements in no particular order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEqualsNoOrder(Object[] actual, Object[] expected) {
    assertEqualsNoOrder(actual, expected, null);
  }

  /** Asserts that two sets are equal. */
  public static void assertEquals(Set<?> actual, Set<?> expected) {
    assertEquals(actual, expected, null);
  }

  /** Assert set equals */
  public static void assertEquals(Set<?> actual, Set<?> expected, String message) {
    if (actual == expected) {
      return;
    }

    if (actual == null || expected == null) {
      // Keep the back compatible
      if (message == null) {
        fail("Sets not equal: expected: " + expected + " and actual: " + actual);
      } else {
        failNotEquals(actual, expected, message);
      }
    }

    if (!actual.equals(expected)) {
      if (message == null) {
        fail("Sets differ: expected " + expected + " but got " + actual);
      } else {
        failNotEquals(actual, expected, message);
      }
    }
  }

  public static void assertEqualsDeep(Set<?> actual, Set<?> expected, String message) {
    if (actual == expected) {
      return;
    }

    if (actual == null || expected == null) {
      // Keep the back compatible
      if (message == null) {
        fail("Sets not equal: expected: " + expected + " and actual: " + actual);
      } else {
        failNotEquals(actual, expected, message);
      }
    }

    if (expected.size() != actual.size()) {
      if (message == null) {
        fail("Sets not equal: expected: " + expected + " and actual: " + actual);
      } else {
        fail(format(actual, expected, message, true));
      }
    }

    Iterator<?> actualIterator = actual.iterator();
    Iterator<?> expectedIterator = expected.iterator();
    while (expectedIterator.hasNext()) {
      Object expectedValue = expectedIterator.next();
      Object value = actualIterator.next();
      if (expectedValue.getClass().isArray()) {
        assertArrayEquals(value, expectedValue, message);
      } else {
        assertEqualsImpl(value, expectedValue, message);
      }
    }
  }

  /** Asserts that two maps are equal. */
  public static void assertEquals(Map<?, ?> actual, Map<?, ?> expected, String message) {
    if (actual == expected) {
      return;
    }

    if (actual == null || expected == null) {
      fail("Maps not equal: expected: " + expected + " and actual: " + actual);
    }

    if (actual.size() != expected.size()) {
      fail("Maps do not have the same size:" + actual.size() + " != " + expected.size());
    }

    Set<?> entrySet = actual.entrySet();
    for (Object anEntrySet : entrySet) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) anEntrySet;
      Object key = entry.getKey();
      Object value = entry.getValue();
      Object expectedValue = expected.get(key);
      String assertMessage =
          message != null
              ? message
              : "Maps do not match for key:"
                  + key
                  + " actual:"
                  + value
                  + " expected:"
                  + expectedValue;
      assertEqualsImpl(value, expectedValue, assertMessage);
    }
  }

  public static void assertEqualsDeep(Map<?, ?> actual, Map<?, ?> expected) {
    assertEqualsDeep(actual, expected, null);
  }

  public static void assertEqualsDeep(Map<?, ?> actual, Map<?, ?> expected, String message) {
    if (actual == expected) {
      return;
    }

    if (actual == null || expected == null) {
      fail("Maps not equal: expected: " + expected + " and actual: " + actual);
    }

    if (actual.size() != expected.size()) {
      fail("Maps do not have the same size:" + actual.size() + " != " + expected.size());
    }

    Set<?> entrySet = actual.entrySet();
    for (Object anEntrySet : entrySet) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) anEntrySet;
      Object key = entry.getKey();
      Object value = entry.getValue();
      Object expectedValue = expected.get(key);
      String assertMessage =
          message != null
              ? message
              : "Maps do not match for key:"
                  + key
                  + " actual:"
                  + value
                  + " expected:"
                  + expectedValue;
      if (expectedValue.getClass().isArray()) {
        assertArrayEquals(value, expectedValue, assertMessage);
      } else {
        assertEqualsImpl(value, expectedValue, assertMessage);
      }
    }
  }

  /////
  // assertNotEquals
  //

  public static void assertNotEquals(Object actual1, Object actual2, String message) {
    boolean fail;
    try {
      Assert.assertEquals(actual1, actual2);
      fail = true;
    } catch (AssertionError e) {
      fail = false;
    }

    if (fail) {
      Assert.fail(format(actual1, actual2, message, false));
    }
  }

  public static void assertNotEquals(Object actual1, Object actual2) {
    assertNotEquals(actual1, actual2, null);
  }

  static void assertNotEquals(String actual1, String actual2, String message) {
    assertNotEquals((Object) actual1, (Object) actual2, message);
  }

  static void assertNotEquals(String actual1, String actual2) {
    assertNotEquals(actual1, actual2, null);
  }

  static void assertNotEquals(long actual1, long actual2, String message) {
    assertNotEquals(Long.valueOf(actual1), Long.valueOf(actual2), message);
  }

  static void assertNotEquals(long actual1, long actual2) {
    assertNotEquals(actual1, actual2, null);
  }

  static void assertNotEquals(boolean actual1, boolean actual2, String message) {
    assertNotEquals(Boolean.valueOf(actual1), Boolean.valueOf(actual2), message);
  }

  static void assertNotEquals(boolean actual1, boolean actual2) {
    assertNotEquals(actual1, actual2, null);
  }

  static void assertNotEquals(byte actual1, byte actual2, String message) {
    assertNotEquals(Byte.valueOf(actual1), Byte.valueOf(actual2), message);
  }

  static void assertNotEquals(byte actual1, byte actual2) {
    assertNotEquals(actual1, actual2, null);
  }

  static void assertNotEquals(char actual1, char actual2, String message) {
    assertNotEquals(Character.valueOf(actual1), Character.valueOf(actual2), message);
  }

  static void assertNotEquals(char actual1, char actual2) {
    assertNotEquals(actual1, actual2, null);
  }

  static void assertNotEquals(short actual1, short actual2, String message) {
    assertNotEquals(Short.valueOf(actual1), Short.valueOf(actual2), message);
  }

  static void assertNotEquals(short actual1, short actual2) {
    assertNotEquals(actual1, actual2, null);
  }

  static void assertNotEquals(int actual1, int actual2, String message) {
    assertNotEquals(Integer.valueOf(actual1), Integer.valueOf(actual2), message);
  }

  static void assertNotEquals(int actual1, int actual2) {
    assertNotEquals(actual1, actual2, null);
  }

  public static void assertNotEquals(float actual1, float actual2, float delta, String message) {
    boolean fail;
    try {
      Assert.assertEquals(actual1, actual2, delta, message);
      fail = true;
    } catch (AssertionError e) {
      fail = false;
    }

    if (fail) {
      Assert.fail(message);
    }
  }

  public static void assertNotEquals(float actual1, float actual2, float delta) {
    assertNotEquals(actual1, actual2, delta, null);
  }

  public static void assertNotEquals(double actual1, double actual2, double delta, String message) {
    boolean fail;
    try {
      Assert.assertEquals(actual1, actual2, delta, message);
      fail = true;
    } catch (AssertionError e) {
      fail = false;
    }

    if (fail) {
      Assert.fail(message);
    }
  }

  public static void assertNotEquals(Set<?> actual, Set<?> expected) {
    assertNotEquals(actual, expected, null);
  }

  public static void assertNotEquals(Set<?> actual, Set<?> expected, String message) {
    boolean fail;
    try {
      Assert.assertEquals(actual, expected, message);
      fail = true;
    } catch (AssertionError e) {
      fail = false;
    }

    if (fail) {
      Assert.fail(message);
    }
  }

  public static void assertNotEqualsDeep(Set<?> actual, Set<?> expected) {
    assertNotEqualsDeep(actual, expected, null);
  }

  public static void assertNotEqualsDeep(Set<?> actual, Set<?> expected, String message) {
    boolean fail;
    try {
      Assert.assertEqualsDeep(actual, expected, message);
      fail = true;
    } catch (AssertionError e) {
      fail = false;
    }

    if (fail) {
      Assert.fail(message);
    }
  }

  public static void assertNotEquals(Map<?, ?> actual, Map<?, ?> expected) {
    assertNotEquals(actual, expected, null);
  }

  public static void assertNotEquals(Map<?, ?> actual, Map<?, ?> expected, String message) {
    boolean fail;
    try {
      Assert.assertEquals(actual, expected, message);
      fail = true;
    } catch (AssertionError e) {
      fail = false;
    }

    if (fail) {
      Assert.fail(message);
    }
  }

  public static void assertNotEqualsDeep(Map<?, ?> actual, Map<?, ?> expected) {
    assertNotEqualsDeep(actual, expected, null);
  }

  public static void assertNotEqualsDeep(Map<?, ?> actual, Map<?, ?> expected, String message) {
    boolean fail;
    try {
      Assert.assertEqualsDeep(actual, expected, message);
      fail = true;
    } catch (AssertionError e) {
      fail = false;
    }

    if (fail) {
      Assert.fail(message);
    }
  }

  public static void assertNotEquals(double actual1, double actual2, double delta) {
    assertNotEquals(actual1, actual2, delta, null);
  }

  /**
   * This interface facilitates the use of {@link #expectThrows} from Java 8. It allows method
   * references to both void and non-void methods to be passed directly into expectThrows without
   * wrapping, even if they declare checked exceptions.
   *
   * <p>This interface is not meant to be implemented directly.
   */
  public interface ThrowingRunnable {
    void run() throws Throwable;
  }

  /**
   * Asserts that {@code runnable} throws an exception when invoked. If it does not, an {@link
   * AssertionError} is thrown.
   *
   * @param runnable A function that is expected to throw an exception when invoked
   * @since 6.9.5
   */
  public static void assertThrows(ThrowingRunnable runnable) {
    assertThrows(Throwable.class, runnable);
  }

  /**
   * Asserts that {@code runnable} throws an exception of type {@code throwableClass} when executed.
   * If it does not throw an exception, an {@link AssertionError} is thrown. If it throws the wrong
   * type of exception, an {@code AssertionError} is thrown describing the mismatch; the exception
   * that was actually thrown can be obtained by calling {@link AssertionError#getCause}.
   *
   * @param throwableClass the expected type of the exception
   * @param runnable A function that is expected to throw an exception when invoked
   * @since 6.9.5
   */
  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static <T extends Throwable> void assertThrows(
      Class<T> throwableClass, ThrowingRunnable runnable) {
    expectThrows(throwableClass, runnable);
  }

  /**
   * Asserts that {@code runnable} throws an exception of type {@code throwableClass} when executed
   * and returns the exception. If {@code runnable} does not throw an exception, an {@link
   * AssertionError} is thrown. If it throws the wrong type of exception, an {@code AssertionError}
   * is thrown describing the mismatch; the exception that was actually thrown can be obtained by
   * calling {@link AssertionError#getCause}.
   *
   * @param throwableClass the expected type of the exception
   * @param runnable A function that is expected to throw an exception when invoked
   * @return The exception thrown by {@code runnable}
   * @since 6.9.5
   */
  public static <T extends Throwable> T expectThrows(
      Class<T> throwableClass, ThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable t) {
      if (throwableClass.isInstance(t)) {
        return throwableClass.cast(t);
      } else {
        String mismatchMessage =
            String.format(
                "Expected %s to be thrown, but %s was thrown",
                throwableClass.getSimpleName(), t.getClass().getSimpleName());

        throw new AssertionError(mismatchMessage, t);
      }
    }
    String message =
        String.format(
            "Expected %s to be thrown, but nothing was thrown", throwableClass.getSimpleName());
    throw new AssertionError(message);
  }
}
