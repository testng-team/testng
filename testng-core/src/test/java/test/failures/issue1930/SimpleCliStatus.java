package test.failures.issue1930;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.ITestContext;
import org.testng.ITestListener;

public class SimpleCliStatus implements ITestListener {
  private Map<String, List<Integer>> failedTests = new HashMap<>();

  public Map<String, List<Integer>> getFailedTests() {
    return failedTests;
  }

  @Override
  public void onFinish(ITestContext context) {
    context
        .getFailedTests()
        .getAllResults()
        .forEach(
            result ->
                failedTests.put(
                    result.getMethod().getMethodName(), result.getMethod().getInvocationNumbers()));
  }
}
