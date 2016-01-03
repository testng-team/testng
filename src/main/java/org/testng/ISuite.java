package org.testng;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlSuite;

import com.google.inject.Injector;

/**
 * Interface defining a Test Suite.
 *
 * @author Cedric Beust, Aug 6, 2004
 *
 */
public interface ISuite extends IAttributes {

  /**
   * @return the name of this suite.
   */
  public String getName();

  /**
   * @return The results for this suite.
   */
  public Map<String, ISuiteResult> getResults();

  /**
   * @return The object factory used to create all test instances.
   */
  public IObjectFactory getObjectFactory();
  public IObjectFactory2 getObjectFactory2();

  /**
   * @return The output directory used for the reports.
   */
  public String getOutputDirectory();

  /**
   * @return true if the tests must be run in parallel.
   */
  public String getParallel();

  public String getParentModule();

  public String getGuiceStage();

  /**
   * @return The value of this parameter, or null if none was specified.
   */
  public String getParameter(String parameterName);

  /**
   * Retrieves the map of groups and their associated test methods.
   *
   * @return A map where the key is the group and the value is a list
   * of methods used by this group.
   */
  public Map<String, Collection<ITestNGMethod>> getMethodsByGroups();

  /**
   * Retrieves the list of all the methods that were invoked during this run.
   * @return a collection of ITestNGMethods belonging to all tests included in the suite.
   * @deprecated Use getAllInvokedMethods().
   */
  @Deprecated
  public Collection<ITestNGMethod> getInvokedMethods();

  /**
   * @return a list of all the methods that were invoked in this suite.
   */
  public List<IInvokedMethod> getAllInvokedMethods();

  /**
   * @return All the methods that were not included in this test run.
   */
  public Collection<ITestNGMethod> getExcludedMethods();

  /**
   * Triggers the start of running tests included in the suite.
   */
  public void run();

  /**
   * @return The host where this suite was run, or null if it was run locally.  The
   * returned string has the form:  host:port
   */
  public String getHost();

  /**
   * Retrieves the shared state for a suite.
   *
   * @return the share state of the current suite.
   */
  public SuiteRunState getSuiteState();

  /**
   * @return the annotation finder used for the specified type (JDK5 or javadoc)
   */
  public IAnnotationFinder getAnnotationFinder();

  /**
   * @return The representation of the current XML suite file.
   */
  public XmlSuite getXmlSuite();

  public void addListener(ITestNGListener listener);

  public Injector getParentInjector();

  public void setParentInjector(Injector injector);

  /**
   * @return the total number of methods found in this suite. The presence of
   * factories or data providers might cause the actual number of test methods
   * run be bigger than this list.
   */
  List<ITestNGMethod> getAllMethods();
}
