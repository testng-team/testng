package test.dataprovider.issue2111;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class CountingListener implements IInvokedMethodListener {
  private final List<ITestResult> results = new ArrayList<>();

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    results.add(testResult);
  }

  public List<ITestResult> getResults() {
    return results;
  }
}
