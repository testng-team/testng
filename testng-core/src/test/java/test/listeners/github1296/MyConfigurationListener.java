package test.listeners.github1296;

import java.util.HashMap;
import java.util.Map;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;

public class MyConfigurationListener implements IConfigurationListener {

  public static final Map<String, Integer> CALLS = new HashMap<>();

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    String xmlTestName = itr.getTestContext().getCurrentXmlTest().getName();
    Integer count = CALLS.get(xmlTestName);
    if (count == null) {
      count = 0;
    }
    count++;
    CALLS.put(xmlTestName, count);
  }

  @Override
  public void onConfigurationFailure(ITestResult iTestResult) {}

  @Override
  public void onConfigurationSkip(ITestResult itr) {}
}
