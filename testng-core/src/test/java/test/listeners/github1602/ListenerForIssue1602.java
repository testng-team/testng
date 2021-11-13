package test.listeners.github1602;

import java.util.Collections;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.collections.Lists;

public class ListenerForIssue1602 implements IInvokedMethodListener {
  private List<String> logs = Collections.synchronizedList(Lists.newArrayList());

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    logs.add(
        "BeforeInvocation_"
            + method.getTestMethod().getMethodName()
            + "_"
            + intToStatus(testResult));
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    logs.add(
        "AfterInvocation_"
            + method.getTestMethod().getMethodName()
            + "_"
            + intToStatus(testResult));
  }

  public List<String> getLogs() {
    return logs;
  }

  private String intToStatus(ITestResult testResult) {
    int status = testResult.getStatus();
    switch (status) {
      case ITestResult.CREATED:
        return "CREATED";
      case ITestResult.SUCCESS:
        return "SUCCESS";
      case ITestResult.SKIP:
        return "SKIP";
      case ITestResult.FAILURE:
        return "FAILURE";
      case ITestResult.STARTED:
        return "STARTED";
      case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
        return "SUCCESS_PERCENTAGE_FAILURE";
    }
    return " ??? " + String.valueOf(status);
  }
}
