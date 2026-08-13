package test.groups.issue1834;

import java.util.ArrayList;
import java.util.List;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;

public class OutputGatheringListener extends TestListenerAdapter {
  private List<String> consoleLogs = new ArrayList<>();

  @Override
  public void onTestSuccess(ITestResult tr) {
    consoleLogs.addAll(Reporter.getOutput(tr));
  }

  public List<String> getConsoleLogs() {
    return consoleLogs;
  }
}
