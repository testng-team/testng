package test.dependent.issue141;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class SkipReasoner implements ITestListener {

  private List<String> upstreamFailures;

  @Override
  public void onTestSkipped(ITestResult result) {
    upstreamFailures =
        result.getSkipCausedBy().stream()
            .map(ITestNGMethod::getMethodName)
            .collect(Collectors.toList());
  }

  public List<String> getUpstreamFailures() {
    return upstreamFailures;
  }
}
