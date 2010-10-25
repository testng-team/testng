package org.testng.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.testng.internal.Parameters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that the current parameter is optional.  TestNG will pass
 * in a specified default value, or <code>null</code> if none is specified.
 */
@Retention(RUNTIME)
@Target({PARAMETER})
public @interface Optional {
  /** The default value to pass to this parameter.  <p>The default deserves
   * a bit of explanation.  JSR-175 (which defines annotations) says that
   * Java annotation parameters can only be ConstantExpressions, which
   * can be primitive/string literals, but not <code>null</code>.</p>
   * <p>In this case, we use this string as a substitute
   * for <code>null</code>; in practice, TestNG will pass <code>null</code>
   * to your code, and not the string "null", if you do not specify
   * a default value here in this parameter.
   */
  public String value() default Parameters.NULL_VALUE;
}
