package org.testng.internal;

import org.testng.IClass;
import org.testng.ITestNGMethod;

public interface IConfigInvoker {

  boolean hasConfigurationFailureFor(
      ITestNGMethod testNGMethod, String[] groups, IClass testClass, Object instance);

  void invokeBeforeGroupsConfigurations(GroupConfigMethodAttributes attributes);

  void invokeAfterGroupsConfigurations(GroupConfigMethodAttributes attributes);

  void invokeConfigurations(ConfigMethodAttributes configMethodAttributes);

}
