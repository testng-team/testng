package org.testng.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.testng.TestNG;

//import static java.lang.annotation.ElementType.METHOD;
//import static java.lang.annotation.ElementType.TYPE;
//import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark a class or a method as part of the test.
 *
 * @author Cedric Beust, Apr 26, 2004
 * 
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE, CONSTRUCTOR})
public @interface Test {
  /**
   * The list of groups this class/method belongs to. 
   */
  public String[] groups() default {};

  /**
   * Whether methods on this class/method are enabled.
   */
  public boolean enabled() default true;
  
  /**
   * The list of variables used to fill the parameters of this method.
   * These variables must be defined in the property file.
   * 
   * @deprecated Use @Parameters
   */
  @Deprecated
  public String[] parameters() default {};

  /**
   * The list of groups this method depends on.  Every method
   * member of one of these groups is guaranteed to have been
   * invoked before this method.  Furthermore, if any of these
   * methods was not a SUCCESS, this test method will not be
   * run and will be flagged as a SKIP.  
   */
  public String[] dependsOnGroups() default {};

  /**
   * The list of methods this method depends on.  There is no guarantee
   * on the order on which the methods depended upon will be run, but you
   * are guaranteed that all these methods will be run before the test method
   * that contains this annotation is run.  Furthermore, if any of these
   * methods was not a SUCCESS, this test method will not be
   * run and will be flagged as a SKIP.  
   * 
   *  If some of these methods have been overloaded, all the overloaded
   *  versions will be run.
   */
  public String[] dependsOnMethods() default {};

  /**
   * The maximum number of milliseconds this test should take.
   * If it hasn't returned after this time, it will be marked as a FAIL.
   */
  public long timeOut() default 0;
  
  /**
   * The number of times this method should be invoked.
   */
  public int invocationCount() default 1;
  
  /**
   * The size of the thread pool for this method.  The method will be invoked
   * from multiple threads as specified by invocationCount.
   * Note:  this attribute is ignored if invocationCount is not specified
   */
  public int threadPoolSize() default 0;
  
  /**
   * The percentage of success expected from this method.
   */
  public int successPercentage() default 100;
  
  /**
   * The name of the data provider for this test method.
   * @see org.testng.annotations.DataProvider
   */
  public String dataProvider() default "";
  
  /**
   * If set to true, this test method will always be run even if it depends
   * on a method that failed.  This attribute will be ignored if this test
   * doesn't depend on any method or group.
   */
  public boolean alwaysRun() default false;

  /**
   * The description for this method.  The string used will appear in the
   * HTML report and also on standard output if verbose >= 2.
   */
  public String description() default "";
  
  /**
    * The list of exceptions that a test method is expected to throw.  If no 
    * exception or a different than one on this list is thrown, this test will be
    *  marked a failure.
   */
  public Class[] expectedExceptions() default {};
  
  /**
   * The name of the suite this test class should be placed in.  This
   * attribute is ignore if @Test is not at the class level.
   */
  public String suiteName() default TestNG.DEFAULT_SUITE_NAME;

  /**
   * The name of the test  this test class should be placed in.  This
   * attribute is ignore if @Test is not at the class level.
   */
  public String testName() default TestNG.DEFAULT_TEST_NAME;

}
