package org.testng.annotations;

/**
 * This interface captures methods common to @Test and @Configuration
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface ITestOrConfiguration extends IParameterizable {
  /**
   * Returns the maximum number of milliseconds this test should take.
   * If it hasn't returned after this time, it will be marked as a FAIL.
   * @return the maximum number of milliseconds this test should take.
   */
  public long getTimeOut();
  public void setTimeOut(long l);

  /**
   * The list of groups this class/method belongs to.
   */
  public String[] getGroups();
  public void setGroups(String[] groups);

  /**
   * The list of groups this method depends on.  Every method
   * member of one of these groups is guaranteed to have been
   * invoked before this method.  Furthermore, if any of these
   * methods was not a SUCCESS, this test method will not be
   * run and will be flagged as a SKIP.
   */
  public String[] getDependsOnGroups();
  public void setDependsOnGroups(String[] groups);

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
  public void setDependsOnMethods(String[] dependsOnMethods);

  /**
   * The description for this method, which will be shown in the reports.
   */
  public String getDescription();
  public void setDescription(String description);

}
