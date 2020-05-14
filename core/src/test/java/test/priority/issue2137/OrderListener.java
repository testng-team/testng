package test.priority.issue2137;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class OrderListener implements IInvokedMethodListener {
  private List<String> logs = new ArrayList<>();

  public List<String> getLogs() {
    return logs;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    logs.add(method.getTestMethod().getMethodName());
  }
}
