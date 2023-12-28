package test.reports;

import java.util.ArrayList;
import java.util.List;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class GitHub447Listener implements ITestListener {

  private final List<Object[]> parameters = new ArrayList<>();

  @Override
  public void onTestSuccess(ITestResult result) {
    parameters.add(result.getParameters());
  }

  public List<Object[]> getParameters() {
    return parameters;
  }
}
