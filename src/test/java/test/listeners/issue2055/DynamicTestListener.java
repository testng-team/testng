package test.listeners.issue2055;

import java.util.List;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.collections.Lists;

public class DynamicTestListener implements ITestListener {

  public static final List<String> MSGS = Lists.newArrayList();

  @Override
  public void onTestStart(ITestResult result) {
    MSGS.add("Starting " + result.getMethod().getMethodName());
  }
}
