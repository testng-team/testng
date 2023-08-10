package org.testng;

import static org.testng.internal.EclipseInterface.ASSERT_EQUAL_LEFT;
import static org.testng.internal.EclipseInterface.ASSERT_LEFT2;
import static org.testng.internal.EclipseInterface.ASSERT_MIDDLE;
import static org.testng.internal.EclipseInterface.ASSERT_RIGHT;
import static org.testng.internal.EclipseInterface.ASSERT_UNEQUAL_LEFT;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.collections.Lists;

/**
 * Assertion tool class. Presents assertion methods with a more natural parameter order. The order
 * is always <B>actualValue</B>, <B>expectedValue</B> [, message].
 *
 * <p><b>Important:</b> Assertion methods comparing two values for equality, such as {@code
 * assertEquals}, are <i>only</i> intended to test equality for an actual and an (un-)expected
 * result value. They are not designed for testing whether a class correctly implements {@code
 * equals(Object)}. For example {@code assertEquals} might return fast when provided with the same
 * object as actual and expected value without calling {@code equals(Object)} at all. Such tests
 * trying to verify the {@code equals(Object)} implementation should instead be written explicitly
 * and {@link #assertTrue(boolean) assertTrue} or {@link #assertFalse(boolean) assertFalse} should
 * be used to verify the result, e.g.: {@code assertTrue(var.equals(var))}, {@code
 * assertFalse(var.equals(null))}.
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
  public static void assertTrue(boolean condition, @Nullable String message) {
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
  public static void assertFalse(boolean condition, @Nullable String message) {
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
  // See https://github.com/typetools/checker-framework/issues/2076
  public static /* @ThrowsException */ void fail(@Nullable String message) {
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
  public static void assertEquals(Object actual, Object expected, @Nullable String message) {
    if (expected != null && expected.getClass().isArray()) {
      assertArrayEquals(actual, expected, message);
      return;
    }
    assertEqualsImpl(actual, expected, message);
  }

  private static boolean areEqual(Object actual, Object expected) {
    if (expected != null && expected.getClass().isArray()) {
      return areArraysEqual(actual, expected);
    }
    return areEqualImpl(actual, expected);
  }

  /**
   * Differs from {@link #assertEquals(Object, Object, String)} by not taking arrays into special
   * consideration hence comparing them by reference. Intended to be called directly to test
   * equality of collections content.
   */
  private static void assertEqualsImpl(
      @Nullable Object actual, @Nullable Object expected, @Nullable String message) {
    boolean equal = areEqualImpl(actual, expected);
    if (!equal) {
      failNotEquals(actual, expected, message);
    }
  }

  private static void assertNotEqualsImpl(
      @Nullable Object actual, @Nullable Object expected, @Nullable String message) {
    boolean notEqual = areNotEqualImpl(actual, expected);
    if (!notEqual) {
      failEquals(actual, expected, message);
    }
  }

  private static boolean areNotEqualImpl(@Nullable Object actual, @Nullable Object expected) {
    if (expected == null) {
      return actual != null;
    }
    // expected != null && actual == null
    if (actual == null) {
      return true;
    }
    return !expected.equals(actual);
  }

  private static boolean areEqualImpl(@Nullable Object actual, @Nullable Object expected) {
    if ((expected == null) && (actual == null)) {
      return true;
    }
    // Only one of them is null
    if (expected == null || actual == null) {
      return false;
    }
    return expected.equals(actual) && actual.equals(expected);
  }

  /** returns not equal reason or null if equal */
  private static @Nullable String getArrayNotEqualReason(
      @Nullable Object actual, @Nullable Object expected) {
    if (Objects.equals(actual, expected)) {
      return null;
    }
    if (null == expected) {
      return "expected a null array, but not null found";
    }
    if (null == actual) {
      return "expected not null array, but null found";
    }
    if (!actual.getClass().isArray()) {
      return "not an array";
    }
    int expectedLength = Array.getLength(expected);
    if (expectedLength != Array.getLength(actual)) {
      return "array lengths are not the same";
    }
    for (int i = 0; i < expectedLength; i++) {
      Object _actual = Array.get(actual, i);
      Object _expected = Array.get(expected, i);
      if (!areEqual(_actual, _expected)) {
        return "(values at index " + i + " are not the same)";
      }
    }
    return null;
  }

  private static boolean areArraysEqual(Object actual, Object expected) {
    return getArrayNotEqualReason(actual, expected) == null;
  }

  private static void assertArrayEquals(
      @Nullable Object actual, @Nullable Object expected, @Nullable String message) {
    @Nullable String reason = getArrayNotEqualReason(actual, expected);
    if (null != reason) {
      failNotEquals(actual, expected, message == null ? "" : message + " (" + message + ")");
    }
  }

  private static void assertArrayNotEquals(
      @Nullable Object actual, @Nullable Object expected, @Nullable String message) {
    @Nullable String reason = getArrayNotEqualReason(actual, expected);
    if (null == reason) {
      failEquals(actual, expected, message);
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
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

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
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

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
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

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
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

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
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

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
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

    for (int i = 0; i < expected.length; i++) {
      assertEquals(
          actual[i],
          expected[i],
          String.format(
              ARRAY_MISMATCH_TEMPLATE,
              i,
              Float.toString(expected[i]),
              Float.toString(actual[i]),
              message));
    }
  }

  /**
   * Asserts that two arrays contain the equal elements concerning a delta in the same order. If
   * they do not, an AssertionError is thrown.
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
   * they do not, an AssertionError is thrown.
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
      assertEquals(
          actual[i],
          expected[i],
          delta,
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
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

    for (int i = 0; i < expected.length; i++) {
      assertEquals(
          actual[i],
          expected[i],
          String.format(
              ARRAY_MISMATCH_TEMPLATE,
              i,
              Double.toString(expected[i]),
              Double.toString(actual[i]),
              message));
    }
  }

  /**
   * Asserts that two arrays contain the equal elements concerning a delta in the same order. If
   * they do not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   */
  public static void assertEquals(double[] actual, double[] expected, double delta) {
    assertEquals(actual, expected, delta, "");
  }

  /**
   * Asserts that two arrays contain the equal elements concerning a delta in the same order. If
   * they do not, an AssertionError, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param delta the absolute tolerable difference between the actual and expected values
   * @param message the assertion error message
   */
  public static void assertEquals(
      double[] actual, double[] expected, double delta, String message) {
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

    for (int i = 0; i < expected.length; i++) {
      assertEquals(
          actual[i],
          expected[i],
          delta,
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
    if (checkRefEqualityAndLength(actual, expected, message)) {
      return;
    }

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
  public static void assertEquals(String actual, String expected, @Nullable String message) {
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

  private static boolean areEqual(double actual, double expected, double delta) {
    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if (Double.isInfinite(expected)) {
      if (!(expected == actual)) {
        return false;
      }
    } else if (Double.isNaN(expected)) {
      if (!Double.isNaN(actual)) {
        return false;
      }
    } else if (!(Math.abs(expected - actual) <= delta)) {
      return false;
    }
    return true;
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
  public static void assertEquals(
      double actual, double expected, double delta, @Nullable String message) {
    if (!areEqual(actual, expected, delta)) {
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
  public static void assertEquals(double actual, double expected, @Nullable String message) {
    if (Double.isNaN(expected)) {
      if (!Double.isNaN(actual)) {
        failNotEquals(actual, expected, message);
      }
    } else if (actual != expected) {
      failNotEquals(actual, expected, message);
    }
  }

  /**
   * Asserts that two doubles are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Double actual, double expected, String message) {
    assertEquals(actual, Double.valueOf(expected), message);
  }

  /**
   * Asserts that two doubles are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(double actual, Double expected, String message) {
    assertEquals(Double.valueOf(actual), expected, message);
  }

  /**
   * Asserts that two doubles are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Double actual, Double expected, @Nullable String message) {
    assertEquals(actual, (Object) expected, message);
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
   * Asserts that two doubles are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Double actual, double expected) {
    assertEquals(actual, Double.valueOf(expected), null);
  }

  /**
   * Asserts that two doubles are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(double actual, Double expected) {
    assertEquals(Double.valueOf(actual), expected, null);
  }

  /**
   * Asserts that two doubles are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Double actual, Double expected) {
    assertEquals(actual, expected, null);
  }

  private static boolean areEqual(float actual, float expected, float delta) {
    // handle infinity specially since subtracting to infinite values gives NaN and
    // the following test fails
    if (Float.isInfinite(expected)) {
      if (!(expected == actual)) {
        return false;
      }
    } else if (Float.isNaN(expected)) {
      if (!Float.isNaN(actual)) {
        return false;
      }
    } else if (!(Math.abs(expected - actual) <= delta)) {
      return false;
    }
    return true;
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
  public static void assertEquals(
      float actual, float expected, float delta, @Nullable String message) {
    if (!areEqual(actual, expected, delta)) {
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
  public static void assertEquals(float actual, float expected, @Nullable String message) {
    if (Float.isNaN(expected)) {
      if (!Float.isNaN(actual)) {
        failNotEquals(actual, expected, message);
      }
    } else if (actual != expected) {
      failNotEquals(actual, expected, message);
    }
  }

  /**
   * Asserts that two floats are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Float actual, float expected, String message) {
    assertEquals(actual, Float.valueOf(expected), message);
  }

  /**
   * Asserts that two floats are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(float actual, Float expected, String message) {
    assertEquals(Float.valueOf(actual), expected, message);
  }

  /**
   * Asserts that two floats are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Float actual, Float expected, @Nullable String message) {
    assertEquals(actual, (Object) expected, message);
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
   * Asserts that two floats are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Float actual, float expected) {
    assertEquals(actual, Float.valueOf(expected), null);
  }

  /**
   * Asserts that two floats are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(float actual, Float expected) {
    assertEquals(Float.valueOf(actual), expected, null);
  }

  /**
   * Asserts that two floats are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Float actual, Float expected) {
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
  public static void assertEquals(long actual, long expected, @Nullable String message) {
    assertEquals(Long.valueOf(actual), Long.valueOf(expected), message);
  }

  /**
   * Asserts that two longs are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Long actual, long expected, String message) {
    assertEquals(actual, Long.valueOf(expected), message);
  }

  /**
   * Asserts that two longs are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Long actual, Long expected, @Nullable String message) {
    assertEquals(actual, (Object) expected, message);
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
   * Asserts that two longs are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Long actual, long expected) {
    assertEquals(actual, Long.valueOf(expected), null);
  }

  /**
   * Asserts that two longs are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(long actual, Long expected) {
    assertEquals(Long.valueOf(actual), expected, null);
  }

  /**
   * Asserts that two longs are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Long actual, Long expected) {
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
  public static void assertEquals(boolean actual, boolean expected, @Nullable String message) {
    assertEquals(Boolean.valueOf(actual), Boolean.valueOf(expected), message);
  }

  /**
   * Asserts that two booleans are equal. If they are not, an AssertionError, with the given
   * message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Boolean actual, boolean expected, String message) {
    assertEquals(actual, Boolean.valueOf(expected), message);
  }

  /**
   * Asserts that two booleans are equal. If they are not, an AssertionError, with the given
   * message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(boolean actual, Boolean expected, String message) {
    assertEquals(Boolean.valueOf(actual), expected, message);
  }

  /**
   * Asserts that two booleans are equal. If they are not, an AssertionError, with the given
   * message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Boolean actual, Boolean expected, @Nullable String message) {
    assertEquals(actual, (Object) expected, message);
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
   * Asserts that two booleans are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Boolean actual, boolean expected) {
    assertEquals(actual, Boolean.valueOf(expected), null);
  }

  /**
   * Asserts that two booleans are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Boolean actual, Boolean expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two booleans are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(boolean actual, Boolean expected) {
    assertEquals(Boolean.valueOf(actual), expected, null);
  }

  /**
   * Asserts that two bytes are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(byte actual, byte expected, @Nullable String message) {
    assertEquals(Byte.valueOf(actual), Byte.valueOf(expected), message);
  }

  /**
   * Asserts that two bytes are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Byte actual, byte expected, String message) {
    assertEquals(actual, Byte.valueOf(expected), message);
  }

  /**
   * Asserts that two bytes are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(byte actual, Byte expected, String message) {
    assertEquals(Byte.valueOf(actual), expected, message);
  }

  /**
   * Asserts that two bytes are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Byte actual, Byte expected, @Nullable String message) {
    assertEquals(actual, (Object) expected, message);
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
   * Asserts that two bytes are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Byte actual, byte expected) {
    assertEquals(actual, Byte.valueOf(expected), null);
  }

  /**
   * Asserts that two bytes are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Byte actual, Byte expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two bytes are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(byte actual, Byte expected) {
    assertEquals(Byte.valueOf(actual), expected, null);
  }

  /**
   * Asserts that two chars are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(char actual, char expected, @Nullable String message) {
    assertEquals(Character.valueOf(actual), Character.valueOf(expected), message);
  }

  /**
   * Asserts that two chars are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Character actual, char expected, String message) {
    assertEquals(actual, Character.valueOf(expected), message);
  }

  /**
   * Asserts that two chars are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(char actual, Character expected, String message) {
    assertEquals(Character.valueOf(actual), expected, message);
  }

  /**
   * Asserts that two chars are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Character actual, Character expected, @Nullable String message) {
    assertEquals(actual, (Object) expected, message);
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
   * Asserts that two chars are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Character actual, char expected) {
    assertEquals(actual, Character.valueOf(expected), null);
  }

  /**
   * Asserts that two chars are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(char actual, Character expected) {
    assertEquals(Character.valueOf(actual), expected, null);
  }

  /**
   * Asserts that two chars are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Character actual, Character expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two shorts are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(short actual, short expected, @Nullable String message) {
    assertEquals(Short.valueOf(actual), Short.valueOf(expected), message);
  }

  /**
   * Asserts that two shorts are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Short actual, short expected, String message) {
    assertEquals(actual, Short.valueOf(expected), message);
  }

  /**
   * Asserts that two shorts are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(short actual, Short expected, String message) {
    assertEquals(Short.valueOf(actual), expected, message);
  }

  /**
   * Asserts that two shorts are equal. If they are not, an AssertionError, with the given message,
   * is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Short actual, Short expected, @Nullable String message) {
    assertEquals(actual, (Object) expected, message);
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
   * Asserts that two shorts are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Short actual, short expected) {
    assertEquals(actual, Short.valueOf(expected), null);
  }

  /**
   * Asserts that two shorts are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(short actual, Short expected) {
    assertEquals(Short.valueOf(actual), expected, null);
  }

  /**
   * Asserts that two shorts are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Short actual, Short expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two ints are equal. If they are not, an AssertionError, with the given message, is
   * thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(int actual, int expected, @Nullable String message) {
    assertEquals(Integer.valueOf(actual), Integer.valueOf(expected), message);
  }

  /**
   * Asserts that two ints are equal. If they are not, an AssertionError, with the given message, is
   * thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Integer actual, int expected, String message) {
    assertEquals(actual, Integer.valueOf(expected), message);
  }

  /**
   * Asserts that two ints are equal. If they are not, an AssertionError, with the given message, is
   * thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(int actual, Integer expected, String message) {
    assertEquals(Integer.valueOf(actual), expected, message);
  }

  /**
   * Asserts that two ints are equal. If they are not, an AssertionError, with the given message, is
   * thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEquals(Integer actual, Integer expected, @Nullable String message) {
    assertEquals(actual, (Object) expected, message);
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
   * Asserts that two ints are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Integer actual, int expected) {
    assertEquals(actual, Integer.valueOf(expected), null);
  }

  /**
   * Asserts that two ints are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(int actual, Integer expected) {
    assertEquals(Integer.valueOf(actual), expected, null);
  }

  /**
   * Asserts that two ints are equal. If they are not, an AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(Integer actual, Integer expected) {
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
   * Asserts that an object isn't null. If it is, an AssertionError, with the given message, is
   * thrown.
   *
   * @param object the assertion object
   * @param message the assertion error message
   */
  public static void assertNotNull(Object object, @Nullable String message) {
    if (object == null) {
      String formatted = "";
      if (message != null) {
        formatted = message + " ";
      }
      fail(formatted + "expected object to not be null");
    }
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
   * Asserts that an object is null. If it is not, an AssertionError, with the given message, is
   * thrown.
   *
   * @param object the assertion object
   * @param message the assertion error message
   */
  public static void assertNull(@Nullable Object object, @Nullable String message) {
    if (object != null) {
      failNotSame(object, null, message);
    }
  }

  /**
   * Asserts that two objects refer to the same object. If they do not, an AssertionError, with the
   * given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertSame(Object actual, Object expected, @Nullable String message) {
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
  public static void assertNotSame(Object actual, Object expected, @Nullable String message) {
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

  private static void failSame(Object actual, Object expected, @Nullable String message) {
    String formatted = "";
    if (message != null) {
      formatted = message + " ";
    }
    fail(formatted + ASSERT_LEFT2 + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT);
  }

  private static void failNotSame(
      @Nullable Object actual, @Nullable Object expected, @Nullable String message) {
    String formatted = "";
    if (message != null) {
      formatted = message + " ";
    }
    fail(formatted + ASSERT_EQUAL_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT);
  }

  private static void failNotEquals(
      @Nullable Object actual, @Nullable Object expected, @Nullable String message) {
    fail(format(actual, expected, message, true));
  }

  private static void failEquals(
      @Nullable Object actual, @Nullable Object expected, @Nullable String message) {
    fail(format(actual, expected, message, false));
  }

  static String format(
      @Nullable Object actual,
      @Nullable Object expected,
      @Nullable String message,
      boolean isAssertEquals) {
    String formatted = "";
    if (null != message) {
      formatted = message + " ";
    }
    if (isAssertEquals) {
      // if equality is asserted but inequality is found
      return formatted + ASSERT_EQUAL_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT;
    }
    // if inequality is asserted but equality is found
    return formatted + ASSERT_UNEQUAL_LEFT + expected + ASSERT_MIDDLE + actual + ASSERT_RIGHT;
  }

  /**
   * Asserts that two collections contain the same elements in the same order. If they do not, an
   * AssertionError is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEquals(
      @Nullable Collection<@Nullable ?> actual, @Nullable Collection<@Nullable ?> expected) {
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
  public static void assertEquals(
      @Nullable Collection<@Nullable ?> actual,
      @Nullable Collection<@Nullable ?> expected,
      @Nullable String message) {
    if (actual == expected) { // We don't use Objects.equals here because order is checked
      return;
    }

    if (actual == null || expected == null) {
      if (message != null) {
        fail(message);
      } else {
        fail("Collections not equal: expected: " + expected + " and actual: " + actual);
      }
      return;
    }

    assertEquals(
        actual.size(),
        expected.size(),
        (message == null ? "" : message + ": ") + "lists don't have the same size");

    Iterator<@Nullable ?> actIt = actual.iterator();
    Iterator<@Nullable ?> expIt = expected.iterator();
    int i = -1;
    while (actIt.hasNext() && expIt.hasNext()) {
      i++;
      @Nullable Object e = expIt.next();
      @Nullable Object a = actIt.next();
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
  public static void assertEquals(@Nullable Iterator<?> actual, @Nullable Iterator<?> expected) {
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
  public static void assertEquals(
      @Nullable Iterator<@Nullable ?> actual,
      @Nullable Iterator<@Nullable ?> expected,
      @Nullable String message) {
    if (actual == expected) { // We don't use Objects.equals here because order is checked
      return;
    }
    if (actual == null || expected == null) {
      String msg =
          message != null
              ? message
              : "Iterators not equal: expected: " + expected + " and actual: " + actual;
      fail(msg);
      return;
    }

    int i = -1;
    while (actual.hasNext() && expected.hasNext()) {

      i++;
      @Nullable Object e = expected.next();
      @Nullable Object a = actual.next();
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
  public static void assertEquals(
      @Nullable Iterable<@Nullable ?> actual, @Nullable Iterable<@Nullable ?> expected) {
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
  public static void assertEquals(
      @Nullable Iterable<@Nullable ?> actual,
      @Nullable Iterable<@Nullable ?> expected,
      @Nullable String message) {
    if (actual == expected) { // We don't use Objects.equals here because order is checked
      return;
    }

    if (actual == null || expected == null) {
      if (message != null) {
        fail(message);
      } else {
        fail("Iterables not equal: expected: " + expected + " and actual: " + actual);
      }
      return;
    }

    Iterator<@Nullable ?> actIt = actual.iterator();
    Iterator<@Nullable ?> expIt = expected.iterator();

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
  public static void assertEquals(
      @Nullable Object @Nullable [] actual,
      @Nullable Object @Nullable [] expected,
      @Nullable String message) {
    if (Arrays.equals(actual, expected)) {
      return;
    }

    if (actual == null || expected == null) {
      if (message != null) {
        fail(message);
      } else {
        fail(
            "Arrays not equal: expected: "
                + Arrays.toString(expected)
                + " and actual: "
                + Arrays.toString(actual));
      }
      return;
    }
    if (actual.length != expected.length) {
      failAssertNoEqual(
          "Arrays do not have the same size:" + actual.length + " != " + expected.length, message);
    }

    for (int i = 0; i < expected.length; i++) {
      @Nullable Object e = expected[i];
      @Nullable Object a = actual[i];
      String explanation = "Arrays differ at element [" + i + "]: " + e + " != " + a;
      String errorMessage = message == null ? explanation : message + ": " + explanation;
      if (a == null && e == null) {
        continue;
      }
      if (a == null || e == null) {
        failNotEquals(a, e, message);
      }
      // Compare by value for multi-dimensional array.
      if (e.getClass().isArray()) {
        assertEquals(a, e, errorMessage);
      } else {
        assertEqualsImpl(a, e, errorMessage);
      }
    }
  }

  /**
   * Asserts that two arrays contain the same elements in no particular order. If they do not, an
   * {@code AssertionError}, with the given message, is thrown. The arrays are not compared
   * 'deeply', that means, if the elements are arrays as well their {@code equals} method is used
   * which only checks for reference equality.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEqualsNoOrder(
      @Nullable Object @Nullable [] actual,
      @Nullable Object @Nullable [] expected,
      @Nullable String message) {
    if (actual == expected) { // We don't use Arrays.equals here because order is not checked
      return;
    }

    if (actual == null || expected == null) {
      failAssertNoEqual(
          "Arrays not equal: expected: "
              + Arrays.toString(expected)
              + " and actual: "
              + Arrays.toString(actual),
          message);
      return;
    }

    if (actual.length != expected.length) {
      failAssertNoEqual(
          "Arrays do not have the same size:" + actual.length + " != " + expected.length, message);
      return;
    }

    List<@Nullable Object> actualCollection = Lists.newArrayList(actual);
    for (Object o : expected) {
      actualCollection.remove(o);
    }
    if (!actualCollection.isEmpty()) {
      failAssertNoEqual(
          "Arrays not equal: expected: "
              + Arrays.toString(expected)
              + " and actual: "
              + Arrays.toString(actual),
          message);
    }
  }

  /**
   * Asserts that two collection contain the same elements in no particular order. If they do not,
   * an {@code AssertionError}, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEqualsNoOrder(
      Collection<@Nullable ?> actual, Collection<@Nullable ?> expected, @Nullable String message) {
    if (actual.size() != expected.size()) {
      failAssertNoEqual(
          "Collections do not have the same size: " + actual.size() + " != " + expected.size(),
          message);
      return;
    }

    List<?> actualCollection = Lists.newArrayList(actual);
    actualCollection.removeAll(expected);
    if (!actualCollection.isEmpty()) {
      failAssertNoEqual(
          "Collections not equal: expected: " + expected + " and actual: " + actual, message);
    }
  }

  /**
   * Asserts that two iterators contain the same elements in no particular order. If they do not, an
   * {@code AssertionError}, with the given message, is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertEqualsNoOrder(
      Iterator<@Nullable ?> actual, Iterator<@Nullable ?> expected, @Nullable String message) {
    List<?> actualCollection = Lists.newArrayList(actual);
    List<?> expectedCollection = Lists.newArrayList(expected);

    if (actualCollection.size() != expectedCollection.size()) {
      failAssertNoEqual(
          "Iterators do not have the same size: "
              + +actualCollection.size()
              + " != "
              + expectedCollection.size(),
          message);
      return;
    }

    actualCollection.removeAll(expectedCollection);
    if (!actualCollection.isEmpty()) {
      failAssertNoEqual(
          "Iterators not equal: expected: "
              + toString(expected)
              + " and actual: "
              + toString(actual),
          message);
    }
  }

  private static @Nullable String toString(@Nullable Iterator<?> iterator) {
    if (iterator == null) {
      return null;
    }
    Iterable<Object> iterable = () -> (Iterator<Object>) iterator;
    return StreamSupport.stream(iterable.spliterator(), false)
        .map(Object::toString)
        .collect(Collectors.joining(", "));
  }

  private static void failAssertNoEqual(String defaultMessage, @Nullable String message) {
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
  public static void assertEquals(
      @Nullable Object @Nullable [] actual, @Nullable Object @Nullable [] expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two arrays contain the same elements in no particular order. If they do not, an
   * {@code AssertionError} is thrown. The arrays are not compared 'deeply', that means, if the
   * elements are arrays as well their {@code equals} method is used which only checks for reference
   * equality.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEqualsNoOrder(
      @Nullable Object @Nullable [] actual, @Nullable Object @Nullable [] expected) {
    assertEqualsNoOrder(actual, expected, null);
  }

  /**
   * Asserts that two collection contain the same elements in no particular order. If they do not,
   * an {@code AssertionError} is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEqualsNoOrder(
      Collection<@Nullable ?> actual, Collection<@Nullable ?> expected) {
    assertEqualsNoOrder(actual, expected, null);
  }

  /**
   * Asserts that two iterators contain the same elements in no particular order. If they do not, an
   * {@code AssertionError} is thrown.
   *
   * @param actual the actual value
   * @param expected the expected value
   */
  public static void assertEqualsNoOrder(
      Iterator<@Nullable ?> actual, Iterator<@Nullable ?> expected) {
    assertEqualsNoOrder(actual, expected, null);
  }

  /**
   * Asserts that two sets are equal.
   *
   * @param actual The actual value
   * @param expected The expected value
   */
  public static void assertEquals(@Nullable Set<?> actual, @Nullable Set<?> expected) {
    assertEquals(actual, expected, null);
  }

  /** returns not equal reason or null if equal */
  private static @Nullable String getNotEqualReason(
      @Nullable Collection<?> actual, @Nullable Collection<?> expected) {
    if (actual == expected) { // We don't use Arrays.equals here because order is checked
      return null;
    }

    if (actual == null || expected == null) {
      // Keep the back compatible
      return "Collections not equal: expected: " + expected + " and actual: " + actual;
    }

    if (!Objects.equals(actual, expected)) {
      return "Collections differ: expected " + expected + " but got " + actual;
    }

    return getNotEqualReason(actual.iterator(), expected.iterator());
  }

  private static @Nullable String getNotEqualReason(
      @Nullable Iterator<@Nullable ?> actual, @Nullable Iterator<@Nullable ?> expected) {
    if (actual == expected) { // We don't use Arrays.equals here because order is checked
      return null;
    }

    if (actual == null || expected == null) {
      // Keep the back compatible
      return "Iterators not equal: expected: "
          + toString(expected)
          + " and actual: "
          + toString(actual);
    }

    while (actual.hasNext() && expected.hasNext()) {
      if (!Objects.equals(actual.next(), expected.next())) {
        return "Iterators not same element order: expected: "
            + toString(expected)
            + " and actual: "
            + toString(actual);
      }
    }
    return null;
  }

  private static @Nullable String getNotEqualReason(
      @Nullable Set<@Nullable ?> actual, @Nullable Set<@Nullable ?> expected) {
    if (actual == expected) {
      return null;
    }

    if (actual == null || expected == null) {
      // Keep the back compatible
      return "Sets not equal: expected: " + expected + " and actual: " + actual;
    }

    if (!Objects.equals(actual, expected)) {
      return "Sets differ: expected " + expected + " but got " + actual;
    }
    return null;
  }

  /**
   * Assert set equals
   *
   * @param actual The actual value
   * @param expected The expected value
   * @param message The message
   */
  public static void assertEquals(
      @Nullable Set<@Nullable ?> actual,
      @Nullable Set<@Nullable ?> expected,
      @Nullable String message) {
    @Nullable String notEqualReason = getNotEqualReason(actual, expected);
    if (null != notEqualReason) {
      // Keep the back compatible
      if (message == null) {
        fail(notEqualReason);
      } else {
        fail(message);
      }
    }
  }

  /** returns not equal deep reason or null if equal */
  private static @Nullable String getNotEqualDeepReason(
      @Nullable Set<@Nullable ?> actual, @Nullable Set<@Nullable ?> expected) {
    if (Objects.equals(actual, expected)) {
      return null;
    }

    if (actual == null || expected == null) {
      // Keep the back compatible
      return "Sets not equal: expected: " + expected + " and actual: " + actual;
    }

    if (expected.size() != actual.size()) {
      return "Sets not equal: expected: " + expected + " and actual: " + actual;
    }

    Iterator<?> actualIterator = actual.iterator();
    Iterator<?> expectedIterator = expected.iterator();
    while (expectedIterator.hasNext()) {
      Object expectedValue = expectedIterator.next();
      Object value = actualIterator.next();
      if (expectedValue.getClass().isArray()) {
        @Nullable String arrayNotEqualReason = getArrayNotEqualReason(value, expectedValue);
        if (arrayNotEqualReason != null) {
          return arrayNotEqualReason;
        }
      } else {
        if (!areEqualImpl(value, expected)) {
          return "Sets not equal: expected: " + expectedValue + " and actual: " + value;
        }
      }
    }
    return null;
  }

  public static void assertEqualsDeep(
      @Nullable Set<@Nullable ?> actual,
      @Nullable Set<@Nullable ?> expected,
      @Nullable String message) {
    @Nullable String notEqualDeepReason = getNotEqualDeepReason(actual, expected);
    if (notEqualDeepReason != null) {
      if (message == null) {
        fail(notEqualDeepReason);
      } else {
        fail(message);
      }
    }
  }

  public static void assertEquals(@Nullable Map<?, ?> actual, @Nullable Map<?, ?> expected) {
    assertEquals(actual, expected, null);
  }

  private static @Nullable String getNotEqualReason(
      @Nullable Map<?, ?> actual, @Nullable Map<?, ?> expected) {
    if (Objects.equals(actual, expected)) {
      return null;
    }

    if (actual == null || expected == null) {
      return "Maps not equal: expected: " + expected + " and actual: " + actual;
    }

    if (actual.size() != expected.size()) {
      return "Maps do not have the same size:" + actual.size() + " != " + expected.size();
    }

    Set<?> entrySet = actual.entrySet();
    for (Object anEntrySet : entrySet) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) anEntrySet;
      Object key = entry.getKey();
      Object value = entry.getValue();
      Object expectedValue = expected.get(key);
      if (!areEqualImpl(value, expectedValue)) {
        String assertMessage =
            "Maps do not match for key:" + key + " actual:" + value + " expected:" + expectedValue;
        return assertMessage;
      }
      if (value == null && !expected.containsKey(key)) {
        return "Maps do not match for key:" + key + " actual: null but not present in expected";
      }
    }
    return null;
  }

  /**
   * Asserts that two maps are equal.
   *
   * @param actual The actual value
   * @param expected The expected value
   * @param message The message
   */
  public static void assertEquals(
      @Nullable Map<?, ?> actual, @Nullable Map<?, ?> expected, @Nullable String message) {
    @Nullable String notEqualReason = getNotEqualReason(actual, expected);
    if (notEqualReason != null) {
      if (message == null) {
        fail(notEqualReason);
      } else {
        fail(message);
      }
    }
  }

  public static void assertEqualsDeep(@Nullable Map<?, ?> actual, @Nullable Map<?, ?> expected) {
    assertEqualsDeep(actual, expected, null);
  }

  /** returns not equal deep reason or null if equal */
  private static @Nullable String getNotEqualDeepReason(
      @Nullable Map<?, ?> actual, @Nullable Map<?, ?> expected) {
    if (Objects.equals(actual, expected)) {
      return null;
    }

    if (actual == null || expected == null) {
      return "Maps not equal: expected: " + expected + " and actual: " + actual;
    }

    if (actual.size() != expected.size()) {
      return "Maps do not have the same size:" + actual.size() + " != " + expected.size();
    }

    Set<?> entrySet = actual.entrySet();
    for (Object anEntrySet : entrySet) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) anEntrySet;
      Object key = entry.getKey();
      Object value = entry.getValue();
      Object expectedValue = expected.get(key);
      String assertMessage =
          "Maps do not match for key:" + key + " actual:" + value + " expected:" + expectedValue;
      if (expectedValue.getClass().isArray()) {
        if (!areArraysEqual(value, expectedValue)) {
          return assertMessage;
        }
      } else {
        if (!areEqualImpl(value, expectedValue)) {
          return assertMessage;
        }
      }
    }
    return null;
  }

  public static void assertEqualsDeep(
      @Nullable Map<?, ?> actual, @Nullable Map<?, ?> expected, @Nullable String message) {
    @Nullable String notEqualDeepReason = getNotEqualDeepReason(actual, expected);
    if (notEqualDeepReason != null) {
      if (message == null) {
        fail(notEqualDeepReason);
      } else {
        fail(message);
      }
    }
  }

  /////
  // assertNotEquals
  //

  public static void assertNotEquals(
      @Nullable Object actual, @Nullable Object expected, @Nullable String message) {
    if (expected != null && expected.getClass().isArray()) {
      assertArrayNotEquals(actual, expected, message);
      return;
    }
    assertNotEqualsImpl(actual, expected, message);
  }

  public static void assertNotEquals(Object[] actual, Object[] expected, String message) {
    assertArrayNotEquals(actual, expected, message);
  }

  public static void assertNotEquals(Iterator<?> actual, Iterator<?> expected, String message) {
    String notEqualReason = getNotEqualReason(actual, expected);
    if (notEqualReason == null) {
      Assert.fail(format(actual, expected, message, false));
    }
  }

  public static void assertNotEquals(Collection<?> actual, Collection<?> expected, String message) {
    String notEqualReason = getNotEqualReason(actual, expected);
    if (notEqualReason == null) {
      Assert.fail(format(actual, expected, message, false));
    }
  }

  public static void assertNotEquals(Object actual, Object expected) {
    assertNotEquals(actual, expected, null);
  }

  public static void assertNotEquals(Collection<?> actual, Collection<?> expected) {
    assertNotEquals(actual, expected, null);
  }

  public static void assertNotEquals(Iterator<?> actual, Iterator<?> expected) {
    assertNotEquals(actual, expected, null);
  }

  static void assertNotEquals(String actual, String expected, String message) {
    assertNotEquals((Object) actual, (Object) expected, message);
  }

  static void assertNotEquals(String actual, String expectec) {
    assertNotEquals(actual, expectec, null);
  }

  static void assertNotEquals(long actual, long expected, String message) {
    assertNotEquals(Long.valueOf(actual), Long.valueOf(expected), message);
  }

  static void assertNotEquals(long actual, long expected) {
    assertNotEquals(actual, expected, null);
  }

  static void assertNotEquals(boolean actual, boolean expected, String message) {
    assertNotEquals(Boolean.valueOf(actual), Boolean.valueOf(expected), message);
  }

  static void assertNotEquals(boolean actual, boolean expected) {
    assertNotEquals(actual, expected, null);
  }

  static void assertNotEquals(byte actual, byte expected, String message) {
    assertNotEquals(Byte.valueOf(actual), Byte.valueOf(expected), message);
  }

  static void assertNotEquals(byte actual, byte expected) {
    assertNotEquals(actual, expected, null);
  }

  static void assertNotEquals(char actual, char expected, String message) {
    assertNotEquals(Character.valueOf(actual), Character.valueOf(expected), message);
  }

  static void assertNotEquals(char actual, char expected) {
    assertNotEquals(actual, expected, null);
  }

  static void assertNotEquals(short actual, short expected, String message) {
    assertNotEquals(Short.valueOf(actual), Short.valueOf(expected), message);
  }

  static void assertNotEquals(short actual, short expected) {
    assertNotEquals(actual, expected, null);
  }

  static void assertNotEquals(int actual, int expected, String message) {
    assertNotEquals(Integer.valueOf(actual), Integer.valueOf(expected), message);
  }

  static void assertNotEquals(int actual, int expected) {
    assertNotEquals(actual, expected, null);
  }

  public static void assertNotEquals(float actual, float expected, float delta, String message) {
    if (areEqual(actual, expected, delta)) {
      Assert.fail(format(actual, expected, message, false));
    }
  }

  public static void assertNotEquals(float actual, float expected, float delta) {
    assertNotEquals(actual, expected, delta, null);
  }

  public static void assertNotEquals(double actual, double expected, double delta, String message) {
    if (areEqual(actual, expected, delta)) {
      Assert.fail(format(actual, expected, message, false));
    }
  }

  public static void assertNotEquals(Set<?> actual, Set<?> expected) {
    assertNotEquals(actual, expected, null);
  }

  public static void assertNotEquals(Set<?> actual, Set<?> expected, String message) {
    String notEqualReason = getNotEqualReason(actual, expected);
    if (notEqualReason == null) {
      Assert.fail(format(actual, expected, message, false));
    }
  }

  public static void assertNotEqualsDeep(Set<?> actual, Set<?> expected) {
    assertNotEqualsDeep(actual, expected, null);
  }

  public static void assertNotEqualsDeep(Set<?> actual, Set<?> expected, String message) {
    String notEqualDeepReason = getNotEqualDeepReason(actual, expected);
    if (notEqualDeepReason == null) {
      Assert.fail(format(actual, expected, message, false));
    }
  }

  public static void assertNotEquals(Map<?, ?> actual, Map<?, ?> expected) {
    assertNotEquals(actual, expected, null);
  }

  public static void assertNotEquals(Map<?, ?> actual, Map<?, ?> expected, String message) {
    String notEqualReason = getNotEqualReason(actual, expected);
    if (notEqualReason == null) {
      Assert.fail(format(actual, expected, message, false));
    }
  }

  public static void assertNotEqualsDeep(Map<?, ?> actual, Map<?, ?> expected) {
    assertNotEqualsDeep(actual, expected, null);
  }

  public static void assertNotEqualsDeep(Map<?, ?> actual, Map<?, ?> expected, String message) {
    String notEqualDeepReason = getNotEqualDeepReason(actual, expected);
    if (notEqualDeepReason == null) {
      Assert.fail(format(actual, expected, message, false));
    }
  }

  public static void assertNotEquals(double actual, double expected, double delta) {
    assertNotEquals(actual, expected, delta, null);
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
   * @param <T> the expected type of the exception
   * @param runnable A function that is expected to throw an exception when invoked
   * @since 6.9.5
   */
  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static <T extends Throwable> void assertThrows(
      Class<T> throwableClass, ThrowingRunnable runnable) {
    expectThrows(throwableClass, runnable);
  }

  /**
   * Asserts that {@code runnable} throws an exception of type {@code throwableClass} when executed.
   * If it does not throw an exception, an {@link AssertionError} is thrown. If it throws the wrong
   * type of exception, an {@code AssertionError} is thrown describing the mismatch; the exception
   * that was actually thrown can be obtained by calling {@link AssertionError#getCause}.
   *
   * @param message fail message
   * @param throwableClass the expected type of the exception
   * @param <T> the expected type of the exception
   * @param runnable A function that is expected to throw an exception when invoked
   */
  public static <T extends Throwable> void assertThrows(
      String message, Class<T> throwableClass, ThrowingRunnable runnable) {
    expectThrows(message, throwableClass, runnable);
  }

  /**
   * Asserts that {@code runnable} throws an exception of type {@code throwableClass} when executed
   * and returns the exception. If {@code runnable} does not throw an exception, an {@link
   * AssertionError} is thrown. If it throws the wrong type of exception, an {@code AssertionError}
   * is thrown describing the mismatch; the exception that was actually thrown can be obtained by
   * calling {@link AssertionError#getCause}.
   *
   * @param throwableClass the expected type of the exception
   * @param <T> the expected type of the exception
   * @param runnable A function that is expected to throw an exception when invoked
   * @return The exception thrown by {@code runnable}
   * @since 6.9.5
   */
  public static <T extends Throwable> T expectThrows(
      Class<T> throwableClass, ThrowingRunnable runnable) {
    return expectThrows(null, throwableClass, runnable);
  }

  /**
   * Asserts that {@code runnable} throws an exception of type {@code throwableClass} when executed
   * and returns the exception. If {@code runnable} does not throw an exception, an {@link
   * AssertionError} is thrown. If it throws the wrong type of exception, an {@code AssertionError}
   * is thrown describing the mismatch; the exception that was actually thrown can be obtained by
   * calling {@link AssertionError#getCause}.
   *
   * @param message fail message
   * @param throwableClass the expected type of the exception
   * @param <T> the expected type of the exception
   * @param runnable A function that is expected to throw an exception when invoked
   * @return The exception thrown by {@code runnable}
   */
  public static <T extends Throwable> T expectThrows(
      String message, Class<T> throwableClass, ThrowingRunnable runnable) {
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

        throw new AssertionError(message != null ? message : mismatchMessage, t);
      }
    }
    String nothingThrownMessage =
        String.format(
            "Expected %s to be thrown, but nothing was thrown", throwableClass.getSimpleName());
    throw new AssertionError(message != null ? message : nothingThrownMessage);
  }

  /**
   * Asserts that List contains object. Uses java.util.List#contains method, be sure that equals and
   * hashCode of tested object are overridden.
   *
   * @param list the list to search in
   * @param object the object to search in list
   * @param message the fail message
   * @param <T> type of object
   */
  public static <T> void assertListContainsObject(List<T> list, T object, String message) {
    assertTrue(list.contains(object), "List NOT contains: [" + message + "]");
  }

  /**
   * Asserts that List not contains object. Opposite to assertListContainsObject() Uses
   * java.util.List#contains method, be sure that equals and hashCode of tested object are
   * overridden.
   *
   * @param list the list to search in
   * @param object to search in list
   * @param message the fail message
   * @param <T> type of object
   */
  public static <T> void assertListNotContainsObject(List<T> list, T object, String message) {
    assertFalse(list.contains(object), "List contains: [" + message + "]");
  }

  /**
   * Asserts that List contains object by specific Predicate. Uses
   * java.util.stream.Stream#anyMatch(java.util.function.Predicate) method.
   *
   * @param list the list to search in
   * @param predicate the Predicate to match object
   * @param message the fail message
   * @param <T> type of object
   */
  public static <T> void assertListContains(List<T> list, Predicate<T> predicate, String message) {
    assertTrue(list.stream().anyMatch(predicate), "List NOT contains: [" + message + "]");
  }

  /**
   * Asserts that List not contains object by specific Predicate. Opposite to assertListContains()
   * method. Uses java.util.stream.Stream#noneMatch(java.util.function.Predicate) method.
   *
   * @param list the list to search in
   * @param predicate the Predicate to match object
   * @param message the fail message
   * @param <T> type of object
   */
  public static <T> void assertListNotContains(
      List<T> list, Predicate<T> predicate, String message) {
    assertTrue(list.stream().noneMatch(predicate), "List contains: [" + message + "]");
  }
}
