package org.testng.annotations;

/**
 * Encapsulate the @Configuration / @testng.configuration annotation
 */
public interface IConfigurationAnnotation extends ITestOrConfiguration {
  /**
   * If true, the annotated method will be run after the test class is instantiated and before the
   * test method is invoked.
   */
  boolean getBeforeTestClass();

  /**
   * If true, the annotated method will be run after all the tests in the test class have been run.
   */
  boolean getAfterTestClass();

  /** If true, the annotated method will be run before any test method is invoked. */
  boolean getBeforeTestMethod();

  /** If true, the annotated method will be run after any test method is invoked. */
  boolean getAfterTestMethod();

  /** If true, the annotated method will be run before this suite starts. */
  boolean getBeforeSuite();

  /** If true, the annotated method will be run after all tests in this suite have run. */
  boolean getAfterSuite();

  /** If true, the annotated method will be run before every test */
  boolean getBeforeTest();

  /** If true, the annotated method will be run after all every test. */
  boolean getAfterTest();

  /**
   * Used only for after type of configuration methods. If set to true than the configuration method
   * will be run whatever the status of before configuration methods was.
   */
  boolean getAlwaysRun();

  /**
   * If true, this @Configuration method will belong to groups specified in the \@Test annotation on
   * the class (if any).
   */
  boolean getInheritGroups();

  /** The list of groups that this configuration method will run before. */
  String[] getBeforeGroups();

  /** The list of groups that this configuration method will run after. */
  String[] getAfterGroups();

  /**
   * Internal use only.
   *
   * @return true if this configuration annotation is not a "true" configuration annotation but
   *     a @BeforeSuite or similar that is represented as a configuration annotation.
   */
  boolean isFakeConfiguration();
}
