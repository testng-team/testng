package org.testng.internal;

/**
 * Utility class for defensive argument checking.
 */
public final class Defense {

  // defeat instantiation
  private Defense() {}

  /**
   * Asserts that the passed value is not null and throws an exception if it is.
   *
   * @param value
   *          The value to check for null.
   * @param name
   *          The descriptive name to reference for the value when throwing an exception.
   * @throws IllegalArgumentException
   *          If the passed in value is null. 
   */
  public static void notNull(Object value, String name) {
    if (value == null) {

      throw new IllegalArgumentException("Parameter " + name + " can't be null.");
    }
  }
}
