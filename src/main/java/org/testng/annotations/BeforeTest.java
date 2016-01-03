package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface BeforeTest {
  /**
   * Whether methods on this class/method are enabled.
   */
  public boolean enabled() default true;

  /**
   * The list of groups this class/method belongs to.
   */
  public String[] groups() default {};

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
   *  For before methods (beforeSuite, beforeTest, beforeTestClass and
   *  beforeTestMethod, but not beforeGroups):
   *  If set to true, this configuration method will be run
   *  regardless of what groups it belongs to.
   *  <br>
   * For after methods (afterSuite, afterClass, ...):
   *  If set to true, this configuration method will be run
   *  even if one or more methods invoked previously failed or
   *  was skipped.
   */
  public boolean alwaysRun() default false;

  /**
   * If true, this &#64;Configuration method will belong to groups specified in the
   * &#64;Test annotation on the class (if any).
   */
  public boolean inheritGroups() default true;

  /**
   * The description for this method.  The string used will appear in the
   * HTML report and also on standard output if verbose >= 2.
   */
  public String description() default "";

  /**
   * The maximum number of milliseconds this method should take.
   * If it hasn't returned after this time, this method will fail and
   * it will cause test methods depending on it to be skipped.
   */
  public long timeOut() default 0;
}
