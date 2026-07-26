package org.testng.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.testng.IRetryDataProvider;

/**
 * Mark a method as supplying data for a test method.
 *
 * <p>The {@link #name() name} defaults to the name of the annotated method.
 *
 * <p>The annotated method must return any of the following:
 *
 * <ul>
 *   <li>{@code Object[][]}, {@code Iterator<Object[]>} or {@code Stream<Object[]>}, where each
 *       {@code Object[]} is assigned to the parameter list of the test method.
 *   <li>{@code Object[]}, {@code Iterator<Object>} or {@code Stream<Object>}, where each {@code
 *       Object} is assigned to the single parameter of the test method.
 * </ul>
 *
 * <p>{@code Iterator} and {@code Stream} return types are consumed lazily.
 *
 * <p><b>Note on parallel streams:</b> a returned {@code Stream} is always consumed sequentially,
 * one row at a time, via its {@link java.util.stream.Stream#iterator() iterator}. Returning a
 * parallel stream (for example one created with {@link java.util.stream.Stream#parallel()}) is
 * accepted and works correctly, but the stream's parallelism has no effect - {@code iterator()}
 * collapses any stream to a sequential traversal, so no data is produced in parallel. To run the
 * test method's invocations in parallel, use {@link #parallel()} (optionally together with the
 * {@code dataproviderthreadcount} setting) instead of {@code Stream.parallel()}.
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

  /**
   * Helps TestNG decide if it should treat data provider failures as test failures.
   *
   * @return the value
   */
  boolean propagateFailureAsTestFailure() default false;

  /**
   * @return - <code>true</code> if TestNG should use data returned by the original data provider
   *     invocation, when a test method fails and is configured to be retried.
   */
  boolean cacheDataForTestRetries() default true;

  /**
   * @return - An Class which implements {@link IRetryDataProvider} and which can be used to retry a
   *     data provider.
   */
  Class<? extends IRetryDataProvider> retryUsing() default
      IRetryDataProvider.DisableDataProviderRetries.class;
}
