package test.listeners.github551;

import org.testng.IConfigurationListener;
import org.testng.ITestResult;

public class ConfigListener implements IConfigurationListener {

  public static long executionTime = 0;

  @Override
  public void onConfigurationSuccess(ITestResult itr) {}

  @Override
  public void onConfigurationFailure(ITestResult itr) {
    executionTime = itr.getEndMillis() - itr.getStartMillis();
  }

  @Override
  public void onConfigurationSkip(ITestResult itr) {}
}
