package org.testng.annotations;

/**
 * Encapsulate the @Configuration / @testng.configuration annotation
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IConfigurationAnnotation extends ITestOrConfiguration {
  /**
   * If true, the annotated method will be run after the test class is instantiated
   * and before the test method is invoked.
   */
  public boolean getBeforeTestClass();

  /**
   * If true, the annotated method will be run after all the tests in the test
   * class have been run.
   */
  public boolean getAfterTestClass();

  /**
   * If true, the annotated method will be run before any test method is invoked.
   */
  public boolean getBeforeTestMethod();

  /**
   * If true, the annotated method will be run after any test method is invoked.
   */
  public boolean getAfterTestMethod();

  /**
   * If true, the annotated method will be run before this suite starts.
   */
  public boolean getBeforeSuite();

  /**
   * If true, the annotated method will be run after all tests in this suite
   * have run.
   */
  public boolean getAfterSuite();

  /**
   * If true, the annotated method will be run before every test
   */
  public boolean getBeforeTest();

  /**
   * If true, the annotated method will be run after all every test.
   */
  public boolean getAfterTest();

  /**
   * Used only for after type of configuration methods. If set to true than
   * the configuration method will be run whatever the status of before
   * configuration methods was.
   */
  public boolean getAlwaysRun();

  /**
   * If true, this @Configuration method will belong to groups specified in the
   * \@Test annotation on the class (if any).
   */
  public boolean getInheritGroups();

  /**
   * The list of groups that this configuration method will run before.
   */
  public String[] getBeforeGroups();

  /**
   * The list of groups that this configuration method will run after.
   */
  public String[] getAfterGroups();

  /**
   * Internal use only.
   * @return true if this configuration annotation is not a "true" configuration
   * annotation but a @BeforeSuite or similar that is represented as a configuration
   * annotation.
   */
  public boolean isFakeConfiguration();
}
