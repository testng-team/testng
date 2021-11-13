package org.testng.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Mark a method as supplying data for a test method.
 *
 * <p>The {@link #name() name} defaults to the name of the annotated method.
 *
 * <p>The annotated method must return any of the following:
 *
 * <ul>
 *   <li>{@code Object[][]} or {@code Iterator<Object[]>}, where each {@code Object[]} is assigned
 *       to the parameter list of the test method.
 *   <li>{@code Object[]} or {@code Iterator<Object>}, where each {@code Object} is assigned to the
 *       single parameter of the test method.
 * </ul>
 *
 * <p>The {@link Test @Test} method that wants to receive data from this {@link DataProvider} needs
 * to use a {@link Test#dataProvider()} name equal to the name of this annotation.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
@Documented
public @interface DataProvider {

  /**
   * The name of this DataProvider.
   *
   * @return the value (default empty)
   */
  String name() default "";

  /**
   * Whether this data provider should be run in parallel.
   *
   * @return the value (default false)
   */
  boolean parallel() default false;

  /**
   * Which indices to run from this data provider, default: all.
   *
   * @return the value
   */
  int[] indices() default {};
}
