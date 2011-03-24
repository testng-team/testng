package org.testng.internal.junit;

public class ArrayAsserts {
  /**
   * Asserts that two object arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message. If
   * <code>expecteds</code> and <code>actuals</code> are <code>null</code>,
   * they are considered equal.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            Object array or array of arrays (multi-dimensional array) with
   *            expected values.
   * @param actuals
   *            Object array or array of arrays (multi-dimensional array) with
   *            actual values
   */
  public static void assertArrayEquals(String message, Object[] expecteds,
          Object[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
  }

  /**
   * Asserts that two object arrays are equal. If they are not, an
   * {@link AssertionError} is thrown. If <code>expected</code> and
   * <code>actual</code> are <code>null</code>, they are considered
   * equal.
   * 
   * @param expecteds
   *            Object array or array of arrays (multi-dimensional array) with
   *            expected values
   * @param actuals
   *            Object array or array of arrays (multi-dimensional array) with
   *            actual values
   */
  public static void assertArrayEquals(Object[] expecteds, Object[] actuals) {
      assertArrayEquals(null, expecteds, actuals);
  }

  /**
   * Asserts that two byte arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            byte array with expected values.
   * @param actuals
   *            byte array with actual values
   */
  public static void assertArrayEquals(String message, byte[] expecteds,
          byte[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
  }

  /**
   * Asserts that two byte arrays are equal. If they are not, an
   * {@link AssertionError} is thrown.
   * 
   * @param expecteds
   *            byte array with expected values.
   * @param actuals
   *            byte array with actual values
   */
  public static void assertArrayEquals(byte[] expecteds, byte[] actuals) {
      assertArrayEquals(null, expecteds, actuals);
  }

  /**
   * Asserts that two char arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            char array with expected values.
   * @param actuals
   *            char array with actual values
   */
  public static void assertArrayEquals(String message, char[] expecteds,
          char[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
  }

  /**
   * Asserts that two char arrays are equal. If they are not, an
   * {@link AssertionError} is thrown.
   * 
   * @param expecteds
   *            char array with expected values.
   * @param actuals
   *            char array with actual values
   */
  public static void assertArrayEquals(char[] expecteds, char[] actuals) {
      assertArrayEquals(null, expecteds, actuals);
  }

  /**
   * Asserts that two short arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            short array with expected values.
   * @param actuals
   *            short array with actual values
   */
  public static void assertArrayEquals(String message, short[] expecteds,
          short[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
  }

  /**
   * Asserts that two short arrays are equal. If they are not, an
   * {@link AssertionError} is thrown.
   * 
   * @param expecteds
   *            short array with expected values.
   * @param actuals
   *            short array with actual values
   */
  public static void assertArrayEquals(short[] expecteds, short[] actuals) {
      assertArrayEquals(null, expecteds, actuals);
  }

  /**
   * Asserts that two int arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            int array with expected values.
   * @param actuals
   *            int array with actual values
   */
  public static void assertArrayEquals(String message, int[] expecteds,
          int[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
  }

  /**
   * Asserts that two int arrays are equal. If they are not, an
   * {@link AssertionError} is thrown.
   * 
   * @param expecteds
   *            int array with expected values.
   * @param actuals
   *            int array with actual values
   */
  public static void assertArrayEquals(int[] expecteds, int[] actuals) {
      assertArrayEquals(null, expecteds, actuals);
  }

  /**
   * Asserts that two long arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            long array with expected values.
   * @param actuals
   *            long array with actual values
   */
  public static void assertArrayEquals(String message, long[] expecteds,
          long[] actuals) throws ArrayComparisonFailure {
      internalArrayEquals(message, expecteds, actuals);
  }

  /**
   * Asserts that two long arrays are equal. If they are not, an
   * {@link AssertionError} is thrown.
   * 
   * @param expecteds
   *            long array with expected values.
   * @param actuals
   *            long array with actual values
   */
  public static void assertArrayEquals(long[] expecteds, long[] actuals) {
      assertArrayEquals(null, expecteds, actuals);
  }
  
  /**
   * Asserts that two double arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            double array with expected values.
   * @param actuals
   *            double array with actual values
   */
  public static void assertArrayEquals(String message, double[] expecteds,
          double[] actuals, double delta) throws ArrayComparisonFailure {
      new InexactComparisonCriteria(delta).arrayEquals(message, expecteds, actuals);
  }

  /**
   * Asserts that two double arrays are equal. If they are not, an
   * {@link AssertionError} is thrown.
   * 
   * @param expecteds
   *            double array with expected values.
   * @param actuals
   *            double array with actual values
   */
  public static void assertArrayEquals(double[] expecteds, double[] actuals, double delta) {
      assertArrayEquals(null, expecteds, actuals, delta);
  }

  /**
   * Asserts that two float arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            float array with expected values.
   * @param actuals
   *            float array with actual values
   */
  public static void assertArrayEquals(String message, float[] expecteds,
          float[] actuals, float delta) throws ArrayComparisonFailure {
      new InexactComparisonCriteria(delta).arrayEquals(message, expecteds, actuals);
  }

  /**
   * Asserts that two float arrays are equal. If they are not, an
   * {@link AssertionError} is thrown.
   * 
   * @param expecteds
   *            float array with expected values.
   * @param actuals
   *            float array with actual values
   */
  public static void assertArrayEquals(float[] expecteds, float[] actuals, float delta) {
      assertArrayEquals(null, expecteds, actuals, delta);
  }

  /**
   * Asserts that two object arrays are equal. If they are not, an
   * {@link AssertionError} is thrown with the given message. If
   * <code>expecteds</code> and <code>actuals</code> are <code>null</code>,
   * they are considered equal.
   * 
   * @param message
   *            the identifying message for the {@link AssertionError} (<code>null</code>
   *            okay)
   * @param expecteds
   *            Object array or array of arrays (multi-dimensional array) with
   *            expected values.
   * @param actuals
   *            Object array or array of arrays (multi-dimensional array) with
   *            actual values
   */
  private static void internalArrayEquals(String message, Object expecteds,
          Object actuals) throws ArrayComparisonFailure {
      new ExactComparisonCriteria().arrayEquals(message, expecteds, actuals);
  }   

}
