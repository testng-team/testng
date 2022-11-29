package test.dependent.issue550;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class OrderedResultsGatherer implements IInvokedMethodListener {

  List<Long> startTimes = new ArrayList<>();

  public List<Long> getStartTimes() {
    return startTimes;
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    startTimes.add(testResult.getStartMillis());
  }
}
