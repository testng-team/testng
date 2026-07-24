package test.listeners.github551;

import org.testng.IConfigurationListener;
import org.testng.ITestResult;

public class ConfigListener implements IConfigurationListener {

  public static long executionTime;

  @Override
  public void onConfigurationFailure(ITestResult itr) {
    executionTime = itr.getEndMillis() - itr.getStartMillis();
  }
}
