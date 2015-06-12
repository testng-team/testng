package org.testng;

import org.testng.xml.XmlTest;




/**
 * This interface allows to modify the strategy used by TestRunner
 * to find its test methods.  At the time of this writing, TestNG
 * supports two different strategies:  TestNG (using annotations to
 * locate these methods) and JUnit (setUp()/tearDown() and all
 * methods that start with "test" or have a suite() method).
 *
 * @author Cedric Beust, May 3, 2004
 *
 */
public interface ITestMethodFinder {

  /**
   * @return All the applicable test methods.
   */
  ITestNGMethod[] getTestMethods(Class<?> cls, XmlTest xmlTest);

  /**
   * @return All the methods that should be invoked
   * before a test method is invoked.
   */
  ITestNGMethod[] getBeforeTestMethods(Class<?> cls);

  /**
   * @return All the methods that should be invoked
   * after a test method completes.
   */
  ITestNGMethod[] getAfterTestMethods(Class<?> cls);

  /**
   * @return All the methods that should be invoked
   * after the test class has been created and before
   * any of its test methods is invoked.
   */
  ITestNGMethod[] getBeforeClassMethods(Class<?> cls);

  /**
   * @return All the methods that should be invoked
   * after the test class has been created and after
   * all its test methods have completed.
   */
  ITestNGMethod[] getAfterClassMethods(Class<?> cls);

  /**
   * @return All the methods that should be invoked
   * before the suite starts running.
   */
  ITestNGMethod[] getBeforeSuiteMethods(Class<?> cls);

  /**
   * @return All the methods that should be invoked
   * after the suite has run all its tests.
   */
  ITestNGMethod[] getAfterSuiteMethods(Class<?> cls);

  ITestNGMethod[] getBeforeTestConfigurationMethods(Class<?> testClass);

  ITestNGMethod[] getAfterTestConfigurationMethods(Class<?> testClass);

  ITestNGMethod[] getBeforeGroupsConfigurationMethods(Class<?> testClass);

  ITestNGMethod[] getAfterGroupsConfigurationMethods(Class<?> testClass);


}
