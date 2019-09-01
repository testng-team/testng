package test.dataprovider.issue2111;

import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.collections.Lists;

public class CountingListener implements IInvokedMethodListener {
  private List<ITestResult> results = Lists.newArrayList();

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    results.add(testResult);
  }

  public List<ITestResult> getResults() {
    return results;
  }
}
