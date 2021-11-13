package org.testng.annotations;

/** Encapsulate the @Configuration / @testng.configuration annotation */
public interface IConfigurationAnnotation extends ITestOrConfiguration {
  /**
   * @return true if the annotated method will be run after the test class is instantiated and
   *     before the test method is invoked.
   */
  boolean getBeforeTestClass();

  /**
   * @return true if the annotated method will be run after all the tests in the test class have
   *     been run.
   */
  boolean getAfterTestClass();

  /** @return true true if the annotated method will be run before any test method is invoked. */
  boolean getBeforeTestMethod();

  /** @return true if the annotated method will be run after any test method is invoked. */
  boolean getAfterTestMethod();

  /** @return true if the annotated method will be run before this suite starts. */
  boolean getBeforeSuite();

  /** @return true if the annotated method will be run after all tests in this suite have run. */
  boolean getAfterSuite();

  /** @return true if the annotated method will be run before every test */
  boolean getBeforeTest();

  /** @return true if the annotated method will be run after all every test. */
  boolean getAfterTest();

  /**
   * Used only for after type of configuration methods.
   *
   * @return true if the configuration method will be run whatever the status of before
   *     configuration methods was.
   */
  boolean getAlwaysRun();

  /**
   * @return true if this @Configuration method will belong to groups specified in the \@Test
   *     annotation on the class (if any).
   */
  boolean getInheritGroups();

  /** @return The list of groups that this configuration method will run before. */
  String[] getBeforeGroups();

  /** @return The list of groups that this configuration method will run after. */
  String[] getAfterGroups();

  /**
   * Internal use only.
   *
   * @return true if this configuration annotation is not a "true" configuration annotation but
   *     a @BeforeSuite or similar that is represented as a configuration annotation.
   */
  boolean isFakeConfiguration();

  default boolean isBeforeGroups() {
    return false;
  }

  default boolean isAfterGroups() {
    return false;
  }
}
