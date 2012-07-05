package org.testng.internal;

/**
 * A class that contains the skipping exceptions
 * @author gjuillot
 */
public class SkippingExceptionsHolder {
  Class<?>[] skippingClasses;

  public SkippingExceptionsHolder(Class<?>[] skippingClasses) {
    this.skippingClasses = skippingClasses;
  }
}
