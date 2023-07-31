package org.testng;

import java.util.Collection;
import java.util.Date;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.xml.XmlTest;

/**
 * This class defines a test context which contains all the information for a given test run. An
 * instance of this context is passed to the test listeners so they can query information about
 * their environment.
 *
 * @author Cedric Beust, Aug 6, 2004
 */
public interface ITestContext extends IAttributes {

  /** Returns the name of this test. */
  String getName();

  /** Returns when this test started running. */
  Date getStartDate();

  /** Returns when this test stopped running. */
  Date getEndDate();

  /** Returns a list of all the tests that run successfully. */
  IResultMap getPassedTests();

  /** Returns a list of all the tests that were skipped */
  IResultMap getSkippedTests();

  /**
   * Returns a list of all the tests that failed but are being ignored because annotated with a
   * successPercentage.
   */
  IResultMap getFailedButWithinSuccessPercentageTests();

  /**
   * Returns a map of all the tests that failed, indexed by their ITestNGMethod.
   *
   * @see org.testng.ITestNGMethod
   */
  IResultMap getFailedTests();

  /** @return All the groups that are included for this test run. */
  String[] getIncludedGroups();

  /** Returns all the groups that are excluded for this test run. */
  String[] getExcludedGroups();

  /** Returns where the reports will be generated. */
  String getOutputDirectory();

  /** Returns the Suite object that was passed to the runner at start-up. */
  ISuite getSuite();

  /** Returns all the test methods that were run. */
  ITestNGMethod[] getAllTestMethods();

  /**
   * Returns the host where this test was run, or null if it was run locally. The returned string
   * has the form: host:port
   */
  String getHost();

  /** Returns all the methods that were not included in this test run. */
  Collection<ITestNGMethod> getExcludedMethods();

  /** Returns the information about the successful configuration method invocations. */
  IResultMap getPassedConfigurations();

  /** Returns the information about the skipped configuration method invocations. */
  IResultMap getSkippedConfigurations();

  /** Returns the information about the failed configuration method invocations. */
  IResultMap getFailedConfigurations();

  /** Returns the current XmlTest. */
  XmlTest getCurrentXmlTest();

  default @Nullable IInjectorFactory getInjectorFactory() {
    return null;
  }
}
