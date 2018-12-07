package org.testng.internal;

import java.util.List;
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
      TestMethodArguments arguments, XmlSuite suite,
      FailureContext failureContext);

  FailureContext retryFailed(
      TestMethodArguments arguments, List<ITestResult> result,
      int failureCount,
      ITestContext testContext);

  void runTestResultListener(ITestResult tr);
}
