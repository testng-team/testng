package org.testng.internal.invokers;

import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.internal.IConfiguration;

public interface IConfigInvoker {

  boolean hasConfigurationFailureFor(
      ITestNGMethod testNGMethod, String[] groups, IClass testClass, Object instance);

  boolean hasConfigurationFailureFor(
      ITestNGMethod configMethod,
      ITestNGMethod testNGMethod,
      String[] groups,
      IClass testClass,
      Object instance);

  void invokeBeforeGroupsConfigurations(GroupConfigMethodArguments arguments);

  void invokeAfterGroupsConfigurations(GroupConfigMethodArguments arguments);

  void invokeConfigurations(ConfigMethodArguments arguments);

  IConfiguration getConfiguration();
}
