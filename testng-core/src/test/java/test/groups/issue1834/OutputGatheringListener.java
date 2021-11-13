package test.groups.issue1834;

import java.util.List;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.collections.Lists;

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
