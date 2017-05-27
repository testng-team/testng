package org.testng;

/**
 * If a test class implements this interface, it will receive a
 * special treatment, such as having the test name displayed
 * in the HTML reports.
 *
 * @author cbeust
 * Jun 6, 2006
 */
public interface ITest {

  /**
   * The name of test instance(s).
   * @return name associated with a particular instance of a test.
   */
  public String getTestName();

}
