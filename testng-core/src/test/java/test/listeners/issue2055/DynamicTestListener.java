package test.listeners.issue2055;

import java.util.ArrayList;
import java.util.List;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class DynamicTestListener implements ITestListener {

  public static final List<String> MSGS = new ArrayList<>();

  @Override
  public void onTestStart(ITestResult result) {
    MSGS.add("Starting " + result.getMethod().getMethodName());
  }
}
