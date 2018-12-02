package org.testng.internal;

import java.util.List;
import java.util.Map;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;

public interface ITestInvoker {

  class FailureContext {

    int count = 0;
    List<Object> instances = Lists.newArrayList();
  }

  List<ITestResult> invokeTestMethods(ITestNGMethod testMethod,
      ConfigurationGroupMethods groupMethods,
      Object instance,
      ITestContext context);

  ITestResult invokeTestMethod(
      Object instance,
      ITestNGMethod tm,
      Object[] parameterValues,
      int parametersIndex,
      XmlSuite suite,
      Map<String, String> params,
      ITestClass testClass,
      ITestNGMethod[] beforeMethods,
      ITestNGMethod[] afterMethods,
      ConfigurationGroupMethods groupMethods,
      FailureContext failureContext);

  FailureContext retryFailed(
      Object instance,
      ITestNGMethod tm,
      Object[] paramValues,
      ITestClass testClass,
      ITestNGMethod[] beforeMethods,
      ITestNGMethod[] afterMethods,
      ConfigurationGroupMethods groupMethods,
      List<ITestResult> result,
      int failureCount,
      ITestContext testContext,
      Map<String, String> parameters,
      int parametersIndex);

  void runTestResultListener(ITestResult tr);
}
