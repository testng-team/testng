package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * List of exceptions that a test method is expected to throw.
 *
 * @deprecated Use @Test(expectedExceptions = "...")
 *
 * @author Cedric Beust, Apr 26, 2004
 *
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface ExpectedExceptions {

  /**
   * The list of exceptions expected to be thrown by this method.
   */
  public Class[] value();
}
