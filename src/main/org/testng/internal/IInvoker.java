package org.testng.internal;

import java.util.List;
import java.util.Map;

import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

/**
 * This class defines an invoker.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IInvoker {
  
  /**
   * Invoke configuration methods if they belong to the same TestClass
   * passed in parameter..
   * 
   * TODO:  Calculate ahead of time which methods should be
   * invoked for each class.   Might speed things up for users who invoke the same
   * test class with different parameters in the same suite run.
   */
  public  void invokeConfigurations(IClass testClass, 
                                    ITestNGMethod[] allMethods,
                                    XmlSuite suite, Map<String, String> parameters,
                                    Object instance);
  
  /**
   * Invoke the given method
   * 
   * @param testMethod
   * @param suite
   * @param parameters
   * @param allTestMethods The list of all the test methods
   * @param methodIndex The index of testMethod in the allTestMethods array
   * @param groupMethods
   * @return
   */
  public List<ITestResult> invokeTestMethods(ITestNGMethod testMethod, XmlSuite suite, 
      Map<String, String> parameters, 
      ITestNGMethod[] allTestMethods, int methodIndex,
      ConfigurationGroupMethods groupMethods);

  public void runTestListeners(ITestResult tr);
}
