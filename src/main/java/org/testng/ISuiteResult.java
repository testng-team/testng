package org.testng;

import java.io.Serializable;


/**
 * This class represents the result of a suite run.
 *
 * @author Cedric Beust, Aug 6, 2004
 *
 */
public interface ISuiteResult extends Serializable {

  /**
   * @return The name of the property file for these tests.
   */
  public String getPropertyFileName();

  /**
   * @return The testing context for these tests.
   */
  public ITestContext getTestContext();

}
