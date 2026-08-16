package org.testng.internal.invokers;

import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.internal.IConfiguration;

public interface IConfigInvoker {

  boolean hasConfigurationFailureFor(
      @Nullable ITestNGMethod testNGMethod,
      String[] groups,
      IClass testClass,
      @Nullable Object instance);

  /**
   * @param configMethod the configuration method being scrutinised, or null to ask about the class
   *     as a whole
   * @param testNGMethod null when the configuration is a class or suite level one, which has no
   *     current test method
   */
  boolean hasConfigurationFailureFor(
      @Nullable ITestNGMethod configMethod,
      @Nullable ITestNGMethod testNGMethod,
      String[] groups,
      IClass testClass,
      @Nullable Object instance);

  void invokeBeforeGroupsConfigurations(GroupConfigMethodArguments arguments);

  void invokeAfterGroupsConfigurations(GroupConfigMethodArguments arguments);

  void invokeConfigurations(ConfigMethodArguments arguments);

  IConfiguration getConfiguration();
}
