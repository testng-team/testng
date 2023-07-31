package org.testng;

import com.google.inject.Injector;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlSuite;

/**
 * Interface defining a Test Suite.
 *
 * @author Cedric Beust, Aug 6, 2004
 */
public interface ISuite extends IAttributes {

  /** Returns the name of this suite. */
  String getName();

  /** Returns the results for this suite. */
  Map<String, ISuiteResult> getResults();

  /** Returns the object factory used to create all test instances. */
  ITestObjectFactory getObjectFactory();

  @Deprecated
  /**
   * Returns null.
   *
   * @deprecated This interface stands deprecated as of TestNG 7.5.0
   */
  default @Nullable IObjectFactory2 getObjectFactory2() {
    return null;
  }

  /** Returns the output directory used for the reports. */
  String getOutputDirectory();

  /** Returns true if the tests must be run in parallel. */
  String getParallel();

  String getParentModule();

  String getGuiceStage();

  /**
   * Returns the value of this parameter, or null if none was specified.
   *
   * @param parameterName The name of the parameter
   */
  String getParameter(String parameterName);

  /**
   * Retrieves the map of groups and their associated test methods.
   *
   * @return A map where the key is the group and the value is a list of methods used by this group.
   */
  Map<String, Collection<ITestNGMethod>> getMethodsByGroups();

  /** Returns a list of all the methods that were invoked in this suite. */
  List<IInvokedMethod> getAllInvokedMethods();

  /** Returns all the methods that were not included in this test run. */
  Collection<ITestNGMethod> getExcludedMethods();

  /** Triggers the start of running tests included in the suite. */
  void run();

  /**
   * Returns the host where this suite was run, or null if it was run locally. The returned string
   * has the form: host:port
   */
  String getHost();

  /**
   * Retrieves the shared state for a suite.
   *
   * @return the share state of the current suite.
   */
  SuiteRunState getSuiteState();

  /** Returns the annotation finder used for the specified type (JDK5 or javadoc) */
  IAnnotationFinder getAnnotationFinder();

  /** Returns the representation of the current XML suite file. */
  XmlSuite getXmlSuite();

  void addListener(ITestNGListener listener);

  Injector getParentInjector();

  void setParentInjector(Injector injector);

  /**
   * Returns the total number of methods found in this suite. The presence of factories or data
   * providers might cause the actual number of test methods run be bigger than this list.
   */
  List<ITestNGMethod> getAllMethods();
}
