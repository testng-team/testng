package org.testng.internal;

import java.util.Map;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlSuite;

public interface IConfigInvoker {

  boolean hasConfigurationFailureFor(
      ITestNGMethod testNGMethod, String[] groups, IClass testClass, Object instance);

  void invokeBeforeGroupsConfigurations(
      ITestNGMethod currentTestMethod,
      ConfigurationGroupMethods groupMethods,
      XmlSuite suite,
      Map<String, String> params,
      Object instance);

  void invokeAfterGroupsConfigurations(
      ITestNGMethod currentTestMethod,
      ConfigurationGroupMethods groupMethods,
      XmlSuite suite,
      Map<String, String> params,
      Object instance);

  void invokeConfigurations(ConfigMethodAttributes configMethodAttributes);


}
