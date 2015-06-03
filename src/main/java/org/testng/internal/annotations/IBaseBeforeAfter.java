package org.testng.internal.annotations;

import org.testng.annotations.ITestOrConfiguration;

/**
 * Base interface for IBeforeSuite, IAfterSuite, etc...
 *
 * @author cbeust
 * @since Jun 22, 2006
 */
public interface IBaseBeforeAfter extends ITestOrConfiguration {
  /**
   * Whether methods on this class/method are enabled.
   */
  public boolean getEnabled();

  /**
   * The list of groups this class/method belongs to.
   */
  public String[] getGroups();

  /**
   * The list of groups this method depends on.  Every method
   * member of one of these groups is guaranteed to have been
   * invoked before this method.  Furthermore, if any of these
   * methods was not a SUCCESS, this test method will not be
   * run and will be flagged as a SKIP.
   */
  public String[] getDependsOnGroups();

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
  public String[] getDependsOnMethods();

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
  public boolean getAlwaysRun();

  /**
   * If true, this &#64;Configuration method will belong to groups specified in the
   * &#64;Test annotation on the class (if any).
   */
  public boolean getInheritGroups();

  /**
   * The description for this method.  The string used will appear in the
   * HTML report and also on standard output if verbose >= 2.
   */
  public String getDescription();

}
