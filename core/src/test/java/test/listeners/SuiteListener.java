package test.listeners;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteListener implements ISuiteListener {
  public static int start = 0;
  public static int finish = 0;

  @Override
  public void onFinish(ISuite suite) {
    finish++;
  }

  @Override
  public void onStart(ISuite suite) {
    start++;
  }

}
