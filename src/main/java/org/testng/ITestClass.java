package org.testng;

/**
 * This class represents a test class:
 *
 * <ul>
 *   <li>The test methods
 *   <li>The configuration methods (test and method)
 *   <li>The class file
 * </ul>
 *
 * Note that the methods returned by instances of this class are expected to be correct at runtime.
 * In other words, they might differ from what the ITestMethodFinder returned since ITestClass will
 * take into account the groups being included and excluded.
 */
public interface ITestClass extends IClass {

  /**
   * Returns all the applicable test methods.
   *
   * @return All the applicable test methods.
   */
  ITestNGMethod[] getTestMethods();

  /**
   * Returns all the methods that should be invoked before a test method is invoked.
   *
   * @return All the methods that should be invoked before a test method is invoked.
   */
  ITestNGMethod[] getBeforeTestMethods();

  /**
   * Returns all the methods that should be invoked after a test method completes.
   *
   * @return All the methods that should be invoked after a test method completes.
   */
  ITestNGMethod[] getAfterTestMethods();

  /**
   * Return all the methods that should be invoked after the test class has been created and before
   * any of its test methods is invoked.
   *
   * @return All the methods that should be invoked after the test class has been created and before
   *     any of its test methods is invoked.
   */
  ITestNGMethod[] getBeforeClassMethods();

  /**
   * Returns all the methods that should be invoked after all the tests have been run on this class.
   *
   * @return All the methods that should be invoked after all the tests have been run on this class.
   */
  ITestNGMethod[] getAfterClassMethods();

  /**
   * Returns All the methods that should be invoked before the suite is run.
   *
   * @return All the methods that should be invoked before the suite is run.
   */
  ITestNGMethod[] getBeforeSuiteMethods();

  /**
   * Returns all the methods that should be invoked after the suite has run.
   *
   * @return All the methods that should be invoked after the suite has run.
   */
  ITestNGMethod[] getAfterSuiteMethods();

  /**
   * Returns all &#64;Configuration methods that should be invoked before any others in the current
   * test.
   *
   * @return all @Configuration methods that should be invoked before any others in the current
   *     test.
   */
  ITestNGMethod[] getBeforeTestConfigurationMethods();

  /**
   * Returns all &#64;Configuration methods that should be invoked last before any others in the
   * current test.
   *
   * @return all @Configuration methods that should be invoked last before any others in the current
   *     test.
   */
  ITestNGMethod[] getAfterTestConfigurationMethods();

  /**
   * Returns all &#64;Configuration methods that should be invoked before certain groups.
   *
   * @return all @Configuration methods that should be invoked before certain groups.
   */
  ITestNGMethod[] getBeforeGroupsMethods();

  /**
   * Returns all &#64;Configuration methods that should be invoked after certain groups.
   *
   * @return all @Configuration methods that should be invoked after certain groups.
   */
  ITestNGMethod[] getAfterGroupsMethods();
}
