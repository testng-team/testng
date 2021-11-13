package org.testng.internal.invokers;

import org.testng.IClass;
import org.testng.ITestNGMethod;

public interface IConfigInvoker {

  boolean hasConfigurationFailureFor(
      ITestNGMethod testNGMethod, String[] groups, IClass testClass, Object instance);

  void invokeBeforeGroupsConfigurations(GroupConfigMethodArguments arguments);

  void invokeAfterGroupsConfigurations(GroupConfigMethodArguments arguments);

  void invokeConfigurations(ConfigMethodArguments arguments);
}
