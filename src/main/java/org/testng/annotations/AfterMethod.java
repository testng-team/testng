package org.testng.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
@Documented
public @interface AfterMethod {
  /** Whether methods on this class/method are enabled. */
  boolean enabled() default true;

  /**
   * The list of groups this class/method belongs to. Note that even if the test method that was
   * invoked belongs to a different group, all @AfterMethod methods will be invoked after it as long
   * as they belong to groups that were selected to run at all. See {@link #onlyForGroups()} to
   * select test method groups which this method will be invoked after.
   */
  String[] groups() default {};

  /**
   * The list of groups this method depends on. Every method member of one of these groups is
   * guaranteed to have been invoked before this method. Furthermore, if any of these methods was
   * not a SUCCESS, this test method will not be run and will be flagged as a SKIP.
   */
  String[] dependsOnGroups() default {};

  /**
   * The list of methods this method depends on. There is no guarantee on the order on which the
   * methods depended upon will be run, but you are guaranteed that all these methods will be run
   * before the test method that contains this annotation is run. Furthermore, if any of these
   * methods was not a SUCCESS, this test method will not be run and will be flagged as a SKIP.
   *
   * <p>If some of these methods have been overloaded, all the overloaded versions will be run.
   */
  String[] dependsOnMethods() default {};

  /**
   * Causes this method to be invoked only if the test method belongs to a listed group. It can be
   * used if different cleanups are needed for different groups. Omitting this or setting it to an
   * empty list will cause this method to run after every test method, regardless of which group it
   * belongs to. Otherwise, this method is only invoked if the test method that was invoked belongs
   * to one of the groups listed here.
   */
  String[] onlyForGroups() default {};

  /**
   * For before methods (beforeSuite, beforeTest, beforeTestClass and beforeTestMethod, but not
   * beforeGroups): If set to true, this configuration method will be run regardless of what groups
   * it belongs to. <br>
   * For after methods (afterSuite, afterClass, ...): If set to true, this configuration method will
   * be run even if one or more test methods invoked previously failed or was skipped.
   */
  boolean alwaysRun() default false;

  /**
   * If true, this &#64;Configuration method will belong to groups specified in the &#64;Test
   * annotation on the class (if any).
   */
  boolean inheritGroups() default true;

  /**
   * The description for this method. The string used will appear in the HTML report and also on
   * standard output if verbose >= 2.
   */
  String description() default "";

  /**
   * If true and the @Test method that was just run has an invocationCount > 1, this AfterMethod
   * will only be invoked once (after the last test invocation).
   */
  boolean lastTimeOnly() default false;

  /**
   * The maximum number of milliseconds this method should take. If it hasn't returned after this
   * time, this method will fail and it will cause test methods depending on it to be skipped.
   */
  long timeOut() default 0;
}
