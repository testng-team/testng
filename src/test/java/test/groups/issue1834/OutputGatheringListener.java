package test.groups.issue1834;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.collections.Lists;

import java.util.List;

public class OutputGatheringListener extends TestListenerAdapter {
  private List<String> consoleLogs = Lists.newArrayList();

  @Override
  public void onTestSuccess(ITestResult tr) {
    consoleLogs.addAll(Reporter.getOutput(tr));
  }

  public List<String> getConsoleLogs() {
    return consoleLogs;
  }
}
