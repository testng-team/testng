package org.testng;

import java.io.Serializable;
import java.util.List;

/**
 * This interface is used to augment or replace TestNG's algorithm to 
 * decide whether a test method should be included in a test run.
 * 
 * Created on Sep 26, 2005
 * @author cbeust
 */
public interface IMethodSelector extends Serializable {
  
  /**
   * @param method The test method
   * @param isTestMethod true if this is a @Test method, false if it's a 
   * @Configuration method
   * @return true if this method should be included in the test run, false
   * otherwise
   */
  public boolean includeMethod(ITestNGMethod method, boolean isTestMethod);

  /**
   * Invoked when all the test methods are known so that the method selector
   * can perform additional work, such as adding the transitive closure of
   * all the groups being included and depended upon.
   */
  public void setTestMethods(List<ITestNGMethod> testMethods);
  
}
