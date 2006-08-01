package org.testng;

import java.util.Collection;
import java.util.Map;

/**
 * Interface defining a Test Suite.
 *
 * @author Cedric Beust, Aug 6, 2004
 * 
 */
public interface ISuite {

	/**
	 * @return the name of this suite.
	 */
	public String getName();

	/**
	 * @return The results for this suite.
	 */
	public Map<String, ISuiteResult> getResults();

	/**
	 * @return The output directory used for the reports.
	 */
	public String getOutputDirectory();

	/**
	 * @return true if the tests must be run in parallel.
	 */
	public boolean isParallel();

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
	 */
	public Collection<ITestNGMethod> getInvokedMethods();
  
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
}
