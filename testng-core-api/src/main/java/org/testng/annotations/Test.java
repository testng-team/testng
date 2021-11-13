package org.testng.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.testng.IRetryAnalyzer;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

/** Mark a class or a method as part of the test. */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
public @interface Test {
  /**
   * The list of groups this class/method belongs to.
   *
   * @return the value
   */
  String[] groups() default {};

  /**
   * Whether methods on this class/method are enabled.
   *
   * @return the value (default true)
   */
  boolean enabled() default true;

  /**
   * The list of groups this method depends on. Every method member of one of these groups is
   * guaranteed to have been invoked before this method. Furthermore, if any of these methods was
   * not a SUCCESS, this test method will not be run and will be flagged as a SKIP.
   *
   * @return the value
   */
  String[] dependsOnGroups() default {};

  /**
   * The list of methods this method depends on. There is no guarantee on the order on which the
   * methods depended upon will be run, but you are guaranteed that all these methods will be run
   * before the test method that contains this annotation is run. Furthermore, if any of these
   * methods was not a SUCCESS, this test method will not be run and will be flagged as a SKIP.
   *
   * <p>If some of these methods have been overloaded, all the overloaded versions will be run.
   *
   * @return the value
   */
  String[] dependsOnMethods() default {};

  /**
   * The maximum number of milliseconds this test should take. If it hasn't returned after this
   * time, it will be marked as a FAIL.
   *
   * @return the value (default 0)
   */
  long timeOut() default 0;

  /**
   * The maximum number of milliseconds that the total number of invocations on this test method
   * should take. This annotation will be ignored if the attribute invocationCount is not specified
   * on this method. If it hasn't returned after this time, it will be marked as a FAIL.
   *
   * @return the value (default 0)
   */
  long invocationTimeOut() default 0;

  /**
   * The number of times this method should be invoked.
   *
   * @return the value (default 1)
   */
  int invocationCount() default 1;

  /**
   * The size of the thread pool for this method. The method will be invoked from multiple threads
   * as specified by invocationCount. Note: this attribute is ignored if invocationCount is not
   * specified
   *
   * @return the value (default 0)
   */
  int threadPoolSize() default 0;

  /**
   * The percentage of success expected from this method.
   *
   * @return the value (default 100)
   */
  int successPercentage() default 100;

  /**
   * The name of the data provider for this test method.
   *
   * @see org.testng.annotations.DataProvider
   * @return the value (default empty)
   */
  String dataProvider() default "";

  /**
   * The class where to look for the data provider. If not specified, the dataprovider will be
   * looked on the class of the current test method or one of its super classes. If this attribute
   * is specified, the data provider method needs to be static on the specified class.
   *
   * @return the value (default same class)
   */
  Class<?> dataProviderClass() default Object.class;

  /**
   * If set to true, this test method will always be run even if it depends on a method that failed.
   * This attribute will be ignored if this test doesn't depend on any method or group.
   *
   * @return the value (default false)
   */
  boolean alwaysRun() default false;

  /**
   * The description for this method. The string used will appear in the HTML report and also on
   * standard output if verbose &gt; 2.
   *
   * @return the value (default empty)
   */
  String description() default "";

  /**
   * The list of exceptions that a test method is expected to throw. If no exception or a different
   * than one on this list is thrown, this test will be marked a failure.
   *
   * @return the value
   */
  Class[] expectedExceptions() default {};

  /**
   * If expectedExceptions was specified, its message must match the regular expression specified in
   * this attribute.
   *
   * @return the value (default all matches)
   */
  String expectedExceptionsMessageRegExp() default ".*";

  /**
   * The name of the suite this test class should be placed in. This attribute is ignore if @Test is
   * not at the class level.
   *
   * @return the value (default empty)
   */
  String suiteName() default "";

  /**
   * The name of the test this test class should be placed in. This attribute is ignore if @Test is
   * not at the class level.
   *
   * @return the value (default empty)
   */
  String testName() default "";

  /**
   * If set to true, all the methods on this test class are guaranteed to run in the same thread,
   * even if the tests are currently being run with parallel="true".
   *
   * <p>This attribute can only be used at the class level and will be ignored if used at the method
   * level.
   *
   * @return true if single threaded (default false)
   */
  boolean singleThreaded() default false;

  /**
   * The name of the class that should be called to test if the test should be retried.
   *
   * @return String The name of the class that will test if a test method should be retried.
   */
  Class<? extends IRetryAnalyzer> retryAnalyzer() default DisabledRetryAnalyzer.class;

  /**
   * If true and invocationCount is specified with a value &gt; 1, then all invocations after a
   * failure will be marked as a SKIP instead of a FAIL.
   *
   * @return the value (default false)
   */
  boolean skipFailedInvocations() default false;

  /**
   * If set to true, this test will run even if the methods it depends on are missing or excluded.
   *
   * @return the value (default false)
   */
  boolean ignoreMissingDependencies() default false;

  /**
   * The scheduling priority. Lower priorities will be scheduled first.
   *
   * @return the value (default 0)
   */
  int priority() default 0;

  /**
   * @return - An array of {@link CustomAttribute} that represents a set of custom attributes for a
   *     test method.
   */
  CustomAttribute[] attributes() default {};
}
