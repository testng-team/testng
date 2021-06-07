package test.listeners.issue1777;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class MyListener implements IInvokedMethodListener, ITestListener {
  List<String> allMsgs = new LinkedList<>();
  List<String> tstMsgs = new LinkedList<>();
  List<String> cfgMsgs = new LinkedList<>();

  @Override
  public void onFinish(ITestContext testContext) {}

  @Override
  public void onStart(ITestContext testContext) {}

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
    log("testFailedBut", testResult);
  }

  @Override
  public void onTestFailure(ITestResult testResult) {
    log("testFailed", testResult);
  }

  @Override
  public void onTestSkipped(ITestResult testResult) {
    log("testSkipped", testResult);
  }

  @Override
  public void onTestStart(ITestResult testResult) {
    log("testStart", testResult);
  }

  @Override
  public void onTestSuccess(ITestResult testResult) {
    log("testSuccess", testResult);
  }

  @Override
  public void afterInvocation(
      IInvokedMethod method, ITestResult testResult, ITestContext testContext) {
    log("after", method, testResult);
  }

  @Override
  public void beforeInvocation(
      IInvokedMethod method, ITestResult testResult, ITestContext testContext) {
    log("before", method, testResult);
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    log("before", method, testResult);
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    log("after", method, testResult);
  }

  private void log(String prefix, ITestResult testResult) {
    log(prefix, testResult.getMethod(), testResult);
  }

  private void log(String prefix, IInvokedMethod method, ITestResult testResult) {
    log(prefix, method.getTestMethod(), testResult);
  }

  private void log(String prefix, ITestNGMethod method, ITestResult testResult) {
    String msg = prefix + "_";
    if (method.isTest()) {
      msg += "test_method: ";
    } else {
      msg += "configuration_method: ";
    }
    msg += method.getMethodName() + parameters(testResult);
    allMsgs.add(msg);
    if (method.isTest()) {
      tstMsgs.add(msg);
    } else {
      cfgMsgs.add(msg);
    }
  }

  private String parameters(ITestResult testResult) {
    Object[] parameters = testResult.getParameters();
    if (parameters != null) {
      StringBuilder builder = new StringBuilder();
      for (Object parameter : parameters) {
        if (parameter instanceof Method) {
          builder.append(((Method) parameter).getName());
        }
      }
      if (builder.length() != 0) {
        return "[" + builder.toString() + "]";
      }
    }
    return "";
  }
}
