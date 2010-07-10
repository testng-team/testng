package org.testng.internal;

import org.testng.ITestResult;


/**
 * Listener interface for events related to results of @Configuration method invocations.
 * 
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface IConfigurationListener {
  void onConfigurationSuccess(ITestResult itr);
  
  void onConfigurationFailure(ITestResult itr);
  
  void onConfigurationSkip(ITestResult itr);
}
