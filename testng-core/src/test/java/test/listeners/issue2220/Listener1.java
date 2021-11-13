package test.listeners.issue2220;

import java.util.LinkedList;
import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class Listener1 implements ITestListener {

  static final List<String> logs = new LinkedList<>();

  @Override
  public void onTestStart(ITestResult result) {
    logs.add("started_test_method_" + getMethodName(result));
  }

  @Override
  public void onStart(ITestContext context) {
    logs.add("started_<test>_" + context.getName());
  }

  @Override
  public void onFinish(ITestContext context) {
    logs.add("finished_<test>_" + context.getName());
  }

  private static String getMethodName(ITestResult result) {
    return result.getTestClass().getRealClass().getName() + "." + result.getName();
  }
}
