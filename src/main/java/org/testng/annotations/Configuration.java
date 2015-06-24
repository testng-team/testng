package org.testng.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Configuration information for a TestNG class.
 *
 * @deprecated Use @BeforeSuite, @AfterSuite, @BeforeTest, @AfterTest,
 * \@BeforeGroups, @AfterGroups, @BeforeClass, @AfterClass, @BeforeMethod,
 * \@AfterMethod
 *
 * @author Cedric Beust, Apr 26, 2004
 *
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface Configuration {

  /**
   * If true, the annotated method will be run after the test class is instantiated
   * and before the test method is invoked.
   */
  public boolean beforeTestClass() default false;

  /**
   * If true, the annotated method will be run after all the tests in the test
   * class have been run.
   */
  public boolean afterTestClass() default false;

  /**
   * If true, the annotated method will be run before any test method is invoked.
   */
  public boolean beforeTestMethod() default false;

  /**
   * If true, the annotated method will be run after any test method is invoked.
   */
  public boolean afterTestMethod() default false;

  /**
   * If true, the annotated method will be run before this suite starts.
   */
  public boolean beforeSuite() default false;

  /**
   * If true, the annotated method will be run after all tests in this suite
   * have run.
   */
  public boolean afterSuite() default false;

  /**
   * If true, the annotated method will be run before every test.
   */
  public boolean beforeTest() default false;

  /**
   * If true, the annotated method will be run after all every test.
   */
  public boolean afterTest() default false;

  /**
   * The list of groups that this configuration method will run before.
   * This method is guaranteed to run shortly before the first test method that
   * belongs to any of these groups is invoked.
   */
  public String[] beforeGroups() default {};

  /**
   * The list of groups that this configuration method will run after.
   * This method is guaranteed to run shortly after the last test method that
   * belongs to any of these groups is invoked.
   */
  public String[] afterGroups() default {};

  /**
   * The list of variables used to fill the parameters of this method.
   * These variables must be defined in the property file.
   *
   * @deprecated Use @Parameters
   */
  @Deprecated
  public String[] parameters() default {};

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
   * HTML report and also on standard output if verbose &gt;= 2.
   */
  public String description() default "";
}
