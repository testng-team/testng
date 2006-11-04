package org.testng;

/**
 * If a test class implements this interface, it will receive a 
 * special treatment, such as having the test name displayed 
 * in the HTML reports.
 * 
 * @author cbeust
 * @date Jun 6, 2006
 */
public interface ITest {
  
  public String getTestName();

}
