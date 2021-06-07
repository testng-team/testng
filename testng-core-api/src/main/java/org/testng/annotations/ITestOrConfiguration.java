package org.testng.annotations;

/** This interface captures methods common to @Test and @Configuration */
public interface ITestOrConfiguration extends IParameterizable {
  /**
   * @return Returns the maximum number of milliseconds this test should take. If it hasn't returned
   *     after this time, it will be marked as a FAIL.
   */
  long getTimeOut();

  void setTimeOut(long l);

  /** @return The list of groups this class/method belongs to. */
  String[] getGroups();

  void setGroups(String[] groups);

  /**
   * @return The list of groups this method depends on. Every method member of one of these groups
   *     is guaranteed to have been invoked before this method. Furthermore, if any of these methods
   *     was not a SUCCESS, this test method will not be run and will be flagged as a SKIP.
   */
  String[] getDependsOnGroups();

  void setDependsOnGroups(String[] groups);

  /**
   * @return The list of methods this method depends on. There is no guarantee on the order on which
   *     the methods depended upon will be run, but you are guaranteed that all these methods will
   *     be run before the test method that contains this annotation is run. Furthermore, if any of
   *     these methods was not a SUCCESS, this test method will not be run and will be flagged as a
   *     SKIP.
   *     <p>If some of these methods have been overloaded, all the overloaded versions will be run.
   */
  String[] getDependsOnMethods();

  void setDependsOnMethods(String[] dependsOnMethods);

  /** @return The description for this method, which will be shown in the reports. */
  String getDescription();

  void setDescription(String description);
}
