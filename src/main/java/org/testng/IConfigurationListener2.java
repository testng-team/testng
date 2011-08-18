package org.testng;

public interface IConfigurationListener2 extends IConfigurationListener {

  /**
   * Invoked before a configuration method is invoked.
   */
  void beforeConfiguration(ITestResult tr);

}
